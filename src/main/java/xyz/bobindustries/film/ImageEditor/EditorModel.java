package xyz.bobindustries.film.ImageEditor;

import xyz.bobindustries.film.gui.Workspace;
import xyz.bobindustries.film.gui.elements.ColorBox;
import xyz.bobindustries.film.gui.panes.EditorPane;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.VolatileImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.Stack;

public class EditorModel {
    private Rectangle selectionToMove = new Rectangle();
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
    private HashMap<Point, Color> previousPoints = new HashMap<>();
    private BufferedImage draggedImage;

    private Point lastDragPoint = null;
    private final BlockingQueue<Point> drawQueue = new LinkedBlockingQueue<>();
    private final HashSet<Point> drawSet = new HashSet<>();
    private final ExecutorService drawExecutor = Executors.newSingleThreadExecutor();
    private final Stack<Color[][]> undoStack = new Stack<>();
    private final Stack<Color[][]> redoStack = new Stack<>();

    public EditorModel(EditorPane parent, Color[][] gridColors, int gridWidth, int gridHeight) {
        this.parent = parent;
        this.gridColors = gridColors;
        this.gridWidth = gridWidth;
        this.gridHeight = gridHeight;

            drawingArea = new Rectangle(50, 50, gridSquareSize * gridWidth, gridSquareSize * gridHeight);

        createVolatileImage();
    }

    public BufferedImage getSubimageFromVolatile(Rectangle region) {
        BufferedImage bImage = gridImage.getSnapshot(); // Crée une copie compatible
        return bImage.getSubimage(region.x, region.y, region.width, region.height);
    }

    public BufferedImage getDraggedImage() {
        return draggedImage;
    }


    public void clearSpaceDraggedImage() {
        Rectangle selection = this.getSelectionToMove();
        int startGridX = (selection.x - drawingArea.x) / gridSquareSize;
        int startGridY = (selection.y - drawingArea.y) / gridSquareSize;
        int endGridX = (selection.x + selection.width - drawingArea.x) / gridSquareSize;
        int endGridY = (selection.y + selection.height - drawingArea.y) / gridSquareSize;

        for (int y = startGridY; y <= endGridY; y++) {
            for (int x = startGridX; x <= endGridX; x++) {
                if (x >= 0 && x < gridWidth && y >= 0 && y < gridHeight) {
                    gridColors[y][x] = Color.WHITE;
                    // Redessine la case dans l'image
                    int px = x * gridSquareSize;
                    int py = y * gridSquareSize;
                    Graphics2D g2d = gridImage.createGraphics();
                    g2d.setColor(Color.WHITE);
                    g2d.fillRect(px, py, gridSquareSize, gridSquareSize);
                    g2d.dispose();
                }
            }
        }
        parent.repaint();
    }

    public void setDraggedImage() {
        draggedImage = getSubimageFromVolatile(selectionToMove);
        clearSpaceDraggedImage();
    }

    public VolatileImage getGridImage() {
        return gridImage;
    }

    public Color[][] getGridColors() {
        return gridColors;
    }

    public Map<Point, Color> getSelectedGridColors() {
        HashMap<Point, Color> selected = new HashMap<>();

        Rectangle selection = this.getSelectionToMove();
        int startGridX = (selection.x - drawingArea.x) / gridSquareSize;
        int startGridY = (selection.y - drawingArea.y) / gridSquareSize;
        int endGridX = (selection.x + selection.width - drawingArea.x) / gridSquareSize;
        int endGridY = (selection.y + selection.height - drawingArea.y) / gridSquareSize;

        for (int y = startGridY; y <= endGridY; y++) {
            for (int x = startGridX; x <= endGridX; x++) {
                if (x >= 0 && x < gridWidth && y >= 0 && y < gridHeight) {
                    Color c = gridColors[y][x];
                    selected.put(new Point(x, y), c);
                }
            }
        }

        return selected;
    }

    public void addRectangleToGrid(Rectangle rect, Color color) {
        saveStateForUndo();
        if (color == null) {
            // Par défaut, prendre la couleur sélectionnée dans le ColorBox
            color = ((ColorBox) Workspace.getInstance().getEditorColors().getContentPane()).getSelectedColor();
        }

        int startGridX = (rect.x - drawingArea.x) / gridSquareSize;
        int startGridY = (rect.y - drawingArea.y) / gridSquareSize;
        int endGridX = (rect.x + rect.width - drawingArea.x) / gridSquareSize;
        int endGridY = (rect.y + rect.height - drawingArea.y) / gridSquareSize;

        for (int y = startGridY; y <= endGridY; y++) {
            for (int x = startGridX; x <= endGridX; x++) {
                if (x >= 0 && x < gridWidth && y >= 0 && y < gridHeight) {
                    gridColors[y][x] = color;
                    // Redessine la case dans l'image
                    int px = x * gridSquareSize;
                    int py = y * gridSquareSize;
                    Graphics2D g2d = gridImage.createGraphics();
                    g2d.setColor(color);
                    g2d.fillRect(px, py, gridSquareSize, gridSquareSize);
                    g2d.dispose();
                }
            }
        }

        parent.repaint();
    }

