package xyz.bobindustries.film.gui.panes;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

public class EditorPane extends JPanel {
  private double scale = 1.0;
  private Point origin = new Point(0, 0);
  private Color[][] gridColors;
  private int hoveredGridX = -1;
  private int hoveredGridY = -1;
  private final int gridSquareSize = 10;
  private int gridWidth;
  private int gridHeight;
  private final Rectangle blueSquare;
  private BufferedImage gridImage;

  public EditorPane(Color[][] gridColors, int gridWidth, int gridHeight) {

    this.gridColors = gridColors;
    this.gridWidth = gridWidth;
    this.gridHeight = gridHeight;

    blueSquare = new Rectangle(50, 50, gridSquareSize * gridWidth, gridSquareSize * gridHeight);

    gridImage = new BufferedImage(blueSquare.width, blueSquare.height, BufferedImage.TYPE_INT_ARGB);
    drawGridImage();

    MouseAdapter mouseHandler = new MouseAdapter() {
      @Override
      public void mouseMoved(MouseEvent e) {
        Point adjustedPoint = new Point(
            (int) ((e.getX() - origin.x) / scale),
            (int) ((e.getY() - origin.y) / scale));

        if (blueSquare.contains(adjustedPoint)) {
          hoveredGridX = (adjustedPoint.x - blueSquare.x) / 10;
          hoveredGridY = (adjustedPoint.y - blueSquare.y) / 10;
        } else {
          hoveredGridX = -1;
          hoveredGridY = -1;
        }
        repaint();
      }

      @Override
      public void mousePressed(MouseEvent e) {
        Point adjustedPoint = new Point(
            (int) ((e.getX() - origin.x) / scale),
            (int) ((e.getY() - origin.y) / scale));

        hoveredGridX = -1;
        hoveredGridY = -1;
        if (blueSquare.contains(adjustedPoint)) {
          colorGridSquare(adjustedPoint);
          updateImage(adjustedPoint);
          // drawGridImage();
          repaint();
        }
      }

      @Override
      public void mouseWheelMoved(MouseWheelEvent e) {
        if (e.isControlDown()) {
          Point mouse = e.getPoint();
          double oldScale = scale;
          if (e.getWheelRotation() < 0) {
            scale *= 1.1;
          } else {
            scale /= 1.1;
          }
          origin.x = (int) (mouse.x - (mouse.x - origin.x) * (scale / oldScale));
          origin.y = (int) (mouse.y - (mouse.y - origin.y) * (scale / oldScale));
          repaint();
        }
      }

      @Override
      public void mouseDragged(MouseEvent e) {
        Point adjustedPoint = new Point(
            (int) ((e.getX() - origin.x) / scale),
            (int) ((e.getY() - origin.y) / scale));

        if (blueSquare.contains(adjustedPoint)) {
          colorGridSquare(adjustedPoint);
          updateImage(adjustedPoint);
          // drawGridImage();
          repaint();
        }
      }

    };

    addMouseMotionListener(mouseHandler);
    addMouseListener(mouseHandler);
    addMouseWheelListener(mouseHandler);
  }

  @Override
  protected void paintComponent(Graphics g) {
    super.paintComponent(g);
    Graphics2D g2d = (Graphics2D) g;
    AffineTransform at = g2d.getTransform();
    g2d.translate(origin.x, origin.y);
    g2d.scale(scale, scale);

    // drawGrid(g2d);

    g2d.drawImage(gridImage, blueSquare.x, blueSquare.y, null);

    // Dessiner le carré survolé
    if (hoveredGridX >= 0 && hoveredGridY >= 0) {
      int hoverX = blueSquare.x + (hoveredGridX * gridSquareSize);
      int hoverY = blueSquare.y + (hoveredGridY * gridSquareSize);
      g2d.setColor(Color.GRAY);
      g2d.fillRect(hoverX, hoverY, gridSquareSize, gridSquareSize);
    }

    g2d.setTransform(at);
  }

  private void drawGrid(Graphics2D g2d) {
    int startX = blueSquare.x;
    int startY = blueSquare.y;

    for (int i = 0; i < gridHeight; i++) {
      for (int j = 0; j < gridWidth; j++) {
        int x = startX + (i * gridSquareSize);
        int y = startY + (j * gridSquareSize);

        g2d.setColor(gridColors[j][i]);
        g2d.fillRect(x, y, gridSquareSize, gridSquareSize);

        if (i == hoveredGridX && j == hoveredGridY) {
          g2d.setColor(Color.GRAY);
          g2d.fillRect(x, y, gridSquareSize, gridSquareSize);
        }
      }
    }
  }

  private void drawGridImage() {
    Graphics2D g2d = gridImage.createGraphics();
    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

    g2d.setColor(Color.WHITE);
    g2d.fillRect(0, 0, gridImage.getHeight(), gridImage.getWidth());

    for (int i = 0; i < gridWidth; i++) {
      for (int j = 0; j < gridHeight; j++) {
        int x = j * gridSquareSize;
        int y = i * gridSquareSize;

        g2d.setColor(gridColors[i][j]);
        g2d.fillRect(x, y, gridSquareSize, gridSquareSize);
      }
    }

    g2d.dispose(); // Libérer les ressources graphiques
  }

  private void updateImage(Point p) {
    Graphics2D g2d = gridImage.createGraphics();
    int gridX = (p.x - blueSquare.x) / gridSquareSize;
    int gridY = (p.y - blueSquare.y) / gridSquareSize;
    int x = gridY * gridSquareSize;
    int y = gridX * gridSquareSize;
    g2d.setColor(gridColors[gridY][gridX]);
    g2d.fillRect(y, x, gridSquareSize, gridSquareSize);
    g2d.dispose();
  }

  private void colorGridSquare(Point p) {
    int gridSize = gridSquareSize;
    int gridX = (p.x - blueSquare.x) / gridSize;
    int gridY = (p.y - blueSquare.y) / gridSize;

    if (gridX >= 0 && gridX < gridHeight && gridY >= 0 && gridY < gridWidth) {
      gridColors[gridY][gridX] = Color.RED;
    }
  }

  public void setScale(double scale) {
    this.scale = scale;
  }

  public void setOrigin(Point origin) {
    this.origin = origin;
  }

  public void setGridColors(Color[][] gridColors) {
    this.gridColors = gridColors;
  }

  public void setHoveredGridX(int hoveredGridX) {
    this.hoveredGridX = hoveredGridX;
  }

  public void setHoveredGridY(int hoveredGridY) {
    this.hoveredGridY = hoveredGridY;
  }
}
