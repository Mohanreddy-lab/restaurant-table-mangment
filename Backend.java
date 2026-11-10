import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class Backend {
    private static final String DATABASE = "jdbc:sqlite:tables.db";

    public static void main(String[] args) throws Exception {
        Connection database = DriverManager.getConnection(DATABASE);
        createTable(database);

        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        server.createContext("/", request -> {
            synchronized (database) {
                handleRequest(request, database);
            }
        });
        server.start();
        System.out.println("Open http://localhost:8080");
    }

    private static void createTable(Connection database) throws SQLException {
        String sql = """
                CREATE TABLE IF NOT EXISTS tables (
                    table_number INTEGER PRIMARY KEY,
                    capacity INTEGER NOT NULL,
                    status TEXT NOT NULL DEFAULT 'AVAILABLE',
                    customer_count INTEGER NOT NULL DEFAULT 0
                )
                """;
        try (var statement = database.createStatement()) {
            statement.execute(sql);
        }
    }

    private static void handleRequest(HttpExchange request, Connection database)
            throws IOException {
        try {
            String path = request.getRequestURI().getPath();
            String method = request.getRequestMethod();

            if (path.equals("/") && method.equals("GET")) {
                byte[] page = Files.readAllBytes(Path.of("index.html"));
                send(request, 200, "text/html", page);
            } else if (path.equals("/api/tables") && method.equals("GET")) {
                sendTableList(request, database);
            } else if (method.equals("POST")) {
                updateTable(request, database, path);
            } else {
                sendError(request, 404, "Not found");
            }
        } catch (IllegalArgumentException error) {
            sendError(request, 400, error.getMessage());
        } catch (SQLException error) {
            sendError(request, 500, "Database error");
        }
    }

    private static void updateTable(HttpExchange request, Connection database, String path)
            throws IOException, SQLException {
        Map<String, String> form = readForm(request);

        if (path.equals("/api/tables")) {
            addTable(database, number(form, "tableNumber"), number(form, "capacity"));
        } else {
            String[] parts = path.split("/");
            if (parts.length != 5 || !parts[1].equals("api")
                    || !parts[2].equals("tables")) {
                throw new IllegalArgumentException("Not found");
            }
            changeTable(database, number(parts[3]), parts[4], form);
        }

        sendTableList(request, database);
    }

    private static void addTable(Connection database, int tableNumber, int capacity)
            throws SQLException {
        String checkSql = "SELECT 1 FROM tables WHERE table_number = ?";
        try (var check = database.prepareStatement(checkSql)) {
            check.setInt(1, tableNumber);
            if (check.executeQuery().next()) {
                throw new IllegalArgumentException("Table already exists");
            }
        }

        String insertSql = "INSERT INTO tables (table_number, capacity) VALUES (?, ?)";
        try (var insert = database.prepareStatement(insertSql)) {
            insert.setInt(1, tableNumber);
            insert.setInt(2, capacity);
            insert.executeUpdate();
        }
    }

    private static void changeTable(Connection database, int tableNumber,
            String action, Map<String, String> form) throws SQLException {
        String sql;

        switch (action) {
            case "assign":
                int customers = number(form, "customerCount");
                sql = "UPDATE tables SET status='OCCUPIED', customer_count=? "
                        + "WHERE table_number=? AND status='AVAILABLE' AND capacity>=?";
                try (var update = database.prepareStatement(sql)) {
                    update.setInt(1, customers);
                    update.setInt(2, tableNumber);
                    update.setInt(3, customers);
                    checkUpdated(update.executeUpdate());
                }
                return;
            case "leave":
                sql = "UPDATE tables SET status='CLEANING', customer_count=0 "
                        + "WHERE table_number=? AND status='OCCUPIED'";
                break;
            case "clean":
                sql = "UPDATE tables SET status='AVAILABLE' "
                        + "WHERE table_number=? AND status='CLEANING'";
                break;
            case "capacity":
                sql = "UPDATE tables SET capacity=? "
                        + "WHERE table_number=? AND status='AVAILABLE'";
                int capacity = number(form, "capacity");
                try (var update = database.prepareStatement(sql)) {
                    update.setInt(1, capacity);
                    update.setInt(2, tableNumber);
                    checkUpdated(update.executeUpdate());
                }
                return;
            default:
                throw new IllegalArgumentException("Not found");
        }

        try (var update = database.prepareStatement(sql)) {
            update.setInt(1, tableNumber);
            checkUpdated(update.executeUpdate());
        }
    }

    private static void checkUpdated(int rowsChanged) {
        if (rowsChanged == 0) {
            throw new IllegalArgumentException("Table unavailable or not found");
        }
    }

    private static void sendTableList(HttpExchange request, Connection database)
            throws SQLException, IOException {
        StringBuilder json = new StringBuilder("[");
        String sql = "SELECT * FROM tables ORDER BY table_number";

        try (var statement = database.createStatement();
             ResultSet rows = statement.executeQuery(sql)) {
            while (rows.next()) {
                if (json.length() > 1) {
                    json.append(",");
                }
                json.append("{\"tableNumber\":").append(rows.getInt("table_number"))
                        .append(",\"capacity\":").append(rows.getInt("capacity"))
                        .append(",\"status\":\"").append(rows.getString("status"))
                        .append("\",\"customerCount\":")
                        .append(rows.getInt("customer_count")).append("}");
            }
        }

        json.append("]");
        send(request, 200, "application/json",
                json.toString().getBytes(StandardCharsets.UTF_8));
    }

    private static Map<String, String> readForm(HttpExchange request) throws IOException {
        String body = new String(request.getRequestBody().readAllBytes(),
                StandardCharsets.UTF_8);
        Map<String, String> form = new HashMap<>();

        for (String item : body.split("&")) {
            String[] pair = item.split("=", 2);
            String key = URLDecoder.decode(pair[0], StandardCharsets.UTF_8);
            String value = pair.length > 1
                    ? URLDecoder.decode(pair[1], StandardCharsets.UTF_8) : "";
            form.put(key, value);
        }
        return form;
    }

    private static int number(Map<String, String> form, String name) {
        return number(form.get(name));
    }

    private static int number(String value) {
        try {
            int result = Integer.parseInt(value);
            if (result > 0) {
                return result;
            }
        } catch (NumberFormatException ignored) {
            // Show one clear message for missing, invalid, or non-positive values.
        }
        throw new IllegalArgumentException("Enter a positive whole number");
    }

    private static void sendError(HttpExchange request, int code, String message)
            throws IOException {
        String json = "{\"error\":\"" + message + "\"}";
        send(request, code, "application/json",
                json.getBytes(StandardCharsets.UTF_8));
    }

    private static void send(HttpExchange request, int code, String type, byte[] body)
            throws IOException {
        request.getResponseHeaders().set("Content-Type", type + "; charset=utf-8");
        request.sendResponseHeaders(code, body.length);
        try (var output = request.getResponseBody()) {
            output.write(body);
        }
    }
}
