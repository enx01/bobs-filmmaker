package xyz.bobindustries.film.utils;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class ImageUtils {

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
      ImageIO.write(exportedImage, "png", new File(outputPath));
      System.out.println("Image exportée avec succès à : " + outputPath);
      return true;
    } catch (IOException e) {
      e.printStackTrace();
      return false;
    }
  }
}