package httpServer;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.*;
import java.net.InetSocketAddress;

/**
 * Created by ohyongtaek on 2017. 7. 18..
 */
public class PDPServer {

    private static PDPServer pdpServer;
    private static PDPInterface pdpInterface = PDPInterface.getInstance();

    public static PDPServer getInstance() {
        if (pdpServer == null) {
            pdpServer = new PDPServer();
        }
        return pdpServer;
    }

    private PDPServer() {

    }

    public void startServer() throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(8000), 0);
        server.createContext("/evaluate", new EvaluateHandler());
        server.start();
        System.out.println("start PDPServer");
    }

    private static class EvaluateHandler implements HttpHandler {

        @Override
        public void handle(HttpExchange httpExchange) throws IOException {
            InputStream inputStream = httpExchange.getRequestBody();
            String inputString = read(inputStream);
            String response = evaluateRequest(inputString);
            if (response != null) {
                httpExchange.sendResponseHeaders(200, response.getBytes().length);
                OutputStream os = httpExchange.getResponseBody();
                os.write(response.getBytes());
                os.close();
            } else {
                handleError(httpExchange, "Not valid request");
            }
        }

        private String read(InputStream inputStream) throws IOException {
            StringBuffer sb = new StringBuffer();
            byte[] b = new byte[4096];
            while(inputStream.available() != 0) {
                int n = inputStream.read(b);
                sb.append(new String(b, 0, n));
            }
            return sb.toString();
        }

        private String evaluateRequest(String request) {
            if (request.equals("")) return null;
            String response = pdpInterface.evaluate(request);
            return response;
        }

        public void handleError(HttpExchange httpExchange, String errMsg) throws IOException {
            httpExchange.sendResponseHeaders(200, errMsg.getBytes().length);
            OutputStream os = httpExchange.getResponseBody();
            os.write(errMsg.getBytes());
            os.close();
        }
    }


}
