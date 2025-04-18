package org.task;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class YamlDataReaderTest {
    private YamlDataReader reader;

    @BeforeEach
    void setUp() {
        reader = new YamlDataReader();
    }

    @Test
    void testReadValidYamlFile(@TempDir Path tempDir) throws IOException {
        String yamlContent = """
                ---
                name: MyAwesomeService
                startup_command: rails server
                """;

        Path yamlFile = tempDir.resolve("service.yaml");
        try (BufferedWriter writer = Files.newBufferedWriter(yamlFile)) {
            writer.write(yamlContent);
        }

        reader.readFromFile(yamlFile.toString());

        Map<String, String> result = reader.getAllData();
        assertEquals(1, result.size());
        assertEquals("rails server", result.get("MyAwesomeService"));
    }

    @Test
    void testReadYamlMissingStartupCommand(@TempDir Path tempDir) throws IOException {
        String yamlContent = """
                ---
                name: LonelyService
                """;

        Path yamlFile = tempDir.resolve("missing_command.yaml");
        Files.writeString(yamlFile, yamlContent);

        reader.readFromFile(yamlFile.toString());

        Map<String, String> result = reader.getAllData();
        assertEquals("N/A", result.get("LonelyService"));
    }

    @Test
    void testEmptyYamlFileOrMissingName(@TempDir Path tempDir) throws IOException {
        Path emptyFile = tempDir.resolve("empty.yaml");
        Files.createFile(emptyFile);

        reader.readFromFile(emptyFile.toString());

        assertTrue(reader.getAllData().isEmpty());
    }

    @Test
    void testFileDoesNotExist() {
        assertDoesNotThrow(() -> reader.readFromFile("nonexistent.yaml"));
        assertTrue(reader.getAllData().isEmpty());
    }

    @Test
    void testGetValueReturnsNullIfKeyMissing() {
        assertNull(reader.getValue("GhostService"));
    }

    @Test
    void testPrintInfoOutput(@TempDir Path tempDir) throws IOException {
        String yamlContent = """
                ---
                name: TestService
                startup_command: rails server
                """;

        Path yamlFile = tempDir.resolve("service.yaml");
        try (BufferedWriter writer = Files.newBufferedWriter(yamlFile)) {
            writer.write(yamlContent);
        }


        reader.readFromFile(yamlFile.toString());

        // Capture System.out
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(outContent));

        try {
            // Act
            reader.printInfo("TestService");

            // Assert
            String expectedOutput = """
                    Project Name: TestService
                    Startup Command: rails server
                    """;
            String actual = outContent.toString().trim().replace("\r\n", "\n"); // normalize newlines
            assertEquals(expectedOutput.trim(), actual);
        } finally {
            // Restore System.out
            System.setOut(originalOut);
        }
    }
}