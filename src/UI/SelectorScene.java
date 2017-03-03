package UI;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;

import java.io.IOException;

/**
 * Created by Quan on 3/4/2017.
 */
public class SelectorScene extends Scene{
    SelectorController controller;
    public SelectorScene(FXMLLoader loader, double width, double height) throws IOException {
        super(loader.load(),width,height);
        controller = loader.getController();
    }

    public static SelectorScene createSelectorScene() throws IOException {
        FXMLLoader loader = new FXMLLoader(
                SelectorScene.class.getResource("selectorScene.fxml")
        );
        return new SelectorScene(loader,650,500);
    }
}

class SelectorController {
    @FXML
    public Pane CurrentSelection;
}