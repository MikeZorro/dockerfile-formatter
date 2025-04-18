package org.task;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class YamlDataReader {

    private Map<String, String> data;

    public YamlDataReader() {
        this.data = new HashMap<>();
    }

    /**
     * Loads a project YAML file and stores key-value pairs in the instance.
     */
    public void readFromFile(String filePath) {

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            String key = "N/A";
            String value = "N/A";
            while ((line = reader.readLine()) != null) {
                line = line.strip();

                if (line.isEmpty() || line.startsWith("---")) continue;

                if (line.contains(":") && line.contains("name")) {
                    String[] parts = line.split(":", 2);
                    key = parts[1].strip();

                }
                if (line.contains(":") && line.contains("startup_command")) {
                    String[] parts = line.split(":", 2);
                    value = parts[1].strip();
                }
            }
            data.put(key, value);
        } catch (IOException e) {
            System.err.println("Error reading YAML file: " + e.getMessage());
        }
    }

    public void printInfo(String projectName) {
        String name = projectName;
        String command = data.getOrDefault(projectName, "N/A");

        System.out.println("Project Name: " + name);
        System.out.println("Startup Command: " + command);
    }


    public Map<String, String> getAllData() {
        return new HashMap<>(data);
    }

    public String getValue(String key) {
        return data.getOrDefault(key, null);  // Return null if key is not found
    }
}
