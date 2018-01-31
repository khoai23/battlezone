package UI;

import data.*;
import data.Item.Item;
import data.Unit.*;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Tab;
import javafx.scene.control.TreeItem;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.stage.Popup;
import javafx.stage.Stage;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

public class Main extends Application {
    MainListener mainListener;

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
