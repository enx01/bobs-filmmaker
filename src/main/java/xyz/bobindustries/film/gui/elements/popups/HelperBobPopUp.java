package xyz.bobindustries.film.gui.elements.popups;

import xyz.bobindustries.film.App;
import xyz.bobindustries.film.gui.elements.utilitaries.Bob;

import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Objects;

public class HelperBobPopUp extends JDialog {
    JScrollPane scrollPane;
    private static final String[] BOB_QUOTES = {
            "hello, i am bob! i love making stop motion animations!",
            "the journey of animation can be a long one, keep practicing!",
            "this software was made by two university students, both loving bob!",
            "gruvbox is my favourite colorscheme!",
            "if you ever feel sad, remember you were able to make bob smile!"
    };

    public static final int BOB_TUTORIAL = 0,
            IMAGE_EDITOR_TUTORIAL = 1,
            SCENARIO_EDITOR_TUTORIAL = 2,
            VISUALIZER_TUTORIAL = 3;

    public HelperBobPopUp(Frame owner) {
        super(owner, "", true);
        setSize(800, 600);
        setResizable(false);

        setLayout(new BorderLayout());

        Bob bob = new Bob(true, .75);
        JPanel paddedPanel = new JPanel(new BorderLayout());
        paddedPanel.setOpaque(false);
        paddedPanel.setBorder(BorderFactory.createEmptyBorder(25, 25, 20, 20));

        paddedPanel.add(bob, BorderLayout.CENTER);

        add(paddedPanel, BorderLayout.PAGE_END);

        JPanel quotePanel = new JPanel(new BorderLayout());
        quotePanel.setPreferredSize(new Dimension(625 - bob.getWidth(), 30));
        quotePanel.setMaximumSize(new Dimension(625 - bob.getWidth(), 30));

        String quoteContent = getRandomBobQuote();
        JLabel quote = new JLabel(quoteContent);
        quotePanel.setToolTipText(quoteContent);
        quotePanel.add(quote, BorderLayout.CENTER);

        paddedPanel.add(quotePanel, BorderLayout.EAST);

    }

    private void setContent(String content) {
        JLabel label = new JLabel(content);

        scrollPane = new JScrollPane(label);
        scrollPane.setBorder(null);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(251, 241, 199), 1, true));
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        add(scrollPane, BorderLayout.CENTER);
    }

    public static void loadTutorial(int tutorialId) {
        HelperBobPopUp hbpu = new HelperBobPopUp(App.getFrame());

        String content = null;
        StringBuilder contentBuilder = new StringBuilder();

        try {
            URL htmlURL = HelperBobPopUp.class.getResource("bob_tutorial.html");

            if (htmlURL == null) {
                content = null;
                throw new IOException();
            }

            String pathStr = htmlURL.toString();
            String baseURL = pathStr.substring(0, pathStr.lastIndexOf('/') + 1);

            String filename = "";
            switch (tutorialId) {
                case BOB_TUTORIAL:
                    filename = "bob_tutorial.html";
                    break;
                case IMAGE_EDITOR_TUTORIAL:
                    filename = "image_editor_tutorial.html";
                    break;
                case SCENARIO_EDITOR_TUTORIAL:
                    filename = "scenario_editor_tutorial.html";
                    break;
                case VISUALIZER_TUTORIAL:
                    filename = "visualizer_tutorial.html";
                    break;
            }

            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(
                            Objects.requireNonNull(
                                    HelperBobPopUp.class.getResourceAsStream(filename))));
            String line;
            while ((line = reader.readLine()) != null) {
                contentBuilder.append(line).append("\n");
            }
            String tmp = contentBuilder.toString();
            content = tmp.replace("<head>", "<head><base href='" + baseURL + "'>");

        } catch (IOException ignored) {
        }

        if (content == null) {
            content = "<html><body><h1>bob tutorial not found</h1><p>bob apologizes.. this tutorial couldn't be found :(</p></body></html>";
        }

        hbpu.setContent(content);

        hbpu.setVisible(true);
    }

    private String getRandomBobQuote() {
        int randomIndex = (int) (Math.random() * BOB_QUOTES.length);
        return BOB_QUOTES[randomIndex];
    }
}
