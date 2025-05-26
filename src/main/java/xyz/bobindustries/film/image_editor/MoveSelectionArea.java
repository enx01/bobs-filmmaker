package xyz.bobindustries.film.image_editor;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;

public class MoveSelectionArea implements Tools {
    private Point dragStartPoint = null;
    private Rectangle originalSelectionBounds = null;
    private boolean isDragging = false;

    @Override
    public void mousePressedAction(MouseEvent e, EditorModel model) {
        Point p = getAdjustedPoint(e, model);
        if (model.getSelectionToMove() != null && model.getSelectionToMove().contains(p)) {
            dragStartPoint = p;
            originalSelectionBounds = model.getSelectionToMove().getBounds();
            isDragging = true;
        }
    }

    @Override
    public void mouseDraggedAction(MouseEvent e, EditorModel model) {
        if (!isDragging || dragStartPoint == null || originalSelectionBounds == null) return;

        Point current = getAdjustedPoint(e, model);
        int dx = current.x - dragStartPoint.x;
        int dy = current.y - dragStartPoint.y;

        // Aligner le déplacement sur la grille
        int gridSize = model.getGridSquareSize();
        dx = (dx / gridSize) * gridSize;
        dy = (dy / gridSize) * gridSize;

        Rectangle newBounds = new Rectangle(originalSelectionBounds);
        newBounds.translate(dx, dy);

        model.getSelectionToMove().setFrame(newBounds);
    }

    @Override
    public void mouseReleasedAction(MouseEvent e, EditorModel model) {
        System.out.println(model.getSelectionToMove().x);
        System.out.println(model.getSelectionToMove().y);
        System.out.println(model.getSelectionToMove().height);
        System.out.println(model.getSelectionToMove().width);
        isDragging = false;
        dragStartPoint = null;
        originalSelectionBounds = null;
    }

    @Override
    public void mouseMovedAction(MouseEvent e, EditorModel model) {
        // Rien ici pour le moment
    }

    @Override
    public void paintHoveredArea(Graphics2D g, EditorModel model, AffineTransform at) {
        if (model.getSelectionToMove() != null) {
            g.setColor(new Color(0, 0, 255, 64));
            g.fill(model.getSelectionToMove());
            g.setColor(Color.RED);
            g.draw(model.getSelectionToMove());
        }

        g.setTransform(at);
    }

    private Point getAdjustedPoint(MouseEvent e, EditorModel model) {
        return new Point(
                (int) ((e.getX() - model.getOrigin().x) / model.getScale()),
                (int) ((e.getY() - model.getOrigin().y) / model.getScale())
        );
    }
}
