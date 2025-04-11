package xyz.bobindustries.film.gui.elements.menubars;

import xyz.bobindustries.film.gui.elements.utilitaries.ActionListenerProvider;

import javax.swing.*;

public class WorkspaceMenuBar extends JMenuBar {

    public WorkspaceMenuBar() {

        // Project Menu :
        JMenu projectMenu = new JMenu("project");

        JMenuItem newProjectItem = new JMenuItem("new project");
        newProjectItem.addActionListener(ActionListenerProvider::getNewProjectDialogAction);

        JMenuItem openProjectItem = new JMenuItem("open project");
        openProjectItem.addActionListener(ActionListenerProvider::getOpenProjectDialogAction);

        JMenuItem saveProjectItem = new JMenuItem("save project");

        projectMenu.add(newProjectItem);
        projectMenu.add(openProjectItem);
        projectMenu.add(saveProjectItem);

        // Window Menu :
        JMenu windowMenu = new JMenu("window");

        JMenuItem welcomeItem = new JMenuItem("welcome");
        JMenuItem imageEditorItem = new JMenuItem("image editor");
        JMenuItem scenarioEditorItem = new JMenuItem("scenario editor");
        JMenuItem filmVisualizerItem = new JMenuItem("film visualizer");
        JMenuItem aboutItem = new JMenuItem("about");

        welcomeItem.addActionListener(ActionListenerProvider::getShowWelcomeFrameAction);
        imageEditorItem.addActionListener(ActionListenerProvider::getShowImageEditorFrameAction);
        scenarioEditorItem.addActionListener(ActionListenerProvider::getShowScenarioEditorFrameAction);
        filmVisualizerItem.addActionListener(ActionListenerProvider::getShowFilmVisualizerFrameAction);
        aboutItem.addActionListener(ActionListenerProvider::getShowAboutFrameAction);

        windowMenu.add(welcomeItem);
        windowMenu.add(imageEditorItem);
        windowMenu.add(scenarioEditorItem);
        windowMenu.add(filmVisualizerItem);
        windowMenu.add(aboutItem);

        this.add(projectMenu);
        this.add(windowMenu);
    }

}
