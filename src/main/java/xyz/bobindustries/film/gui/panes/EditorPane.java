package xyz.bobindustries.film.gui.panes;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.AffineTransform;

public class EditorPane extends JPanel {
  private double scale = 1.0;
  private Point origin = new Point(0, 0);
  private Color[][] gridColors = new Color[10][10];
  private int hoveredGridX = -1;
  private int hoveredGridY = -1;
  private final Rectangle blueSquare = new Rectangle(50, 50, 100, 100);

  public EditorPane() {
    for (int i = 0; i < gridColors.length; i++) {
      for (int j = 0; j < gridColors[i].length; j++) {
        gridColors[i][j] = Color.WHITE;
      }
    }

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

    drawGrid(g2d);

    g2d.setTransform(at);
  }

  private void drawGrid(Graphics2D g2d) {
    int gridSize = 10;
    int startX = blueSquare.x;
    int startY = blueSquare.y;

    for (int i = 0; i < 10; i++) {
      for (int j = 0; j < 10; j++) {
        int x = startX + (i * gridSize);
        int y = startY + (j * gridSize);

        g2d.setColor(gridColors[i][j]);
        g2d.fillRect(x, y, gridSize, gridSize);

        if (i == hoveredGridX && j == hoveredGridY) {
          g2d.setColor(Color.GRAY);
          g2d.fillRect(x, y, gridSize, gridSize);
        }
      }
    }

  }

  private void colorGridSquare(Point p) {
    int gridSize = 10;
    int gridX = (p.x - blueSquare.x) / gridSize;
    int gridY = (p.y - blueSquare.y) / gridSize;

    if (gridX >= 0 && gridX < 10 && gridY >= 0 && gridY < 10) {
      gridColors[gridX][gridY] = Color.RED;
    }
  }
}
