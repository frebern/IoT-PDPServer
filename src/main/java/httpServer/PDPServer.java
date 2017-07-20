package httpServer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
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

        Gson gson = new GsonBuilder().create();

        @Override
        public void handle(HttpExchange httpExchange)  {
            InputStream inputStream = httpExchange.getRequestBody();
            String inputString = null;
            try {
                inputString = read(inputStream);
                JsonObject inputJson = gson.fromJson(inputString, JsonObject.class);
                String requestBody = inputJson.get("body").getAsString();
                //TODO: policiesId 를 이용해 policies 찾는 과정을 고민해야함
                String policiesId = null;
                if (inputJson.get("policyId") != null)
                    policiesId = inputJson.get("policyId").getAsString();
                String sep = File.separator;
                String policies = (new File(".")).getCanonicalPath() + sep + "resources" + sep + "IntentConflictExamplePolicies";

                //TODO: attribute set 을 어떻게 가져올 건지에 대한 고민 필요
                String response = evaluateRequest(requestBody, policies);
                if (response != null) {
                    httpExchange.sendResponseHeaders(200, response.getBytes().length);
                    OutputStream os = httpExchange.getResponseBody();
                    os.write(response.getBytes());
                    os.close();
                } else {
                    handleError(httpExchange, "Not valid request");
                }

            } catch (IOException e) {
                e.printStackTrace();
            }



        }

        private String read(InputStream inputStream) throws IOException {
            StringBuffer sb = new StringBuffer();
            byte[] b = new byte[4096];
            while (inputStream.available() != 0) {
                int n = inputStream.read(b);
                sb.append(new String(b, 0, n));
            }
            return sb.toString();
        }

        private String evaluateRequest(String request, String ...policiesLoc) {
            if (request.equals("")) return null;
            String response = pdpInterface.evaluate(request, policiesLoc);
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
