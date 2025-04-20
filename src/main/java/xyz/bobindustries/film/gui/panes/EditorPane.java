package xyz.bobindustries.film.gui.panes;

import xyz.bobindustries.film.gui.elements.CoordinateBar;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.AffineTransform;
import java.awt.image.VolatileImage;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

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
  private VolatileImage gridImage;
  private JSlider scaleSlider;
  private CoordinateBar coordinatToolbar;
  private final Timer repaintTimer = new Timer(16, e -> repaint());
  private volatile boolean needsRepaint = false;
  private Point lastDragPoint = null;

  private final BlockingQueue<Point> drawQueue = new LinkedBlockingQueue<>();
  private final ExecutorService drawExecutor = Executors.newSingleThreadExecutor();

  public EditorPane(Color[][] gridColors, int gridWidth, int gridHeight) {

    this.gridColors = gridColors;
    this.gridWidth = gridWidth;
    this.gridHeight = gridHeight;

    coordinatToolbar = new CoordinateBar();

    printPaintPanelSize(gridWidth, gridHeight);
    System.out.println("fini1");
    blueSquare = new Rectangle(50, 50, gridSquareSize * gridWidth, gridSquareSize * gridHeight);

    createVolatileImage();

    setLayout(new BorderLayout());
    System.out.println("fini2");
    // Create the slider
    scaleSlider = new JSlider(JSlider.HORIZONTAL, 1, 200, 100); // Scale from 1% to 200%
    scaleSlider.setMajorTickSpacing(50);
    scaleSlider.setMinorTickSpacing(10);
    scaleSlider.setPaintTicks(true);
    scaleSlider.setPaintLabels(true);
    scaleSlider.addChangeListener(e -> {
      scale = scaleSlider.getValue() / 100.0; //
      scheduleRepaint();
    });

    System.out.println("fini3");
    // Add the slider to the bottom right
    coordinatToolbar.add(scaleSlider, BorderLayout.EAST);

    add(coordinatToolbar, BorderLayout.SOUTH);

    MouseAdapter mouseHandler = new MouseAdapter() {


      @Override
      public void mouseReleased(MouseEvent e) {
        lastDragPoint = null;
      }

      @Override
      public void mouseMoved(MouseEvent e) {
        Point adjustedPoint = new Point(
                (int) ((e.getX() - origin.x) / scale),
                (int) ((e.getY() - origin.y) / scale));

        if (blueSquare.contains(adjustedPoint)) {
          hoveredGridX = (adjustedPoint.x - blueSquare.x) / 10;
          hoveredGridY = (adjustedPoint.y - blueSquare.y) / 10;
        } else {
          hoveredGridX = -1;
          hoveredGridY = -1;
        }
        printCoords(hoveredGridX, hoveredGridY);
      }

      @Override
      public void mousePressed(MouseEvent e) {
        Point adjustedPoint = new Point(
                (int) ((e.getX() - origin.x) / scale),
                (int) ((e.getY() - origin.y) / scale));
        hoveredGridX = -1;
        hoveredGridY = -1;
        if (blueSquare.contains(adjustedPoint)) {
          colorGridSquare(adjustedPoint);
          updateImage(adjustedPoint);
          // drawGridImage();

          //repaint();
        }
      }

      @Override
      public void mouseWheelMoved(MouseWheelEvent e) {
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
            int centerX = blueSquare.x + blueSquare.width / 2;
            int centerY = blueSquare.y + blueSquare.height / 2;

            int viewCenterX = getWidth() / 2;
            int viewCenterY = getHeight() / 2;

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

      @Override
      public void mouseDragged(MouseEvent e) {
        Point adjustedPoint = new Point(
                (int) ((e.getX() - origin.x) / scale),
                (int) ((e.getY() - origin.y) / scale));

        if (blueSquare.contains(adjustedPoint)) {
          if (lastDragPoint != null) {
            interpolatePoints(lastDragPoint, adjustedPoint);
          } else {
            drawQueue.offer(adjustedPoint);
          }
          lastDragPoint = adjustedPoint;
        } else {
          lastDragPoint = null;
        }

      }
    };

    addMouseMotionListener(mouseHandler);
    addMouseListener(mouseHandler);
    addMouseWheelListener(mouseHandler);

    startDrawingThread();

    System.out.println("fini4");
  }

  private void scheduleRepaint() {
    if (!needsRepaint) {
      needsRepaint = true;
      repaintTimer.restart();
    }
  }

  private void interpolatePoints(Point p1, Point p2) {
    int dx = p2.x - p1.x;
    int dy = p2.y - p1.y;
    int steps = Math.max(Math.abs(dx), Math.abs(dy));

    for (int i = 1; i <= steps; i++) {
      int x = p1.x + i * dx / steps;
      int y = p1.y + i * dy / steps;
      drawQueue.offer(new Point(x, y));
    }
  }

  @Override
  protected void paintComponent(Graphics g) {
    super.paintComponent(g);
    Graphics2D g2d = (Graphics2D) g;
    AffineTransform at = g2d.getTransform();
    g2d.translate(origin.x, origin.y);
    g2d.scale(scale, scale);

    if (gridImage == null) {
      createVolatileImage();
    } else {
      int valid = gridImage.validate(getGraphicsConfiguration());
      if (valid == VolatileImage.IMAGE_INCOMPATIBLE) {
        createVolatileImage(); // Recrée si plus valide
      }
    }

    g2d.drawImage(gridImage, blueSquare.x, blueSquare.y, this);

    if (hoveredGridX >= 0 && hoveredGridY >= 0) {
      int hoverX = blueSquare.x + (hoveredGridX * gridSquareSize);
      int hoverY = blueSquare.y + (hoveredGridY * gridSquareSize);
      g2d.setColor(Color.GRAY);
      g2d.fillRect(hoverX, hoverY, gridSquareSize, gridSquareSize);
    }

    g2d.setTransform(at);
  }

  private void createVolatileImage() {
    GraphicsConfiguration gc = getGraphicsConfiguration();
    if (gc != null) {
      gridImage = gc.createCompatibleVolatileImage(blueSquare.width, blueSquare.height, Transparency.TRANSLUCENT);
      drawGridImage(); // Dessine dessus dès la création
    }
  }

  private void drawGridImage() {
    if (gridImage == null) {
      createVolatileImage();
      return;
    }

    do {
      int valid = gridImage.validate(getGraphicsConfiguration());
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

  private void startDrawingThread() {
    drawExecutor.submit(() -> {
      while (true) {
        try {
          Point p = drawQueue.take(); // Attend un point à dessiner
          SwingUtilities.invokeLater(() -> {
            colorGridSquare(p);
            updateImage(p);
            scheduleRepaint();
          });
        } catch (InterruptedException ex) {
          Thread.currentThread().interrupt();
          break;
        }
      }
    });
  }

  private void updateImage(Point p) {
    if (gridImage == null) return;
    Graphics2D g2d = gridImage.createGraphics();
    int gridX = (p.x - blueSquare.x) / gridSquareSize;
    int gridY = (p.y - blueSquare.y) / gridSquareSize;
    int x = gridX * gridSquareSize;
    int y = gridY * gridSquareSize;
    g2d.setColor(gridColors[gridY][gridX]);
    g2d.fillRect(x, y, gridSquareSize, gridSquareSize);
    g2d.dispose();
  }

  private void colorGridSquare(Point p) {
    int gridSize = gridSquareSize;
    int gridX = (p.x - blueSquare.x) / gridSize;
    int gridY = (p.y - blueSquare.y) / gridSize;

    if (gridX >= 0 && gridX < gridWidth && gridY >= 0 && gridY < gridHeight) {
      gridColors[gridY][gridX] = Color.RED;
    }
  }

  public void printCoords(int intPosX, int intPosY) {
    String posX = String.valueOf(intPosX);
    String posY = String.valueOf(intPosY);
    coordinatToolbar.getCoordinates().setText(posX + ",  " + posY + " px");
  }

  public void printPaintPanelSize(int width, int height) {
    coordinatToolbar.getFrameSize().setText(width + ",  " + height + " px");
  }

}

