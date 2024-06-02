package controllers;

import webserver.WebServerResponse;

import java.sql.SQLException;
import java.util.Random;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import dao.GameDAO;
import webserver.WebServerContext;
import webserver.WebServerRequest;

public class GameController
{
    /**
     * Créer une partie. Renvoie un code de partie au client ou une erreur.
     * @param context
     */
    public static void createGame(WebServerContext context)
    {
        WebServerResponse response = context.getResponse();
        try 
        {
            GameDAO gameDAO = new GameDAO();
            String code = generateCode();
            if(gameDAO.createGame(code))
            {
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
     * Créer un nouveau joueur à partir du pseudo envoyé et renvoie au client l'id du joueur.
     * @param context
     */
    public static void createPlayer(WebServerContext context)
    {
        WebServerRequest request = context.getRequest();
        WebServerResponse response = context.getResponse();

        String player = request.getParam("nickname");
        try {
            GameDAO gameDAO = new GameDAO();
            int id = gameDAO.createPlayer(player);
            response.send(200, String.format("{\"playerId\":\"%d\"}", id));
        } catch (SQLException e) {
            e.printStackTrace();
            response.serverError("An error occured");
        }
    }

    public static void setRole(WebServerContext context)
    {
        WebServerRequest request = context.getRequest();
        WebServerResponse response = context.getResponse();
      
        String code = request.getParam("code");
        String player = request.getParam("idPlayer");
    }

    public static void startGame(WebServerContext context)
    {
        WebServerRequest request = context.getRequest();
        WebServerResponse response = context.getResponse();
        // TODO: Vérifier qu'il y a deux joueurs, avec deux roles différents et que la partie existe.
        // TODO: Si partie OK, générer les cartes
        String code = request.getParam("code");
        String player = request.getParam("nickname");
    }

    public static void playerJoin(WebServerContext context)
    {
        WebServerRequest request = context.getRequest();
        WebServerResponse response = context.getResponse();

        String code = request.getParam("code");
        try {
            int idPlayer = Integer.valueOf(request.getParam("playerId"));
            
            // TODO: Vérifier que la partie existe et le joueur, que la partie n'est pas pleine.
            GameDAO gameDAO = new GameDAO();
            gameDAO.playerJoin(code, idPlayer);

            response.ok("ok");
            // TODO: Renvoie la liste des joueurs présents
        } catch (SQLException e) {
            System.out.println(e);
            response.serverError("An error occured");
        }
    }
    
    /**
     * Génère un code composé de 5 caractères de lettres et de chiffres.
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
