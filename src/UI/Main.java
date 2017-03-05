package UI;

import data.*;
import data.Item.Item;
import data.Unit.*;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Tab;
import javafx.scene.control.TreeItem;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.stage.Popup;
import javafx.stage.Stage;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.EventListener;

public class Main extends Application {
    javafx.scene.control.Label[] listLabel;
    ProgressBar[] listProgress;
    Controller gameController;
    TreeItem<TreeViewable> root;
    MainListener mainListener;
    Scene mainScene;

    @Override
    public void start(Stage primaryStage) throws Exception{
        ImageHelper.init();
//        FXMLLoader loader = new FXMLLoader(
//                getClass().getResource("mainScene.fxml")
//        );
//        Parent root = loader.load();
//        gameController = loader.<Controller>getController();
//        Scene mainScene = new MainScene(loader, 800, 600);
        Scene mainScene = MainScene.createMainScene();
        primaryStage.setTitle("Fan Game");
        primaryStage.setScene(mainScene);
        mainListener = new MainListener(mainScene);
        primaryStage.setResizable(false);
        primaryStage.show();
//        initStatusTab();
//        initManageUnitTab();
//        initManageInventoryTab();
//        initStarMap();
//        initBattlefield();
//
//        // init completed, showing tab
//        checkAndUpdateTab(gameController.StatusTab);
//        gameController.StatusTab.setOnSelectionChanged(new TabChangeListener(gameController.StatusTab,this));
//        gameController.UnitTab.setOnSelectionChanged(new TabChangeListener(gameController.UnitTab,this));
    }

    public static void main(String[] args) {
        launch(args);
    }

    Popup switchDialog = null;
    public Popup getSwitchDialog() {
        if(switchDialog != null) return switchDialog;
        switchDialog = new Popup();
        switchDialog.setAutoHide(true);
        return switchDialog;
    }

    public void initStatusTab() {
        javafx.scene.control.Label propAnchor = gameController.Line_11;//(javafx.scene.control.Label)mainScene.lookup("#Line_11");

        AnchorPane statusPane = gameController.StatusPane; //((AnchorPane)mainScene.lookup("#StatusPane"));

        double spacingWidth = 200, spacingHeight = 30, anchorX = propAnchor.getLayoutX(), anchorY = propAnchor.getLayoutY();

        listLabel = new javafx.scene.control.Label[(10 + 4)];
        AnchorPane.setTopAnchor(propAnchor,anchorY);
        AnchorPane.setLeftAnchor(propAnchor, anchorX);
        listLabel[0] = propAnchor;
        for(int i=1;i<10;i++) {
            javafx.scene.control.Label item = new javafx.scene.control.Label();
            AnchorPane.setTopAnchor(item, anchorY + spacingHeight * Math.floor(i / 2));
            AnchorPane.setLeftAnchor(item, anchorX + spacingWidth * (i % 2));
            item.setText("Label no." + (i+1));
            listLabel[i] = item;
            statusPane.getChildren().add(item);
        }

        propAnchor = gameController.Line_31;//(javafx.scene.control.Label)mainScene.lookup("#Line_31");
        anchorX = propAnchor.getLayoutX(); anchorY = propAnchor.getLayoutY();

        AnchorPane.setTopAnchor(propAnchor,anchorY);
        AnchorPane.setLeftAnchor(propAnchor, anchorX);
        listLabel[10] = propAnchor;
        for(int i=1;i<4;i++) {
            javafx.scene.control.Label item = new javafx.scene.control.Label();
            AnchorPane.setTopAnchor(item, anchorY + spacingHeight * i);
            AnchorPane.setLeftAnchor(item, anchorX);
            listLabel[i+10] = item;
            statusPane.getChildren().add(item);
        }
        listLabel[10].setText("Imperial Relation: ");
        listLabel[11].setText("Inquisitor Relation: ");
        listLabel[12].setText("Machine Cult Relation: ");
        listLabel[13].setText("Ecclesiarchy Relation: ");

        listProgress = new ProgressBar[4];
        for(int i=0;i<4;i++) {
            ProgressBar item = new ProgressBar();
            AnchorPane.setTopAnchor(item, anchorY + spacingHeight * i);
            AnchorPane.setLeftAnchor(item, anchorX + 150);
            item.setMaxWidth(600);
            item.setPrefWidth(400);
            item.setProgress(.25 * i);
//            item.getStyleClass().add("progress-bar");
            listProgress[i] = item;
            statusPane.getChildren().add(item);
        }

        Pane canvas = gameController.Master_Avatar;
        canvas.setId("canvas");
    }

