package UI;

import data.GameData;
import data.Item.Item;
import data.TreeViewable;
import data.Unit.*;
import data.Utility;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;

import java.io.IOException;
import java.lang.System;
import java.util.List;
import java.util.Optional;

/**
 * Created by Quan on 3/4/2017.
 * The main scene for the game
 */
public class MainScene extends Scene {
    public Controller controller;
    Label[] listLabel;
    Label[] unitTabLabel;
    ProgressBar[] listProgress;
    javafx.scene.control.TreeView<TreeViewable> tree;

    public static MainScene runningScene = null;
    public MainScene(FXMLLoader loader, double width, double height) throws IOException {
        super(loader.load(),width,height);
        controller = loader.getController();
        initStatusTab();
        initUnitTab();
        initInventoryTab();
        initStarMap();
        initBattlefield();
        runningScene = this;

        controller.MainScene.setId("MainScene");
        checkAndUpdateTab(controller.StatusTab);
        controller.StatusTab.setOnSelectionChanged(new TabListener(controller.StatusTab,this));
        controller.UnitTab.setOnSelectionChanged(new TabListener(controller.UnitTab,this));
        controller.InventoryTab.setOnSelectionChanged(new TabListener(controller.InventoryTab,this));
        needReloadingRoster = true;
    }

