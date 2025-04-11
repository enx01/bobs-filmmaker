package xyz.bobindustries.film.gui.elements.utilitaries;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Random;

public class Bob extends JPanel {
    Color skin = new Color(127, 153, 26);

    public Bob() {
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent ignoredMouseEvent) {
                splash();
            }
        });
    }

    public void splash() {
        Random random = new Random();

        skin = new Color(random.nextInt(256), random.nextInt(256), random.nextInt(256));
        repaint();
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        /* Bob's face */
        g.setColor(skin);
        g.fillOval(50, 50, 100, 100);

        /* Bob's eyes */
        g.setColor(Color.BLACK);
        g.fillOval(75, 80, 10, 10);
        g.fillOval(115, 80, 10, 10);

        /* Bob's mouth */
        g.setColor(Color.BLACK);
        g.fillArc(75, 80, 50, 30, 0, -180);
    }
}
