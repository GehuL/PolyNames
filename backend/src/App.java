import controllers.GameController;
import webserver.*;

public class App {
    public static void main(String[] args) throws Exception
    {
        WebServer webServer = new WebServer();
        
        webServer.getRouter().get("/createGame", GameController::createGame);
        webServer.getRouter().post("/joinGame/:nickname/:code", GameController::playerJoin);
        webServer.getRouter().put("/role/:idPlayer/:role", GameController::setRole);
        webServer.getRouter().post("/start/:idPartie", GameController::startGame);

        // webServer.getRouter().post("/role/:idPartie/:mot", GameController::setRole);

        webServer.listen(8080);
    }
}
