package xyz.bobindustries.film.gui.elements.utilitaries;

import xyz.bobindustries.film.App;
import xyz.bobindustries.film.ImageEditor.ToolsList;
import xyz.bobindustries.film.gui.Workspace;
import xyz.bobindustries.film.gui.elements.ToolBoxUI;
import xyz.bobindustries.film.gui.elements.contextualmenu.ContextualMenu;
import xyz.bobindustries.film.gui.elements.dialogs.*;
import xyz.bobindustries.film.gui.helpers.Pair;
import xyz.bobindustries.film.gui.panes.ScenarioEditorPane;
import xyz.bobindustries.film.gui.panes.WelcomePane;
import xyz.bobindustries.film.projects.ConfigProvider;
import xyz.bobindustries.film.projects.ProjectManager;
import xyz.bobindustries.film.projects.elements.ImageFile;
import xyz.bobindustries.film.projects.elements.Project;
import xyz.bobindustries.film.projects.elements.exceptions.InvalidScenarioContentException;
import xyz.bobindustries.film.gui.panes.*;
import xyz.bobindustries.film.utils.ImageUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.beans.PropertyVetoException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

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
                    project.exportAsVideo(Arrays.stream(sep.extractFrameData()).toList());
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

    public static void ShowImageEditorFrame(ImageFile imf) {
        Workspace current_ws = Workspace.getInstance();
        JInternalFrame ief = current_ws.getImageEditorFrame();
        try {
            EditorPane pane = (EditorPane) ief.getContentPane();
            pane.addNewImage(imf.getColorMatrix(), imf.getFileName());
        } catch (ClassCastException ccex) {
            EditorPane pane = ActionListenerProvider.openImageEditor(ief, imf.getColorMatrix(), imf.getFileName());
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

        EditorPane editor = openImageEditor(editorFrame, null, "");
        editorFrame.setContentPane(editor);
    }

    /**
     *
     */
    public static void openImageAction(ActionEvent ignorActionEvent) {
        Workspace workspace = Workspace.getInstance();
        JInternalFrame editorFrame = workspace.getImageEditorFrame();

        System.out.println("image");

        Pair<Color[][], String> result = OpenImageDialog.show(App.getFrame());

        EditorPane editor = openImageEditor(editorFrame, result.getKey(), result.getValue());
        editorFrame.setContentPane(editor);

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
        System.out.println("tool set : "+ e.getActionCommand());
    }

    public static void saveImageAction(ActionEvent ignorActionEvent) {
        Workspace workspace = Workspace.getInstance();
        JInternalFrame editorFrame = workspace.getImageEditorFrame();
        EditorPane editor = (EditorPane) editorFrame.getContentPane();
        String projectPath = ProjectManager.getCurrent().getProjectDir().toString();
        String fileName = editor.getCurrentFileName();
        System.out.println("pp:"+projectPath);
        if (fileName.isEmpty() || fileName.contains("/")) {
            fileName = NameImageDialog.show(App.getFrame(), fileName);
            fileName+=".png";
        }
        ImageUtils.exportImage(editor.getData().getGridColors(), projectPath+"/images/"+fileName);
        editor.setFileName(fileName);
    }

    public static void openExistingFrames(ActionEvent ignoredActionEvent) {
        Workspace workspace = Workspace.getInstance();
        JInternalFrame editorFrame = workspace.getImageEditorFrame();
        Project current = ProjectManager.getCurrent();
        ArrayList <String> selectedFrames = new ArrayList<>();
        EditorPane editor = null;
        selectedFrames = OpenExistingFramesDialog.show(App.getFrame(), (ArrayList<ImageFile>) current.getImages());
        System.out.println(selectedFrames.size());
        for (String currentFrame : selectedFrames) {
            if (editor == null) {
                System.out.println("editor is null");
                editor = openImageEditor(editorFrame, ProjectManager.getImageMatrix(currentFrame), currentFrame);
            } else {
                editor.addNewImage(ProjectManager.getImageMatrix(currentFrame), currentFrame);
            }
        }
        editorFrame.setContentPane(editor);
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

    public static EditorPane openImageEditor(JInternalFrame frame, Color[][] imageMatrix, String fileName) {
        EditorPane editor = null;
        int height = ConfigProvider.getResolutionHeight(Project.getConfig());
        int width = ConfigProvider.getResolutionWidth(Project.getConfig());
        if (imageMatrix == null) {
            Color[][] defaultCanvas = new Color[height][width];
            for (int i = 0; i < defaultCanvas.length; i+=1) {
                for (int j = 0; j < defaultCanvas[0].length; j+=1) {
                    defaultCanvas[i][j] = new Color(255, 255, 255);
                }
            }
            System.out.println("fini");
            editor = new EditorPane(defaultCanvas, width, height, "");
            System.out.println("index:"+editor.getCurrentImageIndex());
        } else {
            Color[][] resizedImage = ImageUtils.resizeColorArray(imageMatrix, width, height);
            editor = new EditorPane(resizedImage, width, height, fileName);
            System.out.println("index:"+editor.getCurrentImageIndex());
        }
        return editor;
    }


}
