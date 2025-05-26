package xyz.bobindustries.film.image_editor;

import xyz.bobindustries.film.gui.Workspace;
import xyz.bobindustries.film.gui.elements.ColorBox;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;

public class ColorPickerTool implements Tools {
    @Override
    public void mouseReleasedAction(MouseEvent e, EditorModel model) {

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

    @Override
    public void mousePressedAction(MouseEvent e, EditorModel model) {
        model.saveStateForUndo();
        Point adjustedPoint = new Point(
                (int) ((e.getX() - model.getOrigin().x) / model.getScale()),
                (int) ((e.getY() - model.getOrigin().y) / model.getScale()));
        model.setHoveredGridX(-1);
        model.setHoveredGridY(-1);
        if (model.getDrawingArea().contains(adjustedPoint)) {
            System.out.println(adjustedPoint.x + " " + adjustedPoint.y);
            Color pickedColor = model.getGridColor(adjustedPoint);
            ColorBox colorBoxPanel = (ColorBox) Workspace.getInstance().getEditorColors().getContentPane();
            colorBoxPanel.setSelectedColor(pickedColor);
        }
    }

    @Override
    public void mouseDraggedAction(MouseEvent e, EditorModel model) {

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
