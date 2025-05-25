package xyz.bobindustries.film.gui.elements.contextualmenu;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;

import java.awt.event.*;
import java.util.Objects;

public class ContextualMenu {
    private final List<JComponent> menuItems;

    private ContextualMenu(List<JComponent> menuItems) {
        this.menuItems = List.copyOf(menuItems);
    }

    /**
     * Adds a right click listener to the JComponent
     *
     * @param component The component to link the contextual menu to.
     */
    public void attachTo(JComponent component) {

        JPopupMenu popupMenu = new JPopupMenu();

        for (JComponent item : menuItems) {
            if (item instanceof JMenuItem || item instanceof JSeparator)
                popupMenu.add(item);
        }

        /* Setting up MouseListeners to the target component */

        component.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                super.mousePressed(e);

                if (e.isPopupTrigger())
                    showMenu(e);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                super.mouseReleased(e);

                if (e.isPopupTrigger())
                    showMenu(e);
            }

            private void showMenu(MouseEvent e) {
                if (popupMenu.getComponentCount() > 0)
                    popupMenu.show(component, e.getX() + 10, e.getY() + 10);
            }
        });

    }

    public static class Builder {
        private final List<JMenuItem> items = new ArrayList<>();

        /**
         * Adds a menu item to the contextual menu being built.
         *
         * @param actionName Text to display for the item.
         * @param action     ActionListener to execute.
         * @return this Builder instance (for method chaining).
         */
        public Builder addItem(String actionName, ActionListener action) {
            JMenuItem item = new JMenuItem(actionName);

            if (action != null) {
                item.addActionListener(action);
            } else {
                item.setEnabled(false);
            }

            items.add(item);
            return this;
        }

        /**
         * Adds a null items to serve as a separator to the contextual menu being built.
         * 
         * @return this Builder instance (for method chaining).
         */
        public Builder addSeparator() {
            items.add(null);
            return this;
        }

        /**
         * Builds and returns configured ContextualMenu instance.
         *
         * @return A new ContextualMenu instance.
         */
        public ContextualMenu build() {
            List<JComponent> finalItems = new ArrayList<>();

            for (JMenuItem item : items) {
                finalItems.add(Objects.requireNonNullElseGet(item, JSeparator::new));
            }

            return new ContextualMenu(finalItems);
        }
    }

}
