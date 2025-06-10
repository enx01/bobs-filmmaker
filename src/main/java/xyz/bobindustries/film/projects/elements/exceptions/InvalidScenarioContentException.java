package xyz.bobindustries.film.projects.elements.exceptions;

public class InvalidScenarioContentException extends Exception {

    public static final int INVALID_LINE_FORMAT = 0,
            EMPTY_FILENAME = 1,
            INVALID_TIME = 2;

    public int type, line;
    public String lineData;

    public InvalidScenarioContentException() {
        super();
    }

    public InvalidScenarioContentException(String message, int type, int line, String lineData) {
        super(message);
        this.type = type;
        this.line = line;
        this.lineData = lineData;
    }
}
