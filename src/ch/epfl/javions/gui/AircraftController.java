package ch.epfl.javions.gui;

import ch.epfl.javions.adsb.CallSign;
import ch.epfl.javions.aircraft.AircraftDescription;
import ch.epfl.javions.aircraft.AircraftTypeDesignator;
import ch.epfl.javions.aircraft.WakeTurbulenceCategory;
import javafx.beans.InvalidationListener;
import javafx.beans.binding.*;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyDoubleWrapper;
import javafx.beans.value.ChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.ObservableSet;
import javafx.collections.SetChangeListener;
import javafx.scene.Group;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.SVGPath;
import javafx.scene.text.Text;

import java.util.Objects;

import static ch.epfl.javions.Units.*;
import static ch.epfl.javions.WebMercator.x;
import static ch.epfl.javions.WebMercator.y;
import static javafx.collections.FXCollections.unmodifiableObservableSet;
import static javafx.scene.paint.CycleMethod.NO_CYCLE;

/**
 * This class handles the view of all the aircraft displayed on the map
 * meaning all aircraft which states are known
 */

public class AircraftController {

    private static final int TEXT_ALIGNMENT_WIDTH = 4;
    private static final int AIRCRAFT_LABEL_ZOOM_THRESHOLD = 11;
    private static final String AIRCRAFT_SHEET = "aircraft.css";

    private final MapParameters mapParameters;
    private final ObservableSet<ObservableAircraftState> states;
    private final ObjectProperty<ObservableAircraftState> selectedAircraftState;
    private final Pane pane;

    /**
     * @param mapParameters         the parameters of the map
     * @param states                the view of the aircraft states
     * @param selectedAircraftState the selected aircraft state
     */

    public AircraftController(MapParameters mapParameters,
                              ObservableSet<ObservableAircraftState> states,
                              ObjectProperty<ObservableAircraftState> selectedAircraftState) {
        this.mapParameters = mapParameters;
        this.states = unmodifiableObservableSet(states);
        this.selectedAircraftState = selectedAircraftState;
        this.pane = new Pane();
        this.pane.setPickOnBounds(false);
        this.pane.getStylesheets().add(AIRCRAFT_SHEET);
        installListeners();
    }

    /**
     * Returns the JavaFX pane on which the aircraft states are shown
     *
     * @return the pane on which the aircraft states are shown
     */

    public Pane pane() {
        return pane;
    }

    /**
     * Creates the icon for the given aircraft state based on its information
     *
     * @param o the observable aircraft state
     * @return the icon for the aircraft
     */

    private SVGPath createIcon(ObservableAircraftState o) {
        SVGPath icon = new SVGPath();
        ObjectBinding<AircraftIcon> aircraftIconObjectBinding;

        if (o.getData() == null) {
            aircraftIconObjectBinding = Bindings.createObjectBinding(() ->
                    AircraftIcon.iconFor(new AircraftTypeDesignator(""), new AircraftDescription(""),
                    o.getCategory(), WakeTurbulenceCategory.UNKNOWN),
                    o.categoryProperty());
        } else {
            aircraftIconObjectBinding = Bindings.createObjectBinding(() ->
                    AircraftIcon.iconFor(o.getData().typeDesignator(), o.getData().description(), o.getCategory(),
                    o.getData().wakeTurbulenceCategory()),
                    o.categoryProperty());
        }

        icon.contentProperty().bind(Bindings.createStringBinding(() ->
                aircraftIconObjectBinding.get().svgPath(), aircraftIconObjectBinding.asString()));
        icon.getStyleClass().add("aircraft");
        icon.setFill(getColorForAltitude(o.getAltitude()));
        icon.fillProperty().bind(Bindings.createObjectBinding(() ->
                        getColorForAltitude(o.getAltitude()),
                o.altitudeProperty()
        ));

        if (aircraftIconObjectBinding.get().canRotate()) {
            icon.rotateProperty().bind(Bindings.createDoubleBinding(() ->
                    convert(o.getTrackOrHeading(), Angle.RADIAN, Angle.DEGREE), o.trackOrHeadingProperty()));
        } else {
            icon.setRotate(0);
        }

        //clicking on an icon make that aircraft get selected, and clicking it again removes the selection
        icon.setOnMousePressed(event -> {
            if (Objects.isNull(selectedAircraftState.getValue())) {
                selectedAircraftState.setValue(o);
            } else {
                if (selectedAircraftState.getValue().equals(o)) {
                    selectedAircraftState.setValue(null);
                } else selectedAircraftState.setValue(o);
            }
            event.consume();
        });

        return icon;
    }

