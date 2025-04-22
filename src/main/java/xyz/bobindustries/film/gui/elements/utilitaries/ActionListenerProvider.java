package xyz.bobindustries.film.gui.elements.utilitaries;

import xyz.bobindustries.film.App;
import xyz.bobindustries.film.ImageEditor.ToolsList;
import xyz.bobindustries.film.gui.Workspace;
import xyz.bobindustries.film.gui.elements.ImageLayers;
import xyz.bobindustries.film.gui.elements.dialogs.NewProjectDialog;
import xyz.bobindustries.film.gui.elements.dialogs.OpenImageDialog;
import xyz.bobindustries.film.gui.elements.dialogs.OpenProjectDialog;
import xyz.bobindustries.film.gui.panes.ScenarioEditorPane;
import xyz.bobindustries.film.projects.ProjectManager;
import xyz.bobindustries.film.projects.elements.Project;
import xyz.bobindustries.film.projects.elements.exceptions.InvalidScenarioContentException;
import xyz.bobindustries.film.gui.panes.*;

import javax.swing.*;
import java.awt.*;
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
    LoadingWindow loadingWindow = new LoadingWindow("loading project...", 200, 100);

    loadingWindow.setVisible(true);
    loadingWindow.requestFocus();

    SwingWorker<Void, Void> worker = new SwingWorker<>() {
      @Override
      protected Void doInBackground() throws Exception {
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

  /**
   *
   */
  public static void createImageAction(ActionEvent ignorActionEvent) {
    Workspace workspace = Workspace.getInstance();
    JInternalFrame editorFrame = workspace.getImageEditorFrame();

    openImageEditor(editorFrame, null);
  }

  /**
   *
   */
  public static void openImageAction(ActionEvent ignorActionEvent) {
    Workspace workspace = Workspace.getInstance();
    JInternalFrame editorFrame = workspace.getImageEditorFrame();

    System.out.println("image");

    int result = OpenImageDialog.show(App.getFrame());

    if (result==1) {
      openImageEditor(editorFrame, ProjectManager.getCurrent().getImages().get(0).getColorMatrix());
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

  public static void setTool(ActionEvent e) {
    Workspace workspace = Workspace.getInstance();
    JInternalFrame editorFrame = workspace.getImageEditorFrame();
    EditorPane editor = (EditorPane) editorFrame.getContentPane();
    editor.setSelectedTool(ToolsList.valueOf(e.getActionCommand()));
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
        protected Void doInBackground() throws Exception {
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

            Thread.sleep(2000);
            App.getFrame().revalidate();
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

  public static void openImageEditor(JInternalFrame frame, Color[][] imageMatrix) {
    JPanel editor = null;
    if (imageMatrix == null) {
      Color[][] defaultCanvas = new Color[600][800];
      for (Color[] currentTab : defaultCanvas) {
        for (Color currentColor : currentTab) {
          currentColor = Color.WHITE;
        }
      }
      System.out.println("fini");
      editor = new EditorPane(defaultCanvas, 800, 600);
    } else {
      editor = new EditorPane(imageMatrix, imageMatrix[0].length, imageMatrix.length);
    }
    frame.setContentPane(editor);
  }
}
