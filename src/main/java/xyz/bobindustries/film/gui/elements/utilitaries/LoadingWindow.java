package xyz.bobindustries.film.gui.elements.utilitaries;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JWindow;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;

public class LoadingWindow extends JWindow {

    public LoadingWindow(String text, int width, int height) {
        setSize(width, height);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.setBackground(Color.WHITE);

        if (text.equals("")) {
            Bob bob = new Bob();
            bob.setScale(2);
            panel.add(bob, BorderLayout.CENTER);
        } else {
            JLabel loadingLabel = new JLabel(text, JLabel.CENTER);
            loadingLabel.setFont(new Font("Arial", Font.BOLD, 16));

            panel.add(loadingLabel, BorderLayout.CENTER);
        }
        add(panel);
    }

}
