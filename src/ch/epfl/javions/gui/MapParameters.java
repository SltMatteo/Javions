package ch.epfl.javions.gui;

import ch.epfl.javions.Math2;
import ch.epfl.javions.Preconditions;
import javafx.beans.property.*;

/**
 * Represents the parameters of the visible map in the graphic interface
 */
public final class MapParameters {

    private final IntegerProperty zoom;
    // minX and minY represent the top left corner of the image
    private final DoubleProperty minX; // Expressed in WebMercator
    private final DoubleProperty minY; // Expressed in WebMercator

    private static final int MIN_ZOOM = 6;
    private static final int MAX_ZOOM = 19;

    /**
     * @param zoom the zoom level of the map
     * @param minX the coordinates of the top-left corner of the visible map
     * @param minY the coordinates of the top-right corner of the visible map
     */
    public MapParameters(int zoom, double minX, double minY) {
        Preconditions.checkArgument(MIN_ZOOM <= zoom && zoom <= MAX_ZOOM,
                "zoom level should be between 6 and 19: " + zoom);
        this.zoom = new SimpleIntegerProperty(zoom);
        this.minX = new SimpleDoubleProperty(minX);
        this.minY = new SimpleDoubleProperty(minY);
    }

    /**
     * Returns the property of the zoom of the map
     *
     * @return the property of the zoom of the map
     */
    public ReadOnlyIntegerProperty zoomProperty() {
        return zoom;
    }

    /**
     * Returns the value of the zoom of the map
     *
     * @return the value of the zoom of the map
     */
    public int getZoom() {
        return zoom.get();
    }

    /**
     * Returns the property of the top-left corner of the map
     *
     * @return the property of the top-left corner of the map
     */
    public ReadOnlyDoubleProperty minXProperty() {
        return minX;
    }

    /**
     * Returns the value of the top-left corner of the map
     *
     * @return the value of the top-left corner of the map
     */
    public double getMinX() {
        return minX.get();
    }

    /**
     * Returns the property of the top-right corner of the map
     *
     * @return the property of the top-right corner of the map
     */
    public ReadOnlyDoubleProperty minYProperty() {
        return minY;
    }

    /**
     * Returns the value of the top-right corner of the map
     *
     * @return the value of the top-right corner of the map
     */
    public double getMinY() {
        return minY.get();
    }

    /**
     * Scrolls the top-left corner of the map to the new position
     *
     * @param dx the x scroll
     * @param dy the y scroll
     */
    public void scroll(double dx, double dy) {
        this.minX.set(getMinX() + dx);
        this.minY.set(getMinY() + dy);
    }

    /**
     * Changes the zoom level incrementing or decrementing it by a given value
     *
     * @param dZoom the zoom difference
     */
    public void changeZoomLevel(int dZoom) {
        int newZoom = Math2.clamp(MIN_ZOOM, getZoom() + dZoom, MAX_ZOOM); // Keeps it between maxZoom and minZoom
        int effectiveDZoom = newZoom - getZoom();
        zoom.set(newZoom);
        // Change minX and minY so that they take the zoom change into account
        minX.set(Math.scalb(getMinX(), effectiveDZoom));
        minY.set(Math.scalb(getMinY(), effectiveDZoom));
    }
}