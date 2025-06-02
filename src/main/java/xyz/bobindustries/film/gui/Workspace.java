package xyz.bobindustries.film.gui;

import xyz.bobindustries.film.App;
import xyz.bobindustries.film.model.tools.ToolsSettings;
import xyz.bobindustries.film.gui.elements.ColorBox;
import xyz.bobindustries.film.gui.elements.NoSettings;
import xyz.bobindustries.film.gui.elements.ToolBoxUI;
import xyz.bobindustries.film.gui.elements.ToolsSettingsUI;
import xyz.bobindustries.film.gui.elements.menubars.WorkspaceMenuBar;
import xyz.bobindustries.film.gui.elements.utilitaries.Bob;
import xyz.bobindustries.film.gui.elements.utilitaries.ConstantsProvider;
import xyz.bobindustries.film.gui.elements.utilitaries.SimpleErrorDialog;
import xyz.bobindustries.film.gui.panes.AboutPane;
import xyz.bobindustries.film.gui.panes.ProjectWelcomePane;
import xyz.bobindustries.film.gui.panes.ScenarioEditorPane;
import xyz.bobindustries.film.gui.panes.VisualizerPane;
import xyz.bobindustries.film.projects.elements.exceptions.InvalidScenarioContentException;
import xyz.bobindustries.film.gui.panes.*;

import javax.swing.*;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;
import java.awt.*;
import java.beans.PropertyVetoException;

/**
 * Classe Workspace permettant l'affichage des multiples outils d'editions du
 * film comme l'utilisateur le souhaite.
 */
public class Workspace extends JDesktopPane {

    /**
     * Internal class BoundedDesktopManager allowing us to prevent the user from
     * dragging frames outside the workspace.
     */
    static class BoundedDesktopManager extends DefaultDesktopManager {
        @Override
        public void beginDraggingFrame(JComponent f) {
            // Don't do anything. Needed to prevent the DefaultDesktopManager setting the
            // dragMode
        }

        @Override
        public void beginResizingFrame(JComponent f, int direction) {
            // Don't do anything. Needed to prevent the DefaultDesktopManager setting the
            // dragMode
        }

        @Override
        public void setBoundsForFrame(JComponent f, int newX, int newY, int newWidth, int newHeight) {
            boolean didResize = (f.getWidth() != newWidth || f.getHeight() != newHeight);
            if (!inBounds((JInternalFrame) f, newX, newY, newWidth, newHeight)) {
                Container parent = f.getParent();
                Dimension parentSize = parent.getSize();
                int boundedX = (int) Math.min(Math.max(0, newX), parentSize.getWidth() - newWidth);
                int boundedY = (int) Math.min(Math.max(0, newY), parentSize.getHeight() - newHeight);
                f.setBounds(boundedX, boundedY, newWidth, newHeight);
            } else {
                f.setBounds(newX, newY, newWidth, newHeight);
            }
            if (didResize) {
                f.validate();
            }
        }

        protected boolean inBounds(JInternalFrame f, int newX, int newY, int newWidth, int newHeight) {
            if (newX < 0 || newY < 0)
                return false;
            if (newX + newWidth > f.getDesktopPane().getWidth())
                return false;
            return newY + newHeight <= f.getDesktopPane().getHeight();
        }
    }

    private static Workspace instance;

    private WorkspaceMenuBar menubar;
    private final BoundedDesktopManager desktopManager;
    private final JInternalFrame editorToolbox;
    private final JInternalFrame editorColors;
    private final JInternalFrame toolsSettings;

    private final JInternalFrame welcomeFrame,
            imageEditorFrame,
            scenarioEditorFrame,
            filmVisualizerFrame,
            aboutFrame;

    private final ScenarioEditorPane scenarioEditorPane;

    private final Bob bob;

    public JInternalFrame getWelcomeFrame() {
        return welcomeFrame;
    }

    public JInternalFrame getImageEditorFrame() {
        return imageEditorFrame;
    }

    public JInternalFrame getScenarioEditorFrame() {
        return scenarioEditorFrame;
    }

