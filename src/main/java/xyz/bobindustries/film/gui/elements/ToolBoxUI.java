package xyz.bobindustries.film.gui.elements;

import java.awt.*;
import javax.swing.*;
import java.awt.event.*;

public class ToolBoxUI extends JPanel {

  public ToolBoxUI() {
    setLayout(new BorderLayout());

    // Création d’un panneau pour les boutons avec une grille 2 colonnes x 5 lignes
    JPanel buttonPanel = new JPanel(new GridLayout(5, 2, 5, 5)); // 5 lignes, 2 colonnes, avec un petit espace
    buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); // marges autour

    // Ajoute des boutons d'outils avec des icônes fictives
    for (int i = 1; i <= 10; i++) {  // 2x5 = 10 boutons
      JButton button = new JButton();
      button.setPreferredSize(new Dimension(40, 40));
      button.setToolTipText("Outil " + i);

      // Icône générique
      Icon icon = UIManager.getIcon("OptionPane.informationIcon");
      button.setIcon(icon);

      int finalI = i;
      button.addActionListener((ActionEvent e) -> System.out.println("Outil " + finalI + " sélectionné"));

      buttonPanel.add(button);
    }

    add(buttonPanel, BorderLayout.CENTER);
  }
}

