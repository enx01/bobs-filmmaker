package xyz.bobindustries.film.gui.panes;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.TransferHandler;
import javax.swing.border.Border;

import xyz.bobindustries.film.projects.ProjectManager;
import xyz.bobindustries.film.projects.elements.ImageFile;
import xyz.bobindustries.film.projects.elements.Project;

public class ScenarioEditorPane extends JPanel {

    class ImagePanel extends JPanel implements Transferable {
        private ImageFile imageFile;
        private JLabel nameLabel;
        private static final DataFlavor DATA_FLAVOR = new DataFlavor(ImageFile.class, "ImageFile");

        public ImagePanel(ImageFile imageFile) {
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

        public ImageFile getImageFile() {
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

    class TimelinePane extends JPanel {
        private List<TimelineElement> elements;
        private JPanel contentPane;
        private JScrollPane scrollPane;

        public TimelinePane() {
            setLayout(new BorderLayout());
            elements = new ArrayList<>();

            // Create a content pane with vertical BoxLayout to hold TimelineElements
            contentPane = new JPanel();
            contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.X_AXIS));
            contentPane.setBackground(Color.DARK_GRAY);

            // Create default TimelineElements (using default time values, e.g., 0.2, 0.5,
            // 0.8)
            TimelineElement defaultElem1 = new TimelineElement(this);
            TimelineElement defaultElem2 = new TimelineElement(this);
            TimelineElement defaultElem3 = new TimelineElement(this);
            TimelineElement defaultElem4 = new TimelineElement(this);
            elements.add(defaultElem1);
            elements.add(defaultElem2);
            elements.add(defaultElem3);
            elements.add(defaultElem4);

            // Add each TimelineElement to the content pane with spacing to avoid overlap.
            for (TimelineElement elem : elements) {
                contentPane.add(elem);
            }

            // Wrap the contentPane into a JScrollPane to allow vertical scrolling
            scrollPane = new JScrollPane(contentPane);
            scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
            scrollPane.getHorizontalScrollBar().setUnitIncrement(16);

            JPanel centerContainer = new JPanel();
            centerContainer.setLayout(new BoxLayout(centerContainer, BoxLayout.Y_AXIS));
            centerContainer.setBackground(Color.BLACK);
            // Add vertical glue, then the scrollPane (with half the height), then glue.
            centerContainer.add(Box.createVerticalGlue());

            // Wrap scrollPane in a panel to restrict its height to half of TimelinePane's
            // height.
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
        public void updateLayout() {
            int currentX = 10; // initial margin
            int gap = 10; // gap between elements

            for (TimelineElement elem : elements) {
                // The width is a function of the time attribute.
                // For simplicity, assume a base width and then additional width by time
                // (scaled)
                int baseWidth = 100;
                int additionalWidth = (int) (elem.getTime() * 200); // scale factor for demo
                int totalWidth = baseWidth + additionalWidth;
                elem.setPreferredSize(new Dimension(totalWidth, 50));
                elem.setMinimumSize(new Dimension(totalWidth, 50));
                elem.setMaximumSize(new Dimension(totalWidth, 50));
                // Set location in container: x coordinate is currentX
                // In a BoxLayout, we can simulate the horizontal shift with a left border
                // margin.
                elem.setBorder(BorderFactory.createEmptyBorder(0, currentX, 0, 0));
                currentX += totalWidth + gap;
                elem.revalidate();
                elem.repaint();
            }

            // Refresh layout
            contentPane.revalidate();
            contentPane.repaint();
        }

        class TimelineElement extends JPanel {

            // Conversion factor: each pixel will correspond to 0.01 time units.
            private static final double PIXEL_TO_TIME = .01;
            // Minimum width for the component (in pixels)
            private static final int MIN_WIDTH = 50;
            // Thickness of the drag handle area at the right edge (in pixels)
            private static final int HANDLE_WIDTH = 5;
            // Arc diameter for rounded corners
            private static final int ARC_WIDTH = 20;
            private static final int ARC_HEIGHT = 20;

            // Double time attribute. Default is 0.2 seconds (depends on conversion factor
            // and initial width)
            private double time;

            // To keep track of resizing
            private boolean dragging = false;
            private int dragOffset;

            public TimelineElement(TimelinePane timelinePane) {
                // Set default preferred size
                int initialWidth = (int) (0.2 / PIXEL_TO_TIME); // e.g., if 0.2 sec then width=20 but ensure it is above
                                                                // MIN_WIDTH
                if (initialWidth < MIN_WIDTH) {
                    initialWidth = MIN_WIDTH;
                }
                this.time = initialWidth * PIXEL_TO_TIME;
                setPreferredSize(new Dimension(initialWidth, 50));
                // Enable mouse events for resize drag
                MouseAdapter mouseAdapter = new MouseAdapter() {
                    @Override
                    public void mousePressed(MouseEvent e) {
                        // If the pressed point is near the right edge (drag handle area), start
                        // dragging.
                        if (isOverHandle(e.getX())) {
                            dragging = true;
                            // Compute the offset between the right edge and the actual click x-coordinate.
                            Point absLocation = getLocationOnScreen();
                            int rightEdge = absLocation.x + getWidth();
                            dragOffset = rightEdge - e.getXOnScreen();
                        }
                    }

                    @Override
                    public void mouseReleased(MouseEvent e) {
                        dragging = false;
                    }

                    @Override
                    public void mouseDragged(MouseEvent e) {
                        if (dragging) {
                            int leftX = getLocationOnScreen().x;

                            int newWidth = e.getXOnScreen() - leftX + dragOffset;

                            newWidth = Math.max(newWidth, MIN_WIDTH);
                            setPreferredSize(new Dimension(newWidth, getHeight()));
                            setSize(newWidth, getHeight());
                            time = newWidth * PIXEL_TO_TIME;
                            timelinePane.updateLayout();
                            revalidate();
                            repaint();
                        }
                    }
                };

                addMouseListener(mouseAdapter);
                addMouseMotionListener(mouseAdapter);
            }

            /**
             * Checks if a given x coordinate is over the drag handle (the right edge area).
             */
            private boolean isOverHandle(int x) {
                return x >= getWidth() - 10 - HANDLE_WIDTH;
            }

            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                // Enable antialiasing for smoother corners and text
                Graphics2D g2 = (Graphics2D) g.create();
                try {
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                    // Calculate rounded rectangle dimensions
                    int width = getWidth();
                    int height = getHeight();

                    // Draw the main rounded rectangle background
                    Shape roundedRect = new RoundRectangle2D.Double(0, 0, width - 1, height - 1, ARC_WIDTH, ARC_HEIGHT);
                    g2.setColor(new Color(173, 216, 230)); // light blue color for example
                    g2.fill(roundedRect);

                    // Draw border
                    g2.setColor(Color.GRAY);
                    g2.draw(roundedRect);

                    // Draw drag handle indicator (a vertical line or a shaded rectangle at the
                    // right edge)
                    g2.setColor(Color.DARK_GRAY);
                    g2.fillRect(width - 10 - HANDLE_WIDTH, 0, HANDLE_WIDTH, height);

                    // Draw time label at the center
                    g2.setColor(Color.BLACK);
                    String timeStr = String.format("%.2f sec", time);
                    FontMetrics fm = g2.getFontMetrics();
                    int textWidth = fm.stringWidth(timeStr);
                    int textHeight = fm.getAscent();
                    int centerX = (width - textWidth) / 2;
                    int centerY = (height + textHeight) / 2 - 2;
                    g2.drawString(timeStr, centerX, centerY);
                } finally {
                    g2.dispose();
                }
            }

            public double getTime() {
                return time;
            }
        }
    }

