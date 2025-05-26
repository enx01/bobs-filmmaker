package xyz.bobindustries.film.image_editor;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;

public class RectangleTool implements Tools, ToolsSettings {
    private Point startPoint = null;
    private Point currentDragPoint = null;
    private int thickness = 1;

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
        if (startPoint == null) return;

        Point endPoint = getAdjustedPoint(e, model);
        if (!model.getDrawingArea().contains(endPoint)) return;

        int x1 = Math.min(startPoint.x, endPoint.x);
        int y1 = Math.min(startPoint.y, endPoint.y);
        int x2 = Math.max(startPoint.x, endPoint.x);
        int y2 = Math.max(startPoint.y, endPoint.y);

        for (int y = y1; y <= y2; y++) {
            for (int x = x1; x <= x2; x++) {
                boolean isBorder =
                    (x >= x1 && x < x1 + thickness) || (x <= x2 && x > x2 - thickness) ||
                    (y >= y1 && y < y1 + thickness) || (y <= y2 && y > y2 - thickness);
                if (isBorder) {
                    Point p = new Point(x, y);
                    if (model.getDrawingArea().contains(p)) {
                        model.colorGridSquare(p, null);
                        model.updateImage(p);
                    }
                }
            }
        }

        startPoint = null;
        currentDragPoint = null;
    }

    @Override
    public void mouseDraggedAction(MouseEvent e, EditorModel model) {
        if (startPoint == null) return;
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
            int x1 = Math.min(startPoint.x, currentDragPoint.x);
            int y1 = Math.min(startPoint.y, currentDragPoint.y);
            int x2 = Math.max(startPoint.x, currentDragPoint.x);
            int y2 = Math.max(startPoint.y, currentDragPoint.y);

            g.setColor(new Color(0, 0, 255, 128)); // Bleu semi-transparent

            for (int y = y1; y <= y2; y++) {
                for (int x = x1; x <= x2; x++) {
                    boolean isBorder =
                        (x >= x1 && x < x1 + thickness) || (x <= x2 && x > x2 - thickness) ||
                        (y >= y1 && y < y1 + thickness) || (y <= y2 && y > y2 - thickness);
                    if (isBorder) {
                        Point p = new Point(x, y);
                        if (model.getDrawingArea().contains(p)) {
                            Point screen = gridToScreen(p, model);
                            g.fillRect(screen.x, screen.y, model.getGridSquareSize(), model.getGridSquareSize());
                        }
                    }
                }
            }
        }

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
                (int) ((e.getY() - model.getOrigin().y) / model.getScale())
        );
    }

    @Override
    public int[] getSliderBounds() {
        return new int[]{1, 20, thickness};
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