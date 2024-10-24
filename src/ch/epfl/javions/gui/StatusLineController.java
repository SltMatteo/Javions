package ch.epfl.javions.gui;

import javafx.beans.binding.Bindings;
import javafx.beans.property.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;

/**
 * Handles the status bar on which will be displayed the number of messages received since the
 * start of the program, and the number of currently visible airplanes
 */
public final class StatusLineController {

    private static final String STATUS_SHEET = "status.css";
    
    private final BorderPane pane;
    private final IntegerProperty aircraftCount;
    private final LongProperty messageCount;

    public StatusLineController() {
        this.pane = new BorderPane();
        this.pane.getStylesheets().add(STATUS_SHEET);
        this.aircraftCount = new SimpleIntegerProperty();
        this.messageCount = new SimpleLongProperty();
        Text aircraftCountText = new Text();
        Text messageCountText = new Text();
        aircraftCountText.textProperty().bind(Bindings.concat("Aéronefs visibles : ", aircraftCount.asString()));
        messageCountText.textProperty().bind(Bindings.concat("Messages reçus: ", messageCount.asString()));
        this.pane.setLeft(aircraftCountText);
        this.pane.setRight(messageCountText);
    }

    /**
     * Returns the JavaFX pane on which the status bar is shown
     *
     * @return the pane on which the status bar is shown
     */
    public Pane pane() {
        return pane;
    }

    /**
     * Returns the property containing the number of visible aircraft states
     *
     * @return the property containing the number of visible aircraft states
     */
    public IntegerProperty aircraftCountProperty() {
        return aircraftCount;
    }

    /**
     * Returns the property containing the number of messages received
     *
     * @return the property containing the number of messages received
     */
    public LongProperty messageCountProperty() {
        return messageCount;
    }
}