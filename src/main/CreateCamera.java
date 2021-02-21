package main;

import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class CreateCamera {

    private static Stage createStage;
    private static Scene createScene;
    private static String path = "src/UAVs";
    private static TextField name = new TextField();
    private static TextField weight = new TextField();
    private static TextField turnRadius = new TextField();
    private static TextField maxIncline = new TextField();
    private static TextField battery = new TextField();
    private static TextField batteryCapacity = new TextField();

    public CreateCamera(){
        createStage = new Stage();
        createStage.setTitle("Camera Setup");
        createStage.initModality(Modality.APPLICATION_MODAL);
    }

    public void newCamera(){




    }

    public void loadCamera(){




    }
}
