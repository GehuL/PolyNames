package controllers;

import webserver.WebServerResponse;

import java.sql.SQLException;
import java.util.Random;

import dao.GameDAO;
import dao.GameDAO.JoinException;
import dao.PlayerDAO;
import models.Player;
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
     * Créer un nouveau joueur à partir du pseudo envoyé et renvoi au client l'id du joueur.
     * @param context
     */
    public static void createPlayer(WebServerContext context)
    {
        WebServerRequest request = context.getRequest();
        WebServerResponse response = context.getResponse();

        String nom = request.getParam("nickname");
        try {
            PlayerDAO playerDAO = new PlayerDAO();
            Player player = playerDAO.createPlayer(nom);
            if(player == null)
            {
                response.serverError("Nickname already exist");
                return;
            }
            response.json(player);
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

        String code = request.getParam("code");
        try {
            int idPlayer = Integer.valueOf(request.getParam("playerId"));
            
            GameDAO gameDAO = new GameDAO();
            
            gameDAO.playerJoin(code, idPlayer);
            response.json(gameDAO.getPlayers(code));

        } catch (SQLException e) {
            System.out.println(e);
            response.serverError("An error occured");
        } catch (JoinException e) {
            response.serverError(e.getMessage());
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
