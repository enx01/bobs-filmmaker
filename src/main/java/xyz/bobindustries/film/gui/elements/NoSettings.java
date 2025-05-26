package xyz.bobindustries.film.gui.elements;

import xyz.bobindustries.film.gui.elements.utilitaries.Bob;

import javax.swing.*;
import java.awt.*;

public class NoSettings extends JPanel {
    public NoSettings() {
        setLayout(new BorderLayout());
        JLabel title = new JLabel("No settings!");
        add(title, BorderLayout.SOUTH);
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // Dessiner le bob
        Bob bob = new Bob();
        int bobWidth = bob.getPreferredSize().width;
        int bobHeight = bob.getPreferredSize().height;

        int x = (getWidth() - bobWidth) / 2;
        int y = (getHeight() - bobHeight) / 2;

        Graphics g2 = g.create();
        g2.translate(x, y);
        bob.paintComponent(g2);
    }
}
