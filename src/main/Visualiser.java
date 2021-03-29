package main;

import javafx.scene.*;
import javafx.scene.Camera;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.paint.Material;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.TriangleMesh;
import javafx.stage.Stage;


public class Visualiser {
    Stage visualiserStage;
    Scene visualiserScene;
    Camera camera = new PerspectiveCamera();

    public Visualiser(){
        visualiserStage = new Stage();
        visualiserStage.setTitle("Flight Path Visualiser");
        MeshView meshView = new MeshView();

        Image image = new Image("src/intermediate/map.png");

        TriangleMesh mesh = new TriangleMesh();
        mesh.getPoints().addAll();
        mesh.getFaces().addAll();

        Material floor = new PhongMaterial(Color.TRANSPARENT,image,null,null,null);


        Group group = new Group();
        PointLight pointLight = new PointLight();
        group.getChildren().addAll(pointLight);

        visualiserScene = new Scene(group,500,500);
        visualiserScene.setCamera(camera);
        visualiserScene.setFill(Color.BLACK);
    }
    public void showVisualiser(){
        visualiserStage.show();
    }
}