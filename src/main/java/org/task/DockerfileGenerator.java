package org.task;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

public class DockerfileGenerator {


    private YamlDataReader yamlReader;

    public DockerfileGenerator(YamlDataReader yamlReader) {
        this.yamlReader = yamlReader;
    }

    public void generateDockerfile(String projectName, String templatePath, String outputPath) {
        try {
            String template = Files.readString(Path.of(templatePath));
            validateTemplate(template);

            String command = formatCommandArray(yamlReader.getValue(projectName));
            if (command == null || command.equals("N/A")) {
                System.err.println("Startup command not found for project: " + projectName);
                return;
            }

            String dockerfileContent = template
                    .replace("<name>", projectName)
                    .replace("<startup_command>", command);

            Files.writeString(Path.of(outputPath), dockerfileContent);
            System.out.println("Dockerfile generated successfully for: " + projectName);
        } catch (IOException e) {
            System.err.println("Error generating Dockerfile: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            System.err.println("Template validation error: " + e.getMessage());
        }
    }

    /**
     * This takes list of commands from template file as an input and formats it according to dockerfile needs -as an array of strings
     * @param command a whitespace seperated list of commands
     * @return formatted command
     */
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

    /**
     * This helper function makes sure that both <name> and <startup_command> fields are present in th template file
     * @param template template to be inspected
     */
    private void validateTemplate(String template) {
        if (!template.contains("<name>") || !template.contains("<startup_command>")) {
            throw new IllegalArgumentException("Template must contain both <name> and <command> placeholders.");
        }
    }
}