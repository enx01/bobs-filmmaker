package xyz.bobindustries.film.image_editor;

import java.awt.*;

public class SelectionArea extends Rectangle {
    int rectWidth;
    int rectHeight;
    int squareSize = 10;
    private EditorModel model;
    int x;
    int y;

    public SelectionArea(int width, int height, EditorModel model, int x, int y) {
        rectWidth = width;
        rectHeight = height;
        this.model = model;
        this.x = x;
        this.y = y;
    }

}