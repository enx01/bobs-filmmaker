package xyz.bobindustries.film.gui.panes;

import javax.swing.*;
import java.awt.*;
import java.awt.event.HierarchyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class EditorPane extends JPanel {
    private double scale = 1.0;
    private Point origin = new Point(0, 0);
    private Color[][] gridColors;
    private int hoveredGridX = -1;
    private int hoveredGridY = -1;
    private final int gridSquareSize = 10;
    private int gridWidth;
    private int gridHeight;
    private final Rectangle blueSquare;
    private BufferedImage gridImage;
    private JSlider scaleSlider;
    private Point lastDragPoint = null;

    // Multi-threading stuff
    private final ExecutorService drawingExecutor;
    private final BlockingQueue<Point> pointsToDrawQueue;
    private volatile boolean drawingTaskScheduled = false;

    public void setScale(double scale) {
        this.scale = scale;
    }

    public void setOrigin(Point origin) {
        this.origin = origin;
    }

    public void setGridColors(Color[][] gridColors) {
        this.gridColors = gridColors;
    }

    public void setHoveredGridX(int hoveredGridX) {
        this.hoveredGridX = hoveredGridX;
    }

    public void setHoveredGridY(int hoveredGridY) {
        this.hoveredGridY = hoveredGridY;
    }

    public EditorPane(Color[][] gridColors, int gridWidth, int gridHeight) {

        this.gridColors = gridColors;
        this.gridWidth = gridWidth;
        this.gridHeight = gridHeight;

        blueSquare = new Rectangle(50, 50, gridSquareSize * gridWidth, gridSquareSize * gridHeight);

        gridImage = new BufferedImage(blueSquare.width, blueSquare.height, BufferedImage.TYPE_INT_ARGB);
        drawGridImage();

        drawingExecutor = Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r, "xyz.bobindustries.film.gui.panes.EditorPane-DrawingThread");
            t.setDaemon(true);
            return t;
        });
        pointsToDrawQueue = new LinkedBlockingQueue<>();

        setLayout(new BorderLayout());

        // Create the slider
        setupSlider();
        setupMouseHandling();

        /* Cleanup hook */
        addHierarchyListener(e -> {
            if ((e.getChangeFlags() & HierarchyEvent.PARENT_CHANGED) != 0 && !isDisplayable()) {
                shutdownExecutor();
            }
        });

        /* Good programming right there : executor shutdown hook for app exit. */
        Runtime.getRuntime().addShutdownHook(new Thread(this::shutdownExecutor));
    }

    private void setupSlider() {
        scaleSlider = new JSlider(JSlider.HORIZONTAL, 1, 200, 100); // Scale from 1% to 200%
        scaleSlider.setMajorTickSpacing(50);
        scaleSlider.setMinorTickSpacing(10);
        scaleSlider.setPaintTicks(true);
        scaleSlider.setPaintLabels(true);
        scaleSlider.addChangeListener(e -> {
            scale = scaleSlider.getValue() / 100.0; // Update scale based on slider value
            repaint();
        });

        // Add the slider to the bottom right
        JPanel sliderPanel = new JPanel();
        sliderPanel.setLayout(new BorderLayout());
        sliderPanel.add(scaleSlider, BorderLayout.EAST);
        add(sliderPanel, BorderLayout.SOUTH);
    }

    private void setupMouseHandling() {
        MouseAdapter mouseHandler = new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                handleMouseHover(e.getPoint());
            }

            @Override
            public void mousePressed(MouseEvent e) {
                Point adjustedPoint = getAdjustedPoint(e.getPoint());
                lastDragPoint = adjustedPoint;
                hoveredGridX = -1;
                hoveredGridY = -1;
                if (blueSquare.contains(adjustedPoint)) {
                    // colorGridSquare(adjustedPoint);
                    //// updateImage(adjustedPoint);
                    // drawGridImage();

                    // repaint();

                    colorGridSquareAt(adjustedPoint);
                }
                repaint();
            }

            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                if (e.isControlDown()) {
                    // Point mouse = e.getPoint();
                    // double oldScale = scale;
                    // if (e.getWheelRotation() < 0) {
                    // scale *= 1.1;
                    // } else {
                    // scale /= 1.1;
                    // }
                    // origin.x = (int) (mouse.x - (mouse.x - origin.x) * (scale / oldScale));
                    // origin.y = (int) (mouse.y - (mouse.y - origin.y) * (scale / oldScale));
                    // repaint();

                    handleZoom(e);
                } else {
                    getParent().dispatchEvent(e);
                }
            }

            @Override
            public void mouseDragged(MouseEvent e) {

                Point adjustedPoint = getAdjustedPoint(e.getPoint());
                /*
                 * if (blueSquare.contains(adjustedPoint)) {
                 * colorGridSquare(adjustedPoint);
                 * // updateImage(adjustedPoint);
                 * drawGridImage();
                 * SwingUtilities.invokeLater(() -> repaint());
                 * }
                 */

                if (lastDragPoint != null) {
                    // int steps = Math.max(Math.abs(adjustedPoint.x - lastDragPoint.x),
                    // Math.abs(adjustedPoint.y - lastDragPoint.y));
                    // System.out.println(steps);
                    // for (int i = 0; i <= steps; i++) {
                    // int x = lastDragPoint.x + (adjustedPoint.x - lastDragPoint.x) * i / steps;
                    // int y = lastDragPoint.y + (adjustedPoint.y - lastDragPoint.y) * i / steps;
                    // Point interp = new Point(x, y);
                    // colorGridSquare(interp);
                    // }
                    // drawGridImage();
                    // repaint();
                    interpolateAndColor(lastDragPoint, adjustedPoint);
                }
                lastDragPoint = adjustedPoint;
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                lastDragPoint = null;
            }

            @Override
            public void mouseExited(MouseEvent e) {
                hoveredGridX = -1;
                hoveredGridY = -1;
                repaint();
            }
        };

        addMouseMotionListener(mouseHandler);
        addMouseListener(mouseHandler);
        addMouseWheelListener(mouseHandler);
    }

    private void handleZoom(MouseWheelEvent e) {
        Point mousePoint = e.getPoint();
        double oldScale = scale;
        double scaleFactor = (e.getWheelRotation() < 0) ? 1.1 : 1.0 / 1.1;
        double newScale = Math.max(0.01, Math.min(scale * scaleFactor, 2.0));

        if (Math.abs(newScale - oldScale) < 1e-6)
            return;

        origin.x = (int) (mousePoint.x - (mousePoint.x - origin.x) * (newScale / oldScale));
        origin.y = (int) (mousePoint.y - (mousePoint.y - origin.y) * (newScale / oldScale));

        scale = newScale;
        scaleSlider.setValue((int) (scale * 100));
        repaint();
    }

    private void handleMouseHover(Point p) {
        Point adjustedPoint = getAdjustedPoint(p);

        if (blueSquare.contains(adjustedPoint)) {
            hoveredGridX = (adjustedPoint.x - blueSquare.x) / 10;
            hoveredGridY = (adjustedPoint.y - blueSquare.y) / 10;
        } else {
            hoveredGridX = -1;
            hoveredGridY = -1;
        }
        repaint();
    }

    private Point getAdjustedPoint(Point p) {
        return new Point(
                (int) ((p.x - origin.x) / scale),
                (int) ((p.y - origin.y) / scale));
    }

    private void colorGridSquareAt(Point p) {
        if (!blueSquare.contains(p))
            return;

        int gridX = (p.x - blueSquare.x) / gridSquareSize;
        int gridY = (p.y - blueSquare.y) / gridSquareSize;

        if (gridX >= 0 && gridX < gridWidth && gridY >= 0 && gridY < gridHeight) {
            pointsToDrawQueue.offer(new Point(gridX, gridY));
            scheduleDrawingUpdate();
        }
    }

    private void scheduleDrawingUpdate() {
        if (!drawingTaskScheduled) {
            drawingTaskScheduled = true;
            drawingExecutor.submit(this::processDrawingQueue);
        }
    }

    /**
     * Core drawing task executed by the background thread.
     */
    private void processDrawingQueue() {
        List<Point> pointsToProcess = new ArrayList<>();

        pointsToDrawQueue.drainTo(pointsToProcess);

        if (pointsToProcess.isEmpty()) {
            drawingTaskScheduled = false;
            return;
        }

        boolean imageUpdated = false;
        Graphics2D g2d = null;
        try {
            g2d = gridImage.createGraphics();

            for (Point gridPoint : pointsToProcess) {
                int gridX = gridPoint.x;
                int gridY = gridPoint.y;

                if (gridX >= 0 && gridX < gridWidth && gridY >= 0 && gridY < gridHeight) {
                    if (gridColors[gridY][gridX] != Color.RED) {
                        gridColors[gridY][gridX] = Color.RED;

                        int pixelX = gridX * gridSquareSize;
                        int pixelY = gridY * gridSquareSize;

                        g2d.setColor(Color.RED);
                        g2d.fillRect(pixelX, pixelY, gridSquareSize, gridSquareSize);
                        imageUpdated = true;
                    }
                }
            }
        } finally {
            if (g2d != null) {
                g2d.dispose();
            }
        }

        drawingTaskScheduled = false;

        if (imageUpdated) {
            SwingUtilities.invokeLater(this::repaint);
        }

        if (!pointsToDrawQueue.isEmpty()) {
            scheduleDrawingUpdate();
        }
    }

    private void interpolateAndColor(Point p1, Point p2) {
        int x1 = p1.x,
                y1 = p1.y,
                x2 = p2.x,
                y2 = p2.y;

        int dx = Math.abs(x2 - x1);
        int dy = Math.abs(y2 - y1);

        int sx = (x1 < x2) ? 1 : -1;
        int sy = (y1 < y2) ? 1 : -1;

        int err = dx - dy;

        while (true) {
            colorGridSquareAt(new Point(x1, y1));

            if (x1 == x2 && y1 == y2)
                break;

            int e2 = 2 * err;
            if (e2 > -dy) {
                err -= dy;
                x1 += sx;
            }
            if (e2 < dx) {
                err += dx;
                y1 += sy;
            }
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        AffineTransform at = g2d.getTransform();
        g2d.translate(origin.x, origin.y);
        g2d.scale(scale, scale);

        // drawGrid(g2d);

        g2d.drawImage(gridImage, blueSquare.x, blueSquare.y, null);

        // Dessiner le carré survolé
        if (hoveredGridX >= 0 && hoveredGridY >= 0) {
            int hoverX = blueSquare.x + (hoveredGridX * gridSquareSize);
            int hoverY = blueSquare.y + (hoveredGridY * gridSquareSize);
            g2d.setColor(Color.GRAY);
            g2d.fillRect(hoverX, hoverY, gridSquareSize, gridSquareSize);
        }

        g2d.setTransform(at);
    }

    private void drawGrid(Graphics2D g2d) {
        int startX = blueSquare.x;
        int startY = blueSquare.y;

        for (int i = 0; i < gridHeight; i++) {
            for (int j = 0; j < gridWidth; j++) {
                int x = startX + (i * gridSquareSize);
                int y = startY + (j * gridSquareSize);

                g2d.setColor(gridColors[j][i]);
                g2d.fillRect(x, y, gridSquareSize, gridSquareSize);

                if (i == hoveredGridX && j == hoveredGridY) {
                    g2d.setColor(Color.GRAY);
                    g2d.fillRect(x, y, gridSquareSize, gridSquareSize);
                }
            }
        }
    }

    private void drawGridImage() {
        Graphics2D g2d = gridImage.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, gridImage.getHeight(), gridImage.getWidth());

        for (int i = 0; i < gridWidth; i++) {
            for (int j = 0; j < gridHeight; j++) {
                int x = j * gridSquareSize;
                int y = i * gridSquareSize;

                g2d.setColor(gridColors[i][j]);
                g2d.fillRect(x, y, gridSquareSize, gridSquareSize);
            }
        }

        g2d.dispose(); // Libérer les ressources graphiques
    }

    private void updateImage(Point p) {
        Graphics2D g2d = gridImage.createGraphics();
        int gridX = (p.x - blueSquare.x) / gridSquareSize;
        int gridY = (p.y - blueSquare.y) / gridSquareSize;
        int x = gridY * gridSquareSize;
        int y = gridX * gridSquareSize;
        g2d.setColor(gridColors[gridY][gridX]);
        g2d.fillRect(y, x, gridSquareSize, gridSquareSize);
        g2d.dispose();
    }

    private void colorGridSquare(Point p) {
        int gridSize = gridSquareSize;
        int gridX = (p.x - blueSquare.x) / gridSize;
        int gridY = (p.y - blueSquare.y) / gridSize;

        if (gridX >= 0 && gridX < gridHeight && gridY >= 0 && gridY < gridWidth) {
            gridColors[gridY][gridX] = Color.RED;
        }
    }

    public void shutdownExecutor() {
        if (drawingExecutor != null && !drawingExecutor.isShutdown()) {
            drawingExecutor.shutdown();
            try {
                if (!drawingExecutor.awaitTermination(50, TimeUnit.MILLISECONDS)) {
                    drawingExecutor.shutdownNow();
                    if (!drawingExecutor.awaitTermination(50, TimeUnit.MILLISECONDS)) {
                        System.err.println("Unable to terminate drawing executor.");
                    }
                }
            } catch (InterruptedException e) {
                drawingExecutor.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }

}
