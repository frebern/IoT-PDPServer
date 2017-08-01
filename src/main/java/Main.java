import httpServer.PDPServer;
import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        try {
            PDPServer.getInstance().startServer();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
