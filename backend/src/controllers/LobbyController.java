package controllers;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

import dao.CardDAO;
import dao.DictionnaireDAO;
import dao.GameDAO;
import dao.GameDAO.JoinException;
import dao.PlayerDAO;
import models.Card;
import models.ECardColor;
import models.EEtatPartie;
import models.Game;
import models.Player;
import models.Word;
import webserver.WebServerContext;
import webserver.WebServerRequest;
import webserver.WebServerResponse;

/**
 * Traite les requêtes en rapport avec la création d'une partie
 */
public class LobbyController
{
    /**
     * Créer une partie. Renvoi un code de partie au client ou une erreur.
     * @param context
     */
    public static void createGame(WebServerContext context)
    {
        WebServerResponse response = context.getResponse();
        try 
        {
            String code = createUniqueCode();
            if(code != null)
            {
                GameDAO gameDAO = new GameDAO();
                gameDAO.createGame(code);
                
                response.json(gameDAO.getGame(code));

                context.getSSE().emit(code, "test");

                return;
            }
        } catch (SQLException e) 
        {
            System.out.println(e);
        }
        response.serverError("An error occured");
    }

    /**
     * Cherche un code unique parmis toutes les parties actuelles
     * @return Le code ou null si aucun code ne peut être utiliser
     */
    private static String createUniqueCode()
    {
        String code = null;
        try {
            GameDAO gameDAO = new GameDAO();
            
            if(gameDAO.getGameCount() >= Math.pow(36, 5))
                return null;
            
            // Génère un code temps qu'un code n'est pas disponible
            do
            {
                code = generateCode();
            }while(gameDAO.getGame(code) != null);

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return code;
    }

    /**
     * Échange les rôles des deux joueurs, ou d'un joueur si il n'y en a qu'un dans la partie.
     * Renvoie une erreur si la partie n'existe pas ou qu'elle a débuté.
     * @param context
     */
    public static void swapRole(WebServerContext context)
    {
        WebServerRequest request = context.getRequest();
        WebServerResponse response = context.getResponse();

        try {
            int idPartie = Integer.valueOf(request.getParam("idPartie"));
            ArrayList<Player> playersUpdated = swapRole(idPartie);
            
            if(playersUpdated.size() == 0)
                response.serverError("Erreur lors du changement de role");
            else
                response.json(playersUpdated);

        } catch (SQLException e) {
            e.printStackTrace();
            response.serverError(e.getMessage());
        }
    }

    /**
     * Échange le role entre entre deux joueurs ou d'un seul.
     * @param idPartie L'id de la partie
     * @return Les données mis à jour des joueurs. Vide si aucun joueur n'a été modifié
     * @throws SQLException
     */

    private static ArrayList<Player> swapRole(int idPartie) throws SQLException
    {
        PlayerDAO playerDAO = new PlayerDAO();
        GameDAO gameDAO = new GameDAO();

        Game game = gameDAO.getGame(idPartie);

        ArrayList<Player> players = playerDAO.getPlayers(idPartie);

        // Partie n'existe pas ou déjà débuté ou pas de joueur
        if(game == null || game.etat() != EEtatPartie.SELECTION_ROLE || players.size() == 0) 
            return new ArrayList<>();

        else if(players.size() == 1)
        {
            // Echange le role
            String role = players.get(0).role().equals("maitre_intuition") ? "maitre_mot" : "maitre_intuition";
            playerDAO.setRole(players.get(0).id(), role);
            return playerDAO.getPlayers(game.id());
        }else if(players.size() == 2)
        {
            // Echange le role entre les deux joueurs
            Player player1 = players.get(0);
            Player player2 = players.get(1);
            if(player1.role().equals("maitre_intuition"))
            {
                playerDAO.setRole(player1.id(), "maitre_mot");
                playerDAO.setRole(player2.id(), "maitre_intuition");
            }else
            {
                playerDAO.setRole(player1.id(), "maitre_intuition");
                playerDAO.setRole(player2.id(), "maitre_mot");
            }
           return playerDAO.getPlayers(game.id());
        }
        return new ArrayList<>();
    }

    /**
     * Débute la partie en générant 25 mots aléatoires
     * @param context
     */
    public static void startGame(WebServerContext context)
    {
        WebServerRequest request = context.getRequest();
        WebServerResponse response = context.getResponse();

        try {
            GameDAO gameDAO = new GameDAO();
            int idPartie = Integer.valueOf(request.getParam("idPartie"));

            Game game = gameDAO.getGame(idPartie);
            ArrayList<Player> players = new PlayerDAO().getPlayers(idPartie);

            if(game == null)
            {
                response.serverError("Partie introuvable");
            } else if(game.etat() != EEtatPartie.SELECTION_ROLE)
            {
                response.serverError("Partie déjà débuté");
                // TODO: SSE EVENT
            }else if(players.size() < 2)
            {
                response.serverError("La partie est en attente de joueur");
            }else if(players.get(0).role().equals(players.get(1).role())) // Les deux roles sont identiques
            {
                response.serverError("Les roles doivent être différents");
            }else
            {
                // Change le statut de la partie et génère les cartes aléatoirement
                generateRandomCards(idPartie);
                gameDAO.startGame(idPartie);
                response.json(new CardDAO().getCards(idPartie));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void generateRandomCards(int idPartie) throws SQLException
    {
        // Génère 25 couleurs et les mets dans le désordre
        ArrayList<ECardColor> colors = new ArrayList<>();
        colors.addAll(Collections.nCopies(8, ECardColor.BLEU));
        colors.addAll(Collections.nCopies(15, ECardColor.GRIS));
        colors.addAll(Collections.nCopies(2, ECardColor.NOIR));
        Collections.shuffle(colors);

        CardDAO cardDAO = new CardDAO();

        // Récupère 25 mots et créer des cartes
        for(Word word : new DictionnaireDAO().getRandomWords(colors.size()))
        {
            Card card = new Card(idPartie, word.id(), false, colors.remove(0));
            cardDAO.addCard(card);
        }
    }

    /**
     * Traite une requete qui contient l'id du joueur et le code de la partie en paramètre.
     * Renvoie une erreur si le code est invalide, la partie est pleine ou le joueur n'existe pas.
     * Renvoie la liste des joueurs en cas de succés.
     * @param context
     */
    public static void playerJoin(WebServerContext context)
    {
        WebServerRequest request = context.getRequest();
        WebServerResponse response = context.getResponse();

        try {
            String code = request.getParam("code");
           
            Player player = request.extractBody(Player.class);
            System.out.println(player.nom());
            
            GameDAO gameDAO = new GameDAO();
            
            int id = gameDAO.playerJoin(code, player.nom());
            response.json(new PlayerDAO().getPlayers(id));

        } catch (SQLException e) {
            System.out.println(e);
            response.serverError("An error occured");
        } catch (JoinException e) {
            response.serverError(e.getMessage());
        }
    }
    
    /**
     * Génère un code composé de 5 caractères de lettres et de chiffres.
     * Le nombre de code total est de 36 puissance 5.
     * @return
     */
    public static String generateCode()
    {
        String code = "";
        Random random = new Random();
        for(int i = 0; i < 5; i++)
        {
            boolean isDigit = random.nextBoolean();
            if(isDigit)
                code += (char) (random.nextInt(10) + 48);
            else
                code += (char) (random.nextInt(26) + 65);
        }
        return code;
    }
}
