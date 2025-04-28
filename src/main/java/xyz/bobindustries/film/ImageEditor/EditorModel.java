package xyz.bobindustries.film.ImageEditor;

import xyz.bobindustries.film.gui.panes.EditorPane;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.AffineTransform;
import java.awt.image.VolatileImage;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

public class EditorModel {
    private double scale = 1.0;
    private Point origin = new Point(0, 0);
    private VolatileImage gridImage;
    private int hoveredGridX = -1;
    private int hoveredGridY = -1;
    private Rectangle drawingArea;
    private final int gridSquareSize = 10;
    private EditorPane parent;
    private int gridWidth;
    private int gridHeight;
    private Color[][] gridColors;

    private Point lastDragPoint = null;
    private final BlockingQueue<Point> drawQueue = new LinkedBlockingQueue<>();
    private final HashSet<Point> drawSet = new HashSet<>();
    private final ExecutorService drawExecutor = Executors.newSingleThreadExecutor();

    public EditorModel(EditorPane parent, Color[][] gridColors, int gridWidth, int gridHeight) {
        this.parent = parent;
        this.gridColors = gridColors;
        this.gridWidth = gridWidth;
        this.gridHeight = gridHeight;

        drawingArea = new Rectangle(50, 50, gridSquareSize * gridWidth, gridSquareSize * gridHeight);

        createVolatileImage();
    }

    public void zoomAndScroll(MouseWheelEvent e) {
        double minZoomThreshold = 0.2;
        if (e.isControlDown()) {
            double oldScale = scale;

            if (e.getWheelRotation() < 0) {
                scale *= 1.1;
            } else {
                scale /= 1.1;
            }
            System.out.println(scale);
            Point zoomCenter;
            if (scale <= minZoomThreshold) {
                int centerX = drawingArea.x + drawingArea.width / 2;
                int centerY = drawingArea.y + drawingArea.height / 2;

                int viewCenterX = parent.getWidth() / 2;
                int viewCenterY = parent.getHeight() / 2;

                origin.x = (int) (viewCenterX - centerX * scale);
                origin.y = (int) (viewCenterY - centerY * scale);
            } else {
                zoomCenter = e.getPoint();
                System.out.println(zoomCenter.x);
                System.out.println(zoomCenter.y);
                origin.x = (int) (zoomCenter.x - (zoomCenter.x - origin.x) * (scale / oldScale));
                origin.y = (int) (zoomCenter.y - (zoomCenter.y - origin.y) * (scale / oldScale));
            }
        } else {
            int scrollAmount = e.getUnitsToScroll() * 10;
            if (0.12 <= scale) {
                if (e.isShiftDown()) {
                    origin.x -= scrollAmount;
                } else {
                    origin.y -= scrollAmount;
                }
            }
        }
    }

    public AffineTransform paint(Graphics2D g2d) {
        AffineTransform at = g2d.getTransform();
        g2d.translate(origin.x, origin.y);
        g2d.scale(scale, scale);

        if (gridImage == null) {
            createVolatileImage();
        } else {
            int valid = gridImage.validate(parent.getGraphicsConfiguration());
            if (valid == VolatileImage.IMAGE_INCOMPATIBLE) {
                createVolatileImage();
            }
        }

        g2d.drawImage(gridImage, drawingArea.x, drawingArea.y, parent);

        return at;
    }

    private void createVolatileImage() {
        GraphicsConfiguration gc = parent.getGraphicsConfiguration();
        if (gc != null) {
            gridImage = gc.createCompatibleVolatileImage(drawingArea.width, drawingArea.height,
                    Transparency.TRANSLUCENT);
            drawGridImage(); // Dessine dessus dès la création
        }
    }

    private void drawGridImage() {
        if (gridImage == null) {
            createVolatileImage();
            return;
        }

        do {
            int valid = gridImage.validate(parent.getGraphicsConfiguration());
            if (valid == VolatileImage.IMAGE_INCOMPATIBLE) {
                createVolatileImage(); // Recrée l'image si plus compatible
                return;
            }

            Graphics2D g2d = gridImage.createGraphics();
            g2d.setColor(Color.WHITE);
            g2d.fillRect(0, 0, gridImage.getWidth(), gridImage.getHeight());

            for (int i = 0; i < gridHeight; i++) {
                for (int j = 0; j < gridWidth; j++) {
                    int x = j * gridSquareSize;
                    int y = i * gridSquareSize;
                    g2d.setColor(gridColors[i][j]);
                    g2d.fillRect(x, y, gridSquareSize, gridSquareSize);
                }
            }

            g2d.dispose();
        } while (gridImage.contentsLost()); // Re-dessiner si perdu
    }

