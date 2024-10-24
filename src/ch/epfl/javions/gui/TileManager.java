package ch.epfl.javions.gui;

import ch.epfl.javions.Preconditions;
import javafx.scene.image.Image;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.LinkedHashMap;

/**
 * TileManager is the object that fetches openstreetmap tiles from the OSM servers,
 * stores them efficiently, and returns them when asked for
 */

public final class TileManager {

    /**
     * Represents an openstreetmap tile; nested in TileManager because the manager will fetch and store tile images using these ids
     *
     * @param zoom the zoom level of the tile
     * @param x    the x coordinate
     * @param y    the y coordinate
     */

    public record TileId(int zoom, int x, int y) {

        private static final int MAX_ZOOM = 19; // Openstreetmap tiles max zoom

        /**
         * Checks if a tileId is valid (meaning the tile exists in openstreetmap)
         *
         * @param zoom the zoom level of the tile we are checking
         * @param x    the x coordinate of the tile we are checking
         * @param y    teh y coordinate of the tile we are checking
         * @return true iff the tileId is valid
         */
        public static boolean isValid(int zoom, int x, int y) {
            int maxIndex = (int) (Math.pow(2, zoom)) - 1;
            return (0 <= x && x <= maxIndex && 0 <= y && y <= maxIndex && 0 <= zoom && zoom <= MAX_ZOOM);
        }

        /**
         * Checks if a tileId is valid (meaning the tile exists in openstreetmap)
         *
         * @param tileId the TileId of the tile we are checking
         * @return true iff the TileId is valid
         * <p>
         * note : this is a duplicate of the previous method except it takes a TileId object as the argument
         * except of the attributes of the tile id
         */
        public static boolean isValid(TileId tileId) {
            int maxIndex = (int) (Math.pow(2, tileId.zoom));
            return (0 <= tileId.x() && tileId.x() < maxIndex && 0 <= tileId.y() && tileId.y() < maxIndex &&
                    0 <= tileId.zoom() && tileId.zoom() <= MAX_ZOOM);

        }

        /**
         * Returns the TileId of the tile at a given zoom level and webMercator coordinates
         *
         * @param zoom         the zoom level
         * @param xWebMercator the x coordinate
         * @param yWebMercator the y coordinate
         * @return the TileId of the tile containing the given point
         */
        public static TileId tileAt(int zoom, double xWebMercator, double yWebMercator) {
            int tileX = (int) (Math.floor(xWebMercator / 256));
            int tileY = (int) (Math.floor(yWebMercator / 256));
            return new TileId(zoom, tileX, tileY);
        }
    }


    //the absolute path of the disk cache
    private final Path path;

    //the address of the server we fetch tiles from
    private final String tileServer;

    //the memory cache
    private final LinkedHashMap<TileId, Image> cache;
    private static final int MEMORY_CACHE_SIZE = 100;
    private static final float DEFAULT_LOAD_FACTOR = 0.75f;

    /**
     * Creates a tile manager
     *
     * @param path       the path of the disk cache
     * @param tileServer the osm server we will download images from
     */

    public TileManager(Path path, String tileServer) {
        this.path = path;
        this.tileServer = tileServer;
        cache = new LinkedHashMap<>(MEMORY_CACHE_SIZE, DEFAULT_LOAD_FACTOR, true);
    }

    /**
     * Returns the image of a tile
     *
     * @param tileId the TileId of the tile
     * @return the image of the tile
     * @throws IOException in case of input/output error
     */
    public Image imageForTileAt(TileId tileId) throws IOException {
        Preconditions.checkArgument(TileId.isValid(tileId),"invalid tile ID: " + tileId );

        // immediately returns the image if it's in the memory cache
        if (cache.containsKey(tileId)) {
            return cache.get(tileId);
        }

        // checks if the image is in the disk cache and returns if it is
        Path zoom = path.resolve(String.valueOf(tileId.zoom));
        Path x = zoom.resolve(String.valueOf(tileId.x));
        Path y = x.resolve(String.valueOf(tileId.y) + ".png");
        if (Files.exists(y)) {
            // it exists in the cache -> returns it
            return imageFromDiskCache(y, tileId);
        } else {
            if (!Files.exists(zoom))
                Files.createDirectory(zoom);
            if (!Files.exists(x))
                Files.createDirectory(x);
        }

        //if the image is not in the memory cache nor in the disk cache,
        // download it from the server and  place it in both the caches
        Image image;
        byte[] imageBytes;
        URL u = new URL(convertTileIdToURL(tileId));
        URLConnection c = u.openConnection();
        c.setRequestProperty("User-Agent", "Javions");
        try (InputStream i = c.getInputStream()) {
            imageBytes = i.readAllBytes();
            try (FileOutputStream o = new FileOutputStream(y.toFile())) {
                //places the image in the disk cache
                o.write(imageBytes);
            }
            image = new Image(new ByteArrayInputStream(imageBytes));
            //places the image in the memory cache
            AddImageInMemoryCache(tileId, image);
            return image;
        }
    }

    /**
     * Gives the url leading to the image of a tile
     *
     * @param tileId the TileId of the tile we want the url to
     * @return the string representing the url of the tile's image
     */
    private String convertTileIdToURL(TileId tileId) {
        return "https://" + tileServer + "/" + tileId.zoom() + "/" + tileId.x() + "/" + tileId.y() + ".png";
    }

    /**
     * Returns the image of a tile stored in the disk cache
     *
     * @param filePath the path of the disk cache
     * @param tileId   the TileId of the tile we are looking for
     * @return the image of the tile
     * @throws IOException in case of input/output error
     */
    private Image imageFromDiskCache(Path filePath, TileId tileId) throws IOException {
        try (InputStream is = new FileInputStream(filePath.toFile())) {
            Image image = new Image(is);
            AddImageInMemoryCache(tileId, image);
            return image;
        }
    }

    /**
     * Adds the image of a tile in the memory cache, after deleting the LRU image if the cache was full
     *
     * @param tileId the TileId of the tile we add in the cache
     * @param image  the image of the tile we add in the cache
     */
    private void AddImageInMemoryCache(TileId tileId, Image image) {
        // frees space on the cache, then adds the image in it
        Iterator<TileId> it = cache.keySet().iterator();
        while (cache.size() >= MEMORY_CACHE_SIZE)
            cache.remove(it.next());
        cache.put(tileId, image);
    }
}