package UI;

import data.Item.Item;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;

import javax.swing.*;
import java.awt.*;
import java.awt.Button;
import java.awt.ScrollPane;

public class Controller {

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
    public Pane Battlefield;

    @FXML
    public Tab StatusTab;
    public Tab UnitTab;
    public Tab InventoryTab;
    public Tab StarTab;
    public Tab BattleTab;

    @FXML
    public AnchorPane BattleArena;
    public AnchorPane VoxLog;
    public Pane Minimap;
    public javafx.scene.control.ScrollPane VoxLogScrollPane;
    public javafx.scene.control.ScrollPane BattleScrollPane;
    public javafx.scene.control.Button MinimapBtn;
    public javafx.scene.control.Button VoxLogBtn;
    public javafx.scene.control.Button ExtraBtn;
    public javafx.scene.control.Button EndTurnBtn;

    @FXML
    public void initialize() {
        System.out.println("Controller initialized.");
        MainScene.getStylesheets().add("file:res/css/stylesheet.css");
    }
}
