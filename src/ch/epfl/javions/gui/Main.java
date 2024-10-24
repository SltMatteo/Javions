package ch.epfl.javions.gui;

import ch.epfl.javions.ByteString;
import ch.epfl.javions.adsb.Message;
import ch.epfl.javions.adsb.MessageParser;
import ch.epfl.javions.adsb.RawMessage;
import ch.epfl.javions.aircraft.AircraftDatabase;
import ch.epfl.javions.demodulation.AdsbDemodulator;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Orientation;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Contains the main program
 */
public final class Main extends Application {

    public static final int NANO_TO_MILLIS_CONVERSION = 1000000;
    public static final int DEFAULT_ZOOM_LEVEL = 8;
    public static final int DEFAULT_MIN_X = 33530;
    public static final int DEFAULT_MIN_Y = 23070;
    private static final int PRIMARY_STAGE_MIN_WIDTH = 800;
    private static final int PRIMARY_STAGE_MIN_HEIGHT = 600;
    private static final long SECOND_IN_NANOSECONDS = 1_000_000_000L; //in nanoSecond
    private static final String STAGE_TITLE = "Javions";

    private ConcurrentLinkedDeque<RawMessage> sharedQueue;
    private int messageCount;
    private final long startTime = System.nanoTime();
    private long lastPurge; // TimeStamp at which the last purge occurred

    /**
     * Runs the application by building the scene graph of the graphic interface
     *
     * @param args the arguments
     */
    public static void main(String[] args) {
        launch(args);
    }

