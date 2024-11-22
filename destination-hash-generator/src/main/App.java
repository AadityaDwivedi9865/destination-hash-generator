package com.bajaj.challenge;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

public class App {
    private static final int RANDOM_STRING_LENGTH = 8;
    private static final String CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static String destinationValue = null;

    public static void main(String[] args) {
        try {
            // Validate command line arguments
            if (args.length != 2) {
                System.err.println("Usage: java -jar app.jar <roll_number> <json_file_path>");
                System.exit(1);
            }

            String rollNumber = args[0];
            String jsonFilePath = args[1];

            // Generate random string
            String randomString = generateRandomString();

            // Parse JSON and find destination
            String destination = findDestination(jsonFilePath);
            if (destination == null) {
                System.err.println("No 'destination' key found in JSON file");
                System.exit(1);
            }

            // Concatenate values and generate hash
            String concatenated = rollNumber + destination + randomString;
            String hash = generateMD5Hash(concatenated);

            // Output result in required format
            System.out.println(hash + ";" + randomString);

        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            System.exit(1);
        }
    }

    private static String generateRandomString() {
        Random random = new Random();
        StringBuilder sb = new StringBuilder(RANDOM_STRING_LENGTH);
        for (int i = 0; i < RANDOM_STRING_LENGTH; i++) {
            sb.append(CHARS.charAt(random.nextInt(CHARS.length())));
        }
        return sb.toString();
    }

    private static String findDestination(String jsonFilePath) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode rootNode = mapper.readTree(new File(jsonFilePath));
        traverseJson(rootNode);
        return destinationValue;
    }

    private static void traverseJson(JsonNode node) {
        if (destinationValue != null) {
            return; // Stop if we've already found the destination
        }

        if (node.isObject()) {
            node.fields().forEachRemaining(entry -> {
                if ("destination".equals(entry.getKey()) && destinationValue == null) {
                    destinationValue = entry.getValue().asText();
                } else if (destinationValue == null) {
                    traverseJson(entry.getValue());
                }
            });
        } else if (node.isArray()) {
            node.elements().forEachRemaining(element -> {
                if (destinationValue == null) {
                    traverseJson(element);
                }
            });
        }
    }

    private static String generateMD5Hash(String input) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("MD5");
        byte[] messageDigest = md.digest(input.getBytes());
        
        StringBuilder hexString = new StringBuilder();
        for (byte b : messageDigest) {
            String hex = Integer.toHexString(0xFF & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }
}