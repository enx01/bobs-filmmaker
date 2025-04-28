package xyz.bobindustries.film.gui.elements.menubars;

import xyz.bobindustries.film.gui.elements.utilitaries.ActionListenerProvider;

import javax.swing.*;

public class WorkspaceMenuBar extends JMenuBar {

    public WorkspaceMenuBar() {

        // Project Menu :
        JMenu projectMenu = new JMenu("project");
        projectMenu.setMnemonic('P');

        JMenuItem newProjectItem = new JMenuItem("new");
        newProjectItem.setMnemonic('N');
        newProjectItem.addActionListener(ActionListenerProvider::getNewProjectDialogAction);

        JMenuItem openProjectItem = new JMenuItem("open");
        openProjectItem.setMnemonic('O');
        openProjectItem.addActionListener(ActionListenerProvider::getOpenProjectDialogAction);

        JMenuItem saveProjectItem = new JMenuItem("save");
        saveProjectItem.setMnemonic('S');
        saveProjectItem.addActionListener(ActionListenerProvider::saveCurrentProject);

        projectMenu.add(newProjectItem);
        projectMenu.add(openProjectItem);
        projectMenu.add(new JSeparator());
        projectMenu.add(saveProjectItem);

        // Window Menu :
        JMenu windowMenu = new JMenu("window");
        windowMenu.setMnemonic('W');

        JMenuItem welcomeItem = new JMenuItem("welcome");
        welcomeItem.setMnemonic('W');
        JMenuItem imageEditorItem = new JMenuItem("image editor");
        imageEditorItem.setMnemonic('I');
        JMenuItem scenarioEditorItem = new JMenuItem("scenario editor");
        scenarioEditorItem.setMnemonic('S');
        JMenuItem filmVisualizerItem = new JMenuItem("film visualizer");
        filmVisualizerItem.setMnemonic('V');
        JMenuItem aboutItem = new JMenuItem("about");
        aboutItem.setMnemonic('A');

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
