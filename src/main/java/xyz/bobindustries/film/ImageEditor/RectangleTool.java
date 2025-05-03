package xyz.bobindustries.film.ImageEditor;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;

public class RectangleTool implements Tools {

    Point rectangleOrigin = new Point(-1, -1);
    ArrayList<Point> previousRectangle = new ArrayList<>();
    Color[][] drawingSnapshot;

    @Override
    public void mouseReleasedAction(MouseEvent e, EditorModel model) {
        Point adjustedPoint = new Point(
                (int) ((e.getX() - model.getOrigin().x) / model.getScale()),
                (int) ((e.getY() - model.getOrigin().y) / model.getScale()));
        previousRectangle.clear();
    }

    @Override
    public void mouseMovedAction(MouseEvent e, EditorModel model) {
        // Optional: implement hover behavior if needed
    }

    @Override
    public void mousePressedAction(MouseEvent e, EditorModel model) {
        Point adjustedPoint = new Point(
                (int) ((e.getX() - model.getOrigin().x) / model.getScale()),
                (int) ((e.getY() - model.getOrigin().y) / model.getScale()));
        rectangleOrigin = adjustedPoint;
        drawingSnapshot = model.getGridColorsCopy();
        System.out.println("snapshot set");
    }

    @Override
    public void mouseDraggedAction(MouseEvent e, EditorModel model) {
        Point adjustedPoint = new Point(
                (int) ((e.getX() - model.getOrigin().x) / model.getScale()),
                (int) ((e.getY() - model.getOrigin().y) / model.getScale()));

        ArrayList<Point> points = getRectanglePoints(rectangleOrigin, adjustedPoint);
        for (Point p : previousRectangle) {
            if (p.x >= 0 && p.x < drawingSnapshot[0].length && p.y >= 0 && p.y < drawingSnapshot.length) {
                System.out.println(drawingSnapshot[p.y][p.x].toString());
                model.colorGridSquare(p, drawingSnapshot[p.y][p.x]); // Restaurer la couleur d'origine
                model.updateImage(p);
            }
        }
        for (Point p : points) {
            model.colorGridSquare(p, null); // Draw new
            model.updateImage(p);
        }
        previousRectangle = points;
    }

    public ArrayList<Point> getRectanglePoints(Point p1, Point p2) {
        ArrayList<Point> points = new ArrayList<>();

        int x1 = Math.min(p1.x, p2.x);
        int y1 = Math.min(p1.y, p2.y);
        int x2 = Math.max(p1.x, p2.x);
        int y2 = Math.max(p1.y, p2.y);

        for (int x = x1; x <= x2; x++) {
            points.add(new Point(x, y1)); // top edge
            points.add(new Point(x, y2)); // bottom edge
        }
        for (int y = y1 + 1; y < y2; y++) {
            points.add(new Point(x1, y)); // left edge
            points.add(new Point(x2, y)); // right edge
        }

        return points;
    }

    @Override
    public void paintHoveredArea(Graphics2D g, EditorModel model, AffineTransform at) {
        if (model.getHoveredGridX() >= 0 && model.getHoveredGridY() >= 0) {
            int hoverX = model.getDrawingArea().x + (model.getHoveredGridX() * model.getGridSquareSize());
            int hoverY = model.getDrawingArea().y + (model.getHoveredGridY() * model.getGridSquareSize());
            g.setColor(Color.GRAY);
            g.fillRect(hoverX, hoverY, model.getGridSquareSize(), model.getGridSquareSize());
        }
        g.setTransform(at);
    }
}
