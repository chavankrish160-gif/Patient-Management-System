package patientrecord;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

class Json {

    // Store the JSON input string and the current reading position.
    private final String source;
    private int index;

    // Constructor: initialize the parser with the JSON input.
    private Json(String source) {
        this.source = source;
    }

    // Parse the JSON input and return the resulting Java object.
    static Object parse(String source) {
        // Create a parser for the given input.
        Json parser = new Json(source);

        // Parse the first JSON value.
        Object result = parser.value();

        // Skip any whitespace remaining after the parsed value.
        parser.space();

        // Reject extra characters after the JSON value.
        if (parser.index != source.length()) {
            throw new IllegalArgumentException("Invalid JSON data.");
        }

        return result;
    }

    // Convert Java objects into their JSON string representation.
    static String stringify(Object value) {

        // Convert a null value into JSON null.
        if (value == null) {
            return "null";
        }

        // Convert a Java String into a quoted JSON string.
        if (value instanceof String text) {
            return quote(text);
        }

        // Numbers and Boolean values can be written directly.
        if (value instanceof Number || value instanceof Boolean) {
            return value.toString();
        }

        // Convert a Java Map into a JSON object.
        if (value instanceof Map<?, ?> map) {
            List<String> entries = new ArrayList<>();

            // Convert each map entry into a JSON key-value pair.
            map.forEach((key, item) ->
                    entries.add(
                            quote(String.valueOf(key))
                                    + ":" + stringify(item)
                    )
            );

            // Join all entries and surround them with braces.
            return "{" + String.join(",", entries) + "}";
        }

        // Convert a Java List into a JSON array.
        if (value instanceof List<?> list) {
            return "[" + String.join(
                    ",",
                    list.stream()
                            .map(Json::stringify)
                            .toList()
            ) + "]";
        }

        // Reject objects that this serializer does not support.
        throw new IllegalArgumentException("Unsupported JSON value.");
    }

    // Read and identify the next JSON value.
    private Object value() {
        // Skip spaces before reading the value.
        space();

        // Check whether the input has ended unexpectedly.
        if (index >= source.length()) {
            throw new IllegalArgumentException("Invalid JSON data.");
        }

        // Read the current character.
        char current = source.charAt(index);

        // Parse an object when the character is an opening brace.
        if (current == '{') {
            return object();
        }

        // Parse an array when the character is an opening bracket.
        if (current == '[') {
            return array();
        }

        // Parse a string when the character is a quotation mark.
        if (current == '"') {
            return string();
        }

        // Parse the Boolean value true.
        if (current == 't') {
            literal("true");
            return true;
        }

        // Parse the Boolean value false.
        if (current == 'f') {
            literal("false");
            return false;
        }

        // Parse the null value.
        if (current == 'n') {
            literal("null");
            return null;
        }

        // If no other type matches, parse a number.
        return number();
    }

    // Parse a JSON object containing key-value pairs.
    private Map<String, Object> object() {
        // Skip the opening brace.
        index++;

        // Create a map to store the object's fields.
        Map<String, Object> result = new LinkedHashMap<>();

        // Skip whitespace before reading the first field.
        space();

        // Return an empty map if the object is empty.
        if (take('}')) {
            return result;
        }

        // Read each key-value pair until the closing brace is found.
        while (true) {
            space();

            // JSON object keys must be quoted strings.
            if (index >= source.length()
                    || source.charAt(index) != '"') {
                throw new IllegalArgumentException("Invalid JSON data.");
            }

            // Read the key.
            String key = string();

            // Skip spaces and require a colon after the key.
            space();
            require(':');

            // Parse the value and store it with its key.
            result.put(key, value());

            // Skip spaces after the value.
            space();

            // Finish parsing when the closing brace is found.
            if (take('}')) {
                return result;
            }

            // Require a comma before the next key-value pair.
            require(',');
        }
    }

    // Parse a JSON array containing multiple values.
    private List<Object> array() {
        // Skip the opening bracket.
        index++;

        // Create a list to store array elements.
        List<Object> result = new ArrayList<>();

        // Skip whitespace before reading elements.
        space();

        // Return an empty list if the array is empty.
        if (take(']')) {
            return result;
        }

        // Read array elements until the closing bracket is found.
        while (true) {
            // Parse and add the next element.
            result.add(value());

            // Skip whitespace after the element.
            space();

            // Finish parsing when the closing bracket is found.
            if (take(']')) {
                return result;
            }

            // Require a comma before the next element.
            require(',');
        }
    }

