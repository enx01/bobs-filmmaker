package xyz.bobindustries.film.gui.elements;

import xyz.bobindustries.film.ImageEditor.ToolsList;

import java.awt.*;
import javax.swing.*;
import java.awt.event.*;
import java.util.ArrayList;

public class ToolBoxUI extends JPanel {

  ArrayList<JButton> boutons;

  public ToolBoxUI() {
    boutons = new ArrayList<>();
    setLayout(new BorderLayout());

    // Création d’un panneau pour les boutons avec une grille 2 colonnes x 5 lignes
    JPanel buttonPanel = new JPanel(new GridLayout(5, 2, 5, 5)); // 5 lignes, 2 colonnes, avec un petit espace
    buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); // marges autour

    // Ajoute des boutons d'outils avec des icônes fictives
    /*for (int i = 1; i <= 12; i++) {
      ToolboxButton button = new ToolboxButton(ToolsList.values()[i]);
      buttonPanel.add(button);
    }*/

    ToolboxButton button = new ToolboxButton(ToolsList.PEN);
    ToolboxButton button2 = new ToolboxButton(ToolsList.BRUSH);
    buttonPanel.add(button);
    buttonPanel.add(button2);

    System.out.println("pill1 workspace");
    add(buttonPanel, BorderLayout.CENTER);
  }
}

