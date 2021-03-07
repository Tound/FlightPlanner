package main;

import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class FlightPlanner extends Application {

    private Text title = new Text("Flight Planner V1.0");
    private Button exportFlight = new Button("Export\nFlight");
    private Button saveFlight = new Button("Save\nFlight");
    private Button loadFlight = new Button("Load\nFlight");
    private static Stage stage;
    private PathDrawer pathDrawer;

    public void start(Stage primaryStage) throws Exception{
        //Parent root = FXMLLoader.load(getClass().getResource("sample.fxml"));
        stage = primaryStage;
        primaryStage.setTitle("FlightPlanner V1.0");

        FlightSettings fs = new FlightSettings();
        pathDrawer = new PathDrawer();

        BorderPane bp = new BorderPane();
        bp.setId("borderpane");
        GridPane topGp = new GridPane();
        topGp.setHgap(30);

        title.setId("title");

        topGp.add(title,0,0);
        topGp.add(exportFlight,1,0);

        topGp.add(saveFlight,2,0);
        topGp.add(loadFlight,3,0);
        topGp.setHgap(50);
        topGp.setPadding(new Insets(10,50,5,10));

        bp.setTop(topGp);
        bp.setLeft(fs.setupFlight());
        bp.setCenter(pathDrawer.createPathDrawer(bp));
        //bp.setRight(PathDrawer.createPathDrawer());
        bp.setBottom(new Text("Created by Thomas Pound"));

        Scene main = new Scene(bp,1280,720);
        main.setUserAgentStylesheet("style/main.css");

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

            }
        });
        //Load project
        loadFlight.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {

            }
        });

        primaryStage.setScene(main);
        primaryStage.show();
    }

    public void exportAFlight(){
        //Export flight menu?
        //File export = new File();
        //export.write();


    }

    public static Stage getStage() {
        return stage;
    }
    public static PathDrawer getPathDrawer(){return pathDrawer;}

    public static void main(String[] args) {
        launch(args);
    }
}
