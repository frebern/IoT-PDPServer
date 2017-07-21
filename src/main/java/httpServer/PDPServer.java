package httpServer;

import com.google.gson.*;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.sql.*;
import java.util.HashSet;
import java.util.LinkedList;

/**
 * Created by ohyongtaek on 2017. 7. 18..
 */
public class PDPServer {

    private static PDPServer pdpServer;
    private static PDPInterface pdpInterface = PDPInterface.getInstance();
    Connection conn;

    public static PDPServer getInstance() throws SQLException {
        if (pdpServer == null) {
            pdpServer = new PDPServer();
        }
        return pdpServer;
    }

    private PDPServer() throws SQLException {
        conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/pdp?autoReconnect=true&useSSL=false&" +
                "user=finder&password=asdasd");
    }

    public void startServer() throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(8000), 0);
        server.createContext("/evaluate", new EvaluateHandler());
        server.createContext("/create/policy", new TestHandler());
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
                String pepId = null;
                if (inputJson.get("pepId") != null)
                    pepId = inputJson.get("pepId").getAsString();
                LinkedList<String> attributeCategoryList = new LinkedList<>();
                if (inputJson.get("attributeList") != null) {
                    JsonArray attributeArray = inputJson.get("attributeList").getAsJsonArray();
                    for (JsonElement c : attributeArray) {
                        String category = c.getAsString();
                        attributeCategoryList.add(category);
                    }
                }

                //TODO: policiesId 를 이용해 policies 찾는 과정을 고민해야함
                HashSet<String> policySet = findPolicies(pepId);
//                HashSet<String> policySet = new HashSet<>();
//                String sep = File.separator;
//                String policies = (new File(".")).getCanonicalPath() + sep + "resources" + sep + "IntentConflictExamplePolicies";
//                policySet.add(policies);
                //TODO: attribute set 을 어떻게 가져올 건지에 대한 고민 필요
                String response = evaluateRequest(requestBody, policySet);
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

        private HashSet<String> findPolicies(String pepId) {
            Statement stmt = null;
            ResultSet rs = null;
            String query = "SELECT file_loc FROM pep_policy JOIN pep on pep._id=pep_policy.pep_id JOIN policy on policy._id=pep_policy.policy_id where pep.pep_id='" + pepId+"'";
            HashSet<String> policies = new HashSet<>();
            try {
                stmt = conn.createStatement();
                rs = stmt.executeQuery(query);
                while (rs.next()) {
                    String fileLoc = rs.getString(1);
                    policies.add(fileLoc);
                }

            } catch (SQLException ex) {
//                System.out.println("SQLException: " + ex.getMessage());
//                System.out.println("SQLState: " + ex.getSQLState());
//                System.out.println("VendorError: " + ex.getErrorCode());
            } finally {
                if (rs != null) {
                    try {
                        rs.close();
                    } catch (SQLException sqlEx) {
                    } // ignore

                    rs = null;
                }

                if (stmt != null) {
                    try {
                        stmt.close();
                    } catch (SQLException sqlEx) {
                    } // ignore

                    stmt = null;
                }
            }
            return policies;
        }


        private String evaluateRequest(String request, HashSet policySet) {
            if (request.equals("")) return null;
            String response = pdpInterface.evaluate(request, policySet);
            return response;
        }

        public void handleError(HttpExchange httpExchange, String errMsg) throws IOException {
            httpExchange.sendResponseHeaders(200, errMsg.getBytes().length);
            OutputStream os = httpExchange.getResponseBody();
            os.write(errMsg.getBytes());
            os.close();
        }
    }

    private class TestHandler implements HttpHandler {

        @Override
        public void handle(HttpExchange httpExchange) throws IOException {
            InputStream inputStream = httpExchange.getRequestBody();
            String input = read(inputStream);

            createPolicy(input);
            String response = "OK";
            httpExchange.sendResponseHeaders(200, response.getBytes().length);
            OutputStream os = httpExchange.getResponseBody();
            os.write(response.getBytes());
            os.close();

        }

        private void createPolicy(String policy_loc) {
            Statement stmt = null;
            String query = "INSERT INTO policy (file_loc) values ('" + policy_loc + "')";
            try {
                stmt = conn.createStatement();
                stmt.execute(query);
            } catch (SQLException ex) {
                System.out.println("SQLException: " + ex.getMessage());
                System.out.println("SQLState: " + ex.getSQLState());
                System.out.println("VendorError: " + ex.getErrorCode());
            } finally {
                if (stmt != null) {
                    try {
                        stmt.close();
                    } catch (SQLException sqlEx) {
                    } // ignore

                    stmt = null;
                }
            }

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
