package xyz.bobindustries.film.gui;

import xyz.bobindustries.film.App;
import xyz.bobindustries.film.gui.elements.menubars.WorkspaceMenuBar;
import xyz.bobindustries.film.gui.elements.utilitaries.SimpleErrorDialog;
import xyz.bobindustries.film.gui.panes.AboutPane;
import xyz.bobindustries.film.gui.panes.ProjectWelcomePane;

import javax.swing.*;
import java.awt.*;
import java.beans.PropertyVetoException;

/**
 *  Classe Workspace permettant l'affichage des multiples outils d'editions du film comme l'utilisateur le souhaite.
 * */
public class Workspace extends JDesktopPane {
    private static Workspace instance;

    private final JInternalFrame
            welcomeFrame,
            imageEditorFrame,
            scenarioEditorFrame,
            filmVisualizerFrame,
            aboutFrame;

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


    static class BoundedDesktopManager extends DefaultDesktopManager {
        @Override
        public void beginDraggingFrame(JComponent f) {
            // Don't do anything. Needed to prevent the DefaultDesktopManager setting the dragMode
        }

        @Override
        public void beginResizingFrame(JComponent f, int direction) {
            // Don't do anything. Needed to prevent the DefaultDesktopManager setting the dragMode
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
            if(didResize) {
                f.validate();
            }
        }

        protected boolean inBounds(JInternalFrame f, int newX, int newY, int newWidth, int newHeight) {
            if (newX < 0 || newY < 0) return false;
            if (newX + newWidth > f.getDesktopPane().getWidth()) return false;
            return newY + newHeight <= f.getDesktopPane().getHeight();
        }
    }

    public Workspace() {
        setDesktopManager(new BoundedDesktopManager());

        App.getFrame().setJMenuBar(new WorkspaceMenuBar());

        welcomeFrame = new JInternalFrame(
                "welcome",
                false,
                true,
                true,
                true
        );
        welcomeFrame.setSize(600, 300);
        welcomeFrame.setMinimumSize(new Dimension(600,300));
        welcomeFrame.setContentPane(new ProjectWelcomePane());
        try {
            welcomeFrame.setMaximum(true);
        } catch (PropertyVetoException e) {
            SimpleErrorDialog.showErrorDialog("Error maximizing welcome frame :(");
        }
        welcomeFrame.setVisible(true);

        imageEditorFrame = new JInternalFrame(
                "image editor",
                true,
                true,
                true,
                true
        );
        imageEditorFrame.setSize(600, 300);
        imageEditorFrame.setMinimumSize(new Dimension(600,300));
        imageEditorFrame.setContentPane(new JPanel());

        scenarioEditorFrame = new JInternalFrame(
                "scenario editor",
                true,
                true,
                true,
                true
        );
        scenarioEditorFrame.setSize(600, 300);
        scenarioEditorFrame.setMinimumSize(new Dimension(600,300));
        scenarioEditorFrame.setContentPane(new JPanel());

        filmVisualizerFrame = new JInternalFrame(
                "film visualizer",
                true,
                true,
                true,
                true
        );
        filmVisualizerFrame.setSize(600, 300);
        filmVisualizerFrame.setMinimumSize(new Dimension(600,300));
        filmVisualizerFrame.setContentPane(new JPanel());

        aboutFrame = new JInternalFrame(
                "about",
                false,
                true,
                false,
                false
        );
        aboutFrame.setSize(600, 300);
        aboutFrame.setMinimumSize(new Dimension(600,300));
        aboutFrame.setContentPane(new AboutPane());
        aboutFrame.setVisible(true);


        add(welcomeFrame);
    }

    public static Workspace getInstance() {
        // Create the instance if it doesn't exist
        if (instance == null) {
            instance = new Workspace();
        }
        return instance;
    }

}
