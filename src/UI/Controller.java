package UI;

import data.Item;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TabPane;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;

import java.awt.*;

public class Controller {

    public Controller()  {
        System.out.println("controller constructor run");
    }

    @FXML
    public Pane Master_Avatar;

    @FXML
    public Pane Avatar_Unit;

    @FXML
    public Pane Avatar_Vehicle;

    @FXML
    public TabPane MainScene;

    @FXML
    public AnchorPane StatusPane;
    public Label Line_11;
    public Label Line_31;

    public Pane UnitScrollPane;

    @FXML
    public javafx.scene.control.TableView<Item> Manage;
    public javafx.scene.control.TableColumn<Item, String> Type;
    public javafx.scene.control.TableColumn<Item, String> Name;
    public javafx.scene.control.TableColumn<Item, String> Description;
    public javafx.scene.control.TableColumn<Item, Integer> Stock;
    public javafx.scene.control.TableColumn<Item, Button> Add;
    public javafx.scene.control.TableColumn<Item, Button> Scrap;

    @FXML
    public Pane StarMap;

    @FXML
    public void initialize() {
        System.out.println("Controller initialized.");
        MainScene.getStylesheets().add("file:res/css/stylesheet.css");
    }
}
