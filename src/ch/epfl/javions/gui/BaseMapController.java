package ch.epfl.javions.gui;

import ch.epfl.javions.GeoPos;
import ch.epfl.javions.WebMercator;
import javafx.application.Platform;
import javafx.beans.property.LongProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.scene.canvas.Canvas;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;
import javafx.scene.canvas.*;
import javafx.scene.paint.Color;

import java.io.IOException;

/**
 * Manages the graphics and the interactions with the map
 */
public final class BaseMapController {

    private final static int TILE_SIZE = 256; // Tiles have size 256x256px
    private final TileManager tileManager;
    private final MapParameters mapParameters;
    private boolean redrawNeeded;
    private final Pane pane;
    private final Canvas canvas;
    private final GraphicsContext graphicsContext;
    private double lastX;
    private double lastY;

    /**
     * Instantiate the map
     * @param tileManager   the tile manager
     * @param mapParameters the parameters of the visible map
     */
    public BaseMapController(TileManager tileManager, MapParameters mapParameters) {
        this.tileManager = tileManager;
        this.mapParameters = mapParameters;
        this.canvas = new Canvas();
        this.pane = new Pane(canvas);
        this.graphicsContext = canvas.getGraphicsContext2D();
        canvas.widthProperty().bind(pane.widthProperty());
        canvas.heightProperty().bind(pane.heightProperty());
        installListeners();
    }

    /**
     * Returns the JavaFX pane representing the base map
     *
     * @return the base map
     */
    public Pane pane() {
        return pane;
    }

    /**
     * Centers the map on the given position, without changing the zoom level
     *
     * @param position the position on which the map will be centered on
     */
    public void centerOn(GeoPos position) {
        int zoom = mapParameters.getZoom();
        double xCoordinate = WebMercator.x(zoom, position.longitude());
        double yCoordinate = WebMercator.y(zoom, position.latitude());
        double dX = xCoordinate - canvas.getWidth() / 2 - mapParameters.getMinX();
        double dY = yCoordinate - canvas.getHeight() / 2 - mapParameters.getMinY();
        mapParameters.scroll(dX, dY);
    }

    /**
     * calls drawMap only when it is necessary (not more than once every frame)
     */
    private void redrawIfNeeded() {
        if (!redrawNeeded) return;
        redrawNeeded = false;
        drawMap();
    }

    /**
     * does something I think
     */
    private void redrawOnNextPulse() {
        redrawNeeded = true;
        Platform.requestNextPulse();
    }

    /** Draws the map with the current map parameters
     */
    private void drawMap() {
        int zoom = mapParameters.getZoom();
        int tileCountX = (int) Math.ceil(canvas.getWidth() / TILE_SIZE);
        int tileCountY = (int) Math.ceil(canvas.getHeight() / TILE_SIZE);
        TileManager.TileId topLeftTile = TileManager.TileId.tileAt(zoom, mapParameters.getMinX(), mapParameters.getMinY());
        double xOffset = topLeftTile.x() * TILE_SIZE - mapParameters.getMinX();
        double yOffset = topLeftTile.y() * TILE_SIZE - mapParameters.getMinY();
        for (int i = 0; i <= tileCountX; i++) {
            for (int j = 0; j <= tileCountY; j++) {
                try {
                    TileManager.TileId tile = TileManager.TileId.tileAt(zoom,
                                    mapParameters.getMinX() + i * TILE_SIZE,
                                    mapParameters.getMinY() + j * TILE_SIZE);
                    Image image = tileManager.imageForTileAt(tile);
                    graphicsContext.drawImage(image, (TILE_SIZE * i) + xOffset, (TILE_SIZE * j) + yOffset);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Adds all the necessary listeners when creating a BaseMapController object
     */
    private void installListeners() {
        // Redraws the map everytime a map parameter changes
        canvas.widthProperty().addListener(o -> redrawOnNextPulse());
        canvas.heightProperty().addListener(o -> redrawOnNextPulse());
        mapParameters.zoomProperty().addListener(o -> redrawOnNextPulse());
        mapParameters.minXProperty().addListener(o -> redrawOnNextPulse());
        mapParameters.minYProperty().addListener(o -> redrawOnNextPulse());
        canvas.sceneProperty().addListener((p, oldS, newS) -> {
            assert oldS == null;
            newS.addPreLayoutPulseListener(this::redrawIfNeeded);
        });
        // Handles the scroll
        LongProperty minScrollTime = new SimpleLongProperty();
        canvas.setOnScroll(event -> {
            int zoomDelta = event.getDeltaY() > 0 ? 1 : -1;
            long currentTime = System.currentTimeMillis();
            if (currentTime < minScrollTime.get()) return;
            minScrollTime.set(currentTime + 300);
            mapParameters.scroll(event.getX(), event.getY());
            mapParameters.changeZoomLevel(zoomDelta);
            mapParameters.scroll(-event.getX(), -event.getY());
            event.consume();
        });
        // Handles the mouse dragging
        canvas.setOnMousePressed(event -> {
            lastX = event.getX();
            lastY = event.getY();
            event.consume();
        });
        canvas.setOnMouseDragged(event -> {
            double deltaX = event.getX() - lastX;
            double deltaY = event.getY() - lastY;
            lastX = event.getX();
            lastY = event.getY();
            mapParameters.scroll(-deltaX, -deltaY);
            event.consume();
        });
    }
}