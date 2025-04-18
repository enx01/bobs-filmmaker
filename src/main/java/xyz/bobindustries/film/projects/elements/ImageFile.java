package xyz.bobindustries.film.projects.elements;

public class ImageFile {
    private final String fileName;
    private byte[] content;

    public ImageFile(String fileName, byte[] content) {
        this.fileName = fileName;
        this.content = content;
    }

    public String getFileName() {
        return fileName;
    }

    public byte[] getContent() {
        return content;
    }

    public void setContent(byte[] content) {
        this.content = content;
    }

}
