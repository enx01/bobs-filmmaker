package xyz.bobindustries.film.ImageEditor;

import javax.tools.Tool;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;

public class SelectionTool implements Tools {

    private Point startPoint = null;
    private Point currentDragPoint = null;
    private ResizeHandle activeHandle = null;
    private static final int HANDLE_SIZE = 100;

    private enum ResizeHandle {
        NONE, N, S, E, W, NE, NW, SE, SW
    }

    @Override
    public void mousePressedAction(MouseEvent e, EditorModel model) {
        Point adjustedPoint = getAdjustedPoint(e, model);

        if (model.getSelectionToMove() != null && model.getSelectionToMove().contains(adjustedPoint)) {
            activeHandle = getHandleAtPoint(adjustedPoint, model);
            return;
        }

        if (model.getDrawingArea().contains(adjustedPoint)) {
            startPoint = adjustedPoint;
            currentDragPoint = null;
            model.setSelectionToMove(null);
            activeHandle = null;
        }
    }

    @Override
    public void mouseReleasedAction(MouseEvent e, EditorModel model) {
        if (activeHandle != ResizeHandle.NONE && model.getSelectionToMove() != null) {
            activeHandle = ResizeHandle.NONE;
            return;
        }

        if (startPoint == null) return;

        Point endPoint = getAdjustedPoint(e, model);
        if (!model.getDrawingArea().contains(endPoint)) return;

        model.setSelectionToMove(createGridAlignedRect(startPoint, currentDragPoint, model)); // ALIGN TO GRID
        startPoint = null;
        currentDragPoint = null;
    }

    @Override
    public void mouseDraggedAction(MouseEvent e, EditorModel model) {
        Point adjusted = getAdjustedPoint(e, model);

        if (model.getSelectionToMove() != null && activeHandle != null && activeHandle != ResizeHandle.NONE) {
            resizeSelection(adjusted, model);  // ALIGN TO GRID
            return;
        }

        if (startPoint != null) {
            currentDragPoint = adjusted;
            // Mettre à jour la zone de sélection pendant le drag
            model.setSelectionToMove(createGridAlignedRect(startPoint, currentDragPoint, model)); // ALIGN TO GRID
        }
    }

