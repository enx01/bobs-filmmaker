package xyz.bobindustries.film.gui.elements.utilitaries;

import xyz.bobindustries.film.App;
import xyz.bobindustries.film.model.tools.ToolsList;
import xyz.bobindustries.film.gui.Workspace;
import xyz.bobindustries.film.gui.elements.ToolBoxUI;
import xyz.bobindustries.film.gui.elements.dialogs.*;
import xyz.bobindustries.film.gui.helpers.Pair;
import xyz.bobindustries.film.gui.elements.dialogs.NewProjectDialog;
import xyz.bobindustries.film.gui.elements.dialogs.OpenProjectDialog;
import xyz.bobindustries.film.gui.elements.dialogs.YesNoDialog;
import xyz.bobindustries.film.gui.elements.popups.HelperBobPopUp;

import xyz.bobindustries.film.gui.panes.ScenarioEditorPane;
import xyz.bobindustries.film.gui.panes.WelcomePane;
import xyz.bobindustries.film.projects.ConfigProvider;
import xyz.bobindustries.film.projects.ProjectManager;
import xyz.bobindustries.film.projects.elements.ImageFile;
import xyz.bobindustries.film.projects.elements.Project;
import xyz.bobindustries.film.projects.elements.exceptions.ImageNotFoundInDirectoryException;
import xyz.bobindustries.film.projects.elements.exceptions.InvalidScenarioContentException;
import xyz.bobindustries.film.gui.panes.*;
import xyz.bobindustries.film.utils.ImageUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.beans.PropertyVetoException;
import java.io.IOException;
import java.util.ArrayList;

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
     * Launch an OpenProjectDialog and load the project accordingly.
     */
    public static void getRecentProjectDialogAction(ActionEvent ignoredActionEvent) {
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
            protected Void doInBackground() throws InterruptedException {
                Workspace workspace = Workspace.getInstance();
                Project project = ProjectManager.getCurrent();
                ScenarioEditorPane sep = workspace.getScenarioEditorPane();

                project.setScenarioContent(sep.extractScenarioContent());

                try {
                    project.save();
                } catch (IOException ioe) {
                    SimpleErrorDialog.show("Failed to save project :(");
                }

                Thread.sleep(300);

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
        int userChoice;
        if (ProjectManager.getCurrent().isDirty()) {
            userChoice = YesNoDialog.show(App.getFrame(), "would you like to save the project before closing it ?",
                    true);
        } else {
            userChoice = YesNoDialog.NO;
        }

        int result = userChoice;

        LoadingWindow loadingWindow = new LoadingWindow("closing project...", 200, 100);

        loadingWindow.setVisible(true);
        loadingWindow.requestFocus();

        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() {
                if (result == YesNoDialog.CANCEL)
                    return null;
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

                ProjectManager.setCurrent(null);

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

    public static void ShowImageEditorFrame(ImageFile imf) {
        Workspace current_ws = Workspace.getInstance();
        JInternalFrame ief = current_ws.getImageEditorFrame();
        try {
            EditorPane pane = (EditorPane) ief.getContentPane();
            pane.addNewImage(imf.getColorMatrix(), imf.getFileName());
        } catch (ClassCastException ccex) {
            EditorPane pane = ActionListenerProvider.openImageEditorOpening(ief, imf.getColorMatrix(),
                    imf.getFileName());
            ief.setContentPane(pane);
            showFrameIfClosed(current_ws, ief);
        }
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

    /**
     *
     */
    public static void createImageAction(ActionEvent ignorActionEvent) {
        Workspace workspace = Workspace.getInstance();
        JInternalFrame editorFrame = workspace.getImageEditorFrame();

        EditorPane editor = openImageEditorCreation(editorFrame);
        editorFrame.setContentPane(editor);
    }

    /**
     *
     */
    public static void openImageAction(ActionEvent ignorActionEvent) {
        Workspace workspace = Workspace.getInstance();
        JInternalFrame editorFrame = workspace.getImageEditorFrame();

        Pair<Color[][], String> result = OpenImageDialog.show(App.getFrame());

        if (result != null) {
            EditorPane editor = openImageEditorOpening(editorFrame, result.key(), result.value());
            editorFrame.setContentPane(editor);
        }
    }

    public static void openEditorToolbox(ActionEvent ignorActionEvent) {
        Workspace workspace = Workspace.getInstance();
        JInternalFrame toolboxFrame = workspace.getEditorToolbox();

        showFrameIfClosed(workspace, toolboxFrame);

        try {
            toolboxFrame.setSelected(true);
            toolboxFrame.toFront();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void openEditorColorBox(ActionEvent ignoredActionEvent) {
        Workspace workspace = Workspace.getInstance();
        JInternalFrame colorboxframe = workspace.getEditorColors();

        showFrameIfClosed(workspace, colorboxframe);

        try {
            colorboxframe.setSelected(true);
            colorboxframe.toFront();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void openEditorToolsSettings(ActionEvent ignoredActionEvent) {
        Workspace workspace = Workspace.getInstance();
        JInternalFrame toolsSettingsFrame = workspace.getToolsSettings();

        showFrameIfClosed(workspace, toolsSettingsFrame);

        try {
            toolsSettingsFrame.setSelected(true);
            toolsSettingsFrame.toFront();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void setTool(ActionEvent e) {
        Workspace workspace = Workspace.getInstance();
        JInternalFrame editorFrame = workspace.getImageEditorFrame();
        EditorPane editor = (EditorPane) editorFrame.getContentPane();
        editor.setSelectedTool(ToolsList.valueOf(e.getActionCommand()));
        JInternalFrame toolboxFrame = workspace.getEditorToolbox();
        ToolBoxUI tui = (ToolBoxUI) toolboxFrame.getContentPane();
        tui.setSelectedButton(e.getActionCommand());
    }

    public static void saveImageAction(ActionEvent ignorActionEvent) {
        Workspace workspace = Workspace.getInstance();
        JInternalFrame editorFrame = workspace.getImageEditorFrame();
        EditorPane editor = (EditorPane) editorFrame.getContentPane();
        String projectPath = ProjectManager.getCurrent().getProjectDir().toString();
        String fileName = editor.getCurrentFileName();
        if (fileName.isEmpty() || fileName.contains("/")) {
            fileName = NameImageDialog.show(App.getFrame(), fileName);
            fileName += ".png";
        }
        ImageUtils.exportImage(editor.getData().getGridColors(), projectPath + "/images/" + fileName);
        editor.setFileName(fileName);
        workspace.getScenarioEditorPane().refresh();
    }

    public static void openExistingFrames(ActionEvent ignoredActionEvent) {
        Workspace workspace = Workspace.getInstance();
        JInternalFrame editorFrame = workspace.getImageEditorFrame();
        Project current = ProjectManager.getCurrent();
        ArrayList<String> selectedFrames = new ArrayList<>();
        EditorPane editor = null;
        selectedFrames = OpenExistingFramesDialog.show(App.getFrame(), (ArrayList<ImageFile>) current.getImages());
        if (selectedFrames != null) {
            for (String currentFrame : selectedFrames) {
                if (editor == null) {
                    editor = openImageEditorOpening(editorFrame, ProjectManager.getImageMatrix(currentFrame),
                            currentFrame);
                } else {
                    editor.addNewImage(ProjectManager.getImageMatrix(currentFrame), currentFrame);
                }
            }
            editorFrame.setContentPane(editor);
        }
    }

    /*
     * Methods to launch tutorial popups
     */

    public static void getShowBobTutorialPopup(ActionEvent ignoredActionEvent) {
        HelperBobPopUp.loadTutorial(HelperBobPopUp.BOB_TUTORIAL);
    }

    public static void getShowImageEditorTutorialPopup(ActionEvent ignoredActionEvent) {
        HelperBobPopUp.loadTutorial(HelperBobPopUp.IMAGE_EDITOR_TUTORIAL);
    }

    public static void getShowScenarioEditorTutorialPopup(ActionEvent ignoredActionEvent) {
        HelperBobPopUp.loadTutorial(HelperBobPopUp.SCENARIO_EDITOR_TUTORIAL);
    }

    public static void getShowVisualizerTutorialPopup(ActionEvent ignoredActionEvent) {
        HelperBobPopUp.loadTutorial(HelperBobPopUp.VISUALIZER_TUTORIAL);
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
                        BrokenProjectRecoveryDialog.show(isce);

                        // Assume the project is now repaired
                        try {
                            instance = Workspace.newInstance();
                        } catch (InvalidScenarioContentException | ImageNotFoundInDirectoryException e) {
                            SimpleErrorDialog.show(e.getMessage());
                        }
                    } catch (ImageNotFoundInDirectoryException infide) {
                        BrokenProjectRecoveryDialog.show(infide);

                        // Assume the project is now repaired
                        try {
                            instance = Workspace.newInstance();
                        } catch (InvalidScenarioContentException | ImageNotFoundInDirectoryException e) {
                            SimpleErrorDialog.show(e.getMessage());
                        }
                    }

                    if (instance != null) {
                        /* Changement du contenu de la fenetre */
                        App.getFrame().getContentPane().removeAll();
                        App.getFrame().add(instance);

                        App.getFrame().revalidate();
                    } else {
                        ProjectManager.setCurrent(null);
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

    public static EditorPane openImageEditorCreation(JInternalFrame frame) {
        EditorPane editor = null;
        int height = ConfigProvider.getResolutionHeight(ProjectManager.getCurrent().getConfig());
        int width = ConfigProvider.getResolutionWidth(ProjectManager.getCurrent().getConfig());
        Color[][] defaultCanvas = new Color[height][width];
        for (int i = 0; i < defaultCanvas.length; i += 1) {
            for (int j = 0; j < defaultCanvas[0].length; j += 1) {
                defaultCanvas[i][j] = new Color(255, 255, 255);
            }
        }
        editor = new EditorPane(defaultCanvas, width, height, "");
        return editor;
    }

    public static EditorPane openImageEditorOpening(JInternalFrame frame, Color[][] imageMatrix, String fileName) {
        EditorPane editor = null;
        int height = ConfigProvider.getResolutionHeight(ProjectManager.getCurrent().getConfig());
        int width = ConfigProvider.getResolutionWidth(ProjectManager.getCurrent().getConfig());
        int t = imageMatrix[0].length;
        for (int i = 0; i < imageMatrix.length; i++) {
            if (imageMatrix[i].length != t) {
                System.out.println("matrix mismatch ! : imageMatrix[0] : " + t + "imageMatrix[" + i + "] : "
                        + imageMatrix[i].length);
            }
        }
        Color[][] resizedImage = ImageUtils.resizeColorArray(imageMatrix, width, height);
        editor = new EditorPane(resizedImage, width, height, fileName);
        return editor;
    }

}
