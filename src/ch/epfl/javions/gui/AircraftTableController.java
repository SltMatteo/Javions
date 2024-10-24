package ch.epfl.javions.gui;

import ch.epfl.javions.Units;
import ch.epfl.javions.adsb.CallSign;
import javafx.beans.binding.Bindings;
import javafx.beans.property.*;
import javafx.collections.ObservableSet;
import javafx.collections.SetChangeListener;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.input.MouseButton;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Comparator;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

import static ch.epfl.javions.Units.*;
import static javafx.collections.FXCollections.unmodifiableObservableSet;
import static javafx.scene.control.TableView.CONSTRAINED_RESIZE_POLICY_SUBSEQUENT_COLUMNS;

/**
 * This class represent the table on which all the aircraft states and their information will be displayed
 */
public final class AircraftTableController {

    private static final String EMPTY_STRING = "";
    private static final int NUMERICAL_COLUMNS_PREF_WIDTH = 85;
    private static final int OACI_PREF_WIDTH = 60;
    private static final int CALLSIGN_PREF_WIDTH = 70;
    private static final int REGISTRATION_PREF_WIDTH = 90;
    private static final int MODEL_PREF_WIDTH = 230;
    private static final int TYPE_PREF_WIDTH = 50;
    private static final int DESCRIPTION_PREF_WIDTH = 70;

    private final ObservableSet<ObservableAircraftState> states;
    private final ObjectProperty<ObservableAircraftState> selectedAircraftState;
    private final TableView<ObservableAircraftState> tableView;
    private Consumer<ObservableAircraftState> cs;

    /**
     * Instantiate the table and creates it with all the information it already has
     * @param states                the set with the aircraft states
     * @param selectedAircraftState the selected aircraft state
     */
    public AircraftTableController(ObservableSet<ObservableAircraftState> states,
                                   ObjectProperty<ObservableAircraftState> selectedAircraftState) {
        this.states = unmodifiableObservableSet(states);
        this.selectedAircraftState = selectedAircraftState;
        this.tableView = new TableView<>();
        this.tableView.getStylesheets().add("table.css");
        this.tableView.setColumnResizePolicy(CONSTRAINED_RESIZE_POLICY_SUBSEQUENT_COLUMNS);
        this.tableView.setTableMenuButtonVisible(true);
        installTextualColumns();
        installNumericalColumns();
        installListeners();

    }

    /**
     * Returns the tableview on which the table is shown
     *
     * @return the tableview on which the table is shown
     */
    public TableView<ObservableAircraftState> pane() {
        return tableView;
    }


    /**
     * sets the consumer to the one given in argument
     * @param consumer the consumer
     */
    void setOnDoubleClick(Consumer<ObservableAircraftState> consumer) {
        this.cs = consumer;
    }

    /**
     * Installs all the required listeners
     */
    private void installListeners() {
        states.addListener((SetChangeListener<ObservableAircraftState>) change -> {
                    if (change.wasAdded()) {
                        tableView.getItems().add(change.getElementAdded());
                        tableView.sort();
                    } else if (change.wasRemoved()) {
                        tableView.getItems().remove(change.getElementRemoved());
                    }
                }
        );
        selectedAircraftState.addListener((p, o, n) -> {
            tableView.getSelectionModel().select(selectedAircraftState.get());
            tableView.scrollTo(selectedAircraftState.get());
        });

        tableView.getSelectionModel().selectedItemProperty().addListener(s ->
                selectedAircraftState.setValue(tableView.getSelectionModel().getSelectedItem()));

        tableView.setOnMouseClicked(event -> {
            int selectedItemIndex = tableView.getSelectionModel().getSelectedIndex();
            tableView.scrollTo(selectedItemIndex);
            if (event.getClickCount() > 1 && event.getButton() == MouseButton.PRIMARY) {
                cs.accept(selectedAircraftState.get());
                event.consume();
            }
        });
    }

    /**
     * Creates the textual columns displayed on the table
     */
    private void installTextualColumns() {
        tableView.getColumns().add(createColumn("OACI", OACI_PREF_WIDTH, f -> f.getAddress().string()));
        TableColumn<ObservableAircraftState, String> callSignColumn = new TableColumn<>("Indicatif");
        callSignColumn.setPrefWidth(CALLSIGN_PREF_WIDTH);
        callSignColumn.setCellValueFactory(f -> f.getValue().callSignProperty().map(CallSign::string));
        tableView.getColumns().add(callSignColumn);
        tableView.getColumns().add(createColumn("Immatriculation", REGISTRATION_PREF_WIDTH,
                f -> Objects.isNull(f.getData()) ? EMPTY_STRING : f.getData().registration().string()));
        tableView.getColumns().add(createColumn("Modèle", MODEL_PREF_WIDTH,
                f -> Objects.isNull(f.getData()) ? EMPTY_STRING : f.getData().model()));
        tableView.getColumns().add(createColumn("Type", TYPE_PREF_WIDTH,
                f -> Objects.isNull(f.getData()) ? EMPTY_STRING : f.getData().typeDesignator().string()));
        tableView.getColumns().add(createColumn("Description", DESCRIPTION_PREF_WIDTH,
                f -> Objects.isNull(f.getData()) ? EMPTY_STRING : f.getData().description().string()));
    }

