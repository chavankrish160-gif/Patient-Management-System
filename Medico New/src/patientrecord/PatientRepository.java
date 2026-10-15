package patientrecord;

// Import classes for file handling, dates, collections, and unique IDs.
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

class PatientRepository {

    // Stores the location of the JSON file containing patient records.
    private final Path dataFile;

    // Constructor: initializes the path of the patient data file.
    PatientRepository(Path dataFile) {
        this.dataFile = dataFile.toAbsolutePath().normalize();
    }

    // Returns the list of all patients.
    List<Map<String, Object>> all() throws IOException {
        return readPatients();
    }

    // Finds and returns one patient using their ID.
    // Returns null if the patient does not exist.
    Map<String, Object> find(String id) throws IOException {
        List<Map<String, Object>> patients = readPatients();

        // Find the position of the patient in the list.
        int index = indexOf(patients, id);

        // Return the patient if found; otherwise, return null.
        return index < 0 ? null : patients.get(index);
    }

    // Creates a new patient record and saves it.
    Map<String, Object> create(Map<String, Object> input)
            throws IOException {

        // Validate the input and create a patient object.
        Map<String, Object> patient = makePatient(input, null);

        // Read existing patients and add the new patient.
        List<Map<String, Object>> patients = readPatients();
        patients.add(patient);

        // Save the updated list to the JSON file.
        writePatients(patients);

        // Return the newly created patient.
        return patient;
    }

    // Updates an existing patient's information using their ID.
    Map<String, Object> update(
            String id, Map<String, Object> input) throws IOException {

        // Load all patient records.
        List<Map<String, Object>> patients = readPatients();

        // Find the patient to update.
        int index = indexOf(patients, id);

        // Return null if the patient does not exist.
        if (index < 0) {
            return null;
        }

        // Create updated data while preserving the existing ID
        // and original creation timestamp.
        Map<String, Object> patient =
                makePatient(input, patients.get(index));

        // Replace the old record with the updated record.
        patients.set(index, patient);

        // Save the updated patient list.
        writePatients(patients);

        return patient;
    }

    // Deletes a patient record using their ID.
    boolean delete(String id) throws IOException {

        // Read all patient records.
        List<Map<String, Object>> patients = readPatients();

        // Find the patient to delete.
        int index = indexOf(patients, id);

        // Return false if the patient does not exist.
        if (index < 0) {
            return false;
        }

        // Remove the patient from the list.
        patients.remove(index);

        // Save the list after deletion.
        writePatients(patients);

        // Return true to indicate successful deletion.
        return true;
    }

    // Creates a patient record and validates the supplied information.
    private Map<String, Object> makePatient(
            Map<String, Object> input,
            Map<String, Object> previous) {

        // Read patient details from the input map.
        String name = string(input, "patientName").trim();
        String gender = string(input, "gender");
        String phone = string(input, "phone").trim();
        String admissionDate = string(input, "admissionDate");

        // Read and validate the patient's age.
        int age = readAge(input.get("age"));

        // Check that required fields contain valid values.
        if (name.isBlank()
                || age < 1
                || phone.isBlank()
                || admissionDate.isBlank()
                || !List.of("Male", "Female", "Other")
                        .contains(gender)) {

            // Stop processing if the information is invalid.
            throw new IllegalArgumentException(
                    "Enter a name, positive age, valid gender, "
                            + "phone, and admission date.");
        }

        // Get the current date and time in ISO-8601 format.
        String now = Instant.now().toString();

        // Create a map to store the patient's details.
        LinkedHashMap<String, Object> patient = new LinkedHashMap<>();

        // Generate a unique ID for a new patient.
        // For an existing patient, keep the original ID.
        patient.put(
                "id",
                previous == null
                        ? UUID.randomUUID().toString()
                        : previous.get("id"));

        // Store the patient's basic information.
        patient.put("patientName", name);
        patient.put("age", age);
        patient.put("gender", gender);
        patient.put("phone", phone);

        // Store optional address and medical problem details.
        patient.put(
                "address", string(input, "address").trim());
        patient.put(
                "medicalProblem",
                string(input, "medicalProblem").trim());

        // Store the admission date.
        patient.put("admissionDate", admissionDate);

        // Set the creation time for a new patient.
        // Preserve the original creation time when updating.
        patient.put(
                "createdAt",
                previous == null
                        ? now
                        : previous.get("createdAt"));

        // Record the latest update time.
        patient.put("updatedAt", now);

        // Return the completed patient record.
        return patient;
    }