    /**
     * Handles the two possible channels from which data can arrive: the AirSpy or the file with all
     * the messages
     *
     * @param primaryStage the primary stage for this application, onto which
     *                     the application scene can be set.
     *                     Applications may create other stages, if needed, but they will not be
     *                     primary stages.
     * @throws Exception in case something bad happens
     */
    @Override
    public void start(Stage primaryStage) throws Exception {
        // Get main args
        List<String> rawArgs = getParameters().getRaw();
        // Get the first arg, other are ignored
        final String firstArg = rawArgs.isEmpty() ? null : rawArgs.get(0);

        // Creation of the queue that will be shared between the animation handler (consumer) and the message reader (supplier)
        this.sharedQueue = new ConcurrentLinkedDeque<>();

        Supplier<RawMessage> supplier = Objects.isNull(firstArg) ? defaultInputSupplier() : binaryFileSupplier(firstArg);

        Thread main = new Thread(() -> {
            while (true) {
                long timeSinceStart = System.nanoTime() - startTime;
                RawMessage toAdd = supplier.get();
                if (Objects.isNull(toAdd)) continue; //only add valid messages
                if (toAdd.timeStampNs() > timeSinceStart) {
                    try {
                        Thread.sleep((toAdd.timeStampNs() - timeSinceStart) / NANO_TO_MILLIS_CONVERSION);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                sharedQueue.add(toAdd);
            }
        });

        main.setDaemon(true);
        main.start();

        // Create the main window
        createUI(primaryStage);
    }

    /**
     * Reads all the raw messages from the given file and returns them in an array list
     *
     * @param fileName the file name
     * @return the list with the raw messages
     * @throws IOException in case of input/output error
     */
    private static List<RawMessage> readAllMessages(String fileName) throws IOException {
        List<RawMessage> l = new ArrayList<>();
        try (DataInputStream s = new DataInputStream(
                new BufferedInputStream(
                        new FileInputStream(fileName)))) {
            byte[] bytes = new byte[RawMessage.LENGTH];

            while (true) {
                long timeStampNs = s.readLong();
                int bytesRead = s.readNBytes(bytes, 0, bytes.length);
                assert bytesRead == RawMessage.LENGTH;
                ByteString message = new ByteString(bytes);
                RawMessage m = new RawMessage(timeStampNs, message);
                l.add(m);
            }
        } catch (EOFException ignored) { }

        return l;
    }

    /**
     * Creates the user interface: map, table, aircraft states, status bar
     *
     * @param primaryStage the primary stage for this application, onto which
     *                     the application scene can be set.
     * @throws URISyntaxException if the URL cannot be parsed as a URI
     */
    private void createUI(Stage primaryStage) throws URISyntaxException {

        // Map settings
        Path tileCache = Path.of("tile-cache");
        TileManager tm =
                new TileManager(tileCache, "tile.openstreetmap.org");
        MapParameters mp =
                new MapParameters(DEFAULT_ZOOM_LEVEL, DEFAULT_MIN_X, DEFAULT_MIN_Y);  //default map location on launch
        BaseMapController bmc =
                new BaseMapController(tm, mp);

        // Database creation
        URL u = getClass().getResource("/aircraft.zip");
        assert u != null;
        Path p = Path.of(u.toURI());
        AircraftDatabase db = new AircraftDatabase(p.toString());

        // Aircraft controller and aircraft table setup
        AircraftStateManager asm = new AircraftStateManager(db);
        ObjectProperty<ObservableAircraftState> sap =
                new SimpleObjectProperty<>();
        AircraftController ac =
                new AircraftController(mp, asm.states(), sap);
        AircraftTableController atb =
                new AircraftTableController(asm.states(), sap);
        StatusLineController slc =
                new StatusLineController();
        slc.aircraftCountProperty().bind(Bindings.size(asm.states()));

        sap.addListener(l -> {
            Consumer<ObservableAircraftState> cs = x -> bmc.centerOn(x.getPosition());
            atb.setOnDoubleClick(cs);
        });

        // Map pane and table pane creation
        var mv = new StackPane(bmc.pane(), ac.pane()); // MapView
        var tv = new BorderPane(); // TableView
        tv.setCenter(atb.pane());
        tv.setTop(slc.pane());

        var root = new SplitPane(mv, tv);
        root.setOrientation(Orientation.VERTICAL);

        // Main window creation
        primaryStage.setScene(new Scene(root));
        primaryStage.setTitle(STAGE_TITLE);
        primaryStage.setMinWidth(PRIMARY_STAGE_MIN_WIDTH);
        primaryStage.setMinHeight(PRIMARY_STAGE_MIN_HEIGHT);
        primaryStage.show();

        // Animation kickoff
        launchAnimation(slc, asm);
    }

    /**
     * Handles the animation of the user interface
     *
     * @param slc the status line controller
     * @param asm the aircraft state manager
     */
    private void launchAnimation(StatusLineController slc, AircraftStateManager asm) {

        // creation and launch of the animation thread
        new AnimationTimer() {
            @Override
            public void handle(long now) {
                while (!sharedQueue.isEmpty()) {
                    Message m;
                    RawMessage current = sharedQueue.poll();
                    m = (Objects.isNull(current)) ? null : MessageParser.parse(current);
                    if (m != null) {
                        messageCount++;
                        slc.messageCountProperty().set(messageCount);
                        asm.updateWithMessage(m);
                    }
                    // Call it every second
                    if (now - lastPurge > SECOND_IN_NANOSECONDS) {
                        asm.purge();
                        lastPurge = now;
                    }
                }
            }
        }.start();
    }

    /**
     * returns the supplier getting the messages in real-time from an airspy
     * @return the supplier getting the messages in real-time from an airspy
     * @throws IOException in case of input/output error
     */
    private Supplier<RawMessage> defaultInputSupplier() throws IOException {
        final AdsbDemodulator demodulator = new AdsbDemodulator(System.in);
        return () -> {
            try {
                return demodulator.nextMessage();
            } catch (IOException e) {
                return null;
            }
        };
    }

    /**
     * returns the supplier getting the messages from a binary file
     * @param fileName the file name (must be an absolute path)
     * @return the supplier getting the messages from a binary file
     * @throws IOException in case of input/output error
     */
    private Supplier<RawMessage> binaryFileSupplier(String fileName) throws IOException {
        List<RawMessage> rawMessages = readAllMessages(fileName);
        final Iterator<RawMessage> it = rawMessages.iterator();
        return () -> (it.hasNext()) ? it.next() : null;
    }
}