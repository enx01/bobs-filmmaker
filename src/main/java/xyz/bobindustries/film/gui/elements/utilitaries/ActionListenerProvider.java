package xyz.bobindustries.film.gui.elements.utilitaries;

import xyz.bobindustries.film.App;
import xyz.bobindustries.film.gui.Workspace;
import xyz.bobindustries.film.gui.elements.dialogs.NewProjectDialog;
import xyz.bobindustries.film.gui.elements.dialogs.OpenProjectDialog;
import xyz.bobindustries.film.gui.panes.ProjectWelcomePane;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.beans.PropertyVetoException;

public class ActionListenerProvider {

    public static void getNewProjectDialogAction(ActionEvent actionEvent) {
        int result = NewProjectDialog.show(App.getFrame());

        showWorkspace(result);
    }

    public static void getOpenProjectDialogAction(ActionEvent actionEvent) {
        int result = OpenProjectDialog.show(App.getFrame());

        showWorkspace(result);
    }

    public static void getShowImageEditorFrameAction(ActionEvent actionEvent) {
        Workspace workspace = Workspace.getInstance();
        JInternalFrame imageEditorFrame = workspace.getImageEditorFrame();

        showFrameIfClosed(workspace, imageEditorFrame);
    }

    public static void getShowAboutFrameAction(ActionEvent actionEvent) {
        Workspace workspace = Workspace.getInstance();
        JInternalFrame aboutFrame = workspace.getAboutFrame();

        showFrameIfClosed(workspace, aboutFrame);
    }

    private static void showFrameIfClosed(Workspace workspace, JInternalFrame aboutFrame) {
        boolean contains = false;
        for (JInternalFrame frame : workspace.getAllFrames()) {
            if (frame == aboutFrame) {
                contains = true;
                break;
            }
        }

        if (!contains) {
            aboutFrame.setVisible(true); /* Re-set image editor frame visible. */
            aboutFrame.toFront();

            workspace.add(aboutFrame);
        }
    }


    private static void showWorkspace(int result) {
        if (result == NewProjectDialog.SUCCESS) {
            LoadingWindow loadingWindow = new LoadingWindow("loading project...", 200, 100);

            loadingWindow.setVisible(true);
            loadingWindow.requestFocus();

            SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
                @Override
                protected Void doInBackground() throws Exception {
                    /* Changement du contenu de la fenetre */
                    App.getFrame().getContentPane().removeAll();
                    App.getFrame().add(new Workspace());

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