    public void showField() {
//        System.out.println("ShowField successfully called");
        controller.BattleArena.getChildren().clear();
//        controller.BattleArena.getChildren().addAll(ImageHelper.getMapFromIntMap(
//                GameData.getCurrentData().getCurrentBattle().displayTerrain(), 64
//        ));
        controller.BattleArena.getChildren().addAll(GameData.getCurrentData().getCurrentBattle().getDisplay(
                GameData.getCurrentData().setting.battleHexSize));
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
                        ImageHelper.getVehicleById(veh.getVehicleType(), veh.getLoadout(), veh.pintle, false));
            }
            if(unitSelected instanceof Individual) {
                Utility.dataToLabel((Individual)unitSelected, unitTabLabel);
            }
        });

        unitTabLabel = new Label[10];
        for(int i=0;i<10;i++) {
            unitTabLabel[i] = new Label("Label no." + (i+1));
//            GridPane.setConstraints(unitTabLabel[i],i/5,i%5);
            controller.DetailPane.add(unitTabLabel[i], i % 2, i / 2);
        }

        GridPane.setConstraints(unitTabLabel[2], 0, 1, 2, 1);
        GridPane.setConstraints(unitTabLabel[3], 2, 1, 1, 1);

        controller.ModifyButton.setOnAction(e-> {
            TreeItem item = tree.getSelectionModel().getSelectedItem();
            if(item == null || !(item.getValue() instanceof Individual)) {
                System.err.print("\n Currently cannot modify squad");
                return;
            }
            Dialog<Individual> unit = DialogManager.getEquipmentDialog(
                    (Individual) item.getValue()
            );
            if(unit != null) {
                Optional<Individual> result = unit.showAndWait();
                if(result.isPresent()) {
                    if(item.getValue() instanceof Astartes && result.get() instanceof Astartes)
                        ((Astartes)item.getValue()).changeEquipment(((Astartes)result.get()).getCloneArray());
                }
            } else {
                System.err.printf("\nError getting ModifyDialog for unit %s", item.getValue());
            }

        });

        tree.setPrefWidth(scrollPane.getPrefWidth());
        tree.setPrefHeight(scrollPane.getPrefHeight());

        this.tree = tree;
    }
    
    void initInventoryTab() {
        ObservableList<Item> data = FXCollections.observableArrayList();
        data.addAll(GameData.getCurrentData().getAllItem());

        controller.Type.setCellValueFactory(new PropertyValueFactory<Item,String>("type"));
        controller.Name.setCellValueFactory(new PropertyValueFactory<Item,String>("name"));
        controller.Description.setCellValueFactory(new PropertyValueFactory<Item,String>("description"));
        controller.Stock.setCellValueFactory(new PropertyValueFactory<Item,Integer>("stock"));

        controller.Manage.setItems(data);
    }
    
    void initStarMap() {
        controller.StarMap.getChildren().add(ImageHelper.getStarMapImage());
        controller.StarMap.getChildren().addAll(GameData.getCurrentData().map.reloadAllElements());
        if(GameData.getCurrentData().map.destination != null) {
            ImageHelper.viewRouteToSystem(GameData.getCurrentData().map.destination);
            //controller.StarMap.getChildren().add(controller.lineToDestination);
        }
    }

    void initBattlefield() {
        controller.MinimapBtn.setOnAction(event -> controller.Minimap.setVisible(!controller.Minimap.isVisible()));
        controller.VoxLogBtn.setOnAction(event -> controller.VoxLogScrollPane.setVisible(!controller.VoxLogScrollPane.isVisible()));
        controller.ExtraBtn.setOnAction(event -> controller.ExtraSettingPane.setVisible(!controller.ExtraSettingPane.isVisible()));

        controller.VoxLog.setSpacing(5);
        controller.VoxLog.setPadding(new Insets(5));
        controller.ExtraSettingPane.setPadding(new Insets(5));
        controller.ExtraSettingPane.setVisible(false);

        controller.DebugMsg.setOnAction(event -> GameData.getMiscSetting().show_debug = controller.DebugMsg.isSelected());
        controller.DamageMsg.setOnAction(event -> GameData.getMiscSetting().show_damage = controller.DamageMsg.isSelected());
        controller.ConvMsg.setOnAction(event -> GameData.getMiscSetting().show_message = controller.ConvMsg.isSelected());
        controller.TooltipDistance.setOnAction(event -> GameData.getMiscSetting().tooltip_distance = controller.TooltipDistance.isSelected());
        controller.TooltipDeepShow.setOnAction(event -> GameData.getMiscSetting().tooltip_show_compo = controller.TooltipDeepShow.isSelected());

        controller.VoxLog.heightProperty().addListener((observable, oldValue, newValue) -> controller.VoxLogScrollPane.setVvalue((Double)newValue ));
//        controller.VoxLog.getChildren().add(Utility.createLine("Friendly",Utility.speaker_friendly,"I have no idea what I am doing."));
//        controller.VoxLog.getChildren().add(Utility.createLine("Hostile",Utility.speaker_hostile,"Neither do I."));
//        controller.VoxLog.getChildren().add(Utility.createLine("Narrator",Utility.speaker_narrator,"Thus the two idiots played."));
    }

    public void checkAndUpdateTab(Tab check) {
        if(check == controller.StatusTab) {
            Astartes you = GameData.getCurrentData().you;
//            listLabel[0].setText("Role: " + you.getRole());
//            listLabel[1].setText("Stat: " + you.statToString());
//            listLabel[2].setText("Wargears: " + you.equipmentToString());
//            listLabel[3].setText("");
//            listLabel[4].setText("Level: " + you.expToString());
//            listLabel[6].setText("Chapter Traits: ");
//            listLabel[7].setText("Command Traits: ");
            Utility.dataToLabel(you,listLabel);

            controller.Master_Avatar.getChildren().clear();
            controller.Master_Avatar.getChildren().addAll(you.getUnitDisplay());
        } else if(check == controller.UnitTab) {
            if(!needReloadingRoster) {
                controller.Avatar_Unit.getChildren().clear();
                controller.Avatar_Vehicle.getChildren().clear();
                TreeItem selectedItem = tree.getSelectionModel().getSelectedItem();
                if(selectedItem != null) {
                    if(selectedItem.getValue() instanceof Astartes) {
                        controller.Avatar_Unit.getChildren().addAll(((Astartes) selectedItem.getValue()).getUnitDisplay());
                    } else if(selectedItem.getValue() instanceof Vehicle) {
                        Vehicle veh = (Vehicle) selectedItem.getValue();
                        controller.Avatar_Vehicle.getChildren().addAll(
                                ImageHelper.getVehicleById(veh.getVehicleType(), veh.getLoadout(), veh.pintle, false));
                    }
                }
                return;
            }
            List<Unit> roster = GameData.getRoster();
            tree.getRoot().getChildren().clear();
            for(Unit unit:roster) {
                TreeItem<TreeViewable> unitItem = new TreeItem<>(unit,ImageHelper.getIconById(unit.getIconId()));
                tree.getRoot().getChildren().add(unitItem);
                if(unit instanceof AstartesSquad) {
                    // add the Astartes inside
                    for(Individual ind : ((AstartesSquad) unit).getMembers()) {
                        Astartes bth = (Astartes) ind;
                        unitItem.getChildren().add(new TreeItem<>(bth,ImageHelper.getIconById(bth.getIconId())));
                    }
                } else if(unit instanceof Vehicle) {
                    for(Astartes bth : ((Vehicle) unit).getCrew()) {
                        unitItem.getChildren().add(new TreeItem<>(bth,ImageHelper.getIconById(bth.getIconId())));
                    }
                }
            }
            needReloadingRoster = false;
        } else if(check == controller.InventoryTab) {

        } else if(check == controller.BattleTab) {
            if(GameData.getCurrentData().getCurrentBattle() != null) {
                //System.out.print("\n\nBattleTab enabled");
                controller.BattleTab.setDisable(false);
                showField();
            } else {
                //System.out.print("\n\nBattleTab disabled due to no current battle");
                controller.BattleTab.setDisable(true);
                controller.MainScene.getSelectionModel().select(controller.StatusTab);
            }
        }
    }

    public static boolean needReloadingRoster = false;

    public static void addToVoxLog(Node comp) {
        if(runningScene == null) {
            System.err.println("Scene have not yet finish initialization.");
        } else if(comp == null) {
            return;
        }

        Platform.runLater( () -> {
            runningScene.controller.VoxLog.getChildren().add(comp);
            runningScene.controller.VoxLogScrollPane.setVvalue(1.0);
        });
//        runningScene.controller.VoxLog.setHeight
    }

    public static void updateTooltip(String description) {
        runningScene.controller.battleTooltip.setText(description);
    }

    public static void updateBattleResult(String result) {
        if(result != null) {
            System.out.printf("\nResult: %s", result);
            // TODO add a dialog showing the result
        }
        runningScene.controller.BattleTab.setDisable(true);
        runningScene.controller.MainScene.getSelectionModel().select(
                runningScene.controller.StatusTab);
        GameData.getCurrentData().setCurrentBattle(null);
    }

    public static void openBattleTab() {
        runningScene.controller.BattleTab.setDisable(false);
        runningScene.showField();
    }

    public static Controller getSceneController() {
        if(runningScene != null) return runningScene.controller;
        return null;
    }
}

class TabListener implements EventHandler<Event> {
    private Tab pane;
    private MainScene main;

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