    // Converts the supplied age into a valid whole number.
    private static int readAge(Object value) {
        try {
            // Handle age values represented as numbers.
            if (value instanceof Number number) {
                double age = number.doubleValue();

                // Check that the number is finite, whole, positive,
                // and within the integer range.
                if (Double.isFinite(age)
                        && age == Math.rint(age)
                        && age >= 1
                        && age <= Integer.MAX_VALUE) {

                    return (int) age;
                }

            } else if (value != null) {
                // Convert a string age, such as "25", into an integer.
                return Integer.parseInt(value.toString().trim());
            }

        } catch (NumberFormatException ignored) {
            // Handle invalid text without exposing the parsing error.
        }

        // Throw an error if the age is missing or invalid.
        throw new IllegalArgumentException(
                "Age must be a whole number greater than zero.");
    }

    // Gets a value from a map and converts it into a string.
    private static String string(
            Map<String, Object> values, String key) {

        // Retrieve the value for the specified key.
        Object value = values.get(key);

        // Return an empty string if the value is null.
        // Otherwise, return its string representation.
        return value == null ? "" : String.valueOf(value);
    }

    // Reads all patient records from the JSON file.
    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> readPatients()
            throws IOException {

        // If the file does not exist, return an empty patient list.
        if (Files.notExists(dataFile)) {
            return new ArrayList<>();
        }

        // Read the file as UTF-8 text and parse the JSON content.
        Object parsed = Json.parse(
                Files.readString(dataFile, StandardCharsets.UTF_8));

        // Ensure the JSON root contains a list of patients.
        if (!(parsed instanceof List<?>)) {
            throw new IOException(
                    "Patient data file has an invalid format.");
        }

        // Convert the parsed list to the expected patient-list type.
        return (List<Map<String, Object>>) parsed;
    }

    // Writes all patient records to the JSON file.
    private void writePatients(
            List<Map<String, Object>> patients) throws IOException {

        // Create the parent directory if it does not exist.
        Files.createDirectories(dataFile.getParent());

        // Create a temporary file beside the main data file.
        Path temporary =
                dataFile.resolveSibling("patients.json.tmp");

        // Convert patient records to JSON and write them to the
        // temporary file using UTF-8 encoding.
        Files.writeString(
                temporary,
                Json.stringify(patients),
                StandardCharsets.UTF_8);

        try {
            // Replace the old file with the temporary file.
            // ATOMIC_MOVE helps prevent a partially written data file.
            Files.move(
                    temporary,
                    dataFile,
                    StandardCopyOption.REPLACE_EXISTING,
                    StandardCopyOption.ATOMIC_MOVE);

        } catch (IOException exception) {
            // If atomic replacement is unsupported or fails,
            // try replacing the file without ATOMIC_MOVE.
            Files.move(
                    temporary,
                    dataFile,
                    StandardCopyOption.REPLACE_EXISTING);
        }
    }

    // Finds the position of a patient by their ID.
    private static int indexOf(
            List<Map<String, Object>> patients, String id) {

        // Check each patient in the list.
        for (int i = 0; i < patients.size(); i++) {

            // Compare the requested ID with the patient's stored ID.
            if (id.equals(
                    String.valueOf(patients.get(i).get("id")))) {

                // Return the position when a match is found.
                return i;
            }
        }

        // Return -1 if no patient matches the supplied ID.
        return -1;
    }
}