package xyz.bobindustries.film.projects.elements.exceptions;

public class InvalidFileFormatException extends RuntimeException {
    public String fileName;

    public InvalidFileFormatException(String message, String fileName) {
        super(message);
        this.fileName = fileName;
    }
}
