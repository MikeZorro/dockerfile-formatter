This project is an attempt to solve given dockerfile formatter task.

Some assumptions I have made during design are:
- support for multiple project.yaml specifications - a map of project names and commands in a YamlDataReader class
- support for generating multiple dockerfiles from different templates for the same project and viceversa - DockerfileGenerator class
- error handing and validation - project file can be loaded if the commands are missing, but the dockerfile wont be generated in such a case nor if template does not have required fields
- commands formated to array of strings as required by Docker
