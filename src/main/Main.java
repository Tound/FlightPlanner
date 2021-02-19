package main;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class Main extends Application {

    public void start(Stage primaryStage) throws Exception{
        //Parent root = FXMLLoader.load(getClass().getResource("sample.fxml"));
        primaryStage.setTitle("FlightPlanner V1.0");
        BorderPane bp = new BorderPane();
        Scene main = new Scene(bp,1280,720);
        primaryStage.setScene(main);
        primaryStage.show();
    }



    public static void main(String[] args) {
        launch(args);
    }
}
