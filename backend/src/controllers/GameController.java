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
import models.StartGame;
import webserver.WebServerContext;
import webserver.WebServerRequest;
import webserver.WebServerResponse;
import webserver.WebServerSSE;

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

            Game game = gameDAO.getGame(idPartie);
            
            if(game == null || game.etat() != EEtatPartie.DEVINER || game.dejaTrouvee() >= game.doitDeviner() + 1)
            {
                response.serverError("Server error");
                return;
            }

            Card card = cardDAO.getCard(idPartie, guess.idCard());
            
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

            if(card.color() == ECardColor.NOIR) // Partie finie
            {
                gameDAO.setState(idPartie, EEtatPartie.FIN);
                gameDAO.setDejaTrouvee(idPartie, 0);
            }else if(card.color() == ECardColor.GRIS) // Tour finie
            {
                gameDAO.setState(idPartie, EEtatPartie.CHOISIR_INDICE);
                gameDAO.setDejaTrouvee(idPartie, 0); // Reset le compteur des cartes trouvées ce tour
            }else // BLEU
            {
                int nbrTrouvee = game.dejaTrouvee() + 1;

                int score = game.score();

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
            }
            
            cardDAO.revealCard(idPartie, card.cardId());

            Game newState = gameDAO.getGame(idPartie);
            GuessRound guessRound = new GuessRound(newState.etat(), game.score(), game.dejaTrouvee(), card.color(), card.cardId());

            response.json(guessRound);

            // Envoie du nouvelle état de la partie au maitre des mots
            Player masterWord = new PlayerDAO().getPlayer(idPartie, EPlayerRole.MAITRE_MOT);
            context.getSSE().emit(String.valueOf(masterWord.id()), newState);

        } catch (SQLException e) 
        {
            e.printStackTrace();
        }
    }

    /**
     * Renvoie les cartes de la partie
     * La route prend en paramètre l'ID de la partie et l'ID du joueur uniquement connu par le client.
     * @param context
     */
    public static void getCards(WebServerContext context)
    {

    }

    
    /**
     * Envoie la liste des cartes aux joueurs de la partie en masquant la couleur pour le maitre des intuitions.
     * @param sse
     * @param players La liste des joueurs
     * @param game La partie 
     * @throws SQLException
     */
    private static void sendCardsToPlayers(WebServerSSE sse, ArrayList<Player> players, Game game) throws SQLException
    {
        if(players.size() == 0)
            return;  

        PlayerDAO playerDAO = new PlayerDAO();

        ArrayList<ClientCard> cards = new CardDAO().getCards(game.id());
        ArrayList<ClientCard> hidden = new ArrayList<>();

        cards.forEach((card) -> {hidden.add(new ClientCard(card.mot(), card.idCard(), ECardColor.UNKNOW));});
        
        // Annonce le début de partie
        for(Player player : playerDAO.getPlayers(game.id()))
        {
            if(player.role() == EPlayerRole.MAITRE_INTUITION)
            {
                sse.emit(String.valueOf(player.id()), new StartGame(game.etat(), player.role(), hidden));
            }else
            {
                sse.emit(String.valueOf(player.id()), new StartGame(game.etat(), player.role(), cards));
            }
        }
    }
}
