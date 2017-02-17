package UI;

import data.*;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TreeItem;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class Main extends Application {
    private javafx.scene.control.Label[] listLabel;
    private ProgressBar[] listProgress;
    private Controller gameController;

    @Override
    public void start(Stage primaryStage) throws Exception{
        ImageHelper.init();
        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("sample.fxml")
        );
        Parent root = loader.load();
        gameController = loader.<Controller>getController();
        Scene mainScene = new Scene(root, 800, 600);
        primaryStage.setTitle("Fan Game");
        primaryStage.setScene(mainScene);
        primaryStage.setResizable(false);
        primaryStage.show();
        initStatusTab();
        initManageUnitTab();
        initManageInventoryTab();
        initStarMap();
    }

    public static void main(String[] args) {
        launch(args);
    }

    public void initStatusTab() {
//        Node node = mainScene.lookup("#Title");
//        if(node instanceof javafx.scene.control.Label) {
//            ((javafx.scene.control.Label)node).setText("Chapter Master " + GameData.getCurrentData().chapterMaster);
//        }
        javafx.scene.control.Label propAnchor = gameController.Line_11;//(javafx.scene.control.Label)mainScene.lookup("#Line_11");

        AnchorPane statusPane = gameController.StatusPane; //((AnchorPane)mainScene.lookup("#StatusPane"));

        double spacingWidth = 200, spacingHeight = 30, anchorX = propAnchor.getLayoutX(), anchorY = propAnchor.getLayoutY();

        listLabel = new javafx.scene.control.Label[(10 + 4)];
        propAnchor.setText("Label no." + 0);
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

        listLabel[10] = propAnchor;
        propAnchor.setText("Label no." + 11);
        for(int i=1;i<4;i++) {
            javafx.scene.control.Label item = new javafx.scene.control.Label();
            AnchorPane.setTopAnchor(item, anchorY + spacingHeight * i);
            AnchorPane.setLeftAnchor(item, anchorX);
            item.setText("Label no." + (i+11));
            listLabel[i+10] = item;
            statusPane.getChildren().add(item);
        }

        listProgress = new ProgressBar[4];
        for(int i=0;i<4;i++) {
            ProgressBar item = new ProgressBar();
            AnchorPane.setTopAnchor(item, anchorY + spacingHeight * i);
            AnchorPane.setLeftAnchor(item, anchorX + spacingWidth / 2);
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
        ImageView[] listArmour = ImageHelper.getArmourImageById(ImageHelper.errantArmour);
//        int[] chosen = new int[]{10,6,9,0};
        canvas.getChildren().addAll(listArmour);

        canvas = gameController.Avatar_Vehicle;
        canvas.setId("canvas");
        ImageView[] listVehicle = ImageHelper.getVehicles(ImageHelper.razorback,ImageHelper.Razorback_HB,1);
        canvas.getChildren().addAll(listVehicle);
//            System.out.println("Armour WH:" + listArmour[chosen[i]].getViewport().getWidth() + ";" +  listArmour[chosen[i]].getViewport().getHeight());

        Squad cothea = new Squad();
        TreeItem<TreeViewable> rootItem = new TreeItem<TreeViewable> (cothea, ImageHelper.getIconById(ImageHelper.normalIcon));

        rootItem.setExpanded(true);
        for (int i = 1; i < 16; i++) {
            Astartes newUnit = new Astartes("Warrior no." + i,new int[0]);
            newUnit.role = i;
            TreeItem<TreeViewable> item = new TreeItem<TreeViewable> (newUnit,ImageHelper.getIconById(newUnit.getIconId()));

            rootItem.getChildren().add(item);
        }

        javafx.scene.control.TreeView<TreeViewable> tree = new javafx.scene.control.TreeView<TreeViewable>(rootItem);
//        tree.setRoot(rootItem);
        Pane scrollPane = gameController.UnitScrollPane;//(Pane)mainScene.lookup("#UnitScrollPane");
        scrollPane.getChildren().add(tree);
//        scrollPane.setFitToWidth(true);

        tree.setPrefWidth(scrollPane.getPrefWidth());
        tree.setPrefHeight(scrollPane.getPrefHeight());
    }

    public void initManageInventoryTab() {
        ObservableList<Item> data = FXCollections.observableArrayList();
//        data.add(new Weapon(0,1,1,4,7,"Bolter","Bolter Desc"));
//        data.add(new Armour(0,1,1,false,7,"Mk VII","Aquila Desc"));
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
