package main;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class FlightPlanner extends Application {

    Text title = new Text("Flight Planner V1.0");
    Button saveFLight = new Button("Save \nFlight");
    Button saveCam = new Button("Save Camera");
    Button saveUAV = new Button("Save UAV");


    public void start(Stage primaryStage) throws Exception{
        //Parent root = FXMLLoader.load(getClass().getResource("sample.fxml"));
        primaryStage.setTitle("FlightPlanner V1.0");
        BorderPane bp = new BorderPane();


        ScrollPane scrollpane = new ScrollPane();

        bp.setTop(title);
        bp.setLeft(scrollpane);
        //bp.setRight();
        //bp.setBottom();











        Scene main = new Scene(bp,1280,720);



        primaryStage.setScene(main);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
