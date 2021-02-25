package main;

import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.awt.*;

public class FlightPlanner extends Application {

    Text title = new Text("Flight Planner V1.0");
    Button exportFlight = new Button("Export\nFlight");
    Button saveFlight = new Button("Save\nFlight");
    Button loadFlight = new Button("Load\nFlight");

    public void start(Stage primaryStage) throws Exception{
        //Parent root = FXMLLoader.load(getClass().getResource("sample.fxml"));
        primaryStage.setTitle("FlightPlanner V1.0");

        FlightSettings fs = new FlightSettings();
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
        topGp.setPadding(new Insets(10,50,0,10));

        bp.setTop(topGp);
        bp.setLeft(FlightSettings.setupFlight());
        bp.setCenter(PathDrawer.createPathDrawer());
        //bp.setRight(PathDrawer.createPathDrawer());
        bp.setBottom(new Text("Created by Thomas Pound"));

        Scene main = new Scene(bp,1280,720);
        main.setUserAgentStylesheet("style/main.css");

        exportFlight.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {

            }
        });
        saveFlight.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {

            }
        });
        loadFlight.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {

            }
        });



        primaryStage.setScene(main);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
