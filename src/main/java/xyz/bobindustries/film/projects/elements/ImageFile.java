package xyz.bobindustries.film.projects.elements;

import java.awt.*;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import java.io.ByteArrayInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ImageFile {
  private String fileName;
  private byte[] content;

  public ImageFile(String fileName, byte[] content) {
    this.fileName = fileName;
    this.content = content;
  }

  public ImageFile(String path) throws IOException {
      Path imgPath = Paths.get(path);
      fileName = getFileName();
      content = Files.readAllBytes(imgPath);
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

  public Color[][] getColorMatrix() {
    try {
      ByteArrayInputStream bais = new ByteArrayInputStream(content);
      BufferedImage image = ImageIO.read(bais);
      int width = image.getWidth();
      int height = image.getHeight();

      Color[][] colorArray = new Color[height][width];

      for (int y = 0; y < height; y++) {
        for (int x = 0; x < width; x++) {
          int rgb = image.getRGB(x, y);
          colorArray[y][x] = new Color(rgb);
        }
      }
      System.out.println("Image importée avec succès dans un tableau de couleurs.");
      System.out.println(colorArray.length);
      return colorArray;
    } catch (IOException e) {
      System.out.println("une erreur est survenue");
      e.printStackTrace();
      return null;
    }

  }

}
