package httpServer;

import com.google.gson.*;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.sql.SQLException;
import java.util.LinkedList;

/**
 * Created by ohyongtaek on 2017. 7. 18..
 */
public class PDPServer {

    private static PDPServer pdpServer;
    private static PDPInterface pdpInterface = PDPInterface.getInstance();

    public static PDPServer getInstance() {
        return pdpServer = Singleton.instance;
    }


    // Thread-safe singleton
    private PDPServer()  {
    }

    private static class Singleton{
        private static final PDPServer instance = new PDPServer();
    }

    public void startServer() throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(8000), 0);
        server.createContext("/evaluate", new EvaluateHandler());
        server.start();
        System.out.println("start PDPServer");
    }

    private class EvaluateHandler implements HttpHandler {

        Gson gson = new GsonBuilder().create();

        @Override
        public void handle(HttpExchange httpExchange) {
            InputStream inputStream = httpExchange.getRequestBody();
            String inputString = null;
            try {
                inputString = read(inputStream);
                JsonObject inputJson = gson.fromJson(inputString, JsonObject.class);
                String requestBody = inputJson.get("body").getAsString();

                LinkedList<String> attributeCategoryList = new LinkedList<>();
                if (inputJson.get("attributeList") != null) {
                    JsonArray attributeArray = inputJson.get("attributeList").getAsJsonArray();
                    for (JsonElement c : attributeArray) {
                        String category = c.getAsString();
                        attributeCategoryList.add(category);
                    }
                }

                // 1. json에서 policy id 가져와서(여기 policy id였나 PEP id였나..?)
                String pepId = null;
                if (inputJson.get("pepId") != null)
                    pepId = inputJson.get("pepId").getAsString();

                // 2. 해당 경로 DB로 부터 검색해서 가져오고 PDP 호출
                String response = evaluateRequest(requestBody, pepId);

                // 3. 결과 리턴.
                httpResponse(httpExchange, response);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }


        private String evaluateRequest(String request, String pepId) {
            return request.isEmpty() ? null : pdpInterface.evaluate(request, pepId);
        }

        private void httpResponse(HttpExchange httpExchange, String response){
            try {
                if (response != null) {
                    httpExchange.sendResponseHeaders(200, response.getBytes().length);
                    OutputStream os = httpExchange.getResponseBody();
                    os.write(response.getBytes());
                    os.close();
                } else {
                    handleError(httpExchange, "Not valid request");
                }
            } catch(IOException e) {
                e.printStackTrace();
            }
        }

        public void handleError(HttpExchange httpExchange, String errMsg) throws IOException {
            httpExchange.sendResponseHeaders(200, errMsg.getBytes().length);
            OutputStream os = httpExchange.getResponseBody();
            os.write(errMsg.getBytes());
            os.close();
        }
    }


    public String read(InputStream inputStream) throws IOException {
        StringBuffer sb = new StringBuffer();
        byte[] b = new byte[4096];
        while (inputStream.available() != 0) {
            int n = inputStream.read(b);
            sb.append(new String(b, 0, n));
        }
        return sb.toString();
    }


}
