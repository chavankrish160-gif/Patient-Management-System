package patientrecord;

// Import classes for file handling, networking, and HTTP server creation.
import java.io.IOException;
import java.net.BindException;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Path;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

public class PatientRecordsApplication {

    // Set the folder where frontend files (HTML, CSS, JS) are stored.
    static final Path WEB_ROOT = Path.of("src/public")
            .toAbsolutePath()
            .normalize();

    // Main method: program execution starts here.
    public static void main(String[] args) throws IOException {

        // Read the server port from the SERVER_PORT environment variable.
        // If it is not set, use port 8081.
        int port = Integer.parseInt(
                System.getenv().getOrDefault("SERVER_PORT", "8081")
        );

        // Declare the HTTP server variable.
        HttpServer server;

        try {
            // Create the server using the selected port.
            // The value 0 means the server uses the default executor.
            server = HttpServer.create(
                    new InetSocketAddress(port), 0
            );

        } catch (BindException exception) {

            // Display an error if another application is using this port.
            System.err.println(
                    "Port " + port
                            + " is already in use. "
                            + "Stop the other server before starting this one."
            );

            // Stop the program if the port is unavailable.
            return;
        }

        // Create the repository to manage patient data.
        // Patient information is stored in a JSON file.
        PatientRepository repository =
                new PatientRepository(Path.of("data/patients.json"));

        // Create the handler that processes patient API requests.
        PatientHandler handler = new PatientHandler(repository);

        // Connect the patient API URL to the handler.
        // Example: /api/patients
        server.createContext("/api/patients", handler::handle);

        // Connect the root URL to the method that serves frontend files.
        server.createContext(
                "/", PatientRecordsApplication::serveStatic
        );

        // Start accepting HTTP requests.
        server.start();

        // Display the URL where the application is running.
        System.out.println(
                "Care Ledger running at http://localhost:" + port
        );
    }

    // Serves frontend files such as HTML, CSS, and JavaScript.
    private static void serveStatic(HttpExchange exchange)
            throws IOException {

        // Allow only GET requests for frontend files.
        if (!exchange.getRequestMethod().equals("GET")) {

            // Return HTTP 405 if another method is used.
            PatientHandler.sendError(
                    exchange, 405, "Method not allowed."
            );

            // Close the request and stop processing.
            exchange.close();
            return;
        }

        // Get the requested URL path.
        String requestPath = exchange.getRequestURI().getPath();

        // Find the requested file inside the frontend folder.
        // The root URL (/) loads index.html.
        Path file = WEB_ROOT.resolve(
                requestPath.equals("/")
                        ? "index.html"
                        : requestPath.substring(1)
        ).normalize();

        // Check that the file stays inside the frontend folder,
        // exists, and is not a directory.
        if (!file.startsWith(WEB_ROOT)
                || Files.notExists(file)
                || Files.isDirectory(file)) {

            // Return HTTP 404 if the requested file is unavailable.
            PatientHandler.sendError(
                    exchange, 404, "Page not found."
            );

            // Close the request and stop processing.
            exchange.close();
            return;
        }

        // Set the correct content type based on the file extension.
        // CSS files use text/css, JS files use text/javascript,
        // and other files are served as HTML.
        String type = file.toString().endsWith(".css")
                ? "text/css; charset=utf-8"
                : file.toString().endsWith(".js")
                        ? "text/javascript; charset=utf-8"
                        : "text/html; charset=utf-8";

        // Read the file contents into a byte array.
        byte[] body = Files.readAllBytes(file);

        // Tell the browser what type of content is being returned.
        exchange.getResponseHeaders().set(
                "Content-Type", type
        );

        // Send HTTP 200 (OK) and the response size.
        exchange.sendResponseHeaders(200, body.length);

        // Send the file contents to the browser.
        exchange.getResponseBody().write(body);

        // Close the HTTP exchange after sending the response.
        exchange.close();
    }
}