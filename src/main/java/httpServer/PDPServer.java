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

    // Thread-safe singleton
    public static PDPServer getInstance() {
        return pdpServer = Singleton.instance;
    }
    private PDPServer() {}
    private static class Singleton{
        private static final PDPServer instance = new PDPServer();
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
                // 1. json에서 policy id 가져와서(여기 policy id였나 PEP id였나..?)
                String policiesId = null;
                if (inputJson.get("policyId") != null)
                    policiesId = inputJson.get("policyId").getAsString();
                // 2. 해당 경로 DB로 부터 검색해서 가져오고 PDP 호출
                String response = evaluateRequest(requestBody, getPolicyPathFromId("IntentConflictExamplePolicies"));
                // 3. 결과 리턴.
                httpResponse(httpExchange, response);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // DB에서 Search 여기 현재 미구현상태.
        private String getPolicyPathFromId(String policiesId) throws IOException{
            return getPolicyPathFromName("IntentConflictExapmle");
        }

        // 우선 ./resources/를 Base 디렉토리로 설정.
        private String getPolicyPathFromName(String dirName) throws IOException{
            final String BASE = "resources";
            final String SEP = File.separator;
            // ./resources/{dirName}
            String path = (new File(".")).getCanonicalPath() + SEP + BASE + SEP + dirName;
            return new File(path).exists() ? path : null;
        }

        //
        private String evaluateRequest(String request, String ...policiesLoc) {
            if (request.equals("")) return null;
            String response = pdpInterface.evaluate(request, policiesLoc);
            return response;
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


}