    public void initManageUnitTab() {
        Pane canvas = gameController.Avatar_Unit; //(AnchorPane)mainScene.lookup("#Avatar_Unit");
        canvas.setId("canvas");

        canvas = gameController.Avatar_Vehicle;
        canvas.setId("canvas");
//            System.out.println("Armour WH:" + listArmour[chosen[i]].getViewport().getWidth() + ";" +  listArmour[chosen[i]].getViewport().getHeight());

//        Squad cothea = new Squad();
//        TreeItem<TreeViewable> rootItem = new TreeItem<TreeViewable> (cothea, ImageHelper.getIconById(ImageHelper.normalIcon));
//
//        rootItem.setExpanded(true);
//        for (int i = 1; i < 10; i++) {
//            Astartes newUnit = new Astartes("no." + i,new int[]{1,1,1,1,i%5,i+6,0,0,i});
//            TreeItem<TreeViewable> item = new TreeItem<TreeViewable> (newUnit,ImageHelper.getIconById(newUnit.getIconId()));
//
//            rootItem.getChildren().add(item);
//            cothea.members.add(newUnit);
//        }

        TreeItem<TreeViewable> trueRoot = new TreeItem<>(new Company(), ImageHelper.getIconById(ImageHelper.normalIcon));
//        trueRoot.getChildren().add(rootItem);
        root = trueRoot;

//        cothea = new Squad("Lucatiel");
//        rootItem = new TreeItem<>(cothea, ImageHelper.getIconById(ImageHelper.normalIcon));
//        Astartes unit = new Astartes("Aginhart", new int[] {90,80,15,4,5,2,3,0,2});
//        cothea.members.add(unit);
//        rootItem.getChildren().add(new TreeItem<>(unit,ImageHelper.getIconById(unit.getIconId())));
//        trueRoot.getChildren().add(rootItem);

        javafx.scene.control.TreeView<TreeViewable> tree = new javafx.scene.control.TreeView<>(trueRoot);
        tree.setShowRoot(false);
        Pane scrollPane = gameController.UnitScrollPane;//(Pane)mainScene.lookup("#UnitScrollPane");
        scrollPane.getChildren().add(tree);
//        scrollPane.setFitToWidth(true);

        tree.getSelectionModel().selectedIndexProperty().addListener((observable, oldValue, newValue) -> {
            if(tree.getTreeItem((int) newValue) ==  null) return;
            TreeViewable unitSelected = tree.getTreeItem((int) newValue).getValue();
            gameController.Avatar_Unit.getChildren().clear();
            if(unitSelected instanceof Astartes) {
                //ImageView[] unitDisplay = ((Astartes) unitSelected).getUnitDisplay();
                gameController.Avatar_Unit.getChildren().addAll(((Astartes) unitSelected).getUnitDisplay());
            }
        });

        tree.setPrefWidth(scrollPane.getPrefWidth());
        tree.setPrefHeight(scrollPane.getPrefHeight());
    }

