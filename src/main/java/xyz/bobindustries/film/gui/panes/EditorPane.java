package xyz.bobindustries.film.gui.panes;

import xyz.bobindustries.film.image_editor.*;
import xyz.bobindustries.film.gui.Workspace;
import xyz.bobindustries.film.gui.elements.CoordinateBar;
import xyz.bobindustries.film.gui.elements.utilitaries.SimpleErrorDialog;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.AffineTransform;
import java.util.Collections;
import java.util.HashMap;

public class EditorPane extends JPanel {

    // private ArrayList<EditorModel> openedImages;
    private HashMap<Integer, String> openedFiles;
    private HashMap<Integer, EditorModel> openedFilesModels;
    private int currentImageIndex;
    private EditorModel data;
    private static Tools selectedTool = new Pen();
    private ToolsList currentTools = ToolsList.PEN;
    private JSlider scaleSlider;
    private CoordinateBar coordinatToolbar;
    private final Timer repaintTimer = new Timer(16, e -> repaint());
    private volatile boolean needsRepaint = false;

    public EditorPane(Color[][] gridColors, int gridWidth, int gridHeight, String currentFile) {

        openedFiles = new HashMap<>();
        openedFilesModels = new HashMap<>();

        currentImageIndex = 0;
        openedFiles.put(currentImageIndex, currentFile);

        data = new EditorModel(this, gridColors, gridWidth, gridHeight);
        openedFilesModels.put(currentImageIndex, data);

        coordinatToolbar = new CoordinateBar();
        coordinatToolbar.setFileName(currentFile);

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

    public String getCurrentFileName() {
        return openedFiles.get(currentImageIndex);
    }

    public ToolsSettings getSelectedToolsSettings() {
        try {
            return (ToolsSettings) selectedTool;

        } catch (ClassCastException ex) {
            return null;
        }
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

    public void setFileName(String fileName) {
        openedFiles.put(currentImageIndex, fileName);
        coordinatToolbar.setFileName(fileName);
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
        System.out.println("selecting tool: " + chosenToolsList.name());
        switch (chosenToolsList) {
            case PEN -> selectedTool = new Pen();
            case BRUSH -> {
                selectedTool = new Brush(10);
            }
            case ERASE -> {
                selectedTool = new Erase(10);
            }
            case CIRCLE -> {
                selectedTool = new Circle();
            }
            case RECTANGLE -> {
                selectedTool = new RectangleTool();
            }
            case COLOR_PICKER -> selectedTool = new ColorPickerTool();
            case SELECT -> selectedTool = new SelectionTool();
            case MOVE_SELECTION_AREA -> selectedTool = new MoveSelectionArea();
            case MOVE_SELECTION -> {
                selectedTool = new MoveSelection();
                data.setDraggedImage();
            }
            case UNDO -> {
                data.undo();
            }
            case REDO -> {
                data.redo();
            }
            case PREVIOUS_FRAME -> {
                if (openedFilesModels.containsKey(currentImageIndex - 1)
                        && openedFiles.containsKey(currentImageIndex - 1)) {
                    changeCurrentImage(currentImageIndex - 1);
                    System.out.println("index:" + currentImageIndex);
                    data.reDrawGrid(data.getGridColors());
                } else {
                    SimpleErrorDialog.show("no previous frame available");
                }
            }
            case NEXT_FRAME -> {
                if (openedFilesModels.containsKey(currentImageIndex + 1)
                        && openedFiles.containsKey(currentImageIndex + 1)) {
                    changeCurrentImage(currentImageIndex + 1);
                    System.out.println("index:" + currentImageIndex);
                    data.reDrawGrid(data.getGridColors());
                } else {
                    SimpleErrorDialog.show("no next frame available");
                }
            }
            default -> throw new IllegalArgumentException("Outil non reconnu : " + chosenToolsList);
        }
        Workspace.getInstance().updateToolsSettings(getSelectedToolsSettings());
    }

    public void setCurrentImageIndex(int currentImageIndex) {
        this.currentImageIndex = currentImageIndex;
    }

    public int getCurrentImageIndex() {
        return currentImageIndex;
    }

    public void addNewImage(Color[][] gridColors, String name) {
        if (openedFiles.containsValue(name)) {
            EditorModel newData = new EditorModel(this, gridColors, gridColors[0].length, gridColors.length);
            openedFiles.put(Collections.max(openedFilesModels.keySet()) + 1, name);
            openedFilesModels.put(Collections.max(openedFilesModels.keySet()) + 1, newData);
        } else {
            SimpleErrorDialog.show("Erreur: l'image existe déjà");
        }
    }

    public void changeCurrentImage(int idImage) {
        currentImageIndex = idImage;
        coordinatToolbar.setFileName(openedFiles.get(currentImageIndex));
        data = openedFilesModels.get(currentImageIndex);
    }

}
