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

  public static Color[][] resizeColorArray(Color[][] originalPixels, int largeur, int hauteur) {
    int hauteurOriginale = originalPixels.length;
    int largeurOriginale = originalPixels[0].length;

    Color[][] redimensionnee = new Color[hauteur][largeur];

    double scaleX = (double) largeurOriginale / largeur;
    double scaleY = (double) hauteurOriginale / hauteur;

    for (int y = 0; y < hauteur; y++) {
      for (int x = 0; x < largeur; x++) {
        int origY = (int) (y * scaleY);
        int origX = (int) (x * scaleX);

        origY = Math.min(origY, hauteurOriginale - 1);
        origX = Math.min(origX, largeurOriginale - 1);

        redimensionnee[y][x] = originalPixels[origY][origX];
      }
    }

    return redimensionnee;
  }


}