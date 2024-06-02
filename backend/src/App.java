import controllers.GameController;
import webserver.*;

public class App {
    public static void main(String[] args) throws Exception
    {
        WebServer webServer = new WebServer();
        
        webServer.getRouter().get("/createGame", GameController::createGame);
        webServer.getRouter().post("/joinGame/:playerId/:code", GameController::playerJoin);
        webServer.getRouter().post("/createPlayer/:nickname", GameController::createPlayer);

        webServer.listen(8080);
    }
}
