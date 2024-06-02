import controllers.GameController;
import webserver.*;

public class App {
    public static void main(String[] args) throws Exception
    {
        WebServer webServer = new WebServer();
        
        webServer.getRouter().get("/create", GameController::createGame);
        webServer.getRouter().post("/join/:playerId/:code", GameController::playerJoin);
        webServer.getRouter().post("/newPlayer/:nickname", GameController::createPlayer);

        webServer.listen(8080);
    }
}
