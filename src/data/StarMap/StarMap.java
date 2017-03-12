package data.StarMap;

import data.Utility;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Quan on 2/22/2017.
 */
public class StarMap implements Serializable {
    public System[] systems;
    public ArrayList<Route> routes;

    public StarMap() {
        this(Utility.rollBetween(10,20));
    }

    public StarMap(int numOfSys) {
        routes = new ArrayList<>();
//        loadTestData();
        createMap(numOfSys);
    }

    public Node[] reloadAllElements() {
        Node[] data = new Node[systems.length + routes.size()];
        // routes are added before systems
        for(int i=0;i<routes.size();i++) {
            data[i] = routes.get(i).getRouteImage();
        }
        for(int i=0;i<systems.length;i++) {
            data[i+routes.size()] = systems[i].getSystemImage();
        }
        return data;
    }

    void loadTestData() {
        systems = new System[2];
        systems[0] = new System("Alpha");
        systems[1] = new System("Beta");
        routes.add(new Route(systems[0],systems[1]));
    }

    void createMap(int numOfSys) {
        systems = new System[numOfSys];
        int mapSize = 1280/squareSize * (720-squareSize)/squareSize, rowMaxSize = 1280/squareSize, temp;
        List<Integer> systemPosition = new ArrayList<>();
        for(int i=0;i<numOfSys;i++) {
            systems[i] = new System("IS no." + i);
            do {
                temp = Utility.rollBetween(0,mapSize);
            } while (systemPosition.contains(temp));
            systemPosition.add(temp);
            systems[i].posX = (float)(Math.random() * (squareSize-76) + temp % rowMaxSize * squareSize);
            systems[i].posY = (float)(Math.random() * (squareSize-73) + temp / rowMaxSize * squareSize);
        }
        for(int i=0;i<numOfSys-1;i++) {
            routes.add(new Route(systems[i],systems[Utility.rollBetween(i+1,numOfSys-1)]));
            //(int)(Math.random() * (numOfSys-i-2)) + i + 1
        }
    }

    public static final int squareSize = 100;
}

class Route implements Serializable {
    final public System front;
    final public System end;

    Route(System end1, System end2) {
        front = end1;
        end = end2;
    }

    public Node getRouteImage() {
        // systems are 76x73 -> aim for the middle
        Line route = new Line(front.posX+38,front.posY+36,end.posX+38,end.posY+36);
        route.setStroke(Color.WHITE);
        return route;
    }
}
