package data.StarMap;

import UI.ImageHelper;
import data.Battle.MissionConfig;
import data.Utility;
import javafx.scene.Node;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Quan on 2/22/2017.
 * A star map detailing the map of the sector in which you are fighting.
 */
public class StarMap implements Serializable {
    public System[] systems;
    public ArrayList<Route> routes;
    public System currentPlanet = null;
    public float playerPosX;
    public float playerPosY;
    public System destination = null;
    public int eta;

    public List<Mission> listOfAvailableMissions = new ArrayList<>();

    public StarMap() {
        this(Utility.rollBetween(10,20));
    }

    public StarMap(int numOfSys) {
        routes = new ArrayList<>();
//        loadTestData();
        createMap(numOfSys);
        playerPosX = systems[0].posX;
        playerPosY = systems[0].posY;
        currentPlanet = systems[0];
    }

    public boolean checkRouteExist(System destination) {
        if(currentPlanet ==  null) return false;
        for(Route r:routes) if(r.isRouteBetween(currentPlanet,destination)) return true;
        return false;
    }

    public int getEtaToSystem(System destination) {
        if(currentPlanet != null)
            for(Route r:routes) if(r.isRouteBetween(currentPlanet,destination)) return r.travelTime;

        return Route.getNormalLength(playerPosX,playerPosY, destination.posX, destination.posY);
    }

    public List<Node> reloadAllElements() {
        ArrayList<Node> data = new ArrayList<>();//Node[systems.length + routes.size()];
        // routes are added before systems
        for (Route route : routes) {
            data.add(route.getRouteImage());
        }
        for (System system1 : systems) {
            Node system = system1.getSystemImage();
//            system.addEventHandler(MouseEvent.MOUSE_ENTERED_TARGET, event -> MainScene.viewRouteToSystem(system1));
//            system.addEventHandler(MouseEvent.MOUSE_EXITED_TARGET, event -> MainScene.hideRouteToSystem());
            system.setOnMouseEntered(event -> ImageHelper.viewRouteToSystem(system1));
            system.setOnMouseExited(event -> ImageHelper.hideRouteToSystem());
            system.setOnMouseClicked(event -> ImageHelper.handleClickOnSystem(system1, event.getX(), event.getY()));
            data.add(system);
        }
        return data;
    }

    public void addMissionForSystem(System sys, MissionConfig mcfg) {
        listOfAvailableMissions.add(new Mission(mcfg, sys));
    }

    public void removeMissionForSystem(System sys, MissionConfig mcfg) {
        listOfAvailableMissions.removeIf(m -> m.mission == mcfg && m.position == sys);
    }

    public List<MissionConfig> getMissionsForSystem(System sys) {
        List<MissionConfig> listMissions = new ArrayList<>();
        for(Mission m:listOfAvailableMissions) {
            if(m.position == sys)
                listMissions.add(m.mission);
        }
        return listMissions;
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
    final public int travelTime;

    Route(System end1, System end2) {
        front = end1;
        end = end2;
        int tvt = getNormalLength(end1.posX,end1.posY,end2.posX,end2.posY);
        tvt -= Utility.rollBetween(1, 5);
        if(tvt > 0) travelTime = tvt;
        else travelTime = getNormalLength(end1.posX,end1.posY,end2.posX,end2.posY);
    }

    public Node getRouteImage() {
        // systems are 76x73 -> aim for the middle
        Line route = new Line(front.posX+38,front.posY+36,end.posX+38,end.posY+36);
        route.setStroke(Color.WHITE);
        route.getStrokeDashArray().addAll(25d, 10d);
        return route;
    }

    public boolean isRouteBetween(System one, System two) {
        return (one == front && two == end) || (one == end && two == front);
    }

    public static int getNormalLength(float x1, float y1, float x2, float y2) {
        return (int)Math.round(Math.sqrt((x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2))) / 50;
    }
}

class Mission implements Serializable {
    MissionConfig mission;
    System position;

    Mission(MissionConfig ms, System at) {
        mission = ms;
        position = at;
    }

    Node getImage() {
        // TODO continue later
        float posX = position.posX, posY = position.posY;
        return new ImageView();
    }
}
