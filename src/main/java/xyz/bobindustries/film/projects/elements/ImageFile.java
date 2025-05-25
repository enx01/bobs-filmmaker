package xyz.bobindustries.film.projects.elements;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;

import javax.imageio.ImageIO;

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

    public Image getImage() {
        return Toolkit.getDefaultToolkit().createImage(content);
    }

    /**
     * Helper method to get a BufferedImage from the byte data.
     * Returns null if data is invalid or null.
     */
    public BufferedImage getBufferedImage() {
        if (content == null || content.length == 0) {
            return null;
        }
        try (ByteArrayInputStream bis = new ByteArrayInputStream(content)) {
            return ImageIO.read(bis);
        } catch (IOException e) {
            System.err.println("Error reading image data for " + fileName + ": " + e.getMessage());
            return null;
        }
    }

}
