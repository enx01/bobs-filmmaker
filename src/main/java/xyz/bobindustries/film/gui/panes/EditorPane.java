package xyz.bobindustries.film.gui.panes;

import xyz.bobindustries.film.ImageEditor.*;
import xyz.bobindustries.film.gui.Workspace;
import xyz.bobindustries.film.gui.elements.ColorBox;
import xyz.bobindustries.film.gui.elements.CoordinateBar;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.VolatileImage;
import java.util.ArrayList;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

public class EditorPane extends JPanel {

    private ArrayList<EditorModel> openedImages;
    private int currentImageIndex;
    private EditorModel data;
    private static Tools selectedTool = new Pen();
    private ToolsList currentTools = ToolsList.PEN;
    private JSlider scaleSlider;
    private CoordinateBar coordinatToolbar;
    private final Timer repaintTimer = new Timer(16, e -> repaint());
    private volatile boolean needsRepaint = false;

    public EditorPane(Color[][] gridColors, int gridWidth, int gridHeight) {

        if (openedImages==null) {
            openedImages = new ArrayList<>();
        }

        currentImageIndex=0;

        data = new EditorModel(this, gridColors, gridWidth, gridHeight);

        openedImages.add(data);

        coordinatToolbar = new CoordinateBar();

        printPaintPanelSize(gridWidth, gridHeight);

        setLayout(new BorderLayout());
        // Create the slider
        scaleSlider = new JSlider(JSlider.HORIZONTAL, 1, 200, 100); // Scale from 1% to 200%
        scaleSlider.setMajorTickSpacing(50);
        scaleSlider.setMinorTickSpacing(10);
        scaleSlider.setPaintTicks(true);
        scaleSlider.setPaintLabels(true);
        scaleSlider.addChangeListener(e -> {
            scheduleRepaint();
        });

        // Add the slider to the bottom right
        coordinatToolbar.add(scaleSlider, BorderLayout.EAST);

        add(coordinatToolbar, BorderLayout.SOUTH);

        MouseAdapter mouseHandler = setMouseAdapter();

        addMouseMotionListener(mouseHandler);
        addMouseListener(mouseHandler);
        addMouseWheelListener(mouseHandler);

        startDrawingThread();
    }


    private MouseAdapter setMouseAdapter() {
        MouseAdapter mouseHandler = new MouseAdapter() {

            @Override
            public void mouseReleased(MouseEvent e) {
                selectedTool.mouseReleasedAction(e, data);
                data.setLastDragPoint(null);
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                selectedTool.mouseMovedAction(e, data);
                printCoords(data.getHoveredGridX(), data.getHoveredGridY());
                scheduleRepaint();
            }

            @Override
            public void mousePressed(MouseEvent e) {
                selectedTool.mousePressedAction(e, data);
            }

            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                data.zoomAndScroll(e);
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                selectedTool.mouseDraggedAction(e, data);
            }
        };
        return mouseHandler;
    }

    private void scheduleRepaint() {
        if (!needsRepaint) {
            needsRepaint = true;
            repaintTimer.restart();
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        AffineTransform at = data.paint(g2d);
        selectedTool.paintHoveredArea(g2d, data, at);
    }

    private void startDrawingThread() {
        data.getDrawExecutor().submit(() -> {
            while (true) {
                try {
                    Point p = data.getDrawQueue().take(); // Attend un point à dessiner
                    SwingUtilities.invokeLater(() -> {
                        if (currentTools.name().equals("ERASE")) {
                            data.colorGridSquare(p, Color.WHITE);
                        } else {
                            data.colorGridSquare(p, null);
                        }
                        data.updateImage(p);
                        scheduleRepaint();
                    });
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        });
    }

    public EditorModel getData() {
        return data;
    }

    public void printCoords(int intPosX, int intPosY) {
        String posX = String.valueOf(intPosX);
        String posY = String.valueOf(intPosY);
        coordinatToolbar.getCoordinates().setText(posX + ",  " + posY + " px");
    }

    public void printPaintPanelSize(int width, int height) {
        coordinatToolbar.getFrameSize().setText(width + ",  " + height + " px");
    }

    public String getSelectedTool() {
        return currentTools.name();
    }

    public void setSelectedTool(ToolsList chosenToolsList) {
        if (getSelectedTool().equals("MOVE_SELECTION")) {
            System.out.println("image merging start");
            data.mergeDraggedImageToGrid();
            System.out.println("image merged");
        }
        currentTools = chosenToolsList;
        System.out.println("selecting tool: "+chosenToolsList.name());
        switch (chosenToolsList) {
          case PEN -> selectedTool = new Pen();
          case BRUSH -> selectedTool = new Brush(10);
          case ERASE -> selectedTool = new Erase(10);
          case CIRCLE -> selectedTool = new Circle();
          case RECTANGLE -> selectedTool = new RectangleTool();
          case SELECT -> selectedTool = new SelectionTool();
          case MOVE_SELECTION_AREA -> selectedTool = new MoveSelectionArea();
          case MOVE_SELECTION -> {
              selectedTool = new MoveSelection();
              data.setDraggedImage();
          }
          case UNDO -> {
              changeCurrentImage(currentImageIndex-1);
              System.out.println("index:"+currentImageIndex);
              data.reDrawGrid(data.getGridColors());
          }
          case REDO -> {
              changeCurrentImage(currentImageIndex+1);
              System.out.println("index:"+currentImageIndex);
              data.reDrawGrid(data.getGridColors());
          }
          default -> throw new IllegalArgumentException("Outil non reconnu : " + chosenToolsList);
      }
    }

    public void setCurrentImageIndex(int currentImageIndex) {
        this.currentImageIndex = currentImageIndex;
    }

    public int getCurrentImageIndex() {
        return currentImageIndex;
    }

    public void addNewImage(Color[][] gridColors) {
        EditorModel newData = new EditorModel(this, gridColors, gridColors[0].length, gridColors.length);
        openedImages.add(newData);
    }

    public void changeCurrentImage(int idImage) {
        currentImageIndex = idImage;
        data = openedImages.get(currentImageIndex);
    }

}