    public JInternalFrame getFilmVisualizerFrame() {
        return filmVisualizerFrame;
    }

    public JInternalFrame getAboutFrame() {
        return aboutFrame;
    }

    public ScenarioEditorPane getScenarioEditorPane() {
        return scenarioEditorPane;
    }

    public JInternalFrame getEditorToolbox() {
        return editorToolbox;
    }

    public JInternalFrame getEditorColors() {
        return editorColors;
    }

    public JInternalFrame getToolsSettings() {
        return toolsSettings;
    }

    public Workspace() throws InvalidScenarioContentException {

        desktopManager = new BoundedDesktopManager();
        setDesktopManager(desktopManager);

        menubar = new WorkspaceMenuBar();

        App.getFrame().setJMenuBar(menubar);

        setLayout(null);

        /*
         * Bob background
         */
        bob = new Bob();

        welcomeFrame = new JInternalFrame(
                "welcome",
                false,
                true,
                true,
                false);
        welcomeFrame.setSize(ConstantsProvider.IFRAME_MIN_SIZE);
        welcomeFrame.setMinimumSize(ConstantsProvider.IFRAME_MIN_SIZE);
        welcomeFrame.setContentPane(new ProjectWelcomePane());
        try {
            welcomeFrame.setMaximum(true);
        } catch (PropertyVetoException e) {
            SimpleErrorDialog.show("Error maximizing welcome frame :(");
        }

        welcomeFrame.setVisible(true);
        welcomeFrame.toFront();
        try {
            welcomeFrame.setSelected(true);
        } catch (PropertyVetoException e) {
            System.err.println(e.getMessage());
        }

        add(welcomeFrame);
        setActiveFrame(welcomeFrame);

        imageEditorFrame = new JInternalFrame(
                "image editor",
                true,
                true,
                true,
                false);
        imageEditorFrame.setSize(ConstantsProvider.IFRAME_MIN_SIZE);
        imageEditorFrame.setMinimumSize(ConstantsProvider.IFRAME_MIN_SIZE);
        imageEditorFrame.setContentPane(new OpenEditor(imageEditorFrame));

        scenarioEditorFrame = new JInternalFrame(
                "scenario editor",
                true,
                true,
                true,
                false);
        scenarioEditorFrame.setSize(ConstantsProvider.IFRAME_MIN_SIZE);
        scenarioEditorFrame.setMinimumSize(ConstantsProvider.IFRAME_MIN_SIZE);

        scenarioEditorPane = new ScenarioEditorPane();

        scenarioEditorFrame.setContentPane(scenarioEditorPane);

        filmVisualizerFrame = new JInternalFrame(
                "film visualizer",
                true,
                true,
                true,
                false);
        filmVisualizerFrame.setSize(ConstantsProvider.IFRAME_MIN_SIZE);
        filmVisualizerFrame.setMinimumSize(ConstantsProvider.IFRAME_MIN_SIZE);
        VisualizerPane visualizerPane = new VisualizerPane(scenarioEditorPane);
        filmVisualizerFrame.setContentPane(visualizerPane);

        aboutFrame = new JInternalFrame(
                "about",
                false,
                true,
                false,
                false);
        aboutFrame.setSize(ConstantsProvider.IFRAME_MIN_SIZE);
        aboutFrame.setMinimumSize(ConstantsProvider.IFRAME_MIN_SIZE);
        aboutFrame.setContentPane(new AboutPane());

        editorToolbox = new JInternalFrame(
                "tools",
                false,
                true,
                false,
                false);
        editorToolbox.setContentPane(new ToolBoxUI());
        editorToolbox.pack();

        editorColors = new JInternalFrame(
                "tools",
                false,
                true,
                false,
                false);
        editorColors.setContentPane(new ColorBox(editorColors));
        editorColors.pack();

        toolsSettings = new JInternalFrame(
                "tools settings",
                false,
                true,
                false,
                false);
        // ToolsSettings.setContentPane(new ToolsSettingsUI(ToolsSettings, 0, 100, 20));
        toolsSettings.setContentPane(new NoSettings());
        toolsSettings.pack();

        editorToolbox.addInternalFrameListener(new InternalFrameAdapter() {
            @Override
            public void internalFrameActivated(InternalFrameEvent e) {
                menubar.displayEditorOptions();
                menubar.revalidate();
                menubar.repaint();
            }

            @Override
            public void internalFrameDeactivated(InternalFrameEvent e) {
                menubar.displayDefaultOptions();
                menubar.revalidate();
                menubar.repaint();
            }
        });

        editorColors.addInternalFrameListener(new InternalFrameAdapter() {
            @Override
            public void internalFrameActivated(InternalFrameEvent e) {
                menubar.displayEditorOptions();
                menubar.revalidate();
                menubar.repaint();
            }

            @Override
            public void internalFrameDeactivated(InternalFrameEvent e) {
                menubar.displayDefaultOptions();
                menubar.revalidate();
                menubar.repaint();
            }
        });

        toolsSettings.addInternalFrameListener(new InternalFrameAdapter() {
            @Override
            public void internalFrameActivated(InternalFrameEvent e) {
                menubar.displayEditorOptions();
                menubar.revalidate();
                menubar.repaint();
            }

            @Override
            public void internalFrameDeactivated(InternalFrameEvent e) {
                menubar.displayDefaultOptions();
                menubar.revalidate();
                menubar.repaint();
            }
        });

        imageEditorFrame.addInternalFrameListener(new InternalFrameAdapter() {
            @Override
            public void internalFrameActivated(InternalFrameEvent e) {
                menubar.displayEditorOptions();
                menubar.revalidate();
                menubar.repaint();
                editorToolbox.toFront();
                editorColors.toFront();
                toolsSettings.toFront();
            }

            @Override
            public void internalFrameDeactivated(InternalFrameEvent e) {
                menubar.displayDefaultOptions();
                menubar.revalidate();
                menubar.repaint();
            }

            @Override
            public void internalFrameClosing(InternalFrameEvent e) {
                int result = JOptionPane.showConfirmDialog(
                        imageEditorFrame,
                        "Voulez-vous vraiment fermer l'éditeur d'image ? Les modifications non enregistrées seront perdues",
                        "Confirmation",
                        JOptionPane.YES_NO_OPTION);
                if (result == JOptionPane.NO_OPTION) {
                    // Annule la fermeture
                    imageEditorFrame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
                } else {
                    imageEditorFrame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
                    editorColors.dispose();
                    editorToolbox.dispose();
                    toolsSettings.dispose();
                    imageEditorFrame.setContentPane(new OpenEditor(imageEditorFrame));
                }
            }
        });
    }