    private JPanel imagesListPane,
            imagesPanelList,
            visualizerPane;
    private TimelinePane timelinePane;
    private JScrollPane imageListScrollPane;
    private JLabel currentImageView;

    private void initializeComponents() {
        imagesListPane = new JPanel();
        visualizerPane = new JPanel();
        timelinePane = new TimelinePane();
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

        currentImageView = new JLabel(); // Initialize the JLabel
        currentImageView.setHorizontalAlignment(SwingConstants.CENTER); // Center the image.

        // Timeline setup (using a placeholder for now)
        // timelinePane.add(new JLabel("Timeline Placeholder"));
    }

    private void layoutComponents() {
        // Main layout using BorderLayout
        add(timelinePane, BorderLayout.SOUTH);
        add(visualizerPane, BorderLayout.CENTER);
        add(imagesListPane, BorderLayout.WEST);

        // Images list layout (using GridBagLayout for more flexibility)
        imagesListPane.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1;
        gbc.weighty = 1;
        gbc.fill = GridBagConstraints.BOTH; // Make the list fill its space
        imagesListPane.add(imageListScrollPane, gbc); // Wrap the JList in a JScrollPane

        // Image viewer layout (basic - just add the label)
        visualizerPane.setLayout(new BorderLayout());
        visualizerPane.add(currentImageView, BorderLayout.CENTER);

        timelinePane.setPreferredSize(new Dimension(0, (int) (this.getPreferredSize().height * 0.8)));

        // Set the preferred size of the imagesListPane. This is crucial.
        imagesListPane.setPreferredSize(new Dimension((int) (this.getPreferredSize().width * 0.6), 0));
    }

    public ScenarioEditorPane() {
        setLayout(new BorderLayout());
        initializeComponents();
        layoutComponents();
        populateImageList();

        /*
         * Resize compenents dynamically.
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
        // Set up the timelinePane to accept drops of type ImageFile.
        timelinePane.setTransferHandler(new TransferHandler() {
            @Override
            public boolean canImport(TransferSupport support) {
                // Only accept drops if the DataFlavor matches ImageFile.
                return support.isDataFlavorSupported(ImagePanel.DATA_FLAVOR);
            }

            @Override
            public boolean importData(TransferSupport support) {
                if (!canImport(support)) {
                    return false;
                }
                try {
                    // Extract the ImageFile from the transferable data.
                    Transferable transferable = support.getTransferable();
                    ImageFile droppedImageFile = (ImageFile) transferable.getTransferData(ImagePanel.DATA_FLAVOR);

                    //// Here you would add the logic to transfer the ImageFile to the timelinePane.
                    //// For demonstration, we'll add a label representing the dropped image.
                    // JLabel droppedLabel = new JLabel(droppedImageFile.getFileName());
                    // droppedLabel.setForeground(new Color(0xEBDBB2));

                    //// Revalidate and repaint to display the update.
                    // timelinePane.revalidate();
                    // timelinePane.repaint();
                    return true;
                } catch (UnsupportedFlavorException | java.io.IOException ex) {
                    ex.printStackTrace();
                }
                return false;
            }
        });

        // For each ImagePanel in the images list, set up a TransferHandler and
        // MouseMotionListener for dragging.
        // We assume the ImagePanel items are already added as children of
        // imagesPanelList.
        for (Component comp : imagesPanelList.getComponents()) {
            if (comp instanceof ImagePanel) {
                ImagePanel imagePanel = (ImagePanel) comp;
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

                // Add a MouseMotionListener to initiate the drag when the mouse is dragged.
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
}
