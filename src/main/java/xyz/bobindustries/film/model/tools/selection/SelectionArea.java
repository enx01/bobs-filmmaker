package xyz.bobindustries.film.model.tools.selection;

import java.awt.*;

public class SelectionArea extends Rectangle {
    int rectWidth;
    int rectHeight;
    int squareSize = 10;
    int x;
    int y;

    public SelectionArea(int width, int height, int x, int y) {
        rectWidth = width;
        rectHeight = height;
        this.x = x;
        this.y = y;
    }

}