    public void updateToolsSettings(ToolsSettings ts) {
        if (ts != null) {
            int[] sliderParams = ts.getSliderBounds();
            try {
                ToolsSettingsUI tsui = (ToolsSettingsUI) toolsSettings.getContentPane();
                tsui.setSlider(sliderParams[0], sliderParams[1], sliderParams[2]);

            } catch (ClassCastException e) {
                ToolsSettingsUI tsui = new ToolsSettingsUI(toolsSettings, sliderParams[0], sliderParams[1],
                        sliderParams[2]);
                toolsSettings.setContentPane(tsui);
            }
        } else {
            toolsSettings.setContentPane(new NoSettings());
        }
        toolsSettings.pack();
    }

    public static Workspace getInstance() {
        assert (instance != null);

        return instance;
    }

    public static Workspace newInstance() throws InvalidScenarioContentException {
        /* Return a clean instance of the workspace */
        instance = new Workspace();
        return instance;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        int bobWidth = bob.getPreferredSize().width;
        int bobHeigth = bob.getPreferredSize().height;

        int x = (getWidth() - bobWidth) / 2;
        int y = (getHeight() - bobHeigth) / 2;

        Graphics g2 = g.create();
        g2.translate(x, y);
        bob.paintComponent(g2);

        g2.dispose();
    }

    public Color getSelectedColor() {
        return ((ColorBox) instance.getEditorColors().getContentPane()).getSelectedColor();
    }

    public void setActiveFrame(JInternalFrame frame) {
        desktopManager.activateFrame(frame);
    }

}
