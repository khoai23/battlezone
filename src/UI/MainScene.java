package UI;

import data.GameData;
import data.Item.Item;
import data.TreeViewable;
import data.Unit.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Tab;
import javafx.scene.control.TreeItem;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import sun.reflect.generics.tree.Tree;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Quan on 3/4/2017.
 */
public class MainScene extends Scene {
    Controller controller;
    javafx.scene.control.Label[] listLabel;
    ProgressBar[] listProgress;
    javafx.scene.control.TreeView<TreeViewable> tree;
    public MainScene(FXMLLoader loader, double width, double height) throws IOException {
        super(loader.load(),width,height);
        controller = loader.getController();
        initStatusTab();
        initUnitTab();
        initInventoryTab();
        initStarMap();
        initBattlefield();

        checkAndUpdateTab(controller.StatusTab);
        controller.StatusTab.setOnSelectionChanged(new TabListener(controller.StatusTab,this));
        controller.UnitTab.setOnSelectionChanged(new TabListener(controller.UnitTab,this));
        needReloadingRoster = true;
    }

    public static MainScene createMainScene() throws IOException {
        FXMLLoader loader = new FXMLLoader(
                MainScene.class.getResource("mainScene.fxml")
        );
        return new MainScene(loader,800,600);
    }
    
    void initStatusTab() {
        javafx.scene.control.Label propAnchor = controller.Line_11;
        AnchorPane statusPane = controller.StatusPane;

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

        propAnchor = controller.Line_31;//(javafx.scene.control.Label)mainScene.lookup("#Line_31");
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
            listProgress[i] = item;
            statusPane.getChildren().add(item);
        }

        Pane canvas = controller.Master_Avatar;
        canvas.setId("canvas");
    }
    
    void initUnitTab() {
        Pane canvas = controller.Avatar_Unit;
        canvas.setId("canvas");

        canvas = controller.Avatar_Vehicle;
        canvas.setId("canvas");

        TreeItem<TreeViewable> trueRoot = new TreeItem<>(new Company(), ImageHelper.getIconById(ImageHelper.normalIcon));

        javafx.scene.control.TreeView<TreeViewable> tree = new javafx.scene.control.TreeView<>(trueRoot);
        tree.setShowRoot(false);
        Pane scrollPane = controller.UnitScrollPane;
        scrollPane.getChildren().add(tree);

        tree.getSelectionModel().selectedIndexProperty().addListener((observable, oldValue, newValue) -> {
            if(tree.getTreeItem((int) newValue) ==  null) return;
            TreeViewable unitSelected = tree.getTreeItem((int) newValue).getValue();
            controller.Avatar_Unit.getChildren().clear();
            controller.Avatar_Vehicle.getChildren().clear();
            if(unitSelected instanceof Astartes) {
                controller.Avatar_Unit.getChildren().addAll(((Astartes) unitSelected).getUnitDisplay());
            } else if(unitSelected instanceof Vehicle) {
                Vehicle veh = (Vehicle) unitSelected;
                controller.Avatar_Vehicle.getChildren().addAll(
                        ImageHelper.getVehicleById(veh.getType(),veh.getLoadout(),veh.pintle));
            }
        });

        tree.setPrefWidth(scrollPane.getPrefWidth());
        tree.setPrefHeight(scrollPane.getPrefHeight());

        this.tree = tree;
    }
    
    public void initInventoryTab() {
        ObservableList<Item> data = FXCollections.observableArrayList();
        GameData.getCurrentData().loadDefaultData();
        data.addAll(GameData.getCurrentData().getAllItem());

        controller.Type.setCellValueFactory(new PropertyValueFactory<Item,String>("type"));
        controller.Name.setCellValueFactory(new PropertyValueFactory<Item,String>("name"));
        controller.Description.setCellValueFactory(new PropertyValueFactory<Item,String>("description"));
        controller.Stock.setCellValueFactory(new PropertyValueFactory<Item,Integer>("stock"));

        controller.Manage.setItems(data);
    }
    
    public void initStarMap() {
        controller.StarMap.getChildren().add(ImageHelper.getBackgroundImage());
        controller.StarMap.getChildren().addAll(GameData.getCurrentData().map.reloadAllElements());
    }


    public void initBattlefield() {
        controller.BattleArena.getChildren().addAll(ImageHelper.getMapFromIntMap(
                GameData.getCurrentData().getCurrentBattle().displayTerrain(), 64
        ));
        controller.MinimapBtn.setOnAction(event -> controller.Minimap.setVisible(!controller.Minimap.isVisible()));
        controller.VoxLogBtn.setOnAction(event -> controller.VoxLogScrollPane.setVisible(!controller.VoxLogScrollPane.isVisible()));
    }

    public void checkAndUpdateTab(Tab check) {
        if(check == controller.StatusTab) {
            Astartes you = GameData.getCurrentData().you;
            listLabel[0].setText("Role: " + "Brother-Captain");
            listLabel[1].setText("Stat: " + you.statToString());
            listLabel[2].setText("Wargears: " + you.equipmentToString());
            listLabel[3].setText("");
            listLabel[4].setText("Level: " + you.expToString());
            listLabel[6].setText("Chapter Traits: ");
            listLabel[7].setText("Command Traits: ");

            controller.Master_Avatar.getChildren().clear();
            controller.Master_Avatar.getChildren().addAll(you.getUnitDisplay());
        } else if(check == controller.UnitTab) {
            if(!needReloadingAll && !needReloadingRoster) {
                controller.Avatar_Unit.getChildren().clear();
                controller.Avatar_Vehicle.getChildren().clear();
                TreeItem selectedItem = tree.getSelectionModel().getSelectedItem();
                if(selectedItem != null) {
                    if(selectedItem.getValue() instanceof Astartes) {
                        controller.Avatar_Unit.getChildren().addAll(((Astartes) selectedItem.getValue()).getUnitDisplay());
                    } else if(selectedItem.getValue() instanceof Vehicle) {
                        Vehicle veh = (Vehicle) selectedItem.getValue();
                        controller.Avatar_Vehicle.getChildren().addAll(
                                ImageHelper.getVehicleById(veh.getType(),veh.getLoadout(),veh.pintle));
                    }
                }
                return;
            }
            List<Unit> roster = GameData.getRoster();
            tree.getRoot().getChildren().clear();
            for(Unit unit:roster) {
                TreeItem<TreeViewable> unitItem = new TreeItem<>(unit,ImageHelper.getIconById(unit.getIconId()));
                tree.getRoot().getChildren().add(unitItem);
                if(unit instanceof Squad) {
                    // add the Astartes inside
                    for(Astartes bth : ((Squad) unit).members) {
                        unitItem.getChildren().add(new TreeItem<>(bth,ImageHelper.getIconById(bth.getIconId())));
                    }
                }
            }
            needReloadingRoster = false;
        }
    }
    
    public static boolean needReloadingAll = false;
    public static boolean needReloadingRoster = false;
}

class TabListener implements EventHandler<Event> {
    Tab pane;
    MainScene main;

    TabListener(Tab pane, MainScene main) {
        this.pane = pane;
        this.main = main;
    }

    @Override
    public void handle(Event event) {
        if(pane.isSelected())
            main.checkAndUpdateTab(pane);
    }
}