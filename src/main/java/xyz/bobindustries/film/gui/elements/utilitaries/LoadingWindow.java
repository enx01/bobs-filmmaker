package xyz.bobindustries.film.gui.elements.utilitaries;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JWindow;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;

public class LoadingWindow extends JWindow {

    public LoadingWindow() {
        setSize(200, 200);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.setBackground(Color.WHITE);

        JLabel loadingLabel = new JLabel("bob's filmmaker", JLabel.CENTER);
        loadingLabel.setFont(new Font("Arial", Font.BOLD, 16));
        loadingLabel.setForeground(Color.BLUE);

        JProgressBar progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        progressBar.setStringPainted(true);
        progressBar.setString("Initializing...");

        panel.add(loadingLabel, BorderLayout.CENTER);
        panel.add(progressBar, BorderLayout.SOUTH);

        add(panel);

        setVisible(true);
    }

}
