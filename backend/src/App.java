import controllers.GameController;
import webserver.*;

public class App {
    public static void main(String[] args) throws Exception
    {
        WebServer webServer = new WebServer();

        webServer.getRouter().put("/createGame", GameController::createGame);
        webServer.getRouter().put("/joinGame/:code", GameController::playerJoin);
        webServer.getRouter().post("/role/swap/:idPartie", GameController::swapRole);
        webServer.getRouter().post("/start/:idPartie", GameController::startGame);

        // webServer.getRouter().post("/role/:idPartie/:mot", GameController::setRole);

        webServer.listen(8080);
    }
}
