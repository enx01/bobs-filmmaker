package xyz.bobindustries.film.gui.elements;

import xyz.bobindustries.film.gui.elements.utilitaries.Bob;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Random;

public class ColorBox extends JPanel {
    private Color selectedColor = Color.RED; // Couleur initiale
    private final int squareSize = 30; // Taille des carrés
    private final Color[] colors = {
            Color.RED, Color.GREEN,
            Color.BLUE, Color.YELLOW,
            Color.PINK, Color.ORANGE,
            Color.BLACK, Color.WHITE
    }; // Palette de couleurs
    private JPanel colorPanel; // Panneau qui contiendra la grille des couleurs

    public ColorBox(JInternalFrame parent) {
        setPreferredSize(new Dimension(100,100));
        setLayout(new BorderLayout());


        // Ajouter un écouteur de clics pour chaque carré
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                Color newColor = JColorChooser.showDialog(parent, "Choisir une couleur", selectedColor);
                selectedColor = newColor;
                repaint();
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // Dessiner le bob
        Bob bob = new Bob();
        bob.setSkin(selectedColor);
        int bobWidth = bob.getPreferredSize().width;
        int bobHeight = bob.getPreferredSize().height;

        int x = (getWidth() - bobWidth) / 2;
        int y = (getHeight() - bobHeight) / 2;

        Graphics g2 = g.create();
        g2.translate(x, y);
        bob.paintComponent(g2);
    }

    public Color getSelectedColor() {
        return selectedColor;
    }

    public void setSelectedColor(Color selectedColor) {
        this.selectedColor = selectedColor;
    }
}
