import httpServer.PDPServer;

import java.io.IOException;
import java.sql.SQLException;

/**
 * Created by ohyongtaek on 2017. 7. 18..
 */
public class Main {

    public static void main(String[] args) {
        try {
            PDPServer pdpServer = PDPServer.getInstance();
            pdpServer.startServer();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
