package xyz.bobindustries.film.ImageEditor;

import xyz.bobindustries.film.gui.Workspace;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.VolatileImage;
import java.util.HashMap;
import java.util.Map;

public class MoveSelection implements Tools {
    private Point dragStartPoint = null;
    private Rectangle originalSelectionBounds = null;
    private boolean isDragging = false;
    private BufferedImage draggedImage = null;
    private Point currentOffset = new Point(0, 0);
    private boolean hasErasedOriginal = false;

    @Override
    public void mousePressedAction(MouseEvent e, EditorModel model) {
        Point adjusted = getAdjustedPoint(e, model);
        Rectangle selection = model.getSelectionToMove();

        if (selection.contains(adjusted)) {
            isDragging = true;
            dragStartPoint = adjusted;
            originalSelectionBounds = new Rectangle(selection);
            draggedImage = model.getSubimageFromVolatile(selection);
            hasErasedOriginal = false; // Réinitialise ici
        } else {
            model.addRectangleToGrid(model.getSelectionToMove(), Workspace.getInstance().getSelectedColor());
            originalSelectionBounds = null;
            isDragging = false;
            draggedImage = null;
            model.setSelectionToMove(null);
        }
    }

    @Override
    public void mouseDraggedAction(MouseEvent e, EditorModel model) {
        if (!isDragging || dragStartPoint == null) return;

        Point currentPoint = getAdjustedPoint(e, model);

        int dx = currentPoint.x - dragStartPoint.x;
        int dy = currentPoint.y - dragStartPoint.y;

        int gridSize = model.getGridSquareSize();
        Point snappedOffset = snapToGrid(dx, dy, gridSize);
        currentOffset = snappedOffset;

        Rectangle moved = new Rectangle(
                originalSelectionBounds.x + snappedOffset.x,
                originalSelectionBounds.y + snappedOffset.y,
                originalSelectionBounds.width,
                originalSelectionBounds.height
        );
        model.setSelectionToMove(moved);

        // Efface la zone d'origine une seule fois
        if (!hasErasedOriginal) {
            Graphics2D g2d = model.getGridImage().createGraphics();
            g2d.setComposite(AlphaComposite.Clear);
            g2d.fillRect(originalSelectionBounds.x, originalSelectionBounds.y,
                    originalSelectionBounds.width, originalSelectionBounds.height);
            g2d.dispose();
            hasErasedOriginal = true;
        }

        Workspace.getInstance().repaint();
    }

    @Override
    public void mouseReleasedAction(MouseEvent e, EditorModel model) {
        if (!isDragging) return;

        isDragging = false;

        Point currentPoint = getAdjustedPoint(e, model);

        // Calculate offset from drag start
        int dx = currentPoint.x - dragStartPoint.x;
        int dy = currentPoint.y - dragStartPoint.y;

        int gridSize = model.getGridSquareSize(); // Assure-toi que ton modèle fournit ça
        Point snappedOffset = snapToGrid(dx, dy, gridSize);
        currentOffset = snappedOffset;

        Rectangle moved = new Rectangle(
                originalSelectionBounds.x + snappedOffset.x,
                originalSelectionBounds.y + snappedOffset.y,
                originalSelectionBounds.width,
                originalSelectionBounds.height
        );
        model.setSelectionToMove(moved);
        Graphics2D g2d = model.getGridImage().createGraphics();
        // Redessiner à la nouvelle position
        g2d.dispose();
    }

    @Override
    public void mouseMovedAction(MouseEvent e, EditorModel model) {
        // Rien ici pour le moment
    }

    @Override
    public void paintHoveredArea(Graphics2D g, EditorModel model, AffineTransform at) {
        if (model.getSelectionToMove()!=null) {
            Rectangle moved = model.getSelectionToMove();
            g.setColor(Color.RED);
            g.drawRect(moved.x, moved.y, (int)moved.getWidth(), (int)moved.getHeight());
            if (draggedImage != null && isDragging) {
                g.drawImage(draggedImage, moved.x, moved.y, null);
            }
        }
        g.setTransform(at);
    }

    private Point getAdjustedPoint(MouseEvent e, EditorModel model) {
        return new Point(
                (int) ((e.getX() - model.getOrigin().x) / model.getScale()),
                (int) ((e.getY() - model.getOrigin().y) / model.getScale())
        );
    }

    private Point snapToGrid(int dx, int dy, int gridSize) {
        int snappedDx = Math.round(dx / (float) gridSize) * gridSize;
        int snappedDy = Math.round(dy / (float) gridSize) * gridSize;
        return new Point(snappedDx, snappedDy);
    }

}