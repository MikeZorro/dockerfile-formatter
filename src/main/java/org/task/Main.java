package org.task;

public class Main {
    public static void main(String[] args) {
        //Task 1
        YamlDataReader yamlDataReader = new YamlDataReader();
        yamlDataReader.readFromFile("src/main/resources/project.yaml");
        yamlDataReader.printInfo("MyAwesomeService");

        yamlDataReader.readFromFile("src/main/resources/project2.yaml");
        yamlDataReader.printInfo("TestService");

        System.out.println(yamlDataReader.getAllData());

        //Task 2
        String templatePath = "src/main/resources/templates/ruby-template.yaml";
        String templatePath2 = "src/main/resources/templates/ruby-template-new.yaml";
        String outputPath = "src/main/resources/output/test";
        String outputPath2 = "src/main/resources/output/test2";

        DockerfileGenerator dockerfileGenerator = new DockerfileGenerator(yamlDataReader);
        dockerfileGenerator.generateDockerfile( "MyAwesomeService", templatePath, outputPath);
        dockerfileGenerator.generateDockerfile( "TestService", templatePath2, outputPath2);

    }
}