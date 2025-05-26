package xyz.bobindustries.film.image_editor;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;

public class Brush implements Tools, ToolsSettings {

  private int radius;
  private ArrayList<Point> brushOffsets;
  private int thickness;

  public Brush(int radius) {
    this.radius = radius;
    generateBrush(radius);
  }

  void generateBrush(int radius) {
    brushOffsets = new ArrayList<>();
    int r2 = radius * radius;
    for (int y = -radius; y <= radius; y++) {
      for (int x = -radius; x <= radius; x++) {
        if (x * x + y * y <= r2) {
          brushOffsets.add(new Point(x, y));
        }
      }
    }
  }

  @Override
  public void mouseReleasedAction(MouseEvent e, EditorModel model) {
      model.resetDrawSet();
  }

  @Override
  public void mouseMovedAction(MouseEvent e, EditorModel model) {
    Point adjustedPoint = new Point(
            (int) ((e.getX() - model.getOrigin().x) / model.getScale()),
            (int) ((e.getY() - model.getOrigin().y) / model.getScale()));

    if (model.getDrawingArea().contains(adjustedPoint)) {
      model.setHoveredGridX((adjustedPoint.x - model.getDrawingArea().x) / 10);
      model.setHoveredGridY((adjustedPoint.y - model.getDrawingArea().y) / 10);
    } else {
      model.setHoveredGridX(-1);
      model.setHoveredGridY(-1);
    }
  }

  private double getBrushStepThreshold() {
    return Math.sqrt(radius); // Exemple : plus le pinceau est grand, plus le seuil augmente
  }

  @Override
  public void mousePressedAction(MouseEvent e, EditorModel model) {
    model.saveStateForUndo();
    Point adjustedPoint = new Point(
            (int) ((e.getX() - model.getOrigin().x) / model.getScale()),
            (int) ((e.getY() - model.getOrigin().y) / model.getScale()));
    model.setHoveredGridX(-1);
    model.setHoveredGridY(-1);
    if (model.getDrawingArea().contains(adjustedPoint)) {
      applyBrushAt(adjustedPoint, model);
    }
  }

  @Override
  public void mouseDraggedAction(MouseEvent e, EditorModel model) {
    Point adjustedPoint = new Point(
            (int) ((e.getX() - model.getOrigin().x) / model.getScale()),
            (int) ((e.getY() - model.getOrigin().y) / model.getScale()));

    if (model.getDrawingArea().contains(adjustedPoint)) {
      Point lastPoint = model.getLastDragPoint();

      /*if (lastPoint != null && lastPoint.distance(adjustedPoint) < getBrushStepThreshold()*100) {
        System.out.println("skip point");
        return; // Trop proche, on saute
      }*/

      if (lastPoint != null) {
        model.interpolatePoints(lastPoint, adjustedPoint, radius);
      } else {
        applyBrushAt(adjustedPoint, model);
      }

      model.setLastDragPoint(adjustedPoint);
    } else {
      model.setLastDragPoint(null);
    }
  }


  private void applyBrushAt(Point point, EditorModel model) {
    int squareSize = model.getGridSquareSize();
    int centerX = point.x / squareSize;
    int centerY = point.y / squareSize;

    for (Point offset : brushOffsets) {
      int gridX = centerX + offset.x;
      int gridY = centerY + offset.y;

      if (gridX >= 0 && gridY >= 0) {
        Point gridPoint = new Point(gridX * squareSize, gridY * squareSize);
        if (model.getDrawSet().add(gridPoint)) {
          try {
            model.getDrawQueue().put(gridPoint);
          } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
          }
        }
      }
    }
  }

  @Override
  public void paintHoveredArea(Graphics2D g, EditorModel model, AffineTransform at) {
    int hoverGridX = model.getHoveredGridX();
    int hoverGridY = model.getHoveredGridY();

    if (hoverGridX >= 0 && hoverGridY >= 0) {
      int squareSize = model.getGridSquareSize();

      int centerX = hoverGridX;
      int centerY = hoverGridY;

      g.setColor(Color.GRAY);

      for (Point offset : brushOffsets) {
        int gridX = centerX + offset.x;
        int gridY = centerY + offset.y;
        if (gridX >= 0 && gridY >= 0) {
          int drawX = model.getDrawingArea().x + (gridX * squareSize);
          int drawY = model.getDrawingArea().y + (gridY * squareSize);
          g.fillRect(drawX, drawY, squareSize, squareSize);
        }
      }
    }

    g.setTransform(at);
  }

  public void setRadius(int radius) {
    this.radius = radius;
    generateBrush(radius);
  }

  public ArrayList<Point> getBrushOffsets() {
    return brushOffsets;
  }

  @Override
  public int[] getSliderBounds() {
    return new int[]{0,100,20};
  }

  @Override
  public int getCurrentThickness() {
    return radius;
  }

  @Override
  public void updateCurrentThickness(int thickness) {
    this.radius = thickness;
    generateBrush(radius);
  }
}
