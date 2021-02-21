package main;

import javafx.scene.canvas.Canvas;
import javafx.scene.layout.StackPane;

public class PathDrawer {
    static StackPane stack = new StackPane();
    static Canvas canvas;
    public static StackPane createPathDrawer(){
        canvas = new Canvas();
        stack.getChildren().add(canvas);
        return stack;
    }
}
