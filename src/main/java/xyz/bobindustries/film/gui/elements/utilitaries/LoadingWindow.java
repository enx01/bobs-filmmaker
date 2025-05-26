package xyz.bobindustries.film.gui.elements.utilitaries;

import javax.swing.*;

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

        if (text.isEmpty()) {
            Bob bob = new Bob( 1.5);

            JPanel paddedPanel = new JPanel(new BorderLayout());
            paddedPanel.setOpaque(false);
            paddedPanel.setBorder(BorderFactory.createEmptyBorder(25, 25, 20, 20));
            paddedPanel.add(bob, BorderLayout.CENTER);

            panel.add(paddedPanel, BorderLayout.CENTER);
        } else {
            JLabel loadingLabel = new JLabel(text, JLabel.CENTER);
            loadingLabel.setFont(new Font("Arial", Font.BOLD, 16));

            panel.add(loadingLabel, BorderLayout.CENTER);
        }
        add(panel);
    }

}