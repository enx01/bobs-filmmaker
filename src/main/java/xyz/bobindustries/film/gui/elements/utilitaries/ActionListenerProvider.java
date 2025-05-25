package xyz.bobindustries.film.gui.elements.utilitaries;

import xyz.bobindustries.film.App;
import xyz.bobindustries.film.gui.Workspace;
import xyz.bobindustries.film.gui.elements.dialogs.NewProjectDialog;
import xyz.bobindustries.film.gui.elements.dialogs.OpenProjectDialog;
import xyz.bobindustries.film.gui.elements.dialogs.YesNoDialog;
import xyz.bobindustries.film.gui.panes.ScenarioEditorPane;
import xyz.bobindustries.film.gui.panes.WelcomePane;
import xyz.bobindustries.film.projects.ProjectManager;
import xyz.bobindustries.film.projects.elements.Project;
import xyz.bobindustries.film.projects.elements.exceptions.InvalidScenarioContentException;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.beans.PropertyVetoException;
import java.io.IOException;

public class ActionListenerProvider {

    /*
     * -- Dialog launchers actions --
     */

    /**
     * Launch a NewProjectDialog and load the project accordingly.
     */
    public static void getNewProjectDialogAction(ActionEvent ignoredActionEvent) {
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

    /**
     * Saves the current project by extracting ScenarioEditorPane's state into
     * scenario.txt
     * as well as saving the edited images.
     */
    public static void saveCurrentProject(ActionEvent ignoredActionEvent) {
        LoadingWindow loadingWindow = new LoadingWindow("saving project...", 200, 100);

        loadingWindow.setVisible(true);
        loadingWindow.requestFocus();

        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() {
                Workspace workspace = Workspace.getInstance();
                Project project = ProjectManager.getCurrent();
                ScenarioEditorPane sep = workspace.getScenarioEditorPane();

                project.setScenarioContent(sep.extractScenarioContent());

                try {
                    project.save();
                } catch (IOException ioe) {
                    SimpleErrorDialog.show("Failed to save project :(");
                }

                return null;
            }

            protected void done() {
                SimpleHappyDialog.show("Project saved succesfully !");
                loadingWindow.dispose();
            }
        };
        worker.execute();

    }

    public static void saveCurrentProjectWithoutSuccessFeedback(ActionEvent ignoredActionEvent) {
        LoadingWindow loadingWindow = new LoadingWindow("saving project...", 200, 100);

        loadingWindow.setVisible(true);
        loadingWindow.requestFocus();

        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() {
                Workspace workspace = Workspace.getInstance();
                Project project = ProjectManager.getCurrent();
                ScenarioEditorPane sep = workspace.getScenarioEditorPane();

                project.setScenarioContent(sep.extractScenarioContent());

                try {
                    project.save();
                } catch (IOException ioe) {
                    SimpleErrorDialog.show("Failed to save project :(");
                }

                return null;
            }

            protected void done() {
                loadingWindow.dispose();
            }
        };
        worker.execute();
    }

    public static void exportProjectAsVideo(ActionEvent ignoredActionEvent) {
        LoadingWindow loadingWindow = new LoadingWindow("exporting video...", 200, 100);

        loadingWindow.setVisible(true);
        loadingWindow.requestFocus();

        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() {
                Workspace workspace = Workspace.getInstance();
                Project project = ProjectManager.getCurrent();
                ScenarioEditorPane sep = workspace.getScenarioEditorPane();

                project.setScenarioContent(sep.extractScenarioContent());

                try {
                    project.save();
                } catch (IOException ioe) {
                    SimpleErrorDialog.show("Failed to export movie :(" + "\n" + ioe.getMessage());
                }

                try {
                    project.exportAsVideo(sep.getCurrentState());
                } catch (IOException ioe) {
                    SimpleErrorDialog.show("Failed to export movie :(" + "\n" + ioe.getMessage());
                }

                return null;
            }

            protected void done() {
                SimpleHappyDialog.show("Movie exported succesfully !");
                loadingWindow.dispose();
            }
        };
        worker.execute();
    }

    public static void closeCurrentProject(ActionEvent ignoredActionEvent) {
        System.out.println("splash");
        int result = YesNoDialog.show(App.getFrame(), "Would you like to save the project before closing it ?");

        LoadingWindow loadingWindow = new LoadingWindow("closing project...", 200, 100);

        loadingWindow.setVisible(true);
        loadingWindow.requestFocus();

            SwingWorker<Void, Void> worker = new SwingWorker<>() {
                @Override
                protected Void doInBackground() {
                    if (result == YesNoDialog.YES) {
                        Workspace workspace = Workspace.getInstance();
                        Project project = ProjectManager.getCurrent();
                        ScenarioEditorPane sep = workspace.getScenarioEditorPane();

                        project.setScenarioContent(sep.extractScenarioContent());

                        try {
                            project.save();
                        } catch (IOException ioe) {
                            SimpleErrorDialog.show("Failed to save project :(" + "\n" + ioe.getMessage());
                        }
                    }

                    App.getFrame().getContentPane().removeAll();
                    App.getFrame().add(new WelcomePane());
                    App.getFrame().setJMenuBar(null);

                    App.getFrame().revalidate();

                    return null;
                }

                protected void done() {
                    loadingWindow.dispose();
                }
            };
            worker.execute();

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
                System.err.println(e.getMessage());
            }

            workspace.add(frame);
            workspace.setActiveFrame(frame);
        } else {
            frame.dispose();
            workspace.remove(frame);
        }
    }

    private static void showWorkspace(int result) {
        if (result == NewProjectDialog.SUCCESS) {
            LoadingWindow loadingWindow = new LoadingWindow("loading project...", 200, 100);

            loadingWindow.setVisible(true);
            loadingWindow.requestFocus();

            SwingWorker<Void, Void> worker = new SwingWorker<>() {
                @Override
                protected Void doInBackground() {
                    /*
                     * Since getting workspace instance throws InvalidScenarioContentException, try
                     * to fetch it here
                     */
                    Workspace instance = null;
                    try {
                        instance = Workspace.newInstance();
                    } catch (InvalidScenarioContentException isce) {
                        SimpleErrorDialog.show(isce.getMessage());
                    }

                    if (instance != null) {
                        /* Changement du contenu de la fenetre */
                        App.getFrame().getContentPane().removeAll();
                        App.getFrame().add(instance);

                        App.getFrame().revalidate();
                    } else {
                        SimpleErrorDialog.show("Failed to load project :(");
                    }
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