    @Override
    public void mouseMovedAction(MouseEvent e, EditorModel model) {
        Point adjustedPoint = getAdjustedPoint(e, model);

        if (model.getSelectionToMove() != null) {
            ResizeHandle hoverHandle = getHandleAtPoint(adjustedPoint, model);  // Vérifie si la souris est sur une poignée

            // Change le curseur selon la poignée
            Cursor cursor = switch (hoverHandle) {
                case N -> Cursor.getPredefinedCursor(Cursor.N_RESIZE_CURSOR);
                case S -> Cursor.getPredefinedCursor(Cursor.S_RESIZE_CURSOR);
                case E -> Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR);
                case W -> Cursor.getPredefinedCursor(Cursor.W_RESIZE_CURSOR);
                case NE -> Cursor.getPredefinedCursor(Cursor.NE_RESIZE_CURSOR);
                case NW -> Cursor.getPredefinedCursor(Cursor.NW_RESIZE_CURSOR);
                case SE -> Cursor.getPredefinedCursor(Cursor.SE_RESIZE_CURSOR);
                case SW -> Cursor.getPredefinedCursor(Cursor.SW_RESIZE_CURSOR);
                default -> Cursor.getDefaultCursor();  // Par défaut, curseur normal
            };

            model.getParent().setCursor(cursor);  // Applique le curseur à la zone de dessin
        } else {
            model.getParent().setCursor(Cursor.getDefaultCursor());
        }
    }

    @Override
    public void paintHoveredArea(Graphics2D g, EditorModel model, AffineTransform at) {
        // Ne pas remplir le rectangle bleu de prévisualisation
        //g.setColor(new Color(0, 0, 255, 128)); // Cette ligne est supprimée pour ne pas remplir le rectangle.

        if (model.getSelectionToMove() != null) {
            // Remplissage bleu semi-transparent constant
            Color semiTransparent = new Color(0, 0, 255, 30);
            g.setColor(semiTransparent);
            g.fill(model.getSelectionToMove());

            // Dessiner les carrés noirs à l’intérieur
            for (int y = model.getSelectionToMove().y; y < model.getSelectionToMove().y + model.getSelectionToMove().height; y += model.getGridSquareSize()) {
                for (int x = model.getSelectionToMove().x; x < model.getSelectionToMove().x + model.getSelectionToMove().width; x += model.getGridSquareSize()) {
                    boolean isInside = (x > model.getSelectionToMove().x && x + model.getGridSquareSize() < model.getSelectionToMove().x + model.getSelectionToMove().width &&
                            y > model.getSelectionToMove().y && y + model.getGridSquareSize() < model.getSelectionToMove().y + model.getSelectionToMove().height);
                    if (isInside) {
                        Point screen = gridToScreen(new Point(x, y), model);
                        g.fillRect(screen.x, screen.y, model.getGridSquareSize(), model.getGridSquareSize());
                    }
                }
            }

            // Contour rouge
            g.setColor(Color.RED);
            g.draw(model.getSelectionToMove());

            // Poignées
            drawHandles(g, model.getSelectionToMove());
        }


        if (activeHandle != null && activeHandle != ResizeHandle.NONE && model.getSelectionToMove() != null) {
            Color semiTransparent = new Color(0, 0, 255, 64);
            g.setColor(semiTransparent);
            g.fill(model.getSelectionToMove()); // Affiche la sélection en bleu transparent pendant redimensionnement
        }

        // Dessiner le rectangle de sélection (sans le remplir, juste les bordures)
        if (model.getSelectionToMove() != null) {
            g.setColor(Color.RED);
            g.draw(model.getSelectionToMove());  // Bordure rouge de la zone de sélection
            drawHandles(g, model.getSelectionToMove());  // Dessiner les poignées de redimensionnement
        }

        g.setTransform(at);
    }

    private void resizeSelection(Point p, EditorModel model) {
        if (model.getSelectionToMove() == null) return;

        Rectangle r = model.getSelectionToMove();
        int x1 = r.x;
        int y1 = r.y;
        int x2 = r.x + r.width;
        int y2 = r.y + r.height;

        int grid = model.getGridSquareSize();
        p.x = (p.x / grid) * grid;
        p.y = (p.y / grid) * grid;

        switch (activeHandle) {
            case N -> y1 = p.y;
            case S -> y2 = p.y;
            case W -> x1 = p.x;
            case E -> x2 = p.x;
            case NW -> { x1 = p.x; y1 = p.y; }
            case NE -> { x2 = p.x; y1 = p.y; }
            case SW -> { x1 = p.x; y2 = p.y; }
            case SE -> { x2 = p.x; y2 = p.y; }
        }

        model.setSelectionToMove(createGridAlignedRect(new Point(x1, y1), new Point(x2, y2), model)); // ALIGN TO GRID
    }

    private Rectangle createGridAlignedRect(Point p1, Point p2, EditorModel model) {
        int grid = model.getGridSquareSize();
        int x1 = Math.min(p1.x, p2.x);
        int y1 = Math.min(p1.y, p2.y);
        int x2 = Math.max(p1.x, p2.x);
        int y2 = Math.max(p1.y, p2.y);

        x1 = (x1 / grid) * grid;
        y1 = (y1 / grid) * grid;
        x2 = ((x2 + grid - 1) / grid) * grid; // arrondi vers le haut
        y2 = ((y2 + grid - 1) / grid) * grid;

        return new Rectangle(x1, y1, x2 - x1, y2 - y1);
    }

    private void drawHandles(Graphics2D g, Rectangle r) {
        g.setColor(Color.BLACK);
        for (Point handle : getHandlePoints(r)) {
            g.fillRect(handle.x - HANDLE_SIZE / 2, handle.y - HANDLE_SIZE / 2, HANDLE_SIZE, HANDLE_SIZE);
        }
    }

    private Point[] getHandlePoints(Rectangle r) {
        return new Point[]{
                new Point(r.x, r.y),
                new Point(r.x + r.width / 2, r.y),
                new Point(r.x + r.width, r.y),
                new Point(r.x + r.width, r.y + r.height / 2),
                new Point(r.x + r.width, r.y + r.height),
                new Point(r.x + r.width / 2, r.y + r.height),
                new Point(r.x, r.y + r.height),
                new Point(r.x, r.y + r.height / 2)
        };
    }

    private ResizeHandle getHandleAtPoint(Point p, EditorModel model) {
        int tolerance = 100; // taille de la zone de détection
        Point[] handles = getHandlePoints(model.getSelectionToMove());
        ResizeHandle[] types = {
                ResizeHandle.NW, ResizeHandle.N, ResizeHandle.NE, ResizeHandle.E,
                ResizeHandle.SE, ResizeHandle.S, ResizeHandle.SW, ResizeHandle.W
        };

        for (int i = 0; i < handles.length; i++) {
            Rectangle area = new Rectangle(
                    handles[i].x - tolerance / 2,
                    handles[i].y - tolerance / 2,
                    tolerance, tolerance
            );
            if (area.contains(p)) return types[i];
        }

        return ResizeHandle.NONE;
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

}

