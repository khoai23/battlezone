package UI;

import data.Item.Item;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.shape.Line;
import javafx.scene.text.Text;

public class Controller {

    @FXML
    public TabPane MainScene;

    @FXML
    public AnchorPane StatusPane;
    public Pane Master_Avatar;
    public Label Line_11;
    public Label Line_31;

    public Pane UnitScrollPane;

    @FXML
    public Pane Avatar_Unit;
    public Pane Avatar_Vehicle;
    public GridPane DetailPane;
    public javafx.scene.control.Button ModifyButton;
    public javafx.scene.control.Button DeployButton;
    public javafx.scene.control.Button ArrangeButton;

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
    public AnchorPane BattleArena;
    public VBox VoxLog;
    public VBox ExtraSettingPane;
    public Pane Minimap;
    public javafx.scene.control.ScrollPane VoxLogScrollPane;
    public javafx.scene.control.ScrollPane BattleScrollPane;
    public javafx.scene.control.Button MinimapBtn;
    public javafx.scene.control.Button VoxLogBtn;
    public javafx.scene.control.Button ExtraBtn;
    public javafx.scene.control.Button EndTurnBtn;
    public CheckBox DebugMsg;
    public CheckBox DamageMsg;
    public CheckBox ConvMsg;
    public CheckBox TooltipDistance;
    public CheckBox TooltipDeepShow;

    @FXML
    public Tab StatusTab;
    public Tab UnitTab;
    public Tab InventoryTab;
    public Tab StarTab;
    public Tab BattleTab;

    public Line lineToDestination = null;
    public Text etaText = null;

    public Tooltip battleTooltip = new Tooltip();

    @FXML
    public void initialize() {
        System.out.println("Controller @MainScene initialized.");
        MainScene.getStylesheets().add("file:res/css/stylesheet.css");
    }
}
