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
  private String path;

  public ImageFile(String fileName, String path) {
    this.fileName = fileName;
    this.path = path;
  }

  public ImageFile(String path) throws IOException {
    Path imgPath = Paths.get(path);
    fileName = imgPath.getFileName().toString();
    this.path = path;
    System.out.println("pathcst;"+path);
  }

  public String getPath() {
    return path;
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
      System.out.println("path;"+path);
      BufferedImage image = ImageIO.read(new File(path));
      if (image == null) {
        System.out.println("L'image n'a pas pu être lue. Vérifie le format.");
        return null;
      }

      int width = image.getWidth();
      int height = image.getHeight();
      System.out.println("width: " + width);
      System.out.println("height: " + height);

      Color[][] colorArray = new Color[height][width];

      for (int y = 0; y < height; y++) {
        for (int x = 0; x < width; x++) {
          int rgb = image.getRGB(x, y);
          colorArray[y][x] = new Color(rgb, true); // true = prendre en compte l'alpha
        }
      }

      System.out.println("Image importée avec succès dans un tableau de couleurs.");
      return colorArray;

    } catch (IOException e) {
      System.out.println("Une erreur est survenue lors de la lecture de l'image");
      e.printStackTrace();
      return null;
    }
  }

}
