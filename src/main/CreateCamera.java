package main;

import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class CreateCamera {

    private static Stage createStage;
    private static Scene createScene;
    private static String path = "src/cameras";
    private static TextField name = new TextField();
    private static TextField sensorWidth = new TextField();
    private static TextField sensorHeight = new TextField();
    private static TextField focalLocal = new TextField();
    private static TextField resolution = new TextField();
    private static TextField aspectRatio = new TextField();

    private static Stage dialog = new Stage();
    private static Label dialogMessage = new Label("The filename "+ name.getText() +".uav already exists in "+path+".\n Would you like to overwrite?");

    /**
     * Create camera constructor
     *
     */
    public CreateCamera(){
        createStage = new Stage();
        createStage.setTitle("Camera Setup");
        createStage.initModality(Modality.APPLICATION_MODAL);
        setupOverwriteDialog();      // Setup overwrite dialog box
    }

    private void setupOverwriteDialog(){
        // Setup dialog box
        dialog.setTitle("Overwrite File?");
        dialog.initModality(Modality.APPLICATION_MODAL);

        Button yes = new Button("Yes");
        Button no = new Button("No");
        BorderPane bp = new BorderPane();
        bp.setCenter(dialogMessage);
        HBox hb = new HBox(yes, no);

        BorderPane.setAlignment(hb, Pos.CENTER);
        bp.setBottom(hb);

        Scene dialogScene = new Scene(bp,400,100);
        dialogScene.setUserAgentStylesheet("style/menus.css");
        dialog.setScene(dialogScene);

        yes.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                System.out.println("Set overwrite to true");
                writeCamera();
                dialog.close();
                createStage.close();
            }
        });
        no.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                System.out.println("Change the name of the camera and re-save");
                dialog.close();
            }
        });
    }

    public void newCamera(){
        GridPane gp = new GridPane();
        Text title =  new Text("Create a Camera:\nCamera Settings");
        title.setTextAlignment(TextAlignment.CENTER);
        title.setId("title");
        GridPane.setColumnSpan(title,3);
        GridPane.setHalignment(title, HPos.CENTER);

        gp.setId("gridpane");
        gp.getStyleClass().add("grid");

        Label nameLabel = new Label("Name:");
        Label sensorWidthLabel = new Label("Sensor Width (mm):");
        Label sensorHeightLabel = new Label("Sensor height (mm):");
        Label focalLengthLabel = new Label("Focal Length:");
        Label resolutionLabel = new Label("Resolution:");
        Label aspectRatioLabel = new Label("Aspect Ratio:");

        name.setPromptText("Name of Camera");
        sensorWidth.setPromptText("Sensor Width (mm)");
        sensorHeight.setPromptText("Sensor Height (mm)");
        focalLocal.setPromptText("Focal Length");
        resolution.setPromptText("Resolution (MP)");
        aspectRatio.setPromptText("Aspect Ratio");

        Button save = new Button("Save");
        Button cancel = new Button("Cancel");
        Button clear = new Button("Set to default");

        gp.add(title,0,0);

        gp.add(nameLabel,0,1);
        gp.add(sensorWidthLabel,0,2);
        gp.add(sensorHeightLabel,0,3);
        gp.add(focalLengthLabel,0,4);
        gp.add(resolutionLabel,0,5);
        gp.add(aspectRatioLabel,0,6);


        gp.add(name,1,1);
        gp.add(sensorWidth,1,2);
        gp.add(sensorHeight,1,3);
        gp.add(focalLocal,1,4);
        gp.add(resolution,1,5);
        gp.add(aspectRatio,1,6);

        gp.add(save,0,7);
        gp.add(cancel,2,7);
        gp.add(clear,1,7);

        gp.setHgap(10);
        gp.setVgap(20);
        gp.setAlignment(Pos.CENTER);

        save.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {

                if(name.getText().isEmpty()){
                    System.out.println("Missing name for UAV");
                }else {
                    File file = new File(path+name.getText()+".cam");
                    if(file.exists()){
                        dialogMessage.setText("The filename "+ name.getText() + ".cam already exists in "+path+".\n Would you like to overwrite?");
                        dialog.show();
                    }else {
                        writeCamera();
                        createStage.close();
                    }
                }
            }
        });

        cancel.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                createStage.close();
            }
        });

        clear.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                name.clear();
                sensorWidth.clear();
                sensorHeight.clear();
                focalLocal.clear();
                resolution.clear();
                aspectRatio.clear();
            }
        });

        createScene = new Scene(gp,500,700);
        createScene.setUserAgentStylesheet("style/menus.css");
        createStage.setScene(createScene);
        createStage.show();
    }

    /**
     * Write camera settings in XML file
     */
    public void writeCamera(){
        System.out.println("Writing new Camera");
        Document dom;
        Element e = null;

        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            dom = documentBuilder.newDocument();
            Element rootElement = dom.createElement("camera");

            e = dom.createElement("name");
            e.appendChild(dom.createTextNode(name.getText()));
            rootElement.appendChild(e);

            e = dom.createElement("sensor_x");
            e.appendChild(dom.createTextNode(sensorWidth.getText()));
            rootElement.appendChild(e);

            e = dom.createElement("sensor_y");
            e.appendChild(dom.createTextNode(sensorHeight.getText()));
            rootElement.appendChild(e);

            e = dom.createElement("focal_length");
            e.appendChild(dom.createTextNode(focalLocal.getText()));
            rootElement.appendChild(e);

            e = dom.createElement("resolution");
            e.appendChild(dom.createTextNode(resolution.getText()));
            rootElement.appendChild(e);

            e = dom.createElement("aspect_ratio");
            e.appendChild(dom.createTextNode(aspectRatio.getText()));
            rootElement.appendChild(e);

            dom.appendChild(rootElement);

            Transformer tr = TransformerFactory.newInstance().newTransformer();
            tr.setOutputProperty(OutputKeys.INDENT,"yes");
            tr.setOutputProperty(OutputKeys.METHOD,"xml");
            tr.setOutputProperty(OutputKeys.ENCODING,"UTF-8");
            //tr.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM,"uav.dtd");
            tr.setOutputProperty("{http://xml.apache.org/xslt}indent-amount","4");

            tr.transform(new DOMSource(dom),new StreamResult(new FileOutputStream("src/cameras/"+name.getText()+".cam")));

        }catch (IOException | ParserConfigurationException | TransformerException ioe){
            Dialog.showDialog("Unable to write to write camera settings.\n\n" +
                    "Ensure that " + path + " is visible by the application.","Unable to write settings");
        }
    }
}
