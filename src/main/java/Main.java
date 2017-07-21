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
            Class.forName("com.mysql.jdbc.Driver").newInstance();
            pdpServer.startServer();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
