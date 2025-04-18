package xyz.bobindustries.film.gui.panes;

import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.List;

import javax.swing.*;

import xyz.bobindustries.film.projects.ProjectManager;
import xyz.bobindustries.film.projects.elements.ImageFile;
import xyz.bobindustries.film.projects.elements.Project;
import xyz.bobindustries.film.projects.elements.exceptions.InvalidScenarioContentException;

public class ScenarioEditorPane extends JPanel {

    static class ImagePanel extends JPanel implements Transferable {
        private final ImageFile imageFile;
        private final JLabel nameLabel;
        private static final DataFlavor DATA_FLAVOR = new DataFlavor(ImageFile.class, "ImageFile");

        ImagePanel(ImageFile imageFile) {
            this.imageFile = imageFile;
            this.nameLabel = new JLabel(imageFile.getFileName());
            setLayout(new BorderLayout());
            add(nameLabel, BorderLayout.CENTER);
            setPreferredSize(new Dimension(100, 50)); // Adjust as needed
            setBorder(BorderFactory.createLineBorder(Color.BLACK)); // Add a border
            setBackground(new Color(0x3C3836));
            nameLabel.setForeground(new Color(0xEBDBB2));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)); // indicate draggable
        }

        ImageFile getImageFile() {
            return imageFile;
        }

        // Implementation of Transferable interface
        @Override
        public DataFlavor[] getTransferDataFlavors() {
            return new DataFlavor[] { DATA_FLAVOR };
        }

        @Override
        public boolean isDataFlavorSupported(DataFlavor flavor) {
            return DATA_FLAVOR.equals(flavor);
        }

        @Override
        public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException {
            if (DATA_FLAVOR.equals(flavor)) {
                return imageFile;
            } else {
                throw new UnsupportedFlavorException(flavor);
            }
        }

        @Override
        public String toString() {
            return nameLabel.getText();
        }
    }

    static class TimelinePane extends JPanel {
        static class TimelineElement extends JPanel {
            private static final double DEFAULT_TIME = 0.2;
            private static final double MIN_TIME = 0.05;
            private static final double MAX_TIME = 1.0;
            private static final int PIXELS_PER_SECOND = 300; // How many pixels represent 1 second
            private static final int DEFAULT_HEIGHT = 50;
            private static final int ARC_WIDTH = 15;
            private static final int ARC_HEIGHT = 15;
            private static final int HANDLE_WIDTH = 3;
            private static final int HANDLE_PADDING = 3; // Padding from the right edge
            private static final double DRAG_PRECISION_FACTOR = 1.0; // Higher value = less sensitive drag

            private double time; // Duration of this element in seconds
            private final Color backgroundColor = new Color(0x25, 0x65, 0x68); // Default background color
            private final Color handleColor = new Color(0x68, 0x9d, 0x6a); // Handle color
            private final Color textColor = new Color(0xeb, 0xdb, 0xb2); // Text color

            private boolean isDragging = false;
            private int dragStartX;
            private double timeAtDragStart;

            private final ImageFile data;

            TimelineElement(TimelinePane timelinePane, ImageFile imageFile) {
                this.time = DEFAULT_TIME;
                this.data = imageFile;
                updatePreferredSize();

                // Add mouse listeners for dragging the handle
                HandleDragListener listener = new HandleDragListener(timelinePane);

                addMouseListener(listener);
                addMouseMotionListener(listener);

                // Set opaque to false if you want the parent's background to show through
                setOpaque(false);
            }

            TimelineElement(TimelinePane timelinePane, ImageFile imageFile, double time) {
                this(timelinePane, imageFile);
                this.time = time;
            }

            public double getTime() {
                return time;
            }

            public ImageFile getData() {
                return data;
            }

            public void setTime(double time) {
                this.time = Math.max(MIN_TIME, Math.min(MAX_TIME, time));
                updatePreferredSize();
                revalidate(); // Notify layout manager of size change
                repaint(); // Redraw the component
            }

            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();

                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int width = getWidth();
                int height = getHeight();

                /*
                 * Drawing background rectangle
                 */
                g2d.setColor(backgroundColor);
                RoundRectangle2D roundedRect = new RoundRectangle2D.Float(0, 0, width - 1, height - 1, ARC_WIDTH,
                        ARC_HEIGHT);
                g2d.fill(roundedRect);

                /*
                 * Drawing handle bar
                 */
                int handleHeight = (int) (height * 0.75);
                int handleY = (height - handleHeight) / 2;
                int handleX = width - HANDLE_WIDTH - HANDLE_PADDING;
                g2d.setColor(handleColor);
                g2d.fillRect(handleX, handleY, HANDLE_WIDTH, handleHeight);

                /*
                 * Drawing text
                 */
                g2d.setColor(textColor);
                String timeString = String.format("%.2fs", time);
                FontMetrics fm = g2d.getFontMetrics();
                int textWidth = fm.stringWidth(timeString);
                int textHeight = fm.getAscent();
                int textX = 0;
                int textY = height - fm.getDescent();

                if (textX + textWidth > handleX - HANDLE_PADDING) {
                    textX = handleX - HANDLE_PADDING - textWidth;
                }
                textX = Math.max(5, textX);

                g2d.drawString(timeString, textX, textY);

                String filename = data.getFileName();
                textWidth = fm.stringWidth(filename);
                textHeight = fm.getAscent();
                textX = (width - textWidth) / 2;
                textY = (height + textHeight) / 2 - fm.getDescent();

                if (textX + textWidth > handleX - HANDLE_PADDING) {
                    textX = handleX - HANDLE_PADDING - textWidth;
                }
                textX = Math.max(5, textX);

                g2d.drawString(filename, textX, textY);

                g2d.dispose();
            }

            // Sizing stuff

            private int calculatePreferredWidth() {
                return (int) (time * PIXELS_PER_SECOND);
            }

            private void updatePreferredSize() {
                setPreferredSize(new Dimension(calculatePreferredWidth(), DEFAULT_HEIGHT));
            }

            private Rectangle getHandleBounds() {
                int width = getWidth();
                int height = getHeight();
                int clickableHandleWidth = HANDLE_WIDTH + 2 * HANDLE_PADDING;
                int clickableHandleX = width - clickableHandleWidth;
                return new Rectangle(clickableHandleX, 0, clickableHandleWidth, height);
            }

            // Private HandleDragListener class
            private class HandleDragListener extends MouseAdapter {
                TimelinePane pane;

                public HandleDragListener(TimelinePane pane) {
                    this.pane = pane;
                }

                @Override
                public void mousePressed(MouseEvent e) {
                    if (getHandleBounds().contains(e.getPoint())) {
                        isDragging = true;
                        dragStartX = e.getXOnScreen();
                        timeAtDragStart = time;
                        setCursor(Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR));
                    }
                }

                @Override
                public void mouseDragged(MouseEvent e) {
                    if (isDragging) {
                        int currentX = e.getXOnScreen();
                        int deltaX = currentX - dragStartX;

                        double deltaTime = (double) deltaX / PIXELS_PER_SECOND / DRAG_PRECISION_FACTOR;

                        double newTime = timeAtDragStart + deltaTime;
                        setTime(newTime);
                        pane.updateLayout();
                    }
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                    if (isDragging) {
                        isDragging = false;
                        setCursor(Cursor.getDefaultCursor());
                    }
                }

                @Override
                public void mouseMoved(MouseEvent e) {
                    if (getHandleBounds().contains(e.getPoint())) {
                        setCursor(Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR));
                    } else {
                        setCursor(Cursor.getDefaultCursor());
                    }
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    if (!isDragging) {
                        setCursor(Cursor.getDefaultCursor());
                    }
                }
            }
        }

        private final List<TimelineElement> elements;
        private final JPanel contentPane;

        TimelinePane(String scenarioContent) throws InvalidScenarioContentException {
            setLayout(new BorderLayout());
            elements = new ArrayList<>();

            contentPane = new JPanel();
            contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.X_AXIS));
            contentPane.setBackground(Color.DARK_GRAY);

            Project current = ProjectManager.getCurrent();

            if (current != null) {
                List<ImageFile> images = current.getImages();

                String[] lines = scenarioContent.split("\\r?\n");

                if (lines.length >= 1 && !lines[0].equals("")) {
                    int i = 0;
                    for (String line : lines) {
                        String[] lineData = line.split(",");

                        if (lineData.length != 2) {
                            throw new InvalidScenarioContentException("Error line " + i + ". : Wrong file format.");
                        } else {
                            String fileName = lineData[0].trim();
                            Double time = Double.parseDouble(lineData[1].trim());

                            if (fileName.equals(""))
                                throw new InvalidScenarioContentException("Error line " + i + ". : Empty file name.");

                            if (time > 1 || time < 0.05)
                                throw new InvalidScenarioContentException(
                                        "Error line " + i + ". : Invalid time. Must be < 1 && > .2");

                            boolean foundFile = false;
                            for (ImageFile imf : images) {
                                if (imf.getFileName().equals(lineData[0].trim())) {
                                    elements.add(
                                            new TimelineElement(
                                                    this,
                                                    imf,
                                                    Double.parseDouble(lineData[1].trim())));
                                    foundFile = true;
                                    break;
                                }
                            }
                            if (!foundFile) {
                                throw new InvalidScenarioContentException(
                                        "Error line " + i + ". : File : " + lineData[0].trim() + " not found.");
                            }
                        }
                        i++;
                    }
                }
            }

            JScrollPane scrollPane = new JScrollPane(contentPane);
            scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
            scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
            scrollPane.getHorizontalScrollBar().setUnitIncrement(16);

            JPanel centerContainer = new JPanel();
            centerContainer.setLayout(new BoxLayout(centerContainer, BoxLayout.Y_AXIS));
            centerContainer.setBackground(Color.BLACK);
            centerContainer.add(Box.createVerticalGlue());

            JPanel fixedHeightPanel = new JPanel(new BorderLayout());
            fixedHeightPanel.setOpaque(false);
            fixedHeightPanel.add(scrollPane, BorderLayout.CENTER);
            centerContainer.add(fixedHeightPanel);

            centerContainer.add(Box.createVerticalGlue());

            addComponentListener(new java.awt.event.ComponentAdapter() {
                @Override
                public void componentResized(java.awt.event.ComponentEvent e) {
                    int halfHeight = getHeight() / 2;
                    fixedHeightPanel.setPreferredSize(new Dimension(getWidth(), halfHeight));
                    fixedHeightPanel.revalidate();
                    updateLayout();
                }
            });

            add(centerContainer, BorderLayout.CENTER);
            updateLayout();
        }

        /**
         * Updates the layout of the timeline elements.
         * Ensures that no elements overlap, and if one element is
         * stretched (i.e., increased in width based on its time attribute),
         * the subsequent elements are shifted to the right.
         */
        void updateLayout() {
            int currentX = 10; // initial margin
            int gap = 10; // gap between elements

            for (TimelineElement elem : elements) {
                boolean contains = false;
                for (Component c : contentPane.getComponents()) {
                    if (c.equals(elem)) {
                        contains = true;
                        break;
                    }
                }

                if (!contains) {
                    contentPane.add(elem);
                }

                int baseWidth = 100;
                int additionalWidth = (int) (elem.getTime() * 200); // scale factor for demo
                int totalWidth = baseWidth + additionalWidth;
                elem.setPreferredSize(new Dimension(totalWidth, 50));
                elem.setMinimumSize(new Dimension(totalWidth, 50));
                elem.setMaximumSize(new Dimension(totalWidth, 50));

                elem.setBorder(BorderFactory.createEmptyBorder(0, currentX, 0, 0));
                currentX += totalWidth + gap;
                elem.revalidate();
                elem.repaint();
            }

            // Refresh layout
            contentPane.revalidate();
            contentPane.repaint();
        }

        void addTimelineElement(ImageFile droppedImageFile) {
            elements.add(new TimelineElement(this, droppedImageFile));
            updateLayout();
        }

        String getScenarioFileContent() {
            StringBuilder sb = new StringBuilder();

            for (int i = 0; i < elements.size(); i++) {
                String fileName = elements.get(i).getData().getFileName();
                double time = elements.get(i).getTime();
                sb
                        .append(fileName)
                        .append(",")
                        .append(time);

                if (i < elements.size() - 1) {
                    sb.append(System.lineSeparator());
                }
            }

            return sb.toString();
        }

    }

    private JPanel imagesListPane,
            imagesPanelList,
            visualizerPane;
    private TimelinePane timelinePane;
    private JScrollPane imageListScrollPane;
    private JLabel currentImageView;

    private void initializeComponents() throws InvalidScenarioContentException {
        imagesListPane = new JPanel();
        visualizerPane = new JPanel();
        timelinePane = new TimelinePane(ProjectManager.getCurrent().getScenarioContent());
        imagesPanelList = new JPanel();

        imagesListPane.setBackground(Color.LIGHT_GRAY);
        visualizerPane.setBackground(Color.WHITE);
        timelinePane.setBackground(Color.GRAY);

        imagesListPane.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
        visualizerPane.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
        timelinePane.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));

        imagesPanelList.setLayout(new BoxLayout(imagesPanelList, BoxLayout.Y_AXIS));
        imageListScrollPane = new JScrollPane(imagesPanelList);
        imageListScrollPane.setPreferredSize(new Dimension(150, 0));
        imageListScrollPane.getVerticalScrollBar().setUnitIncrement(16);

        currentImageView = new JLabel();
        currentImageView.setHorizontalAlignment(SwingConstants.CENTER);
    }

    private void layoutComponents() {
        add(timelinePane, BorderLayout.SOUTH);
        add(visualizerPane, BorderLayout.CENTER);
        add(imagesListPane, BorderLayout.WEST);

        imagesListPane.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1;
        gbc.weighty = 1;
        gbc.fill = GridBagConstraints.BOTH;
        imagesListPane.add(imageListScrollPane, gbc);

        visualizerPane.setLayout(new BorderLayout());
        visualizerPane.add(currentImageView, BorderLayout.CENTER);

        timelinePane.setPreferredSize(new Dimension(0, (int) (this.getPreferredSize().height * 0.8)));

        imagesListPane.setPreferredSize(new Dimension((int) (this.getPreferredSize().width * 0.6), 0));
    }

    public ScenarioEditorPane() throws InvalidScenarioContentException {
        setLayout(new BorderLayout());
        initializeComponents();
        layoutComponents();
        populateImageList();

        /*
         * Resize components dynamically.
         */
        addComponentListener(new ComponentListener() {
            @Override
            public void componentResized(ComponentEvent e) {
                resizeComponents();
            }

            @Override
            public void componentMoved(ComponentEvent e) {
            }

            @Override
            public void componentShown(ComponentEvent e) {
            }

            @Override
            public void componentHidden(ComponentEvent e) {
            }
        });

        enableDragAndDrop();
    }

    private void enableDragAndDrop() {
        /*
         * Set up the timelinePane to accept drops of type ImageFile.
         */
        timelinePane.setTransferHandler(new TransferHandler() {
            @Override
            public boolean canImport(TransferSupport support) {
                return support.isDataFlavorSupported(ImagePanel.DATA_FLAVOR);
            }

            @Override
            public boolean importData(TransferSupport support) {
                if (!canImport(support)) {
                    return false;
                }
                try {
                    Transferable transferable = support.getTransferable();
                    ImageFile droppedImageFile = (ImageFile) transferable.getTransferData(ImagePanel.DATA_FLAVOR);

                    timelinePane.addTimelineElement(droppedImageFile);
                    return true;
                } catch (UnsupportedFlavorException | java.io.IOException ex) {
                    ex.printStackTrace();
                }
                return false;
            }
        });

        /*
         * For each ImagePanel in the images list, set up a TransferHandler and
         * MouseMotionListener for dragging.
         * We assume the ImagePanel items are already added as children of
         * imagesPanelList.
         */
        for (Component comp : imagesPanelList.getComponents()) {
            if (comp instanceof ImagePanel imagePanel) { // Pattern variable
                // We set a dummy TransferHandler to enable dragging using the custom
                // transferable.
                imagePanel.setTransferHandler(new TransferHandler() {
                    @Override
                    public int getSourceActions(JComponent c) {
                        return COPY;
                    }

                    @Override
                    protected Transferable createTransferable(JComponent c) {
                        return (Transferable) c;
                    }
                });

                imagePanel.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
                    @Override
                    public void mouseDragged(java.awt.event.MouseEvent e) {
                        JComponent c = (JComponent) e.getSource();
                        TransferHandler handler = c.getTransferHandler();
                        handler.exportAsDrag(c, e, TransferHandler.COPY);
                    }
                });
            }
        }
    }

    private void resizeComponents() {
        Dimension currentSize = getSize();

        imagesListPane.setPreferredSize(new Dimension((int) (currentSize.width * 0.3), currentSize.height));
        timelinePane.setPreferredSize(new Dimension(0, (int) (currentSize.height * 0.4)));

        revalidate();
        repaint();
    }

    private void populateImageList() {
        imagesPanelList.removeAll();
        Project current = ProjectManager.getCurrent();

        if (current != null) {
            List<ImageFile> images = current.getImages();

            for (ImageFile imf : images) {
                ImagePanel imagePanel = new ImagePanel(imf);

                imagesPanelList.add(imagePanel);
            }
        }

        imagesPanelList.revalidate();
        imagesPanelList.repaint();
    }

    // Helper method to scale images
    private Image scaleImage(Image image, int maxWidth, int maxHeight) {
        if (image == null)
            return null; // handle null
        int originalWidth = image.getWidth(null);
        int originalHeight = image.getHeight(null);

        if (originalWidth <= maxWidth && originalHeight <= maxHeight) {
            return image; // No scaling needed
        }

        int newWidth = originalWidth;
        int newHeight = originalHeight;

        if (originalWidth > maxWidth) {
            newWidth = maxWidth;
            newHeight = (newWidth * originalHeight) / originalWidth;
        }

        if (newHeight > maxHeight) {
            newHeight = maxHeight;
            newWidth = (newHeight * originalWidth) / originalHeight;
        }
        return image.getScaledInstance(newWidth, newHeight, Image.SCALE_SMOOTH);
    }

    public String extractScenarioContent() {
        return timelinePane.getScenarioFileContent();
    }
}
