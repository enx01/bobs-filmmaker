package xyz.bobindustries.film.model.tools;

public interface ToolsSettings {
    int[] getSliderBounds();

    int getCurrentThickness();

    void updateCurrentThickness(int thickness);
}
