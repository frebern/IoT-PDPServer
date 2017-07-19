import httpServer.PDPServer;

import java.io.IOException;

/**
 * Created by ohyongtaek on 2017. 7. 18..
 */
public class Main {

    public static void main(String[] args) {

        PDPServer pdpServer = PDPServer.getInstance();
        try {
            pdpServer.startServer();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
