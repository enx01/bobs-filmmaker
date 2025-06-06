package xyz.bobindustries.film.model.tools.drawing;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;

import xyz.bobindustries.film.model.EditorModel;
import xyz.bobindustries.film.model.tools.Tools;
import xyz.bobindustries.film.model.tools.ToolsSettings;

public class Circle implements Tools, ToolsSettings {
    private Point startPoint = null;
    private Point currentDragPoint = null;
    private int thickness = 1; // Valeur par défaut pour l'épaisseur

    @Override
    public void mousePressedAction(MouseEvent e, EditorModel model) {
        Point adjustedPoint = getAdjustedPoint(e, model);
        if (model.getDrawingArea().contains(adjustedPoint)) {
            startPoint = adjustedPoint;
        } else {
            startPoint = null;
        }
    }

    @Override
    public void mouseReleasedAction(MouseEvent e, EditorModel model) {
        model.saveStateForUndo();
        if (startPoint == null)
            return;

        Point endPoint = getAdjustedPoint(e, model);
        if (!model.getDrawingArea().contains(endPoint))
            return;

        ArrayList<Point> points = getCirclePoints(startPoint, endPoint, thickness);

        for (Point point : points) {
            if (model.getDrawingArea().contains(point)) {
                model.colorGridSquare(point, null);
                model.updateImage(point);
            }
        }

        startPoint = null;
        currentDragPoint = null;
    }

    @Override
    public void mouseDraggedAction(MouseEvent e, EditorModel model) {
        if (startPoint == null)
            return;

        currentDragPoint = getAdjustedPoint(e, model);
    }

    @Override
    public void mouseMovedAction(MouseEvent e, EditorModel model) {
        Point adjustedPoint = getAdjustedPoint(e, model);

        if (model.getDrawingArea().contains(adjustedPoint)) {
            model.setHoveredGridX((adjustedPoint.x - model.getDrawingArea().x) / model.getGridSquareSize());
            model.setHoveredGridY((adjustedPoint.y - model.getDrawingArea().y) / model.getGridSquareSize());
        } else {
            model.setHoveredGridX(-1);
            model.setHoveredGridY(-1);
        }
    }

    @Override
    public void paintHoveredArea(Graphics2D g, EditorModel model, AffineTransform at) {
        if (currentDragPoint != null && startPoint != null) {

            ArrayList<Point> points = getCirclePoints(startPoint, currentDragPoint, thickness);

            g.setColor(new Color(0, 0, 255, 128)); // Bleu semi-transparent

            for (Point point : points) {
                if (model.getDrawingArea().contains(point)) {
                    Point screen = gridToScreen(point, model);
                    g.fillRect(screen.x, screen.y, model.getGridSquareSize(), model.getGridSquareSize());
                }
            }
        }

        // Rendu normal du survol
        if (model.getHoveredGridX() >= 0 && model.getHoveredGridY() >= 0) {
            int hoverX = model.getDrawingArea().x + (model.getHoveredGridX() * model.getGridSquareSize());
            int hoverY = model.getDrawingArea().y + (model.getHoveredGridY() * model.getGridSquareSize());
            g.setColor(Color.BLUE);
            g.fillRect(hoverX, hoverY, model.getGridSquareSize(), model.getGridSquareSize());
        }

        g.setTransform(at);
    }

    private Point gridToScreen(Point logical, EditorModel model) {
        int gridSize = model.getGridSquareSize();
        int gridX = (logical.x - model.getDrawingArea().x) / gridSize;
        int gridY = (logical.y - model.getDrawingArea().y) / gridSize;
        int screenX = model.getDrawingArea().x + (gridX * gridSize);
        int screenY = model.getDrawingArea().y + (gridY * gridSize);
        return new Point(screenX, screenY);
    }

    private Point getAdjustedPoint(MouseEvent e, EditorModel model) {
        return new Point(
                (int) ((e.getX() - model.getOrigin().x) / model.getScale()),
                (int) ((e.getY() - model.getOrigin().y) / model.getScale()));
    }

    private void drawFilledCircle(Graphics2D g, Point center, int radius) {
        int diameter = radius * 2;
        int topLeftX = center.x - radius;
        int topLeftY = center.y - radius;

        g.fillOval(topLeftX, topLeftY, diameter, diameter); // Remplir un cercle
    }

    private static ArrayList<Point> getCirclePoints(Point startPoint, Point endPoint, int thickness) {
        ArrayList<Point> points = new ArrayList<>();
        double dx = endPoint.x - startPoint.x;
        double dy = endPoint.y - startPoint.y;
        int radius = (int) Math.round(Math.sqrt(dx * dx + dy * dy));
        for (int y = -radius; y <= radius; y++) {
            for (int x = -radius; x <= radius; x++) {
                int distance = x * x + y * y;
                if (distance <= radius * radius && distance >= (radius - thickness) * (radius - thickness)) {
                    Point p = new Point(startPoint.x + x, startPoint.y + y);
                    points.add(p);
                }
            }
        }
        return points;
    }

    // Setter pour l'épaisseur
    public void setThickness(int thickness) {
        this.thickness = Math.max(1, thickness);
    }

    @Override
    public int[] getSliderBounds() {
        return new int[] { 1, 100, thickness };
    }

    @Override
    public int getCurrentThickness() {
        return thickness;
    }

    @Override
    public void updateCurrentThickness(int thickness) {
        this.thickness = thickness;
    }
}