    /**
     * Creates the numerical columns displayed on the table
     */
    private void installNumericalColumns() {
        installLongitudeAndLatitudeColumns();
        installAltitudeAndVelocityColumns();
    }

    /**
     * Crates the columns for the latitude and longitude
     */
    private void installLongitudeAndLatitudeColumns() {
        NumberFormat format = NumberFormat.getInstance();
        format.setMinimumFractionDigits(4);
        format.setMaximumFractionDigits(4);

        TableColumn<ObservableAircraftState, String> longitudeColumn = new TableColumn<>("Longitude(°)");
        longitudeColumn.getStyleClass().add("numeric");
        longitudeColumn.setCellValueFactory(f ->
                Bindings.createStringBinding(() -> {
                    double longitudeDegrees =
                            Units.convert(f.getValue().getPosition().longitude(), Angle.RADIAN, Angle.DEGREE);
                    return format.format(longitudeDegrees);
                }, f.getValue().geoPositionProperty()));

        TableColumn<ObservableAircraftState, String> latitudeColumn = new TableColumn<>("Latitude(°)");
        latitudeColumn.getStyleClass().add("numeric");
        latitudeColumn.setCellValueFactory(f ->
                Bindings.createStringBinding(() -> {
                    double latitudeDegrees =
                            Units.convert(f.getValue().getPosition().latitude(), Angle.RADIAN, Angle.DEGREE);
                    return format.format(latitudeDegrees);
                }, f.getValue().geoPositionProperty()));

        longitudeColumn.setComparator(numberColumnsComparator(format));
        latitudeColumn.setComparator(numberColumnsComparator(format));
        longitudeColumn.setPrefWidth(NUMERICAL_COLUMNS_PREF_WIDTH);
        latitudeColumn.setPrefWidth(NUMERICAL_COLUMNS_PREF_WIDTH);
        tableView.getColumns().add(longitudeColumn);
        tableView.getColumns().add(latitudeColumn);
    }

    /**
     * creates the columns for the altitude and the velocity
     */
    private void installAltitudeAndVelocityColumns() {
        //for alt and speed
        NumberFormat format = NumberFormat.getInstance();
        format.setMinimumFractionDigits(0);
        format.setMaximumFractionDigits(0);
        TableColumn<ObservableAircraftState, String> altitudeColumn = new TableColumn<>("Altitude(m)");
        altitudeColumn.getStyleClass().add("numeric");
        altitudeColumn.setCellValueFactory(f ->
                Bindings.createStringBinding(() ->
                        format.format(f.getValue().getAltitude()), f.getValue().altitudeProperty()));
        TableColumn<ObservableAircraftState, String> velocityColumn = new TableColumn<>("Vitesse(km/h)");
        velocityColumn.getStyleClass().add("numeric");
        velocityColumn.setCellValueFactory(f -> {
            double velocityKMH = Units.convertTo(f.getValue().getVelocity(), Speed.KILOMETER_PER_HOUR);
            return Bindings.createStringBinding(() -> format.format(velocityKMH), f.getValue().velocityProperty());
        });

        velocityColumn.setComparator(numberColumnsComparator(format));
        altitudeColumn.setComparator(numberColumnsComparator(format));
        velocityColumn.setPrefWidth(NUMERICAL_COLUMNS_PREF_WIDTH);
        altitudeColumn.setPrefWidth(NUMERICAL_COLUMNS_PREF_WIDTH);
        tableView.getColumns().add(altitudeColumn);
        tableView.getColumns().add(velocityColumn);
    }

    /**
     * Creates the textual column according to the given information
     *
     * @param name        the name of the column
     * @param prefWidth   the width of the column
     * @param valueGetter the function the column must follow
     * @return the textual column according to te given information
     */
    private TableColumn<ObservableAircraftState, String> createColumn(String name, int prefWidth,
                                                                      Function<ObservableAircraftState,
                                                                              String> valueGetter) {
        TableColumn<ObservableAircraftState, String> column = new TableColumn<>(name);
        column.setPrefWidth(prefWidth);
        column.setCellValueFactory(f -> new ReadOnlyObjectWrapper<>(valueGetter.apply(f.getValue())));
        return column;
    }


    /**
     * creates the comparator used for sorting the numeric columns
     * @param format the number format used for the numbers in the columns
     * @return the comparator
     */
    Comparator<String> numberColumnsComparator(NumberFormat format) {
        return (s1, s2) -> {
            if (s1.isEmpty() || s2.isEmpty())
                return s1.compareTo(s2);
            try {
                double ns1 = format.parse(s1).doubleValue();
                double ns2 = format.parse(s2).doubleValue();
                return Double.compare(ns1, ns2);
            } catch (ParseException e) {
                return 0;
            }
        };
    }
}