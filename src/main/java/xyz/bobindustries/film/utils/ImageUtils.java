package xyz.bobindustries.film.utils;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class ImageUtils {

  public static Color[][] importImage(String path) {
    try {
      BufferedImage image = ImageIO.read(new File(path));

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

  public static boolean exportImage(Color[][] image, String outputPath) {
    int width = image[0].length;
    int height = image.length;

    BufferedImage exportedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

    for (int y = 0; y < height; y++) {
      for (int x = 0; x < width; x++) {
        Color color = image[y][x];
        exportedImage.setRGB(x, y, color.getRGB());
      }
    }

    try {
      ImageIO.write(exportedImage, "jpg", new File(outputPath));
      System.out.println("Image exportée avec succès à : " + outputPath);
      return true;
    } catch (IOException e) {
      e.printStackTrace();
      return false;
    }
  }
}