    public Rectangle getSelectionToMove() {
        return selectionToMove;
    }

    public void setSelectionToMove(Rectangle selectionToMove) {
        this.selectionToMove = selectionToMove;
    }

    public EditorPane getParent() {
        return parent;
    }

    public HashMap<Point, Color> getPreviousPoints() {
        return previousPoints;
    }

    public int getGridHeight() {
        return gridHeight;
    }

    public int getGridWidth() {
        return gridWidth;
    }

    public Color[][] getGridColorsCopy() {
        int rows = gridColors.length;
        int cols = gridColors[0].length;
        System.out.println(gridColors.length);
        System.out.println(gridColors[0].length);
        Color[][] copy = new Color[rows][cols];

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (gridColors[i][j] != null) {
                    copy[i][j] = gridColors[i][j]; // Color est immutable, donc pas besoin de cloner
                } else {
                    copy[i][j] = Color.WHITE;
                }
                //System.out.println(copy[i][j].toString());
                //System.out.println("done");
            }
        }

        return copy;
    }

    public void resetDrawSet() {
        drawSet.clear();
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
        if (gridY < gridHeight && gridX < gridWidth && gridX >= 0 && gridY >= 0) {
            g2d.setColor(gridColors[gridY][gridX]);
            g2d.fillRect(x, y, gridSquareSize, gridSquareSize);
        }
        g2d.dispose();
    }

    public void colorGridSquare(Point p, Color color) {
        int gridSize = gridSquareSize;
        int gridX = (p.x - drawingArea.x) / gridSize;
        int gridY = (p.y - drawingArea.y) / gridSize;

        if (gridX >= 0 && gridX < gridWidth && gridY >= 0 && gridY < gridHeight) {
            if (color!=null) {
                gridColors[gridY][gridX] = color;
            } else {
                Color currentColor = ((ColorBox) Workspace.getInstance().getEditorColors().getContentPane()).getSelectedColor();
                gridColors[gridY][gridX] = currentColor;
            }
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

    public void interpolatePoints(Point p1, Point p2, int radius) {
        int dx = p2.x - p1.x;
        int dy = p2.y - p1.y;

        int baseSteps = Math.max(Math.abs(dx), Math.abs(dy));
        int stepDivisor = Math.max(1, radius * 5);
        int steps = Math.max(1, baseSteps / stepDivisor);

        int squareSize = getGridSquareSize();

        int lastGridX = -1;
        int lastGridY = -1;

        for (int i = 1; i <= steps; i++) {
            int x = p1.x + i * dx / steps;
            int y = p1.y + i * dy / steps;

            int centerX = x / squareSize;
            int centerY = y / squareSize;

            if (centerX == lastGridX && centerY == lastGridY)
                continue;

            lastGridX = centerX;
            lastGridY = centerY;

            fillCircle(centerX, centerY, radius);
        }
    }

    private void fillCircle(int centerX, int centerY, int radiusInGridUnits) {
        for (int dy = -radiusInGridUnits; dy <= radiusInGridUnits; dy++) {
            for (int dx = -radiusInGridUnits; dx <= radiusInGridUnits; dx++) {
                if (dx * dx + dy * dy <= radiusInGridUnits * radiusInGridUnits) {
                    int gridX = centerX + dx;
                    int gridY = centerY + dy;

                    if (gridX >= 0 && gridY >= 0) {
                        Point gridPoint = new Point(drawingArea.x + gridX * gridSquareSize, drawingArea.y + gridY * gridSquareSize);
                        if (drawSet.add(gridPoint)) {
                            try {
                                drawQueue.put(gridPoint);
                            } catch (InterruptedException ex) {
                                Thread.currentThread().interrupt();
                            }
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
    
    /*public void mergeDraggedImageToGrid() {
        if (draggedImage == null) return;

        // dropPoint : position où l'utilisateur veut déposer l'image (en pixels affichés)
        int realX = (int) ((selectionToMove.x - origin.x) / scale);
        int realY = (int) ((selectionToMove.y - origin.y) / scale);

        int gridStartX = (realX - drawingArea.x) / gridSquareSize;
        int gridStartY = (realY - drawingArea.y) / gridSquareSize;

        int gridCountX = selectionToMove.width / gridSquareSize;
        int gridCountY = selectionToMove.height / gridSquareSize;

        // Sécurité si la sélection n'est pas alignée sur la grille
        if (selectionToMove.width % gridSquareSize != 0) gridCountX++;
        if (selectionToMove.height % gridSquareSize != 0) gridCountY++;

        for (int gridY = 0; gridY < gridCountY; gridY++) {
            for (int gridX = 0; gridX < gridCountX; gridX++) {
                int destGridX = gridStartX + gridX;
                int destGridY = gridStartY + gridY;
                Point gridPoint = new Point(gridX * gridSquareSize, gridY * gridSquareSize);
                try {
                    drawQueue.put(gridPoint);
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
                if (destGridX >= 0 && destGridX < gridWidth && destGridY >= 0 && destGridY < gridHeight) {
                    // Prendre le pixel central de la zone correspondante dans draggedImage
                    int px = gridX * gridSquareSize + gridSquareSize / 2;
                    int py = gridY * gridSquareSize + gridSquareSize / 2;
                    // Clamp pour éviter les débordements
                    px = Math.min(px, draggedImage.getWidth() - 1);
                    py = Math.min(py, draggedImage.getHeight() - 1);
                    int rgb = draggedImage.getRGB(px, py);
                    Color color = new Color(rgb, true);
                    gridColors[destGridY][destGridX] = color;
                }
            }
        }
        drawGridImage();
        parent.repaint();
    }*/

    public void mergeDraggedImageToGrid() {
        saveStateForUndo();
        if (draggedImage == null) return;

        System.out.println("height:"+selectionToMove.getHeight()+"width:"+selectionToMove.getWidth());

        System.out.println("origin:"+origin.x+","+origin.y);

        // real coordinates of the selection to move

        int gridStartX = (selectionToMove.x - drawingArea.x) / gridSquareSize;
        int gridStartY = (selectionToMove.y - drawingArea.y) / gridSquareSize;

        System.out.println("gridStartXY");
        System.out.println(gridStartX + " " + gridStartY);

        // width and height of the selection to move

        int gridCountX = selectionToMove.width / gridSquareSize;
        int gridCountY = selectionToMove.height / gridSquareSize;

        System.out.println("gridCountXY");
        System.out.println(gridCountX + " " + gridCountY);

        for (int i = gridStartX; i < gridStartX + gridCountX; i++) {
            for (int j = gridStartY; j < gridStartY + gridCountY; j++) {
                int relX = (i - gridStartX)*10;
                int relY = (j - gridStartY)*10;
                int rgb = draggedImage.getRGB(relX, relY);
                int red = (rgb >> 16) & 0xFF;
                int green = (rgb >> 8) & 0xFF;
                int blue = rgb & 0xFF;
                Color currentColor = new Color(red, green, blue);
                //System.out.println("colors: " + red + " " + green + " " + blue);
                /*if (i >= 0 && i < gridWidth && j >= 0 && j < gridHeight) {
                    gridColors[i][j] = currentColor;
                }*/
                Point gridPoint = new Point(drawingArea.y + i * gridSquareSize, drawingArea.x + j * gridSquareSize);
                colorGridSquare(gridPoint, currentColor);
                updateImage(gridPoint);
            }
        }
    }

    public void reDrawGrid(Color[][] newGridColors) {
        for (int i = 0; i < gridColors.length; i++) {
            for (int j = 0; j < gridColors[i].length; j++) {
                gridColors[i][j] = newGridColors[i][j];
                Point gridPoint = new Point(drawingArea.x + j * gridSquareSize, drawingArea.y + i * gridSquareSize);
                colorGridSquare(gridPoint, gridColors[i][j]);
                updateImage(gridPoint);
            }
        }
    }

    public void saveStateForUndo() {
        // On fait une copie profonde de gridColors
        Color[][] copy = getGridColorsCopy();
        undoStack.push(copy);
        // Quand on fait une nouvelle action, on vide la pile redo
        redoStack.clear();
        System.out.println("undo saved");
    }

    public void undo() {
        if (!undoStack.isEmpty()) {
            Color[][] previous = undoStack.pop();
            redoStack.push(getGridColorsCopy());
            reDrawGrid(previous);
            parent.repaint();
        }
    }

    public void redo() {
        if (!redoStack.isEmpty()) {
            Color[][] next = redoStack.pop();
            undoStack.push(getGridColorsCopy());
            reDrawGrid(next);
            parent.repaint();
        }
    }
}
