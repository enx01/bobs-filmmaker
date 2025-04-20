package xyz.bobindustries.film.gui.elements;

import java.awt.*;
import javax.swing.*;
import java.awt.event.*;

public class ToolBoxUI extends JPanel {

  public ToolBoxUI() {
    setLayout(new BorderLayout());

    // Barre d'outils verticale
    JToolBar toolBar = new JToolBar(JToolBar.VERTICAL);
    toolBar.setFloatable(false);

    // Ajoute des boutons d'outils avec des icônes fictives
    for (int i = 1; i <= 18; i++) {
      JButton button = new JButton();
      button.setPreferredSize(new Dimension(40, 40));
      button.setToolTipText("Outil " + i);

      // Icône générique (remplace par des vraies icônes si tu veux)
      Icon icon = UIManager.getIcon("OptionPane.informationIcon");
      button.setIcon(icon);

      int finalI = i;
      button.addActionListener((ActionEvent e) -> System.out.println("Outil " + finalI + " sélectionné"));

      toolBar.add(button);
    }

    add(toolBar, BorderLayout.CENTER);
  }
}
