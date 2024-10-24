package ch.epfl.customTests;

import ch.epfl.javions.WebMercator;
import ch.epfl.javions.gui.BaseMapController;
import ch.epfl.javions.gui.MapParameters;
import ch.epfl.javions.gui.TileManager;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.nio.file.Path;

public final class TestMapBaseController extends Application {
    public static void main(String[] args) { launch(args); }

    @Override
    public void start(Stage primaryStage) {
        Path tileCache = Path.of("tile-cache");
        TileManager tm =
                new TileManager(tileCache, "tile.openstreetmap.org");
        MapParameters mp =
               new MapParameters(17, 17_389_327, 11_867_430);
                //new MapParameters(10, 528*256, 361*256);
        BaseMapController bmc = new BaseMapController(tm, mp);
        BorderPane root = new BorderPane(bmc.pane());
        primaryStage.setScene(new Scene(root));
        primaryStage.show();
    }

}
