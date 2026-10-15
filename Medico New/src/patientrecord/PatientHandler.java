package patientrecord;

// Import classes required for handling HTTP requests and responses.
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import com.sun.net.httpserver.HttpExchange;

class PatientHandler {

    // Repository handles patient data operations.
    private final PatientRepository repository;

    // Constructor: receives the repository object.
    PatientHandler(PatientRepository repository) {
        this.repository = repository;
    }

    // Handles all incoming API requests.
    void handle(HttpExchange exchange) throws IOException {

        try {
            // Get the requested URL path.
            String path = exchange.getRequestURI().getPath();

            // Extract the part of the URL after /api/patients.
            String suffix = path.substring("/api/patients".length());

            // Extract and decode the patient ID, if provided.
            String id = suffix.startsWith("/")
                    ? URLDecoder.decode(
                            suffix.substring(1),
                            StandardCharsets.UTF_8)
                    : "";

            // Check which HTTP method the client used.
            switch (exchange.getRequestMethod()) {

                // GET: Fetch all patients or one patient.
                case "GET" -> {
                    if (id.isEmpty()) {
                        // Return all patients.
                        sendJson(exchange, 200, repository.all());

                    } else {
                        // Find a patient using their ID.
                        Map<String, Object> patient =
                                repository.find(id);

                        // Return an error if the patient does not exist.
                        if (patient == null) {
                            sendError(
                                    exchange, 404,
                                    "Patient not found.");
                        } else {
                            // Return the requested patient.
                            sendJson(exchange, 200, patient);
                        }
                    }
                }

                // POST: Create a new patient.
                case "POST" -> {
                    // Read the request data and save the patient.
                    Map<String, Object> patient =
                            repository.create(readRequest(exchange));

                    // Send status 201 (Created).
                    sendJson(exchange, 201, patient);
                }

                // PUT: Update an existing patient.
                case "PUT" -> {
                    // Read updated data and update the patient by ID.
                    Map<String, Object> patient =
                            repository.update(
                                    id, readRequest(exchange));

                    // Check whether the patient exists.
                    if (patient == null) {
                        sendError(
                                exchange, 404,
                                "Patient not found.");
                    } else {
                        // Return the updated patient.
                        sendJson(exchange, 200, patient);
                    }
                }

                // DELETE: Remove a patient by ID.
                case "DELETE" -> {
                    // Delete the patient and check the result.
                    if (!repository.delete(id)) {
                        sendError(
                                exchange, 404,
                                "Patient not found.");
                    } else {
                        // Send status 204 (No Content).
                        exchange.sendResponseHeaders(204, -1);
                    }
                }

                // Return an error for unsupported HTTP methods.
                default -> sendError(
                        exchange, 405,
                        "Method not allowed.");
            }

        } catch (IllegalArgumentException exception) {
            // Return status 400 for invalid request data.
            sendError(
                    exchange, 400,
                    exception.getMessage());

        } catch (Exception exception) {
            // Print unexpected errors in the server console.
            exception.printStackTrace();

            // Return status 500 for a server error.
            sendError(
                    exchange, 500,
                    "The server could not complete the request.");

        } finally {
            // Close the HTTP exchange after handling the request.
            exchange.close();
        }
    }

    // Reads JSON data from the client's request body.
    @SuppressWarnings("unchecked")
    private static Map<String, Object> readRequest(
            HttpExchange exchange) throws IOException {

        // Read the request body and convert bytes into a UTF-8 string.
        Object parsed = Json.parse(
                new String(
                        exchange.getRequestBody().readAllBytes(),
                        StandardCharsets.UTF_8));

        // Ensure the JSON data represents an object.
        if (!(parsed instanceof Map<?, ?>)) {
            throw new IllegalArgumentException(
                    "Invalid patient data.");
        }

        // Convert the parsed object into a map and return it.
        return (Map<String, Object>) parsed;
    }

    // Converts data into JSON and sends it to the client.
    static void sendJson(
            HttpExchange exchange,
            int status,
            Object value) throws IOException {

        // Convert the response data into a JSON string and then bytes.
        byte[] body = Json.stringify(value)
                .getBytes(StandardCharsets.UTF_8);

        // Tell the client that the response contains JSON.
        exchange.getResponseHeaders().set(
                "Content-Type",
                "application/json; charset=utf-8");

        // Send the HTTP status code and response length.
        exchange.sendResponseHeaders(status, body.length);

        // Write the JSON response to the client.
        exchange.getResponseBody().write(body);
    }

    // Sends a JSON error message with the specified status code.
    static void sendError(
            HttpExchange exchange,
            int status,
            String message) throws IOException {

        // Create a JSON object containing the error message.
        sendJson(
                exchange,
                status,
                Map.of(
                        "message",
                        message == null
                                ? "Request failed."
                                : message));
    }
}