    public void initManageInventoryTab() {
        ObservableList<Item> data = FXCollections.observableArrayList();
        GameData.getCurrentData().loadDefaultData();
        data.addAll(GameData.getCurrentData().getAllItem());

        gameController.Type.setCellValueFactory(new PropertyValueFactory<Item,String>("type"));
        gameController.Name.setCellValueFactory(new PropertyValueFactory<Item,String>("name"));
        gameController.Description.setCellValueFactory(new PropertyValueFactory<Item,String>("description"));
        gameController.Stock.setCellValueFactory(new PropertyValueFactory<Item,Integer>("stock"));

        gameController.Manage.setItems(data);
    }

    public void initStarMap() {
        gameController.StarMap.getChildren().add(ImageHelper.getBackgroundImage());
        gameController.StarMap.getChildren().addAll(GameData.getCurrentData().map.reloadAllElements());
//        ImageView star = ImageHelper.getStarById(2);
//        star.setX(422.7);
//        star.setY(72.4);
//        gameController.StarMap.getChildren().add(star);
//        star = ImageHelper.getStarById(4);
//        star.setX(65.1);
//        star.setY(229.3);
//        gameController.StarMap.getChildren().add(star);
    }

    public void initBattlefield() {
//        ImageView temp;
//        for(int i=0;i<5;i++) {
//            temp = ImageHelper.getSquareById(i,false);
//            temp.setX(0);
//            temp.setY(128 * i + 64);
//            gameController.Battlefield.getChildren().add(temp);
//            temp = ImageHelper.getSquareById(i,true);
//            temp.setX(110);
//            temp.setY(128 * i);
//            gameController.Battlefield.getChildren().add(temp);
//        }
        gameController.BattleArena.getChildren().addAll(ImageHelper.getMapFromIntMap(
                GameData.getCurrentData().getCurrentBattle().displayTerrain(),64
        ));
        gameController.MinimapBtn.setOnAction(event -> gameController.Minimap.setVisible(!gameController.Minimap.isVisible()));
        gameController.VoxLogBtn.setOnAction(event -> gameController.VoxLogScrollPane.setVisible(!gameController.VoxLogScrollPane.isVisible()));
    }

    public void checkAndUpdateTab(Tab check) {
        if(check == gameController.StatusTab) {
            Astartes you = GameData.getCurrentData().you;
            listLabel[0].setText("Role: " + "Brother-Captain");
            listLabel[1].setText("Stat: " + you.statToString());
            listLabel[2].setText("Wargears: " + you.equipmentToString());
            listLabel[3].setText("");
            listLabel[4].setText("Level: " + you.expToString());
            listLabel[6].setText("Chapter Traits: ");
            listLabel[7].setText("Command Traits: ");

            gameController.Master_Avatar.getChildren().clear();
            gameController.Master_Avatar.getChildren().addAll(you.getUnitDisplay());
        } else if(check == gameController.UnitTab) {
        }
    }

    void setUnitByRoster(ArrayList<Unit> roster) {
        // TODO more efficient load to the root node (checking existed unit within node)
        root.getChildren().clear();
        for(Unit unit:roster) {
            TreeItem<TreeViewable> unitItem = new TreeItem<>(unit,ImageHelper.getIconById(unit.getIconId()));
            root.getChildren().add(unitItem);
            if(unit instanceof Squad) {
                // add the Astartes inside
                for(Astartes bth : ((Squad) unit).members) {
                    unitItem.getChildren().add(new TreeItem<>(bth,ImageHelper.getIconById(bth.getIconId())));
                }
            }
        }
    }

}

class MainListener implements ActionListener {

    private Scene currentScene;

    public MainListener(Scene sc) {
        currentScene = sc;
    }

    @Override
    public void actionPerformed(ActionEvent e) {

    }
}

class TabChangeListener implements EventHandler<Event> {
    Tab pane;
    Main main;

    TabChangeListener(Tab pane, Main main) {
        this.pane = pane;
        this.main = main;
    }

    @Override
    public void handle(Event event) {
        if(pane.isSelected())
            main.checkAndUpdateTab(pane);
    }
}
