package xyz.bobindustries.film.gui.panes;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;

public class EditorPane extends JPanel {
    private JButton button1;
    private JButton button2;
    private JButton button3;
    private JButton button4;
    private JButton button5;
    private JTextField footerTextField;
    private double scale = 1.0;
    private Point origin = new Point(0, 0);
    private Point mouse = new Point(0, 0);
    private ArrayList<Point> points = new ArrayList<>();
    private final Rectangle blueSquare = new Rectangle(50, 50, 100, 100);

    public EditorPane() {
        MouseAdapter mouseHandler = new MouseAdapter() {
            private Point lastPoint;

            @Override
            public void mousePressed(MouseEvent e) {
                Point adjustedPoint = new Point(
                        (int) ((e.getX() - origin.x) / scale),
                        (int) ((e.getY() - origin.y) / scale)
                );
                lastPoint = adjustedPoint;
                if (blueSquare.contains(adjustedPoint)) {
                    points.add(lastPoint);
                }
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                Point adjustedPoint = new Point(
                        (int) ((e.getX() - origin.x) / scale),
                        (int) ((e.getY() - origin.y) / scale)
                );
                lastPoint = adjustedPoint;
                if (blueSquare.contains(adjustedPoint)) {
                    points.add(lastPoint);
                }
                repaint();
            }

            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                if (e.isControlDown()) {
                    mouse = e.getPoint();

                    double oldScale = scale;
                    if (e.getWheelRotation() < 0) {
                        scale *= 1.1;
                    } else {
                        scale /= 1.1;
                    }

                    origin.x = (int) (mouse.x - (mouse.x - origin.x) * (scale / oldScale));
                    origin.y = (int) (mouse.y - (mouse.y - origin.y) * (scale / oldScale));

                    repaint();
                }
            }

        };

        addMouseListener(mouseHandler);
        addMouseMotionListener(mouseHandler);
        addMouseWheelListener(mouseHandler);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        AffineTransform at = g2d.getTransform();
        g2d.translate(origin.x, origin.y);
        g2d.scale(scale, scale);

        g2d.setColor(Color.BLUE);
        g2d.fillRect(blueSquare.x, blueSquare.y, blueSquare.width, blueSquare.height);


        g2d.setColor(Color.RED);
        for (Point p : points) {
            g2d.fillOval(p.x - 2, p.y - 2, 4, 4);
        }

        g2d.setTransform(at);
    }

}
