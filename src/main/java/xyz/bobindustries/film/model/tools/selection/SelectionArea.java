package xyz.bobindustries.film.model.tools.selection;

import java.awt.*;

import xyz.bobindustries.film.model.EditorModel;

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
