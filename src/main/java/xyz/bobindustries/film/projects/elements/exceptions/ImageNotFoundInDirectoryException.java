package xyz.bobindustries.film.projects.elements.exceptions;

public class ImageNotFoundInDirectoryException extends RuntimeException {
    public String imageName;

    public ImageNotFoundInDirectoryException(String message, String imageName) {
        super(message);
        this.imageName = imageName;
    }
}
