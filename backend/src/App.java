import webserver.*;

public class App {
    public static void main(String[] args) throws Exception
    {
        WebServer webServer = new WebServer();
        webServer.listen(8080);
    }
}