    /**
     * Creates the label for the given aircraft state based on its information
     *
     * @param o the observable aircraft state we create the label of
     * @return the javafx group containing the label for the aircraft
     */

    private Group createLabel(ObservableAircraftState o) {
        Group label = new Group();
        label.getStyleClass().add("label");

        Text text = new Text();
        Text velocityText = new Text();
        Text altitudeText = new Text();
        Text labelText = new Text();

        altitudeText.textProperty().bind(Bindings.createStringBinding(() ->
                String.format("%.0f m", (o.getAltitude())), o.altitudeProperty()));
        velocityText.textProperty().bind(Bindings.createStringBinding(() ->
                        Double.isNaN(o.getVelocity()) ? "? km/h" :
                                String.format("%.0f km/h", (convertTo(o.getVelocity(), Speed.KILOMETER_PER_HOUR))),
                o.velocityProperty()));

        if (o.getData() != null) {
            labelText.setText(o.getData().registration().string());
        } else {
            labelText.textProperty().bind(Bindings.when(o.callSignProperty().isNotNull())
                    .then(Bindings.convert(o.callSignProperty().map(CallSign::string)))
                    .otherwise(
                    o.getAddress().string()));
        }

        text.textProperty().bind(Bindings.createStringBinding(() ->
                        labelText.getText() + "\n" + velocityText.getText() + "\u2002" + altitudeText.getText(),
                velocityText.textProperty(), altitudeText.textProperty()));

        Rectangle rectangle = new Rectangle();
        rectangle.widthProperty().bind(text.layoutBoundsProperty().map(b -> b.getWidth() + TEXT_ALIGNMENT_WIDTH));
        rectangle.heightProperty().bind(text.layoutBoundsProperty().map(b -> b.getHeight() + TEXT_ALIGNMENT_WIDTH));

        label.getChildren().add(rectangle);
        label.getChildren().add(text);

        label.visibleProperty().bind(selectedAircraftState.isEqualTo(o).or((mapParameters.zoomProperty()
                .greaterThanOrEqualTo(AIRCRAFT_LABEL_ZOOM_THRESHOLD))));

        return label;
    }

    /**
     * Sets the layout for the icon and label of the aircraft state
     *
     * @param o the observable aircraft state we create the label and icon of
     * @return the javafx group containing the icon and the label
     */

    private Group createIconAndLabel(ObservableAircraftState o) {
        Group iconAndLabel = new Group(createIcon(o), createLabel(o));

        DoubleBinding xPos = Bindings.createDoubleBinding(() -> x(mapParameters.getZoom(), o.getPosition().longitude()) - mapParameters.getMinX(),
                o.geoPositionProperty(),
                mapParameters.zoomProperty(),
                mapParameters.minXProperty()
        );

        DoubleBinding yPos = Bindings.createDoubleBinding(() -> y(mapParameters.getZoom(), o.getPosition().latitude()) - mapParameters.getMinY(),
                o.geoPositionProperty(),
                mapParameters.zoomProperty(),
                mapParameters.minYProperty()
        );

        iconAndLabel.layoutXProperty().bind(xPos);
        iconAndLabel.layoutYProperty().bind(yPos);

        return iconAndLabel;
    }

    /**
     * Auxiliary method that calculates and creates the line that will then be displayed as the trajectory
     * @param trajectoryGroup the JavaFX group in which the trajectory lines are created
     * @param o the state of the aircraft we are drawing the trajectory of
     */

