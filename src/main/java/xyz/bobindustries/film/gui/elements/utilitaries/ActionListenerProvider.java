package xyz.bobindustries.film.gui.elements.utilitaries;

import xyz.bobindustries.film.App;
import xyz.bobindustries.film.gui.Workspace;
import xyz.bobindustries.film.gui.elements.dialogs.NewProjectDialog;
import xyz.bobindustries.film.gui.elements.dialogs.OpenProjectDialog;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.beans.PropertyVetoException;

public class ActionListenerProvider {

    /*
     * -- Dialog launchers actions --
     */

    /**
     * Launch a NewProjectDialog and load the project accordingly.
     */
    public static void getNewProjectDialogAction(ActionEvent ignorActionEvent) {
        int result = NewProjectDialog.show(App.getFrame());

        showWorkspace(result);
    }

    /**
     * Launch an OpenProjectDialog and load the project accordingly.
     */
    public static void getOpenProjectDialogAction(ActionEvent ignoredActionEvent) {
        int result = OpenProjectDialog.show(App.getFrame());

        showWorkspace(result);
    }

    /*
     * -- These methods are used for launching frames from the menubar. --
     */

    /**
     * Launch welcomeFrame in the workspace.
     */
    public static void getShowWelcomeFrameAction(ActionEvent ignoredActionEvent) {
        Workspace workspace = Workspace.getInstance();
        JInternalFrame welcomeFrame = workspace.getWelcomeFrame();

        showFrameIfClosed(workspace, welcomeFrame);
    }

    /**
     * Launch imageEditorFrame in the workspace.
     */
    public static void getShowImageEditorFrameAction(ActionEvent ignoredActionEvent) {
        Workspace workspace = Workspace.getInstance();
        JInternalFrame imageEditorFrame = workspace.getImageEditorFrame();

        showFrameIfClosed(workspace, imageEditorFrame);
    }

    /**
     * Launch senarioEditorFrame in the workspace.
     */
    public static void getShowScenarioEditorFrameAction(ActionEvent ignoredActionEvent) {
        Workspace workspace = Workspace.getInstance();
        JInternalFrame scenarioEditorFrame = workspace.getScenarioEditorFrame();

        showFrameIfClosed(workspace, scenarioEditorFrame);
    }

    /**
     * Launch filmVisualizerFrame in the workspace.
     */
    public static void getShowFilmVisualizerFrameAction(ActionEvent ignoredActionEvent) {
        Workspace workspace = Workspace.getInstance();
        JInternalFrame filmVisualizerFrame = workspace.getFilmVisualizerFrame();

        showFrameIfClosed(workspace, filmVisualizerFrame);
    }

    /**
     * Launch aboutFrame in the workspace.
     */
    public static void getShowAboutFrameAction(ActionEvent ignoredActionEvent) {
        Workspace workspace = Workspace.getInstance();
        JInternalFrame aboutFrame = workspace.getAboutFrame();

        showFrameIfClosed(workspace, aboutFrame);
    }

    /*
     * -- Helper methods to prevent code duplication --
     */

    private static void showFrameIfClosed(Workspace workspace, JInternalFrame frame) {
        boolean contains = false;
        for (JInternalFrame f : workspace.getAllFrames()) {
            if (frame == f) {
                contains = true;
                break;
            }
        }

        if (!contains) {
            frame.setVisible(true); /* Re-set image editor frame visible. */
            frame.toFront();

            try {
                frame.setSelected(true);
            } catch (PropertyVetoException e) {
                e.printStackTrace();
            }

            workspace.add(frame);
        } else {
            frame.dispose();
        }
    }

    private static void showWorkspace(int result) {
        if (result == NewProjectDialog.SUCCESS) {
            LoadingWindow loadingWindow = new LoadingWindow("loading project...", 200, 100);

            loadingWindow.setVisible(true);
            loadingWindow.requestFocus();

            SwingWorker<Void, Void> worker = new SwingWorker<>() {
                @Override
                protected Void doInBackground() throws Exception {
                    /* Changement du contenu de la fenetre */
                    App.getFrame().getContentPane().removeAll();
                    App.getFrame().add(Workspace.newInstance());

                    Thread.sleep(2000);
                    App.getFrame().revalidate();

                    return null;
                }

                protected void done() {
                    loadingWindow.dispose();
                }
            };
            worker.execute();
        }
    }

}
