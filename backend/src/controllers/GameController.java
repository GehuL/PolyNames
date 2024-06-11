package controllers;

import java.sql.SQLException;

import dao.GameDAO;
import models.Clue;
import models.EEtatPartie;
import models.Game;
import webserver.WebServerContext;
import webserver.WebServerRequest;
import webserver.WebServerResponse;

/**
 * Traite les requêtes en rapport avec le déroulement d'une partie
 */
public class GameController
{
    public static void guess(WebServerContext context)
    {

    }

    public static void clue(WebServerContext context)
    {
        WebServerRequest request = context.getRequest();
        WebServerResponse response = context.getResponse();
        
        int idPartie = Integer.valueOf(request.getParam("idPartie"));
        
        try {
            GameDAO gameDAO = new GameDAO();
            Game game = gameDAO.getGame(idPartie);
            
            if(game == null)
            {
                response.serverError("La partie n'existe pas");
                return;
            }   
                
            if(game.etat() != EEtatPartie.CHOISIR_INDICE)
            {
                response.serverError("Ce n'est pas à ton tour !");
                return;
            }

            Clue clue = request.extractBody(Clue.class);
            boolean updateOk = gameDAO.setClue(idPartie, clue) && gameDAO.setState(idPartie, EEtatPartie.DEVINER);
            if(!updateOk)
            {
                response.serverError("Erreur interne");
                return;
            }

            response.json(clue);
            // TODO:: Emetre SSE à l'autre joueur

        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
    }
}
