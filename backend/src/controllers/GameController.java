package controllers;

import webserver.WebServerResponse;

import java.sql.SQLException;
import java.util.Random;

import dao.GameDAO;
import dao.GameDAO.JoinException;
import dao.PlayerDAO;
import webserver.WebServerContext;
import webserver.WebServerRequest;

public class GameController
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
                response.send(200, String.format("{\"code\":\"%s\"}", code));
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
     * Affecte le role à un joueur
     * Renvoie une erreur si le role n'existe pas ou le joueur.
     * @param context
     */
    public static void setRole(WebServerContext context)
    {
        WebServerRequest request = context.getRequest();
        WebServerResponse response = context.getResponse();

        int playerId = Integer.valueOf(request.getParam("idPlayer"));
        String role = request.getParam("role");
        try {
            PlayerDAO playerDAO = new PlayerDAO();
            if(playerDAO.setRole(playerId, role))
                response.ok("Role affecté");
            else
                response.serverError("Le joueur ou le role n'existe pas");

        } catch (SQLException e) {
            e.printStackTrace();
            response.serverError(e.getMessage());
        }
    }

    /**
     * Débute la partie en générant 25 mots aléatoires
     * @param context
     */
    public static void startGame(WebServerContext context)
    {
        WebServerRequest request = context.getRequest();
        WebServerResponse response = context.getResponse();
        // TODO: Vérifier qu'il y a deux joueurs, avec deux roles différents et que la partie existe.
        // TODO: Si partie OK, générer les cartes
        try {
            GameDAO gameDAO = new GameDAO();
            int idPartie = Integer.valueOf(request.getParam("idPartie"));
           // gameDAO.generateCards(idPartie);

        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
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
            String nickname = request.getParam("nickname");
            
            GameDAO gameDAO = new GameDAO();
            
            gameDAO.playerJoin(code, nickname);
            response.json(new PlayerDAO().getPlayers(code));

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