    private void calculateTrajectory(Group trajectoryGroup, ObservableAircraftState o) {
        ObservableList<ObservableAircraftState.AirbornePos> trajectory = o.getTrajectory();

        for (int i = 2; i < trajectory.size(); i++) {
            ObservableAircraftState.AirbornePos previous = trajectory.get(i - 1);
            ObservableAircraftState.AirbornePos current = trajectory.get(i);
            Line line = new Line();
            if (previous.altitude() == current.altitude()) {
                line.setStroke(getColorForAltitude(current.altitude()));
            } else {
                Stop s1 = new Stop(0, getColorForAltitude(previous.altitude()));
                Stop s2 = new Stop(1, getColorForAltitude(current.altitude()));
                LinearGradient gradient = new LinearGradient(
                        0, 0, 1, 0, true, NO_CYCLE, s1, s2);
                line.setStroke(gradient);
            }
            int zoom = mapParameters.getZoom();
            double startX = x(zoom, previous.geoPos().longitude());
            double startY = y(zoom, previous.geoPos().latitude());
            double endX = x(zoom, current.geoPos().longitude());
            double endY = y(zoom, current.geoPos().latitude());
            line.setStartX(startX);
            line.setStartY(startY);
            line.setEndX(endX);
            line.setEndY(endY);
            trajectoryGroup.getChildren().add(line);
        }
    }

    /**
     * Displays the trajectory previously calculated at the correct position on the screen
     *
     * @param o the observable aircraft state
     * @return the trajectory for the aircraft
     */

    private Group createTrajectoryWithLayout(ObservableAircraftState o) {
        Group trajectory = new Group();
        trajectory.getStyleClass().add("trajectory");

        trajectory.visibleProperty().bind(selectedAircraftState.isEqualTo(o));
        DoubleBinding actualXBinding = Bindings.createDoubleBinding(trajectory::getLayoutX);
        DoubleBinding actualYBinding = Bindings.createDoubleBinding(trajectory::getLayoutY);

        trajectory.visibleProperty().addListener((ChangeListener<? super Boolean>) (l, oldValue, newValue) -> {
            if (oldValue)
                trajectory.getChildren().clear();
            else {
                o.getTrajectory().addListener((InvalidationListener) m -> calculateTrajectory(trajectory, o));
                ReadOnlyDoubleWrapper minX = new ReadOnlyDoubleWrapper();
                ReadOnlyDoubleWrapper minY = new ReadOnlyDoubleWrapper();
                minX.bind(mapParameters.minXProperty());
                minY.bind(mapParameters.minYProperty());
                mapParameters.zoomProperty().addListener(m -> trajectory.getChildren().clear());
                trajectory.layoutXProperty().bind(Bindings.createDoubleBinding(() ->
                        actualXBinding.get() - minX.get(), actualXBinding, minX));
                trajectory.layoutYProperty().bind(Bindings.createDoubleBinding(() ->
                        actualYBinding.get() - minY.get(), actualYBinding, minY));
            }
        });

        return trajectory;
    }

    /**
     * Sets the ID and creates a noted aircraft by creating all its components: icon, label, trajectory line
     *
     * @param o the observable aircraft state
     * @return the noted aircraft
     */

    private Group createNotedAircraft(ObservableAircraftState o) {
        Group notedAircraft = new Group(createTrajectoryWithLayout(o), createIconAndLabel(o));
        notedAircraft.setId(o.getAddress().toString());
        notedAircraft.viewOrderProperty().bind(o.altitudeProperty().negate());
        return notedAircraft;
    }

    /**
     * Installs all needed listeners for this class
     */

    private void installListeners() {
        states.addListener((SetChangeListener<ObservableAircraftState>) change -> {
                    if (change.wasAdded()) {
                        Group g = createNotedAircraft(change.getElementAdded());
                        pane.getChildren().add(g);
                    } else if (change.wasRemoved()) {
                        // Handles the case where the aircraft that disappears was selected
                        if (Objects.nonNull(selectedAircraftState.getValue()) &&
                                selectedAircraftState.getValue().equals(change.getElementRemoved()))
                            selectedAircraftState.setValue(null);
                        pane.getChildren().removeIf(p ->
                                change.getElementRemoved().getAddress().toString().equals(p.getId()));
                    }
                }
        );
    }

    /**
     * Returns the color for the given altitude
     *
     * @param alt the altitude
     * @return the color for the altitude
     */
    
    private Color getColorForAltitude(double alt) {
        return ColorRamp.PLASMA.at(Math.pow(alt / 12000d, 1d / 3));
    }
}