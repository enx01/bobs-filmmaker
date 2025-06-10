package xyz.bobindustries.film.gui.panes;

import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.*;
import javax.swing.border.Border;

import xyz.bobindustries.film.App;
import xyz.bobindustries.film.gui.elements.contextualmenu.ContextualMenu;
import xyz.bobindustries.film.gui.elements.dialogs.BrokenProjectRecoveryDialog;
import xyz.bobindustries.film.gui.elements.dialogs.YesNoDialog;
import xyz.bobindustries.film.gui.elements.popups.SimpleValueChangerPopUp;
import xyz.bobindustries.film.gui.elements.utilitaries.ActionListenerProvider;
import xyz.bobindustries.film.gui.elements.utilitaries.SimpleErrorDialog;
import xyz.bobindustries.film.gui.helpers.Pair;
import xyz.bobindustries.film.gui.panes.ScenarioEditorPane.TimelinePane.TimelineElement;
import xyz.bobindustries.film.projects.ProjectManager;
import xyz.bobindustries.film.projects.elements.ImageFile;
import xyz.bobindustries.film.projects.elements.Project;
import xyz.bobindustries.film.projects.elements.exceptions.CouldntDeleteImageException;
import xyz.bobindustries.film.projects.elements.exceptions.ImageNotFoundInDirectoryException;
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
            setPreferredSize(new Dimension(350, 40));
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
            setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY, 1, true));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            String text = ProjectManager.getCurrent().getToolTipImageText(imageFile);
            setToolTipText(text);
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

        private static final int PIXELS_PER_SECOND = 300; // How many pixels represent 1 second

        class TimelineElement extends JPanel {

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
                    } else {
                        if (pane.getCurrentSelectedItem() == TimelineElement.this) {
                            pane.setCurrentSelectedItem(null);
                        } else {
                            pane.setCurrentSelectedItem(TimelineElement.this);
                        }
                    }

                    ScenarioEditorPane.this.setCurrentImageView(
                            currentSelectedItem != null ? currentSelectedItem.getData()
                                    : null);
                }

                @Override
                public void mouseDragged(MouseEvent e) {
                    if (isDragging) {
                        int currentX = e.getXOnScreen();
                        int deltaX = currentX - dragStartX;

                        double deltaTime = (double) deltaX / PIXELS_PER_SECOND;

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

            private static final double DEFAULT_TIME = 0.25;
            private static final double MIN_TIME = 0.10;
            private static final double MAX_TIME = 1.0;
            private static final int DEFAULT_HEIGHT = 50;
            private static final int ARC_WIDTH = 15;
            private static final int ARC_HEIGHT = 15;
            private static final int HANDLE_WIDTH = 3;
            private static final int HANDLE_PADDING = 3; // Padding from the right edge

            private double time; // Duration of this element in seconds
            private final Color backgroundColor = new Color(0x25, 0x65, 0x68); // Default background color
            private final Color handleColor = new Color(0x68, 0x9d, 0x6a); // Handle color
            private final Color textColor = new Color(0xeb, 0xdb, 0xb2); // Text color

            private boolean isDragging = false;
            private int dragStartX;
            private double timeAtDragStart;

            private final ImageFile data;

            TimelineElement(TimelinePane timelinePane, ImageFile imageFile) {
                setTime(DEFAULT_TIME);
                this.data = imageFile;
                updatePreferredSize();

                // Add mouse listeners for dragging the handle
                HandleDragListener listener = new HandleDragListener(timelinePane);

                addMouseListener(listener);
                addMouseMotionListener(listener);

                ContextualMenu contxtMenu = new ContextualMenu.Builder()
                        .addItem(imageFile.getFileName(), null)
                        .addSeparator()
                        .addItem("remove element", e -> timelinePane.removeTimelineElement(this))
                        .addItem("change time", e -> {
                            double newTime = SimpleValueChangerPopUp.show(time, App.getFrame());

                            if (newTime >= MIN_TIME && newTime <= MAX_TIME) {
                                time = newTime;
                                timelinePane.updateLayout();
                            }
                        })
                        .addSeparator()
                        .addItem("move to right", e -> timelinePane.moveElementToRight(this))
                        .addItem("move to left", e -> timelinePane.moveElementToLeft(this))
                        .build();

                contxtMenu.attachTo(this);

                // Set opaque to false if you want the parent's background to show through
                setOpaque(false);
            }

            TimelineElement(TimelinePane timelinePane, ImageFile imageFile, double time) {
                this(timelinePane, imageFile);
                setTime(time);
            }

            public double getTime() {
                return time;
            }

            public ImageFile getData() {
                return data;
            }

            public void setTime(double time) {
                this.time = Math.max(MIN_TIME, Math.min(MAX_TIME, time));
                String timeString = String.format("%.2fs", getTime());
                this.setToolTipText(timeString);
                updatePreferredSize();
                revalidate(); // Notify layout manager of size change
                repaint(); // Redraw the component
            }

            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();

                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // int width = getWidth();
                int width = calculatePreferredWidth();
                int height = getHeight();

                setSize(new Dimension(width, height));

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
                int textX = 0;
                int textY = height - fm.getDescent();

                if (textX + textWidth > handleX - HANDLE_PADDING) {
                    textX = handleX - HANDLE_PADDING - textWidth;
                }
                textX = Math.max(5, textX);

                if (textX + textWidth < width - HANDLE_PADDING) {
                    g2d.drawString(timeString, textX, textY);
                }

                String filename = data.getFileName();
                textWidth = fm.stringWidth(filename);
                int textHeight = fm.getAscent();
                textX = (width - textWidth) / 2;
                textY = (height + textHeight) / 2 - fm.getDescent();

                if (textX + textWidth > handleX - HANDLE_PADDING) {
                    textX = handleX - HANDLE_PADDING - textWidth;
                }
                textX = Math.max(5, textX);

                if (textX + textWidth < width - HANDLE_PADDING) {
                    g2d.drawString(filename, textX, textY);
                }

                g2d.dispose();
            }

            // Sizing stuff

            private int calculatePreferredWidth() {
                return (int) (time * PIXELS_PER_SECOND);
            }

            private void updatePreferredSize() {
                setSize(new Dimension(calculatePreferredWidth(), DEFAULT_HEIGHT));
            }

            private Rectangle getHandleBounds() {
                int width = calculatePreferredWidth();
                int height = getHeight();
                int clickableHandleWidth = HANDLE_WIDTH + 2 * HANDLE_PADDING;
                int clickableHandleX = width - clickableHandleWidth;
                return new Rectangle(clickableHandleX, 0, clickableHandleWidth, height);
            }

        }

        private final List<TimelineElement> elements;
        private TimelineElement currentSelectedItem = null;
        private final JScrollPane scrollPane;
        private final JPanel contentPane;
        private final JPanel dummy;

        TimelinePane(String scenarioContent) throws InvalidScenarioContentException, ImageNotFoundInDirectoryException {
            setLayout(new BorderLayout());
            elements = new ArrayList<>();

            // dummy item to act as padding for contentPane
            dummy = new JPanel();
            dummy.setPreferredSize(new Dimension(500, 50));

            Color primaryMarkerColor = new Color(0xeb, 0xdb, 0xb2);
            Color terciaryMarkerColor = new Color(112, 97, 87);
            Color secondaryMarkerColor = new Color(53, 47, 47);

            contentPane = new JPanel() {
                @Override
                public void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    Graphics2D g2d = (Graphics2D) g.create();
                    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                    int width = getWidth();

                    int nbTercieraryMarkers = width / (PIXELS_PER_SECOND / 4);
                    for (int i = 0; i < nbTercieraryMarkers + 1; i++) {
                        int computed = i * (PIXELS_PER_SECOND / 4);
                        g2d.setColor(terciaryMarkerColor);
                        g2d.fillRect(computed, 0, 1, getHeight());
                    }

                    int nbSecondaryMarkers = width / (PIXELS_PER_SECOND / 2);
                    for (int i = 0; i < nbSecondaryMarkers + 1; i++) {
                        int computed = i * (PIXELS_PER_SECOND / 2);
                        g2d.setColor(secondaryMarkerColor);
                        g2d.fillRect(computed, 0, 1, getHeight());
                    }

                    int nbPrimaryMarkers = width / PIXELS_PER_SECOND;
                    for (int i = 0; i < nbPrimaryMarkers + 1; i++) {
                        int computed = i * PIXELS_PER_SECOND;
                        g2d.setColor(primaryMarkerColor);
                        g2d.fillRect(computed, 0, 1, getHeight());
                    }

                }
            };
            contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.X_AXIS));
            contentPane.setBackground(Color.DARK_GRAY);

            scrollPane = new JScrollPane(contentPane);
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

            loadScenarioContent(scenarioContent);
        }

        private void loadScenarioContent(String scenarioContent)
                throws InvalidScenarioContentException, ImageNotFoundInDirectoryException {
            Project current = ProjectManager.getCurrent();
            elements.clear();

            if (current != null) {
                List<Pair<ImageFile, Double>> orderedImages = current.getOrderedImagesWithTime();

                for (Pair<ImageFile, Double> p : orderedImages) {
                    elements.add(new TimelineElement(this, p.key(), p.value()));
                }
            }

            updateLayout();
            updateLayoutOrder();
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

            contentPane.remove(dummy);

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

                int totalWidth = (int) (elem.getTime() * PIXELS_PER_SECOND);
                elem.setPreferredSize(new Dimension(totalWidth, 50));
                elem.setMinimumSize(new Dimension(totalWidth, 50));
                elem.setMaximumSize(new Dimension(totalWidth, 50));

                if (elem == currentSelectedItem)
                    elem.setBorder(BorderFactory.createLineBorder(Color.WHITE, 1, true));
                else
                    elem.setBorder(BorderFactory.createEmptyBorder(0, currentX, 0, 0));
                currentX += totalWidth + gap;
                elem.revalidate();
                elem.repaint();
            }

            contentPane.add(dummy);

            // Refresh layout
            contentPane.revalidate();
            contentPane.repaint();
        }

        /*
         * This method is called whenever a TimelineElement is moved so we need to clear
         * all the components to add them back.
         */
        void updateLayoutOrder() {
            contentPane.removeAll();

            for (TimelineElement elem : elements) {
                contentPane.add(elem);
            }
            // Add the dummy item at the end of contentPane
            contentPane.add(dummy);

            contentPane.revalidate();
            contentPane.repaint();
        }

        void removeTimelineElement(TimelineElement elem) {
            contentPane.remove(elem);
            elements.remove(elem);
            updateLayout();
        }

        void addTimelineElement(ImageFile droppedImageFile) {
            elements.add(new TimelineElement(this, droppedImageFile));
            updateLayout();
        }

        void moveElementToRight(TimelineElement elem) {
            if (elem == null)
                return;
            int index = elements.indexOf(elem);
            if (index < elements.size() - 1) {
                TimelineElement save = elements.get(index + 1);
                elements.set(index + 1, elem);
                elements.set(index, save);
            }
            updateLayoutOrder();
        }

        void moveElementToLeft(TimelineElement elem) {
            if (elem == null)
                return;
            int index = elements.indexOf(elem);
            if (index > 0) {
                TimelineElement save = elements.get(index - 1);
                elements.set(index - 1, elem);
                elements.set(index, save);
            }
            updateLayoutOrder();
        }

        TimelineElement getCurrentSelectedItem() {
            return currentSelectedItem;
        }

        void setCurrentSelectedItem(TimelineElement elem) {
            currentSelectedItem = elem;
            updateLayout();
        }

        void moveSelectedIndexToRight() {
            if (currentSelectedItem == null && !elements.isEmpty()) {
                currentSelectedItem = elements.get(elements.size() - 1);
                return;
            } else {
                int index = elements.indexOf(currentSelectedItem);
                currentSelectedItem = elements.get((index + 1) % elements.size());
            }

            updateLayout();
        }

        void moveSelectedIndexToLeft() {
            if (currentSelectedItem == null && !elements.isEmpty()) {
                currentSelectedItem = elements.get(0);
                return;
            } else {
                int index = elements.indexOf(currentSelectedItem);
                currentSelectedItem = elements.get(((index - 1) % elements.size() + elements.size()) % elements.size());
            }

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

        List<Pair<ImageFile, Double>> getCurrentState() {
            List<Pair<ImageFile, Double>> state = new ArrayList<>();

            for (TimelineElement elem : elements) {
                state.add(new Pair<>(elem.data, elem.time));
            }

            return state;
        }

        int getElementsCount() {
            return elements.size();
        }

        JScrollPane getScrollPane() {
            return scrollPane;
        }
    }

    private JPanel imagesListPane,
            imagesPanelList,
            visualizerPane;
    private TimelinePane timelinePane;
    private JScrollPane imageListScrollPane;
    private JLabel currentImageView;

    /*
     * Keyboard Adapter to allow for keyboard TimelinePane manipulation
     */
    private class KeyboardTimelineListener extends KeyAdapter {

        @Override
        public void keyPressed(KeyEvent e) {
            switch (e.getKeyCode()) {
                case KeyEvent.VK_RIGHT:
                    if (e.isControlDown() && !e.isShiftDown())
                        timelinePane.moveElementToRight(timelinePane.getCurrentSelectedItem());
                    else if (e.isControlDown() && !e.isAltDown()) {
                        timelinePane.getCurrentSelectedItem().setTime(
                                timelinePane.getCurrentSelectedItem().getTime() + 0.05);
                        timelinePane.updateLayout();
                    } else if (e.isControlDown()) {
                        timelinePane.getCurrentSelectedItem().setTime(
                                timelinePane.getCurrentSelectedItem().getTime() + 0.1);
                        timelinePane.updateLayout();
                    } else
                        timelinePane.moveSelectedIndexToRight();
                    break;
                case KeyEvent.VK_LEFT:
                    if (e.isControlDown() && !e.isShiftDown())
                        timelinePane.moveElementToLeft(timelinePane.getCurrentSelectedItem());
                    else if (e.isControlDown() && !e.isAltDown()) {
                        timelinePane.getCurrentSelectedItem().setTime(
                                timelinePane.getCurrentSelectedItem().getTime() - 0.05);
                        timelinePane.updateLayout();
                    } else if (e.isControlDown()) {
                        timelinePane.getCurrentSelectedItem().setTime(
                                timelinePane.getCurrentSelectedItem().getTime() - 0.1);
                        timelinePane.updateLayout();
                    } else
                        timelinePane.moveSelectedIndexToLeft();
                    break;
            }

            if (timelinePane.getCurrentSelectedItem() != null) {
                setCurrentImageView(timelinePane.getCurrentSelectedItem().getData());

                // Ensure the selected item is visible in the scroll view
                Rectangle bounds = timelinePane.getCurrentSelectedItem().getBounds();
                bounds.x += timelinePane.getCurrentSelectedItem().getParent().getLocation().x;
                timelinePane.getScrollPane().getViewport().scrollRectToVisible(bounds);
            }

        }

    }

    private void initializeComponents() throws InvalidScenarioContentException, ImageNotFoundInDirectoryException {
        imagesListPane = new JPanel();
        visualizerPane = new JPanel();
        // visualizerPane.setFocusable(false);

        timelinePane = new TimelinePane(ProjectManager.getCurrent().getScenarioContent());
        imagesPanelList = new JPanel();

        addKeyListener(new KeyboardTimelineListener());

        imagesListPane.setBackground(Color.LIGHT_GRAY);
        // visualizerPane.setBackground(Color.WHITE);
        timelinePane.setBackground(Color.GRAY);

        Border common = BorderFactory.createEtchedBorder(new Color(0x3C3836), new Color(0x7c6f64));
        imagesListPane.setBorder(common);
        visualizerPane.setBorder(common);
        timelinePane.setBorder(common);

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

        timelinePane.setPreferredSize(new Dimension(0, (int) (this.getPreferredSize().height * 0.8)));

        imagesListPane.setPreferredSize(new Dimension((int) (this.getPreferredSize().width * 0.6), 0));

        visualizerPane.add(currentImageView);
    }

    public ScenarioEditorPane() throws InvalidScenarioContentException, ImageNotFoundInDirectoryException {
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
                resizeComponents();
            }

            @Override
            public void componentShown(ComponentEvent e) {
                resizeComponents();
            }

            @Override
            public void componentHidden(ComponentEvent e) {
                resizeComponents();
            }
        });

        // VOODOO : Ignore.
        for (int i = 0; i < timelinePane.getElementsCount(); i++) {
            timelinePane.moveSelectedIndexToRight();
            setCurrentImageView(timelinePane.getCurrentSelectedItem().getData());
        }

        setCurrentImageView(null);
        timelinePane.setCurrentSelectedItem(null);

        enableDragAndDrop();

        // visualizerPane.removeAll();
        // remove(visualizerPane);
        // visualizerPane = new VisualizerPane(this);
        // visualizerPane.setFocusable(false);
        // add(visualizerPane);
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
                    SimpleErrorDialog.show(ex.getMessage());
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
                        if (SwingUtilities.isLeftMouseButton(e)) {
                            JComponent c = (JComponent) e.getSource();
                            TransferHandler handler = c.getTransferHandler();
                            handler.exportAsDrag(c, e, TransferHandler.COPY);
                        }
                    }
                });
            }
        }
    }

    private void resizeComponents() {
        Dimension currentSize = getSize();

        imagesListPane.setPreferredSize(new Dimension((int) (currentSize.width *
                0.3), currentSize.height));

        timelinePane.updateLayout();
        timelinePane.setPreferredSize(new Dimension(0, (int) (currentSize.height *
                0.4)));

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                TimelineElement current = timelinePane.getCurrentSelectedItem();
                if (current != null)
                    setCurrentImageView(current.getData());
            }
        });
        revalidate();
        repaint();
    }

    public void refresh() {
        populateImageList();
        timelinePane.updateLayoutOrder();
        timelinePane.updateLayout();
    }

    private void populateImageList() {
        imagesPanelList.removeAll();
        Project current = ProjectManager.getCurrent();

        if (current != null) {
            List<ImageFile> images = current.getImages();

            for (ImageFile imf : images) {
                ImagePanel imagePanel = new ImagePanel(imf);

                ContextualMenu imgPanelContxt = new ContextualMenu.Builder()

                        .addItem("insert into timeline", e -> timelinePane.addTimelineElement(imf))
                        .addItem("send to image editor", e -> {
                            ActionListenerProvider.ShowImageEditorFrame(imf);
                        })
                        .addSeparator()
                        .addItem("delete image", e -> {
                            int userResponse = YesNoDialog.show(App.getFrame(),
                                    "<html><body>do you really want to delete this image?<br><br>"
                                            + imf.getFileName()
                                            + "<br><br>as this will delete its corresponding file<br>as well as all its occurences in scenario.txt."
                                            + "</body></html>",
                                    false);

                            if (userResponse == YesNoDialog.YES) {
                                Project curProject = ProjectManager.getCurrent();

                                try {
                                    curProject.deleteImage(imf);
                                } catch (CouldntDeleteImageException | IOException ex) {
                                    SimpleErrorDialog.show(ex.getMessage());
                                }

                                populateImageList();

                                try {
                                    timelinePane.loadScenarioContent(curProject.getScenarioContent());
                                } catch (InvalidScenarioContentException ex) {
                                    throw new RuntimeException(ex);
                                } catch (ImageNotFoundInDirectoryException ex) {
                                    BrokenProjectRecoveryDialog.show(ex);
                                }
                            }
                        })
                        .build();

                imgPanelContxt.attachTo(imagePanel);

                imagesPanelList.add(imagePanel);
            }
        }

        imagesPanelList.revalidate();
        imagesPanelList.repaint();
    }

    private void setCurrentImageView(ImageFile imageFile) {
        if (imageFile != null && imageFile.getImage() != null) {
            // int panelH, panelW;
            // Dimension pref = visualizerPane.getSize();
            // ImageIcon scaledIcon = new ImageIcon(scaleImage(
            // imageFile.getImage(),
            // currentImageView.getPref(),
            // currentImageView.getHeight()));
            // currentImageView.setIcon(scaledIcon);
            BufferedImage img = imageFile.getBufferedImage();

            int panelW = visualizerPane.getWidth() > 0 ? visualizerPane.getWidth() : 400;
            int panelH = visualizerPane.getHeight() > 0 ? visualizerPane.getHeight() : 300;
            Image scaledImg = img.getScaledInstance(-1, panelH, Image.SCALE_SMOOTH);
            if (scaledImg.getWidth(null) > panelW) {
                scaledImg = img.getScaledInstance(panelW, -1, Image.SCALE_SMOOTH);
            }
            currentImageView.setIcon(new ImageIcon(scaledImg));
            currentImageView.setText(null);

        } else {
            currentImageView.setIcon(null);
        }

    }

    public String extractScenarioContent() {
        return timelinePane.getScenarioFileContent();
    }

    public List<Pair<ImageFile, Double>> getCurrentState() {
        return timelinePane.getCurrentState();
    }
}
