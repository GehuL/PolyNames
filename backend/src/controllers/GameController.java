package controllers;

import java.sql.SQLException;

import dao.CardDAO;
import dao.GameDAO;
import models.Card;
import models.Clue;
import models.ECardColor;
import models.EEtatPartie;
import models.Game;
import models.Guess;
import webserver.WebServerContext;
import webserver.WebServerRequest;
import webserver.WebServerResponse;

/**
 * Traite les requêtes en rapport avec le déroulement d'une partie
 */
public class GameController
{
    /**
     * Traite les requêtes pour les indices
     * @param context
     */
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

    public static void guess(WebServerContext context)
    {
        WebServerRequest request = context.getRequest();
        WebServerResponse response = context.getResponse();

        int idPartie = Integer.valueOf(request.getParam("idPartie"));
        
        try
        {
            Guess guess = request.extractBody(Guess.class);
            
            GameDAO gameDAO = new GameDAO();        
            CardDAO cardDAO = new CardDAO();

            Game game = gameDAO.getGame(idPartie);
            
            if(game == null || game.etat() != EEtatPartie.DEVINER || game.doitDeviner() == 0)
            {
                response.serverError("Server error");
                return;
            }

            Card card = cardDAO.getCard(idPartie, idPartie);
            
            if(card == null)
            {
                response.serverError("La carte n'existe pas");
                return;
            }

            if(card.revealed())
            {
                response.serverError("La carte est déjà révelée");
                return;
            }

            if(card.color() == ECardColor.NOIR)
            {
                gameDAO.setState(idPartie, EEtatPartie.FIN);
            }else if(card.color() == ECardColor.GRIS)
            {
                gameDAO.setState(idPartie, EEtatPartie.CHOISIR_INDICE);
            }else
            {
                int nbrIndice = game.doitDeviner() - 1;

                if(nbrIndice == 0)
                {
                    gameDAO.setState(idPartie, EEtatPartie.CHOISIR_INDICE);
                }

                gameDAO.setClue(idPartie, new Clue(game.indiceCourant(), game.doitDeviner() - 1));
            }
            
            response.json(card);

        } catch (SQLException e) 
        {
            e.printStackTrace();
        }
    }
}
