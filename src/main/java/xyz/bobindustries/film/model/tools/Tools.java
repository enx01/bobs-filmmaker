package xyz.bobindustries.film.model.tools;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;

import xyz.bobindustries.film.model.EditorModel;

public interface Tools {

    void mouseReleasedAction(MouseEvent e, EditorModel model);

    void mouseMovedAction(MouseEvent e, EditorModel model);

    void mousePressedAction(MouseEvent e, EditorModel model);

    void mouseDraggedAction(MouseEvent e, EditorModel model);

    void paintHoveredArea(Graphics2D g, EditorModel model, AffineTransform at);
}
