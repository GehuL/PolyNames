import controllers.GameController;
import controllers.LobbyController;
import webserver.WebServer;

public class App {
    public static void main(String[] args) throws Exception
    {
        WebServer webServer = new WebServer();

        // Routes pour la création de partie 
        webServer.getRouter().put("/createGame", LobbyController::createGame);
        webServer.getRouter().put("/joinGame/:code", LobbyController::joinGame);
        webServer.getRouter().post("/role/swap/:idPartie", LobbyController::swapRole);
        webServer.getRouter().put("/start/:idPartie", LobbyController::startGame);
        webServer.getRouter().put("/start/random/:idPartie", LobbyController::startGameRandomly);

        // Routes pour le déroulement de la partie
        webServer.getRouter().post("/guess/:idPartie", GameController::guess);
        webServer.getRouter().post("/clue/:idPartie", GameController::clue);

        // Utilitaires
        webServer.getRouter().get("/players/:idPartie", LobbyController::getPlayers);
        webServer.getRouter().get("/cards/:idPartie/:idJoueur", GameController::getCards);


        webServer.listen(8080);
    }   
}