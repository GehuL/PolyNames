package controllers;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

import dao.CardDAO;
import dao.DictionnaireDAO;
import dao.GameDAO;
import dao.PlayerDAO;
import models.Card;
import models.ClientCard;
import models.ECardColor;
import models.EEtatPartie;
import models.EPlayerRole;
import models.Game;
import models.Player;
import models.StartGame;
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
     * Renvoie un code d'erreur 500 si la partie n'existe pas ou qu'elle a débuté.
     * Sinon un code 200.
     * Exemple SSE payload (JSON): {"role": ""}
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

            // Envoie des roles aux joueurs
            for(Player player : playersUpdated)
                context.getSSE().emit(String.valueOf(player.id()), playersUpdated);

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

        // Echange le role entre les deux joueurs
        Player player1 = players.get(0);
        
        playerDAO.setRole(player1.id(), player1.role().inverse());
        
        if(players.size() > 1)
        {
            Player player2 = players.get(1);
            playerDAO.setRole(player2.id(), player1.role());
        }

        return playerDAO.getPlayers(game.id());
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

            PlayerDAO playerDAO = new PlayerDAO();

            ArrayList<Player> players = playerDAO.getPlayers(idPartie);

            if(game == null)
            {
                response.serverError("Partie introuvable");
            }else if(game.etat() != EEtatPartie.SELECTION_ROLE)
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
                gameDAO.setState(idPartie, EEtatPartie.CHOISIR_INDICE);
                response.json(game);

                game = gameDAO.getGame(idPartie);
            
                ArrayList<ClientCard> cards = new CardDAO().getCards(idPartie);
                ArrayList<ClientCard> hidden = new ArrayList<>();
                cards.forEach((card) -> {hidden.add(new ClientCard(card.mot(), ECardColor.UNKNOW));});
                
                // Annonce le début de partie
                for(Player player : playerDAO.getPlayers(idPartie))
                {
                    if(player.role() == EPlayerRole.MAITRE_INTUITION)
                    {
                        context.getSSE().emit(String.valueOf(player.id()), new StartGame(game.etat(), player.role(), hidden));
                    }else
                    {
                        context.getSSE().emit(String.valueOf(player.id()), new StartGame(game.etat(), player.role(), cards));
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void startGameRandomly(WebServerContext context)
    {
        WebServerRequest request = context.getRequest();
        WebServerResponse response = context.getResponse();

        try {
            GameDAO gameDAO = new GameDAO();
            int idPartie = Integer.valueOf(request.getParam("idPartie"));

            Game game = gameDAO.getGame(idPartie);
           
            PlayerDAO playerDAO = new PlayerDAO();
            ArrayList<Player> players = playerDAO.getPlayers(idPartie);

            if(game == null)
            {
                response.serverError("Partie introuvable");
            }else if(game.etat() != EEtatPartie.SELECTION_ROLE)
            {
                response.serverError("Partie déjà débuté");
            }else if(players.size() < 2)
            {
                response.serverError("La partie est en attente de joueur");
            }else
            {
                if(new Random().nextBoolean()) // Une chance sur deux d'inverser les roles
                {
                    Player player1 = players.get(0);
                    Player player2 = players.get(1);

                    playerDAO.setRole(player1.id(), player1.role().inverse());
                    playerDAO.setRole(player2.id(), player1.role());
                }

                // Change le statut de la partie et génère les cartes aléatoirement
                generateRandomCards(idPartie);
                gameDAO.setState(idPartie, EEtatPartie.CHOISIR_INDICE);
                response.json(game);

                ArrayList<ClientCard> cards = new CardDAO().getCards(idPartie);
                ArrayList<ClientCard> hidden = new ArrayList<>();
                cards.forEach((card) -> {hidden.add(new ClientCard(card.mot(), ECardColor.UNKNOW));});
                
                // Annonce le début de partie
                for(Player player : playerDAO.getPlayers(idPartie))
                {
                    if(player.role() == EPlayerRole.MAITRE_INTUITION)
                    {
                        context.getSSE().emit(String.valueOf(player.id()), new StartGame(game.etat(), player.role(), hidden));
                    }else
                    {
                        context.getSSE().emit(String.valueOf(player.id()), new StartGame(game.etat(), player.role(), cards));
                    }
                }
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
            // L'ID ne sert à rien ici car il est généré dans la BDD
            Card card = new Card(0, idPartie, word.id(), false, colors.remove(0));
            cardDAO.addCard(card);
        }
    }

    /**
     * Traite une requete qui contient l'id du joueur et le code de la partie en paramètre.
     * Renvoie une erreur si le code est invalide, la partie est pleine ou le joueur n'existe pas.
     * Renvoie la liste des joueurs en cas de succès.
     * @param context
     */
    public static void joinGame(WebServerContext context)
    {
        WebServerRequest request = context.getRequest();
        WebServerResponse response = context.getResponse();

        try {
            String code = request.getParam("code");
           
            Player player = request.extractBody(Player.class);
            System.out.println(player.nom());
            
            GameDAO gameDAO = new GameDAO();
            Game game = gameDAO.getGame(code);
            
            if(game == null)
                throw new JoinException("Code de partie invalide", JoinException.Type.CODE_INVALID);
            
            if(game.etat() != EEtatPartie.SELECTION_ROLE)
                throw new JoinException("La partie à déjà commencé", JoinException.Type.STATE_INVALID);

            PlayerDAO playerDAO = new PlayerDAO();

            ArrayList<Player> players = playerDAO.getPlayers(game.id());
            if (players.size() >= 2)
                throw new JoinException("La partie est pleine", JoinException.Type.MAX_PLAYER);
            
            EPlayerRole role = EPlayerRole.MAITRE_MOT;
            if(players.size() == 1)
                role = players.get(0).role().inverse();

            int idJoueur = playerDAO.createPlayer(player.nom(), game.id(), role);
            response.json(playerDAO.getPlayer(idJoueur));

            for(Player p : players)
                context.getSSE().emit(String.valueOf(p.id()), playerDAO.getPlayers(game.id()));

        } catch (SQLException e) {
            e.printStackTrace();
            response.serverError("An error occured");
        } catch (JoinException e) {
            response.serverError(e.getMessage());
        }
    }

    public static void getPlayers(WebServerContext context)
    {
        WebServerRequest request = context.getRequest();
        WebServerResponse response = context.getResponse();

        try
        {
            int gameId = Integer.valueOf(request.getParam("idPartie"));
            ArrayList<Player> players = new PlayerDAO().getPlayers(gameId);
                
            // Remplace les ID par 0 des joueurs pour la sécurité
            players.replaceAll((p) -> 
            {
                return new Player(0, p.nom(), gameId, p.role());
            });

            response.json(players);

        }catch (SQLException e) {
            System.out.println(e);
            response.serverError("An error occured");
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

    public static class JoinException extends Exception
    {
        public enum Type
        {
            MAX_PLAYER,
            CODE_INVALID,
            PLAYER_INVALID,
            STATE_INVALID
        }

        private Type type;

        public JoinException(String message, Type type) {
            super(message);
            this.type=type;
        }

        public Type getType()
        {
            return type;
        }
    }
}
