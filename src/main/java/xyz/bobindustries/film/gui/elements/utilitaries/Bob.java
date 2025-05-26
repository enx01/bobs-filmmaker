package xyz.bobindustries.film.gui.elements.utilitaries;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Random;

public class Bob extends JPanel {
    Color skin = new Color(127, 153, 26);
    double scale = 1;
    boolean helper = false;

    public void setScale(double scale) {
        this.scale = scale;
    }

    public Bob() {
        setPreferredSize(new Dimension(100, 100));
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent ignoredMouseEvent) {
                splash();
            }
        });
    }

    public Bob(double scale) {
        this();
        setPreferredSize(new Dimension((int) (100 * scale), (int) (100 * scale)));
        setScale(scale);
    }

    @SuppressWarnings("unused")
    public Bob(boolean helper) {
        this();
        this.helper = helper;
    }

    public Bob(boolean helper, double scale) {
        this(scale);
        this.helper = helper;
    }

    public void splash() {
        Random random = new Random();

        skin = new Color(random.nextInt(256), random.nextInt(256), random.nextInt(256));
        repaint();
    }

    public void setSkin(Color skin) {
        this.skin = skin;
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g.create();

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        /* Bob's face */
        g2d.setColor(skin);
        g2d.fillOval(0, 0, (int) (100 * scale), (int) (100 * scale));

        /* Bob's eyes */
        g2d.setColor(Color.BLACK);
        g2d.fillOval((int) (25 * scale), (int) (30 * scale), (int) (10 * scale), (int) (10 * scale));
        g2d.fillOval((int) (65 * scale), (int) (30 * scale), (int) (10 * scale), (int) (10 * scale));

        /* Bob's mouth */
        g2d.setColor(Color.BLACK);
        g2d.fillArc((int) (25 * scale), (int) (30 * scale), (int) (50 * scale), (int) (30 * scale), 0, -180);

        if (helper) {
            /* Glasses frame */
            g2d.setColor(Color.BLACK);
            g2d.setStroke(new BasicStroke((int) (3 * scale)));
            g2d.drawRect((int) (15 * scale), (int) (22 * scale), (int) (30 * scale), (int) (20 * scale));
            g2d.drawRect((int) (55 * scale), (int) (22 * scale), (int) (30 * scale), (int) (20 * scale));

            /* Glasses bridge */
            g2d.drawLine((int) (45 * scale), (int) (30 * scale), (int) (55 * scale), (int) (30 * scale));

            /* Yellow rectangle on Bob's left */
            g2d.setColor(new Color(255, 245, 153));
            g2d.rotate(Math.toRadians(15), (int) (95 * scale + 7.5 * scale), (int) (15 * scale + 40 * scale)); // Rotate around rectangle center
            g2d.fillRect((int) (95 * scale), (int) (15 * scale), (int) (15 * scale), (int) (80 * scale));
            g2d.setColor(Color.BLACK);
            g2d.setStroke(new BasicStroke(1));
            for (int i = 1; i < 8; i++) {
                g2d.drawLine(
                        (int) (100 * scale),
                        (int) (15 * scale + i * 10 * scale),
                        (int) (110 * scale),
                        (int) (15 * scale + i * 10 * scale)
                );
            }
            g2d.rotate(-Math.toRadians(15), (int) (95 * scale + 7.5 * scale), (int) (15 * scale + 40 * scale)); // Reset rotation

        }

        g2d.dispose();
    }
}