package main;

import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class Dialog {
    /**
     * Create and show simple dialog box for a specific message
     *
     * @param message - Message to be shown
     * @param title - Title of the dialog box to be shown
     */
    public static void showDialog(String message, String title){
        Stage dialogStage = new Stage();
        dialogStage.setTitle(title);

        BorderPane bp = new BorderPane();
        Button ok = new Button("OK");           // Add universal button that closes the dialog box

        ok.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                dialogStage.close();                // Close dialog box if button is pressed
            }
        });

        Label dialogText = new Label(message);      // Create a new label for the error message
        dialogText.setWrapText(true);

        bp.setCenter(dialogText);                   // Add the text to the centre of the dialog box
        bp.setBottom(ok);
        bp.setId("border");                         // Set the styling ID

        // Set alignment of the text and button
        BorderPane.setAlignment(dialogText, Pos.CENTER);
        BorderPane.setAlignment(ok,Pos.CENTER);

        Scene dialogScene = new Scene(bp,400,200);
        dialogScene.setUserAgentStylesheet("style/menus.css");      // Set the style for the dialog box
        dialogStage.initModality(Modality.APPLICATION_MODAL);
        dialogStage.setScene(dialogScene);
        dialogStage.show();
    }
}