    public void updateImage(Point p) {
        if (gridImage == null)
            return;
        Graphics2D g2d = gridImage.createGraphics();
        int gridX = (p.x - drawingArea.x) / gridSquareSize;
        int gridY = (p.y - drawingArea.y) / gridSquareSize;
        int x = gridX * gridSquareSize;
        int y = gridY * gridSquareSize;
        g2d.setColor(gridColors[gridY][gridX]);
        g2d.fillRect(x, y, gridSquareSize, gridSquareSize);
        g2d.dispose();
    }

    public void colorGridSquare(Point p) {
        int gridSize = gridSquareSize;
        int gridX = (p.x - drawingArea.x) / gridSize;
        int gridY = (p.y - drawingArea.y) / gridSize;

        if (gridX >= 0 && gridX < gridWidth && gridY >= 0 && gridY < gridHeight) {
            gridColors[gridY][gridX] = Color.RED;
        }
    }

    public void interpolatePoints(Point p1, Point p2) {
        int dx = p2.x - p1.x;
        int dy = p2.y - p1.y;
        int steps = Math.max(Math.abs(dx), Math.abs(dy));

        for (int i = 1; i <= steps; i++) {
            int x = p1.x + i * dx / steps;
            int y = p1.y + i * dy / steps;
            drawQueue.offer(new Point(x, y));
        }
    }

    /*public void interpolatePoints(Point p1, Point ignored, ArrayList<Point> brushOffsets) {
        int squareSize = getGridSquareSize();
        int centerX = p1.x / squareSize;
        int centerY = p1.y / squareSize;

        for (Point offset : brushOffsets) {
            int gridX = centerX + offset.x;
            int gridY = centerY + offset.y;

            if (gridX >= 0 && gridY >= 0) {
                Point gridPoint = new Point(gridX * squareSize, gridY * squareSize);
                if (drawSet.add(gridPoint)) {
                    try {
                        drawQueue.put(gridPoint);
                    } catch (InterruptedException ex) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        }
    }*/

    public void interpolatePoints(Point p1, Point p2, ArrayList<Point> brushOffsets, int radius) {
        int dx = p2.x - p1.x;
        int dy = p2.y - p1.y;

        // On augmente la "taille du pas" proportionnellement au rayon
        int baseSteps = Math.max(Math.abs(dx), Math.abs(dy));
        int stepDivisor = Math.max(1, radius*10); // ex: pinceau 5x5 → stepDivisor = 2
        int steps = Math.max(1, baseSteps / stepDivisor); // Réduit drastiquement le nombre d'étapes

        int squareSize = getGridSquareSize();

        int lastGridX = -1;
        int lastGridY = -1;

        for (int i = 1; i <= steps; i++) {
            int x = p1.x + i * dx / steps;
            int y = p1.y + i * dy / steps;

            int centerX = x / squareSize;
            int centerY = y / squareSize;

            if (centerX == lastGridX && centerY == lastGridY)
                continue; // Skip duplicate points

            lastGridX = centerX;
            lastGridY = centerY;

            for (Point offset : brushOffsets) {
                int gridX = centerX + offset.x;
                int gridY = centerY + offset.y;

                if (gridX >= 0 && gridY >= 0) {
                    Point gridPoint = new Point(gridX * squareSize, gridY * squareSize);
                    if (drawSet.add(gridPoint)) {
                        try {
                            drawQueue.put(gridPoint); // Insert point into queue
                        } catch (InterruptedException ex) {
                            Thread.currentThread().interrupt(); // Handle interruption
                        }
                    }
                }
            }
        }
    }

    public int getHoveredGridX() {
        return hoveredGridX;
    }

    public int getHoveredGridY() {
        return hoveredGridY;
    }

    public Rectangle getDrawingArea() {
        return drawingArea;
    }

    public int getGridSquareSize() {
        return gridSquareSize;
    }

    public Point getOrigin() {
        return origin;
    }

    public double getScale() {
        return scale;
    }

    public void setGridColors(Color[][] gridColors) {
        this.gridColors = gridColors;
    }

    public void setGridWidth(int gridWidth) {
        this.gridWidth = gridWidth;
    }

    public void setGridHeight(int gridHeight) {
        this.gridHeight = gridHeight;
    }

    public void setHoveredGridX(int hoveredGridX) {
        this.hoveredGridX = hoveredGridX;
    }

    public void setHoveredGridY(int hoveredGridY) {
        this.hoveredGridY = hoveredGridY;
    }

    public Point getLastDragPoint() {
        return lastDragPoint;
    }

    public void setLastDragPoint(Point lastDragPoint) {
        this.lastDragPoint = lastDragPoint;
    }

    public BlockingQueue<Point> getDrawQueue() {
        return drawQueue;
    }

    public ExecutorService getDrawExecutor() {
        return drawExecutor;
    }

    public HashSet<Point> getDrawSet() {
        return drawSet;
    }
}
