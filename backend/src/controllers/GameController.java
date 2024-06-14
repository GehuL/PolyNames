package controllers;

import java.sql.SQLException;
import java.util.ArrayList;

import dao.CardDAO;
import dao.GameDAO;
import dao.PlayerDAO;
import models.Card;
import models.ClientCard;
import models.Clue;
import models.ECardColor;
import models.EEtatPartie;
import models.EPlayerRole;
import models.Game;
import models.Guess;
import models.GuessRound;
import models.Player;
import webserver.WebServerContext;
import webserver.WebServerRequest;
import webserver.WebServerResponse;

/**
 * Traite les requêtes en rapport avec le déroulement d'une partie
 */
public class GameController
{
    /**
     * Traite les requêtes pour les indices.
     *
     * Exemple Request payload (JSON): {"clue": "BANANE", "toFind": "5"}
     * Exemple SSE payload (JSON): {"clue": "BANANE", "toFind": "5"}
     *
     * Renvoie un code d'erreur 500 si la partie n'existe pas ou que ce n'est pas au tour du maitre des mots.
     * En cas de succés, le serveur renvoie un code OK.
     * Le mot est transmis au SSE de l'autre joueur.
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

            response.ok("Ok");

            Player guesser = new PlayerDAO().getPlayer(idPartie, EPlayerRole.MAITRE_INTUITION);
            context.getSSE().emit(String.valueOf(guesser.id()), clue);

        } catch (SQLException e) 
        {
            e.printStackTrace();
        }
    }

    /**
     * Traite les mots devinés par le maitre des intuitions.
     * Renvoie un code d'erreur 500 si la partie n'existe pas, ce n'est pas le tour du maitre des intuitions, 
     * la carte est déjà révelée ou n'existe pas dans la partie, 
     * le nombre max de carte révelée est atteint (N + 1).
     * En cas de succés, un JSON est renvoyé au deux joueurs.
     * 
     * Exemple Request payload (JSON): {"idCard": 5}
     * Exemple SSE et Response payload (JSON): {"etatPartie": "DEVINER", "score": 12, "dejaTrouvee": 2, "color": "BLEU", "idCard": 7}
     * 
     * @param context
     */
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

            final Game game = gameDAO.getGame(idPartie);
            
            if(game == null)
                throw new GameException("Partie introuvable", GameException.Type.CODE_INVALID);

            if(game.etat() != EEtatPartie.DEVINER)
                throw new GameException("Ce n'est pas a ton tour !", GameException.Type.STATE_INVALID);

           
            Card card = cardDAO.getCard(idPartie, guess.idCard());
            
            if(card == null)
                throw new GameException("La carte n'existe pas");

            if(card.revealed())
                throw new GameException("Carte deja revelee");

            switch(card.color())
            {
                case NOIR:  // Partie finie
                    gameDAO.setScore(idPartie, 0);
                    gameDAO.setState(idPartie, EEtatPartie.FIN);
                    gameDAO.setDejaTrouvee(idPartie, 0);
                break;
                case GRIS: // Tour fini
                    gameDAO.setState(idPartie, EEtatPartie.CHOISIR_INDICE);
                    gameDAO.setDejaTrouvee(idPartie, 0); // Reset le compteur des cartes trouvées ce tour
                break;
                case BLEU:
                {
                    int nbrTrouvee = game.dejaTrouvee() + 1;

                    int score = game.score();
    
                    // Nombre de carte découverte dépassé
                    if(nbrTrouvee >= game.doitDeviner() + 1)
                    {
                        gameDAO.setState(idPartie, EEtatPartie.CHOISIR_INDICE);
                        gameDAO.setDejaTrouvee(idPartie, 0);
                        score += nbrTrouvee * nbrTrouvee; // score += N² 
                    }else
                    {
                        gameDAO.setDejaTrouvee(idPartie, nbrTrouvee);
                        score += nbrTrouvee;
                    }
    
                    gameDAO.setScore(idPartie, score);
                    
                    if(cardDAO.countCardRevelead(idPartie, ECardColor.BLEU) >= 8)
                        gameDAO.setState(idPartie, EEtatPartie.FIN);
                }
                break;
                default:
                    break;
            }
            
            cardDAO.revealCard(idPartie, card.cardId());

            final Game newState = gameDAO.getGame(idPartie);
            GuessRound guessRound = new GuessRound(newState.etat(), newState.score(), newState.dejaTrouvee(), card.color(), card.cardId());

            response.json(guessRound);

            // Envoi du nouvel état de la partie au maitre des mots
            Player masterWord = new PlayerDAO().getPlayer(idPartie, EPlayerRole.MAITRE_MOT);
            context.getSSE().emit(String.valueOf(masterWord.id()), guessRound);

            // On peut supprimer la partie de la BDD
            if(guessRound.etatPartie() == EEtatPartie.FIN)
            {
                gameDAO.deleteGame(idPartie);
            }

        } catch (SQLException e) 
        {
            e.printStackTrace();
        } catch (GameException e) {
            response.serverError(e.getMessage());
        }
    }

    /**
     * Renvoie les cartes de la partie
     * La route prend en paramètre l'ID de la partie et l'ID du joueur uniquement connu par le client.
     * @param context
     */
    public static void getCards(WebServerContext context)
    {
        WebServerRequest request = context.getRequest();
        WebServerResponse response = context.getResponse();

        int idPartie = Integer.valueOf(request.getParam("idPartie"));
        int idJoueur = Integer.valueOf(request.getParam("idJoueur"));

        try 
        {
            CardDAO cardDAO = new CardDAO();
            ArrayList<ClientCard> cards = cardDAO.getCards(idPartie);

            PlayerDAO playerDAO = new PlayerDAO();
            Player player = playerDAO.getPlayer(idJoueur);
            
            if(player == null)
                throw new GameException("La partie n'existe pas", GameException.Type.CODE_INVALID);

            // Masque la couleur pour le maitre des intuitions 
            if(player.role() == EPlayerRole.MAITRE_INTUITION)
                cards.replaceAll((card) -> {return new ClientCard(card.mot(), card.idCard(), ECardColor.UNKNOW);});

            response.json(cards);

        } catch (SQLException e) {
            e.printStackTrace();
        } catch (GameException e)
        {
            response.serverError(e.getMessage());
        }
    }


}
