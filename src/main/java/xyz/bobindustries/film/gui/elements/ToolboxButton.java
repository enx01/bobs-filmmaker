package xyz.bobindustries.film.gui.elements;

import xyz.bobindustries.film.image_editor.ToolsList;
import xyz.bobindustries.film.gui.elements.utilitaries.ActionListenerProvider;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.InputStream;

public class ToolboxButton extends JButton {

    public ToolboxButton(ToolsList tool) {
        setActionCommand(tool.name());
        setPreferredSize(new Dimension(40, 40));
        setToolTipText("Outil " + tool);

        System.out.println("creating tool button:"+tool.name());

        try (InputStream is = ToolboxButton.class.getResourceAsStream(tool.name().toLowerCase()+".jpg")) {
            if (is != null) {
                BufferedImage img = ImageIO.read(is);
                Image scaledImg = img.getScaledInstance(40, 40, Image.SCALE_SMOOTH);
                ImageIcon icon = new ImageIcon(scaledImg);
                setIcon(icon);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        addActionListener(ActionListenerProvider::setTool);
    }

    public void setHoveredStyle(boolean hovered) {
        if (hovered) {
            setBackground(new Color(200, 200, 255)); // couleur de survol
            setBorder(BorderFactory.createLineBorder(Color.BLUE, 2));
        } else {
            setBackground(UIManager.getColor("Button.background"));
            setBorder(UIManager.getBorder("Button.border"));
        }
    }
}