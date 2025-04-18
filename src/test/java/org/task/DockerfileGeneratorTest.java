package org.task;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;
import org.task.DockerfileGenerator;

import java.io.*;
import java.nio.file.*;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;


public class DockerfileGeneratorTest {

    private static final String TEMPLATE_CONTENT = """
                FROM ruby:3.0
                LABEL maintainer="John Doe"
                WORKDIR /app
                COPY . /app
                RUN bundle install
                CMD ["<startup_command>"] # Startup command for <name>.
                """;

    private static final String TEST_CONFIG = """
                ---
                name: MyAwesomeService
                startup_command: rails server
                """;

    private Path tempDir;
    private Path templateFile;
    private Path configFile;

    @BeforeEach
    public void setUp() throws IOException {
        // Create a temporary directory for test files
        tempDir = Files.createTempDirectory("test-output");

        // Create a temporary template file
        templateFile = Files.createTempFile(tempDir, "Dockerfile.template", ".txt");
        Files.writeString(templateFile, TEMPLATE_CONTENT);

        // Create a temporary config file
        configFile = Files.createTempFile(tempDir, "config.yaml", ".yaml");
        Files.writeString(configFile, TEST_CONFIG);
    }

    @Test
    public void testNormalConfig() throws IOException {
        runTest(configFile, "MyAwesomeService", """
                FROM ruby:3.0
                LABEL maintainer="John Doe"
                WORKDIR /app
                COPY . /app
                RUN bundle install
                CMD ["rails", "server"] # Startup command for MyAwesomeService.
                """.trim());
    }

    @Test
    public void testMissingCommand() throws IOException {
        String config = """
                ---
                name: MyAwesomeService
                """;
        Path tempConfig = Files.createTempFile(tempDir, "config_missing_command.yaml", ".yaml");
        Files.writeString(tempConfig, config);

        runTestExpectNoOutput(tempConfig, "MyAwesomeService");
    }

    @Test
    public void testExtraWhitespaceCommand() throws IOException {
        String config = """
                ---
                name: SpacedOutService
                startup_command:   rails     server  
                """;
        Path tempConfig = Files.createTempFile(tempDir, "config_whitespace.yaml", ".yaml");
        Files.writeString(tempConfig, config);

        runTest(tempConfig, "SpacedOutService", """
                FROM ruby:3.0
                LABEL maintainer="John Doe"
                WORKDIR /app
                COPY . /app
                RUN bundle install
                CMD ["rails", "server"] # Startup command for SpacedOutService.
                """.trim());
    }

    @Test
    public void testMultipleCommandParts() throws IOException {
        String config = """
                ---
                name: FancyApp
                startup_command: bundle exec rails server
                """;
        Path tempConfig = Files.createTempFile(tempDir, "config_multiple_commands.yaml", ".yaml");
        Files.writeString(tempConfig, config);

        runTest(tempConfig, "FancyApp", """
                FROM ruby:3.0
                LABEL maintainer="John Doe"
                WORKDIR /app
                COPY . /app
                RUN bundle install
                CMD ["bundle", "exec", "rails", "server"] # Startup command for FancyApp.
                """.trim());
    }

    // Helper: Successful generation
    private void runTest(Path configFile, String project, String expectedOutput) throws IOException {
        // Load YAML to Map
        YamlDataReader reader = new YamlDataReader();
        reader.readFromFile(configFile.toString());

        // Generate Dockerfile
        DockerfileGenerator generator = new DockerfileGenerator(reader);
        Path outputPath = tempDir.resolve("Dockerfile_" + project);
        generator.generateDockerfile(project, templateFile.toString(), outputPath.toString());

        // Assert the generated output is as expected
        String actual = Files.readString(outputPath).trim();

        // Normalize line endings for comparison
        actual = normalizeLineEndings(actual);
        expectedOutput = normalizeLineEndings(expectedOutput);

        assertEquals(expectedOutput, actual);
    }

    // Helper: Expect no file generated (error case)
    private void runTestExpectNoOutput(Path configFile, String project) throws IOException {
        // Load YAML to Map
        YamlDataReader reader = new YamlDataReader();
        reader.readFromFile(configFile.toString());

        // Generate Dockerfile
        DockerfileGenerator generator = new DockerfileGenerator(reader);
        Path outputPath = tempDir.resolve("Dockerfile_" + project);
        generator.generateDockerfile(project, templateFile.toString(), outputPath.toString());

        assertFalse(Files.exists(outputPath), "Output file should not be generated for project: " + project);
    }

    @AfterEach
    public void cleanUp() throws IOException {
        // Clean up the temporary files and directories after each test
        Files.walk(tempDir)
                .sorted(Comparator.reverseOrder())
                .map(Path::toFile)
                .forEach(File::delete);
    }

    // Helper: Normalize line endings to avoid OS-specific issues
    private String normalizeLineEndings(String input) {
        return input.replaceAll("\r\n|\n\r|\r", "\n");
    }
}