    // Parse a quoted JSON string and handle escape sequences.
    private String string() {
        // Require the opening quotation mark.
        require('"');

        // Build the decoded string character by character.
        StringBuilder result = new StringBuilder();

        while (index < source.length()) {
            // Read the next character and advance the position.
            char current = source.charAt(index++);

            // Return the string when the closing quote is found.
            if (current == '"') {
                return result.toString();
            }

            // Add ordinary characters directly.
            if (current != '\\') {
                result.append(current);
                continue;
            }

            // An escape marker must be followed by another character.
            if (index >= source.length()) {
                break;
            }

            // Read the escape sequence character.
            char escape = source.charAt(index++);

            // Convert JSON escape sequences into Java characters.
            switch (escape) {
                case '"', '\\', '/' -> result.append(escape);
                case 'b' -> result.append('\b');
                case 'f' -> result.append('\f');
                case 'n' -> result.append('\n');
                case 'r' -> result.append('\r');
                case 't' -> result.append('\t');

                // Decode a Unicode escape containing four hexadecimal digits.
                case 'u' -> {
                    if (index + 4 > source.length()) {
                        throw new IllegalArgumentException(
                                "Invalid JSON escape.");
                    }

                    try {
                        // Convert the hexadecimal value into a character.
                        result.append((char) Integer.parseInt(
                                source.substring(index, index + 4),
                                16
                        ));
                    } catch (NumberFormatException exception) {
                        throw new IllegalArgumentException(
                                "Invalid JSON escape.");
                    }

                    // Move past the four Unicode digits.
                    index += 4;
                }

                // Reject unsupported escape sequences.
                default -> throw new IllegalArgumentException(
                        "Invalid JSON escape.");
            }
        }

        // Throw an error if the closing quotation mark is missing.
        throw new IllegalArgumentException("Invalid JSON string.");
    }

    // Parse a numeric value as an integer or decimal number.
    private Number number() {
        // Remember where the number begins.
        int start = index;

        // Read characters that may form a numeric token.
        while (index < source.length()
                && "-+0123456789.eE".indexOf(
                        source.charAt(index)) >= 0) {
            index++;
        }

        // Reject input that does not contain a numeric token.
        if (start == index) {
            throw new IllegalArgumentException("Invalid JSON value.");
        }

        // Extract the numeric text from the input.
        String token = source.substring(start, index);

        try {
            // Parse decimal and scientific-notation numbers.
            if (token.contains(".")
                    || token.contains("e")
                    || token.contains("E")) {
                return Double.parseDouble(token);
            }

            // Parse whole numbers as long integers.
            return Long.parseLong(token);

        } catch (NumberFormatException exception) {
            // Report an error when the number cannot be converted.
            throw new IllegalArgumentException("Invalid JSON number.");
        }
    }

    // Read a JSON keyword such as true, false, or null.
    private void literal(String text) {
        // Check whether the expected keyword exists at this position.
        if (!source.startsWith(text, index)) {
            throw new IllegalArgumentException("Invalid JSON value.");
        }

        // Advance past the keyword.
        index += text.length();
    }

    // Skip whitespace characters before parsing the next token.
    private void space() {
        while (index < source.length()
                && Character.isWhitespace(source.charAt(index))) {
            index++;
        }
    }

    // Consume a character only if it matches the expected character.
    private boolean take(char expected) {
        if (index < source.length()
                && source.charAt(index) == expected) {
            index++;
            return true;
        }

        // Return false when the character does not match.
        return false;
    }

    // Require a specific character or report invalid JSON.
    private void require(char expected) {
        if (!take(expected)) {
            throw new IllegalArgumentException("Invalid JSON data.");
        }
    }

    // Escape special characters to create a valid JSON string.
    private static String quote(String value) {
        // Start the JSON string with a quotation mark.
        StringBuilder result = new StringBuilder("\"");

        // Process every character in the supplied Java string.
        for (char character : value.toCharArray()) {
            switch (character) {
                // Escape quotation marks and backslashes.
                case '"' -> result.append("\\\"");
                case '\\' -> result.append("\\\\");

                // Escape special control characters.
                case '\b' -> result.append("\\b");
                case '\f' -> result.append("\\f");
                case '\n' -> result.append("\\n");
                case '\r' -> result.append("\\r");
                case '\t' -> result.append("\\t");

                default -> {
                    // Convert other control characters to Unicode escapes.
                    if (character < 0x20) {
                        result.append(String.format(
                                "\\u%04x", (int) character));
                    } else {
                        // Append ordinary characters without modification.
                        result.append(character);
                    }
                }
            }
        }

        // Add the closing quotation mark and return the JSON string.
        return result.append('"').toString();
    }
}
