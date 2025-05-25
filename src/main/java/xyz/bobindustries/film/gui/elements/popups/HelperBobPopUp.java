package xyz.bobindustries.film.gui.elements.popups;

import xyz.bobindustries.film.App;
import xyz.bobindustries.film.gui.elements.utilitaries.Bob;

import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Objects;

public class HelperBobPopUp extends JDialog {
    JPanel tutorialHolder;

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

        JLabel quote = new JLabel(getRandomBobQuote());
        quotePanel.add(quote, BorderLayout.CENTER);

        paddedPanel.add(quotePanel, BorderLayout.EAST);

        tutorialHolder = new JPanel();

        tutorialHolder.setPreferredSize(new Dimension(600, 400));

        JScrollPane scrollPane = new JScrollPane(tutorialHolder);
        scrollPane.setBorder(null);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(251, 241, 199), 2, true));

        add(scrollPane, BorderLayout.CENTER);
    }

    private static final String[] BOB_QUOTES = {
            "hello, i am bob! i love making stop motion animations!",
            "the journey of animation can be a long one, keep practicing!",
            "this software was made by two university students, both loving bob!",
            "gruvbox is my favourite colorscheme!",
            "if you ever feel sad, remember you were able to make bob smile!"
    };

    private String getRandomBobQuote() {
        int randomIndex = (int) (Math.random() * BOB_QUOTES.length);
        return BOB_QUOTES[randomIndex];
    }

    public static void loadBobTutorial() {
        HelperBobPopUp hbpu = new HelperBobPopUp(App.getFrame());

        JLabel tutorialContent = new JLabel();

        String content = null;
        StringBuilder contentBuilder = new StringBuilder();

        try {
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(
                            Objects.requireNonNull(
                                    HelperBobPopUp.class.getResourceAsStream("bob_tutorial.html"))
                    )
            );
            String line;
            while ((line = reader.readLine()) != null) {
                contentBuilder.append(line).append("\n");
            }
            content = contentBuilder.toString();
        } catch (IOException ignored) {
        }

        if (content == null) {
            content = "<html><body><h1>bob tutorial not found</h1><p>bob apologizes.. this tutorial couldn't be found :(</p></body></html>";
        }

        tutorialContent.setText(content);

        hbpu.tutorialHolder.add(tutorialContent);

        hbpu.setVisible(true);
    }
}
