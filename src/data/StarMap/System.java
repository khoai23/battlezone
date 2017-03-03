package data.StarMap;

import UI.ImageHelper;
import UI.Main;
import data.GameData;
import javafx.scene.Node;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;

import java.io.Serializable;

/**
 * Created by Quan on 2/20/2017.
 */
public class System implements Serializable {
    public String name;
    public int starType;
    public float posX;
    public float posY;
    public Planet[] listPlanet;

    public System(String name) {
        this.name = name;
        int planetNum = (int)Math.floor(Math.random() * 4) + 1;
        listPlanet = new Planet[planetNum];
        for(int i=0;i<planetNum;i++) {
            listPlanet[i] = new Planet(i+1);
        }
        posX = (float)(Math.random() * ( 1280 - 76));
        posY = (float)(Math.random() * ( 720  - 73));
        starType = (int)Math.floor(Math.random() * 6);
    }

    public Node getSystemImage() {
        Label systemName = new Label(name);
        ImageView image =  ImageHelper.getStarById(starType);
        systemName.setGraphic(image);
        systemName.setTextFill(Color.WHITE);
        systemName.setContentDisplay(ContentDisplay.TOP);
//        image.setX(posX);
//        image.setY(posY);
        systemName.setLayoutX(posX);
        systemName.setLayoutY(posY);
        return systemName;
    }
}

class Planet implements Serializable {
    public final int designation;
    public int habitat;
    public long population;
    public String extra;

    public Planet(int number) {
        designation = number;
        // planet type forge/fortress/daemon are not randomized
        habitat = (int)Math.floor(Math.random() * 5) * 3 + (int)(Math.floor(Math.random() * 3));
        // habitat type*3+specific
        if(habitat < 3 * habitat_type_death) {
            // dead, no population
            population = 0;
        } else {
            int power = habitat/3;
            if(power == habitat_type_feral || power == habitat_type_death) power = 2;
            population = (int)(100 * Math.pow(1000,power-1) * Math.random());
        }
    }

    // TODO a roman number generator
    public static String getName(String name, String designation) {
        return name + " " + designation;
    }

    public static final int habitat_type_dead=0;
    public static final int habitat_type_death=1;
    public static final int habitat_type_feral=2;
    public static final int habitat_type_agri=3;
    public static final int habitat_type_hive=4;
    public static final int habitat_type_forge=5;
    public static final int habitat_type_fortress=6;
    public static final int habitat_type_daemon=7;
}
