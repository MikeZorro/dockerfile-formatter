package org.task;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

public class DockerfileGenerator {


    private YamlDataReader yamlReader;

    public DockerfileGenerator(YamlDataReader yamlReader) {
        this.yamlReader = yamlReader;
    }

    public void generateDockerfile(String projectName, String templatePath, String outputPath) {
        String command = formatCommandArray(yamlReader.getValue(projectName));

        if (command == null) {
            System.out.println("Project '" + projectName + "' not found.");
            return;
        }

        // Ensure output directory exists
        File outputFile = new File(outputPath);
        File parentDir = outputFile.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            boolean created = parentDir.mkdirs();
            if (!created) {
                System.err.println("Failed to create output directory: " + parentDir.getAbsolutePath());
                return;
            }
        }

        // Process the Dockerfile template
        try (BufferedReader reader = new BufferedReader(new FileReader(templatePath));
             PrintWriter writer = new PrintWriter(new FileWriter(outputFile))) {

            String line;
            while ((line = reader.readLine()) != null) {
                line = line.replace("<name>", projectName);
                line = line.replace("<startup_command>", command);
                writer.println(line);
            }

            System.out.println("✅ Dockerfile generated for project: " + projectName + " → " + outputPath);
        } catch (IOException e) {
            System.err.println("Error generating Dockerfile: " + e.getMessage());
        }
    }

    private String formatCommandArray(String command) {
        String[] parts = command.trim().split("\\s+");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < parts.length; i++) {
            sb.append(parts[i]);
            if (i != parts.length - 1) {
                sb.append("\", \"");
            }
        }
        return sb.toString();
    }

}