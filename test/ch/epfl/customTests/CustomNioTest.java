package ch.epfl.customTests;

import ch.epfl.javions.Preconditions;
import ch.epfl.javions.adsb.MessageParser;
import ch.epfl.javions.gui.TileManager;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import javafx.scene.image.Image;
import static org.junit.jupiter.api.Assertions.*;


public class CustomNioTest {

    @Test
    public void getImage() throws IOException {
        //Path base = Paths.get("C:/Users/matte/OneDrive/Bureau/JavaNIO");
        Path base = Paths.get("/Users/matteo/Desktop/perso/TileAtTest");
        String tileServer = "tile.openstreetmap.org";
        TileManager manager = new TileManager(base, tileServer);
        TileManager other = new TileManager(base, tileServer, 200, 0.9f);
        TileManager.TileId tileId1 = new TileManager.TileId(17, 67927, 46357);
        TileManager.TileId tileId2 = new TileManager.TileId(17, 67957, 42357);
        TileManager.TileId tileId3 = new TileManager.TileId(15555, 1245, -1);
        TileManager.TileId tileId4 = new TileManager.TileId(16, 5193, 1553);
        TileManager.TileId tileId5 = new TileManager.TileId(16, 5193, 1557);
        TileManager.TileId tileId1bis = new TileManager.TileId(17, 67927, 46358);
        Image image1 = manager.imageForTileAt(tileId1);
        Image image2 = manager.imageForTileAt(tileId2);
        Image image4 = manager.imageForTileAt(tileId4);
        Image image5 = manager.imageForTileAt(tileId5);
        Image image6 = manager.imageForTileAt(tileId1);
        Image image7 = manager.imageForTileAt(tileId1bis);
        manager.getCache();
        assertThrows(IllegalArgumentException.class, () -> manager.imageForTileAt(tileId3));
    }

    @Test
    public void areValid() {
        Path base = Paths.get("C:/Users/matte/OneDrive/Bureau/JavaNIO");
        String tileServer = "tile.openstreetmap.org";
        TileManager manager = new TileManager(base, tileServer);
        System.out.println(TileManager.TileId.isValid(0,0,0));
        System.out.println(TileManager.TileId.isValid(0,0,1));
        System.out.println(TileManager.TileId.isValid(1,0,0));
        System.out.println(TileManager.TileId.isValid(3, 0,7));
        System.out.println(TileManager.TileId.isValid(3, 0,8));
        System.out.println(TileManager.TileId.isValid(23, 0,7));
        System.out.println(TileManager.TileId.isValid(19, 5242887, 524287));
        System.out.println(TileManager.TileId.isValid(19, 5242888, 524287));
    }



    @Test
    public void createSub() throws IOException{
        Path base = Paths.get("C:/Users/matte/OneDrive/Bureau/JavaNIO");
        Path zoom = base.resolve("zoom");
        Path x = zoom.resolve("x");
        Path y = x.resolve("y");
        Path not = y.resolve("sub");
        System.out.println(Files.isDirectory(zoom));
        System.out.println(Files.isDirectory(x));
        System.out.println(Files.isDirectory(y));
        System.out.println(Files.exists(not));
        System.out.println(Files.isDirectory(not));
        System.out.println(Files.exists(zoom));
    }

    @Test
    public void t() throws IOException{
        Path base = Paths.get("C:/Users/matte/OneDrive/Bureau/JavaNIO/zoom");
        Path newX = base.resolve("x");
        //Files.createDirectory(newX);
    }

    @Test
    public void files() throws IOException {
        //path du dossier JavaNIO
        Path path = Paths.get("C:/Users/matte/OneDrive/Bureau/JavaNIO");

        //path du dossier poetry contenu dans le dossier javaNIO
        Path poetry = path.resolve("poetry");

        //crée le dossier poetry dans les fichiers ordinateur (s'il n'existe pas déjà)
        if (Files.exists(poetry)) {
            System.out.println("Directory poetry already exists");
        } else {
            Files.createDirectory(poetry);
        }

        //path du dossier poem.txt contenu dans poetry
        Path poem = poetry.resolve("poem.txt");

        //crée le fichier poem.txt s'il n'existe pas deja
        if (Files.exists(poem)) {
            System.out.println("poem.txt already exists!");
        }
        else {
            Files.createFile(poem);
        }

        Path sh = poetry.resolve("shakespear");
        if (Files.exists(sh)) {
            System.out.println("shakespear directory already exists");
        } else {
            Files.createDirectory(sh);
        }

        Path life = sh.resolve("life of shakespear");
        Path enfance = life.resolve("chapter 1");
        System.out.println(life);
        //Files.createDirectory(life);
        //Files.createFile(enfance);

    }
}
