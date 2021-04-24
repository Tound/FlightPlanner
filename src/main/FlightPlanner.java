package main;

import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class FlightPlanner extends Application {

    private Text title = new Text("FLIGHT PLANNER V1.0");                // Set the title text
    private Button exportFlight = new Button("Export\nFlight");     // Create button to export a flight
    private Button saveFlight = new Button("Save\nFlight");         // Create button to save a flight
    private Button loadFlight = new Button("Load\nFlight");         // Create button to load a flight
    private static Stage stage;
    private static PathDrawer pathDrawer;
    private static FlightSettings flightSettings;

    public void start(Stage primaryStage) throws Exception{
        //Parent root = FXMLLoader.load(getClass().getResource("sample.fxml"));
        stage = primaryStage;
        primaryStage.setTitle("FlightPlanner V1.0");        // Set the title

        pathDrawer = new PathDrawer();                      // Create path planner object
        flightSettings = new FlightSettings(pathDrawer);    // Create flight settings object

        BorderPane bp = new BorderPane();                   // Create the borderpane for the layout of the screen
        bp.setId("borderpane");                             // Set the style ID of the borderpane
        GridPane topGp = new GridPane();                    // Create gridpane for the top of the screen
        topGp.setHgap(30);                                  // Set the horizontal gap for the items in the gridpane

        title.setId("title");                               // Set the CSS style for the title

        topGp.setMaxWidth(Double.MAX_VALUE);
        exportFlight.setMaxWidth(Double.MAX_VALUE);
        saveFlight.setMaxWidth(Double.MAX_VALUE);
        loadFlight.setMaxWidth(Double.MAX_VALUE);

        // Add elements to the gridpane
        topGp.add(title,0,0);
        topGp.add(exportFlight,1,0);
        topGp.add(saveFlight,2,0);
        topGp.add(loadFlight,3,0);
        topGp.setHgap(50);
        topGp.setPadding(new Insets(10,50,5,10));

        // Add all elements to border pane
        bp.setTop(topGp);                                   // Add the gridpane to the top of the screen
        bp.setLeft(flightSettings.setupFlight());           // Add flight settings object to the left of the screen
        bp.setCenter(pathDrawer.createPathDrawer(bp));      // Add the path drawer object to the center of the screen
        // Add the about label to the bottom of the screen
        bp.setBottom(new Label("Created by Thomas Pound for the MEng Project Autopilot for Aerial Photography"));

        Scene main = new Scene(bp,1280,720);    // Create new scene
        main.setUserAgentStylesheet("style/main.css");      // Set style sheet

        //Export waypoint file
        exportFlight.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                exportAFlight();
            }
        });

        //Save project
        saveFlight.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                System.out.println("Saving flights needs to be implemented");
            }
        });
        //Load project
        loadFlight.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                System.out.println("Loading flight needs to be implemented");
            }
        });

        primaryStage.setScene(main);
        primaryStage.show();
    }

    public void exportAFlight(){
        System.out.println("Export flight needs to be implemented");
    }

    public static Stage getStage() { return stage; }
    public static PathDrawer getPathDrawer(){return pathDrawer;}
    public static FlightSettings getFlightSettings(){ return flightSettings; }

    public static void main(String[] args) { launch(args); }
}
