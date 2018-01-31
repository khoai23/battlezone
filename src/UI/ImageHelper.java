package UI;

import data.Battle.Battle;
import data.Battle.Field;
import data.Battle.MissionConfig;
import data.GameData;
import data.Item.VehicleChassis;
import data.Item.VehicleType;
import data.Unit.Unit;
import javafx.animation.Interpolator;
import javafx.animation.RotateTransition;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.text.Text;
import javafx.util.Callback;
import javafx.util.Duration;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Quan on 12/27/2016.
 */
public class ImageHelper {
    public static void init() {
        rootIcon = new Image("file:res/texture/all_icon.png");

        starMapImage = new Image("file:res/texture/bg_star.jpg");

        starImage = loadPrefixedImage("starmap/star_",6);

        planetImage = loadPrefixedImage("starmap/star_",24);

        Image[] mapImage = new Image[10];
        mapImage[0] = new Image("file:res/texture/battlemap/sqr_empty.png");
        mapImage[1] = new Image("file:res/texture/battlemap/sqr_empty_selected.png");
        mapImage[2] = new Image("file:res/texture/battlemap/sqr_impassable.png");
        mapImage[3] = new Image("file:res/texture/battlemap/sqr_impassable_selected.png");
        mapImage[4] = new Image("file:res/texture/battlemap/sqr_objective.png");
        mapImage[5] = new Image("file:res/texture/battlemap/sqr_objective_selected.png");
        mapImage[6] = new Image("file:res/texture/battlemap/sqr_friendly.png");
        mapImage[7] = new Image("file:res/texture/battlemap/sqr_friendly_selected.png");
        mapImage[8] = new Image("file:res/texture/battlemap/sqr_hostile.png");
        mapImage[9] = new Image("file:res/texture/battlemap/sqr_hostile_selected.png");

        combatMapImage = mapImage;

        int armorNum = GameData.getArmoursImageName().size();
        armorImage = new Image[armorNum][];

        for(int i=0;i<armorNum;i++) {
            // armor default with 12 parts
            armorImage[i] = loadPrefixedImage("view_unit/" + getArmourNameById(i) + "_",12);
        }

        int weaponNum = GameData.getWeaponsImageName().size();
        weaponImage = new Image[weaponNum][];
        for(int i=0;i<weaponNum;i++) {
            // armor default with 8 parts
            weaponImage[i] = loadPrefixedImage("view_weapon/weapon_" + getWeaponNameById(i) + "_",5);
        }

        int accNum = GameData.getAccessoryList().size();
        accessoryImage = new Image[accNum][];
        for(int i=0;i<accNum;i++) {
            // accessory with 4 parts
            accessoryImage[i] = loadPrefixedImage("view_unit/acc_" + GameData.getAccessoryById(i).imgName + "_",4);
        }

//        vehicleWeaponImage = new Image[5][];

//        // rhino chassis with top hatch in 7
//        vehicleWeaponImage[0] = loadPrefixedImage("view_vehicle/rhino_",7);
//        // predator turret lasc/auto 2x3
//        vehicleWeaponImage[1] = loadPrefixedImage("view_vehicle/pred_top_",6);
//        // predator sponsons lasc/hb 2x3
//        vehicleWeaponImage[2] = loadPrefixedImage("view_vehicle/pred_spo_",6);
//        // predator pintle storm bolter 3
//        vehicleWeaponImage[3] = loadPrefixedImage("view_vehicle/pred_pintle_",3);
//        // razorback turret hb/mm/lasc 3x3
//        vehicleWeaponImage[4] = loadPrefixedImage("view_vehicle/raz_top_",9);
        // ??
//        vehicleWeaponImage[3] = loadPrefixedImage("raz_top_",9);

        int vehicleWeaponNum = GameData.getVehicleWeaponsImageName().size();
        vehicleWeaponImage = new Image[vehicleWeaponNum][];
        for(int i=0;i<vehicleWeaponNum;i++) {
            vehicleWeaponImage[i] = loadPrefixedImage("view_vehicle/" + getVWeaponNameById(i) + "_",4);
        }

        int chassisNum = GameData.getVehiclesChassus().size();
        vehicleChassisImage = new Image[chassisNum][];
        for(int i=0;i<chassisNum;i++) {
            vehicleChassisImage[i] = loadPrefixedImage("view_vehicle/" + getChassisById(i) + "_",6);
        }

        List<String> badgeListNames = GameData.getCombatBadgeList();
        fieldBadgeName = new String[badgeListNames.size()];
        badgeListNames.toArray(fieldBadgeName);
        fieldBadgeImage = new Image[fieldBadgeName.length];
        for(int i=0;i<fieldBadgeName.length;i++) {
            fieldBadgeImage[i] = new Image("file:res/texture/unit_icon/" + fieldBadgeName[i] + ".png");
        }

        possibleWaitIcon = new Image[4];
        possibleWaitIcon[friendlyWait] = new Image("file:res/texture/unit_icon/spin_green.png");
        possibleWaitIcon[hostileWait] = new Image("file:res/texture/unit_icon/spin_red.png");
        possibleWaitIcon[neutralWait] = new Image("file:res/texture/unit_icon/spin_yellow.png");
        possibleWaitIcon[otherWait] = new Image("file:res/texture/unit_icon/spin_white.png");
    }

    /**
     * return an array with images loaded from the prefix
     * */
    public static Image[] loadPrefixedImage(String prefix,int number) {
        Image[] array = new Image[number];
        for(int i=0;i<number;i++) {
            array[i] = new Image("file:res/texture/" + prefix + i + ".png");
        }
        return array;
    }

    /**
     * Getting the current background for star map
     * */
    @NotNull
    public static ImageView getStarMapImage() {
        return new ImageView(starMapImage);
    }

    /**
     * Getting the corresponding star by id
     * */
    @NotNull
    public static ImageView getStarById(int id) {
        return new ImageView(starImage[id]);
    }

    /**
     * Get the corresponding name by id, used for loading resource
     * @param id armor id
     * @return string
     * */
    public static String getArmourNameById(int id) {
        List<String> armorlist = GameData.getArmoursImageName();

        if(id<armorlist.size() && !armorlist.get(id).equals("")) {
            return armorlist.get(id);
        }

        return "aquila";
    }

    /**
     * Get the corresponding name by id, used for loading resource
     * @param id weapon id
     * @return string
     * */
    public static String getWeaponNameById(int id) {
        List<String> weaponlist = GameData.getWeaponsImageName();

        if(id>=0 && id<weaponlist.size() && !weaponlist.get(id).equals("")) {
            return weaponlist.get(id);
        }

        return "";
    }

    /**
     * Get the corresponding vehicle weapon name by id, used for loading resource
     * @param id weapon id
     * @return string
     * */
    public static String getVWeaponNameById(int id) {
        List<String> weaponList = GameData.getVehicleWeaponsImageName();

        if(id<weaponList.size() && !weaponList.get(id).equals("")) {
            return weaponList.get(id);
        }

        return "";
    }

    /**
     * Get the corresponding vehicle weapon name by id, used for loading resource
     * @param id weapon id
     * @return string
     * */
    public static String getChassisById(int id) {
        List<VehicleChassis> weaponList = GameData.getVehiclesChassus();

        if(id<weaponList.size() && !weaponList.get(id).imgName.equals("")) {
            return weaponList.get(id).imgName;
        }

        return "";
    }

    public static ImageView[] rightWeaponImageList = null;
    public static ImageView[] leftWeaponImageList = null;
    public static ImageView[] getWeaponImageById(int id, boolean isLeft) {
        if(id>=GameData.getWeaponsImageName().size() || getWeaponNameById(id).equals("")) return new ImageView[0];
        ImageView[] list;
        if(isLeft) {
            if(leftWeaponImageList == null) {
                leftWeaponImageList = new ImageView[5];
                for (int i=0;i<5;i++) {
                    leftWeaponImageList[i] = new ImageView();
                }
            }
            list = leftWeaponImageList;
        } else {
            if(rightWeaponImageList == null) {
                rightWeaponImageList = new ImageView[5];
                for (int i=0;i<5;i++) {
                    rightWeaponImageList[i] = new ImageView();
                    rightWeaponImageList[i].setScaleX(-1);
//                    rightWeaponImageList[i].setX(150);
                }
            }
            list = rightWeaponImageList;
        }

        if(id<0) {
            for (ImageView aList : list) aList.setImage(null);
            return list;
        }

        int colorscheme = GameData.getCurrentData().colorScheme;
        for (int i=0;i<5;i++) {
            list[i].setImage(weaponImage[id][i]);
            list[i].setFitWidth(150);
            list[i].setFitHeight(200);
            if(i==1) {
                list[i].setEffect(weaponColor);
            } else if(i==2) {
                list[i].setEffect(ornamentColor);
            } else if(i==3) {
                if((colorscheme == GameData.scheme_monotone || colorscheme == GameData.scheme_center
                ) || !isLeft) {
                    list[i].setEffect(primaryColor);
                } else {
                    list[i].setEffect(secondaryColor);
                }
            } else if(i==4) {
                if((colorscheme == GameData.scheme_half || colorscheme == GameData.scheme_quad) && !isLeft) {
                    list[i].setEffect(secondaryColor);
                } else {
                    list[i].setEffect(primaryColor);
                }
            }
        }

        return list;
    }

    private static ImageView[] unitImageList = null;
    /**
     * Get the armor's images
     * @param id armor id
     * @return array of ImageView
     * */
    public static ImageView[] getArmourImageById(int id) {
        ImageView[] list;
        if(unitImageList == null) {
            list = new ImageView[12];
            for(int i=0;i<12;i++)
                list[i] = new ImageView();
            unitImageList = list;
        } else {
            list = unitImageList;
        }

        if(id<0) {
            for (ImageView aList : list) aList.setImage(null);
            return list;
        }

        for(int i=0;i<12;i++) {
            list[i].setImage(armorImage[id][i]);
            list[i].setFitWidth(150);
            list[i].setFitHeight(200);

            // color using scheme, with 4-7 as secondary distribution
            if(i==0 || i==1 || i==9 || ((i==2 || i==10) && (GameData.getCurrentData().colorScheme == GameData.scheme_monotone ||
                            GameData.getCurrentData().colorScheme == GameData.scheme_center))
                    ) {
                list[i].setEffect(primaryColor);
            } else if(i==2 || i==10) { // && colorscheme = half/quad
                list[i].setEffect(secondaryColor);
            } else if(i==3) {
                list[i].setEffect(ornamentColor);
            } else if(i==4) {
                list[i].setEffect(pauldronColor);
            } else {
                if(i == GameData.getCurrentData().colorScheme + 5) {
                    list[i].setVisible(true);
                    list[i].setEffect(secondaryColor);
                } else if(i != 11) {
                    list[i].setVisible(false);
                }
            }
        }
        return list;
    }

    /**
     * Get the armor's images by name
     * @param name armor image name, designated on the json file
     * @return array of ImageView
     * */
    public static ImageView[] getArmourImageByName(String name) {
        int idx = GameData.getArmoursImageName().indexOf(name);
        if(idx < 0 || idx >= armorImage.length) idx = 0;
        return getArmourImageById(idx);
    }

    private static ImageView[] accessoryImageList = null;
    /**
     * Get the accessory's images
     * @param id accessory id
     * @return array of ImageView
     * */
    public static ImageView[] getAccessoryImageById(int id) {
        ImageView[] list;
        if(accessoryImageList == null) {
            list = new ImageView[4];
            for(int i=0;i<4;i++)
                list[i] = new ImageView();
            accessoryImageList = list;
        } else {
            list = accessoryImageList;
        }

        if(id<0) {
            for (ImageView aList : list) aList.setImage(null);
            return list;
        }

        for (int i = 0; i < 4; i++) {
            list[i].setImage(accessoryImage[id][i]);
            list[i].setFitWidth(150);
            list[i].setFitHeight(200);

            if(i==1) list[i].setEffect(primaryColor);
            else if(i==2) list[i].setEffect(secondaryColor);
            else if(i==3) list[i].setEffect(ornamentColor);
        }
        return list;
    }

    private static ImageView[] vehiclesImageList = null;
    /**
     * Get the vehicle's images
     * @param id vehicle id
     * @param loadout vehicle loadout dictated by variant
     * @param pintle is vehicle with pintle mounting
     * @param isLarge if vehicle is shown with large or small size
     * @return array of ImageView
     * */
    public static ImageView[] getVehicleById(int id, int loadout, boolean pintle, boolean isLarge) {
        ImageView[] list;
        if (vehiclesImageList == null) {
            list = new ImageView[3 + 4 + 4 + 4]; // pintle 3, loadout top(4), chassis 4/6, loadout bottom(4)
            for (int i = 0; i < 15; i++) {
                list[i] = new ImageView();
                list[i].setFitHeight(isLarge ? 300 : 150);
                list[i].setFitWidth(isLarge ? 500 : 250);
            }
            vehiclesImageList = list;
        } else {
            list = vehiclesImageList;
        }

        VehicleType variant = GameData.getVehiclesVariantById(id);
        int counter = 0;
        if(pintle) {
            int pintleType = variant.getPintle();
            list[counter].setImage(vehicleWeaponImage[pintleType][0]);
            list[counter++].setEffect(null);
            list[counter].setImage(vehicleWeaponImage[pintleType][1]);
            list[counter++].setEffect(primaryColor);
            list[counter].setImage(vehicleWeaponImage[pintleType][2]);
            list[counter++].setEffect(weaponColor);
        }

        int top = variant.getLoadoutPrimary(loadout);
        if(top > -1) {
            list[counter].setImage(vehicleWeaponImage[top][0]);
            list[counter++].setEffect(null);
            list[counter].setImage(vehicleWeaponImage[top][1]);
            list[counter++].setEffect(primaryColor);
            list[counter].setImage(vehicleWeaponImage[top][2]);
            list[counter++].setEffect(secondaryColor);
            list[counter].setImage(vehicleWeaponImage[top][3]);
            list[counter++].setEffect(weaponColor);
        }

        int chassisType = GameData.getVehiclesVariantById(id).getChassis();
        if(loadout <= -1) {
            // No loadout, using custom hatches and TODO ornament coloring
            list[counter].setImage(vehicleChassisImage[chassisType][3]);
            list[counter++].setEffect(null);
            list[counter].setImage(vehicleChassisImage[chassisType][4]);
            list[counter++].setEffect(primaryColor);
        }
        list[counter].setImage(vehicleChassisImage[chassisType][0]);
        list[counter++].setEffect(null);
        list[counter].setImage(vehicleChassisImage[chassisType][1]);
        list[counter++].setEffect(primaryColor);
        list[counter].setImage(vehicleChassisImage[chassisType][2]);
        list[counter++].setEffect(secondaryColor);

        int sponson = variant.getLoadoutSecondary(loadout);
        if(sponson > -1) {
            list[counter].setImage(vehicleWeaponImage[sponson][0]);
            list[counter++].setEffect(null);
            list[counter].setImage(vehicleWeaponImage[sponson][1]);
            list[counter++].setEffect(primaryColor);
            list[counter].setImage(vehicleWeaponImage[sponson][2]);
            list[counter++].setEffect(secondaryColor);
            list[counter].setImage(vehicleWeaponImage[sponson][3]);
            list[counter++].setEffect(weaponColor);
        }

        for(int i=0;i<list.length;i++) {
            list[i].setVisible(i<counter);
        }

        return list;
    }

    public static ImageView getSquareById(int id) {
        ImageView result = new ImageView();
        setSquareWithId(result,id);
        return result;
    }

    public static void setSquareWithId(ImageView img, int id) {
        String cssStyle;
        switch (id) {
            case Field.normal: cssStyle = "land-empty"; break;
            case Field.obstacles: cssStyle = "land-obstacle"; break;
            case Field.impassable: cssStyle = "land-impassable"; break;
            case Field.occupied_friendly: cssStyle = "land-friendly"; break;
            case Field.occupied_hostile: cssStyle = "land-hostile"; break;
            case Field.occupied_both: cssStyle = "land-contested"; break;
            case Field.occupied_objective: cssStyle = "land-objective"; break;
            case Field.movable_friendly: cssStyle = "land-movable-friendly"; break;
            case Field.movable_hostile: cssStyle = "land-movable-hostile"; break;
            case Field.movable_both: cssStyle = "land-movable-contested"; break;
            case Field.movable_objective: cssStyle = "land-movable-objective"; break;
            default: cssStyle = "land-impassable"; break;
        }
        img.getStyleClass().clear();
        img.getStyleClass().add(cssStyle);
    }

    static int[][] currentMapping;
    static List<ImageView> currentDisplay;
    public static List<ImageView> getMapFromIntMap(int[][] map, float sqr_size) {
        ArrayList<ImageView> data = new ArrayList<>();
        ImageView temp;
        float x,y;
        for(int i=0;i<map.length;i++) {
            final int itemX = i;
            for(int j=0;j<map[i].length;j++) {
                temp = new ImageView();
                setSquareWithId(temp,map[i][j]);
                temp.setFitWidth(sqr_size);
                temp.setFitHeight(sqr_size);
                x = i * sqr_size * 27 / 32; y = j * sqr_size;
                if(i%2==0) y+=sqr_size/2;// else x+=sqr_size;
                temp.setX(x);
                temp.setY(y);
                // TODO move this function out of the ImageView group
                final int itemY = j;
                temp.setOnMouseClicked(event -> {
                    System.out.printf("\nPosition clicked %d %d",itemX,itemY);
                    int[][] newMap = GameData.getCurrentData().getCurrentBattle().handleClick(itemX,itemY);
                    if(newMap!=null) updateFromIntMap(newMap);
                });
                temp.setOnMouseEntered(event -> {
                    Battle.handleTooltipDisplay(itemX,itemY);
                });
                Tooltip.install(temp,MainScene.getSceneController().battleTooltip);
                data.add(temp);
            }
        }
        currentMapping = map;
        currentDisplay = data;
        return data;
    }

    public static List<ImageView> updateFromIntMap(int[][] map) {
        int counter = 0;
        for(int i=0;i<map.length;i++)
            for(int j=0;j<map[i].length;j++) {
                if(map[i][j] != currentMapping[i][j]) {
                    setSquareWithId(currentDisplay.get(counter),map[i][j]);
                    //System.out.println("Sqr ("+i+","+j+")("+counter+") replaced " + currentMapping[i][j] + "->" + map[i][j]);
                }
                counter++;
            }
        currentMapping = map;
        return currentDisplay;
    }

    public static void moveUnitToPosition(ImageView unitImg, int x, int y) {
        if(currentMapping == null) {
            System.err.printf("Cannot move unit to %d %d (uninitialized).",x,y);
            return;
        }
        if(x < 0 || y < 0) {
            System.err.printf("Cannot move unit to %d %d (wrong value).",x,y);
            return;
        }

        int counter = y;
        for(int i=0;i<x;i++)
            counter += currentMapping[i].length;
        ImageView currentSquare = currentDisplay.get(counter);
        double validX = currentSquare.getX() + currentSquare.getFitWidth() / 2 - unitImg.getFitWidth() / 2;
        double validY = currentSquare.getY() + currentSquare.getFitHeight() / 2 - unitImg.getFitHeight() / 2;
        unitImg.setX(validX);
        unitImg.setY(validY);
        System.err.printf("Moved map badge to %.2f %.2f (%d %d).",validX,validY, x, y);
    }

    public static ImageView getSquareAtPosition(int x, int y) {
        if(x < 0 || y < 0) return null;
        int counter = y;
        for(int i=0;i<x;i++)
            counter += currentMapping[i].length;
        return currentDisplay.get(counter);
    }

    /**
     * Get the icon's images
     * @param id icon id
     * @return ImageView
     * */
    public static Node getIconById(int id) {
        Rectangle2D rectangle = getIconRectById(id);
        ImageView cropper = new ImageView(rootIcon);
        cropper.setPreserveRatio(true);
        cropper.setFitHeight(rectangle.getHeight() * 30 / 70);
        cropper.setViewport(rectangle);
        cropper.setEffect(currentColor);
        return cropper;
    }

    public static ImageView getBadgeByName(String name, boolean isFriendly, float badgeSize) {
        ImageView result = null;
        for(int i=0;i<fieldBadgeName.length;i++) {
            if(fieldBadgeName[i].equals(name)) {
                result = new ImageView(fieldBadgeImage[i]);
            }
        }
        // friendlyBadge and hostileBadge is at 0 and 1 respectively due to initialization
        // most likely never going to reach, but better safe
        if(result == null) result = new ImageView(fieldBadgeImage[isFriendly ? 0 : 1]);
        result.setFitHeight(badgeSize);
        result.setFitWidth(badgeSize);
        result.getStyleClass().add("growWhenHover");
        return result;
    }

    public static ImageView currentWaitAnimation = null;
    private static ImageView getWaitAnimation(int type, float badgeSize, double x, double y) {
        System.out.printf("\n WaitAnimation %d, badgeSize %f, coord (%.2f, %.2f) ", type, badgeSize, x, y);
        if(currentWaitAnimation != null) {
            if(type >= 0)
                currentWaitAnimation.setImage(possibleWaitIcon[type]);
            currentWaitAnimation.setX(x);
            currentWaitAnimation.setY(y);
            return currentWaitAnimation;
        }

        currentWaitAnimation = new ImageView((type >= 0) ? possibleWaitIcon[type] : null);
        currentWaitAnimation.setFitHeight(badgeSize);
        currentWaitAnimation.setFitWidth(badgeSize);
        //Creating a rotate transition
        RotateTransition rotateTransition = new RotateTransition();

        //Setting the duration for the transition
        rotateTransition.setDuration(Duration.millis(1000));

        //Setting the node for the transition
        rotateTransition.setNode(currentWaitAnimation);

        //Setting the angle of the rotation
        rotateTransition.setByAngle(360);

        // Use LINEAR to make sure a smooth transition
        rotateTransition.setInterpolator(Interpolator.LINEAR);

        //Setting the cycle count for the transition
        rotateTransition.setCycleCount(1000);

        //Setting auto reverse value to false
        rotateTransition.setAutoReverse(false);

        //Playing the animation
        rotateTransition.play();

        currentWaitAnimation.setX(x);
        currentWaitAnimation.setY(y);
        return currentWaitAnimation;
    }

    public static ImageView getWaitAnimation(int type, int x, int y) {
        ImageView squareNeeded = getSquareAtPosition(x, y);
        int badgeSize = GameData.getCurrentData().setting.badgeSize + 12;
        if(squareNeeded == null) {
            squareNeeded = new ImageView();
            squareNeeded.setX(-1000);
            squareNeeded.setY(-1000);
        }
        double validX = squareNeeded.getX() + squareNeeded.getFitWidth() / 2 - badgeSize / 2;
        double validY = squareNeeded.getY() + squareNeeded.getFitHeight() / 2 - badgeSize / 2;
        return getWaitAnimation(type, badgeSize, validX, validY);
    }

    private static Image rootIcon = null;

    private static Image starMapImage = null;

    private static Image[] starImage = null;

    private static Image[] planetImage = null;

    private static Image[] combatMapImage = null;

    private static Image[][] armorImage = null;

    private static Image[][] weaponImage = null;

    private static Image[][] accessoryImage = null;

    private static Image[][] vehicleWeaponImage = null;

    private static Image[][] vehicleChassisImage = null;

    private static Image[] fieldBadgeImage = null;
    private static String[] fieldBadgeName = null;

    private static Image[] possibleWaitIcon = null;

    public static Rectangle2D getIconRectById(int id) {
        switch (id) {
            case normalIcon: return new Rectangle2D(0, 0, 140, 90);
            case squadIcon:  return new Rectangle2D(0, 90, 140, 90);
            case wingedIcon: return new Rectangle2D(0, 180, 140, 90);
            case eliteIcon: return new Rectangle2D(0, 300, 140, 90);

            case honourIcon: return new Rectangle2D(315, 0, 140, 90);
            case injuredIcon: return new Rectangle2D(315, 90, 140, 80);
            case chaplainIcon: return new Rectangle2D(315, 170, 140, 130);
            case apothecaryIcon: return new Rectangle2D(315, 300, 140, 80);

            case captainIcon: return new Rectangle2D(615, 0, 140, 80);
            case librarianIcon: return new Rectangle2D(615, 90, 140, 90);
            case terminatorIcon: return new Rectangle2D(615, 180, 140, 100);
            case devastatorIcon: return new Rectangle2D(615, 280, 140, 110);

            case assaultIcon: return new Rectangle2D(915, 0, 140, 90);
            case tacticalIcon: return new Rectangle2D(915, 90, 140, 80);
            case inquisitorIcon: return new Rectangle2D(915, 170, 140, 120);
            case cogIcon: return new Rectangle2D(915, 290, 140, 80);

            default: return new Rectangle2D(49, 15, 108, 72);
        }
    }

    public static ColorAdjust currentColor = modifyBaseColor(Color.GREEN);

    public static ColorAdjust primaryColor = modifyBaseColor(Color.RED);
    public static ColorAdjust secondaryColor = modifyBaseColor(Color.BLUE);
    public static ColorAdjust ornamentColor = modifyBaseColor(Color.GOLD);
    public static ColorAdjust pauldronColor = modifyBaseColor(Color.BLANCHEDALMOND);
    public static ColorAdjust weaponColor = modifyBaseColor(Color.TEAL);

    /**
     * Creating a adjustment that recolor an item from white base
     * @param clr the color needed to be changed into
     * @return ColorAdjust (use in setEffect)
     * */
    public static ColorAdjust modifyBaseColor(Color clr) {
        ColorAdjust adjust = new ColorAdjust();
        adjust.setHue((clr.getHue() + 180) % 360 / 180 - 1);
        adjust.setSaturation(clr.getSaturation());
        adjust.setBrightness(1 - clr.getBrightness());
        return adjust;
    }

    // Icon list
    public static final int normalIcon = 0;
    public static final int squadIcon = 1;
    public static final int wingedIcon = 2;
    public static final int eliteIcon = 3;
    public static final int honourIcon = 4;
    public static final int injuredIcon = 5;
    public static final int chaplainIcon = 6;
    public static final int apothecaryIcon = 7;
    public static final int captainIcon = 8;
    public static final int librarianIcon = 9;
    public static final int terminatorIcon = 10;
    public static final int devastatorIcon = 11;
    public static final int assaultIcon = 12;
    public static final int tacticalIcon = 13;
    public static final int inquisitorIcon = 14;
    public static final int cogIcon = 15;

    public static final int friendlyWait=0;
    public static final int hostileWait=1;
    public static final int neutralWait=2;
    public static final int otherWait=3;

    public static void viewRouteToSystem(data.StarMap.System sys) {
        //System.out.printf("\nRoute to %.2f %.2f.",sys.posX,sys.posY);
        Controller mainController = MainScene.runningScene.controller;
        Line line = mainController.lineToDestination;
        Text text = mainController.etaText;
        if(line == null) {
            mainController.lineToDestination = new Line();
            line = mainController.lineToDestination;
            mainController.StarMap.getChildren().add(line);
            mainController.etaText = new Text();
            text = mainController.etaText;
            mainController.StarMap.getChildren().add(text);
        }
        line.setOpacity(1.0);
        line.setEndX(sys.posX + 38);
        line.setEndY(sys.posY + 36);
        line.setStartX(GameData.getCurrentData().map.playerPosX + 20);
        line.setStartY(GameData.getCurrentData().map.playerPosY + 20);

        text.setText("ETA: " + GameData.getCurrentData().map.getEtaToSystem(sys));
        text.setOpacity(1.0);
        text.setX((sys.posX + GameData.getCurrentData().map.playerPosX + 30) / 2);
        text.setY((sys.posY + GameData.getCurrentData().map.playerPosY + 30) / 2);
        if(GameData.getCurrentData().map.checkRouteExist(sys)) {
            line.setStroke(Color.GREEN);
            text.setFill(Color.GREEN);
        } else {
            line.setStroke(Color.RED);
            text.setFill(Color.RED);
        }

    }

    public static void hideRouteToSystem() {
        if(MainScene.runningScene.controller.lineToDestination != null) {
            MainScene.runningScene.controller.lineToDestination.setOpacity(0.0);
            MainScene.runningScene.controller.etaText.setOpacity(0.0);
        }
    }
    //public static final int square_hostile=4;

    public static MissionDisplay currentMissionDisplay = null;
    public static void handleClickOnSystem(data.StarMap.System sys, double x, double y) {
        List<MissionConfig> listMissions = GameData.getCurrentData().map.getMissionsForSystem(sys);
        if(listMissions.size() == 0) {
            System.out.printf("\nNo mission available for System %s.", sys.name);
            return;
        }
        if(currentMissionDisplay == null) {
            try {
                currentMissionDisplay = new MissionDisplay();
            } catch (IOException e) {
                e.printStackTrace();
            }
            currentMissionDisplay.initialize();
            currentMissionDisplay.loadDisplayForMission(listMissions.get(0), MainScene.runningScene.controller.StarMap, x, y);
        } else {
            currentMissionDisplay.loadDisplayForMission(listMissions.get(0), MainScene.runningScene.controller.StarMap, x, y);
        }
    }
}

class MissionDisplay {
    public MissionDisplay(String sourceFile) throws IOException {
        FXMLLoader loader = new FXMLLoader(this.getClass().getResource(sourceFile));
        controller = new MissionDisplayController();
        loader.setController(controller);
        component = loader.load();
//        controller = loader.getController();
    }

    public MissionDisplay() throws IOException {
        this("missionBox.fxml");
    }

    Pane component;
    MissionDisplayController controller;
    ObservableList<Unit> selected = FXCollections.observableList(new ArrayList<>());
    ObservableList<Unit> available = FXCollections.observableList(new ArrayList<>());

    public void initialize() {
        component.getStylesheets().add("file:res/css/stylesheet.css");
//        if(controller == null) { System.err.print("CONTROLLER NULL!!1!"); }
        controller.MissionImg.setId("canvas");
        controller.DeployingIconList.setId("canvas");
        controller.MissionImg.getParent().getStyleClass().add("pane");
        controller.DeployPane.getStyleClass().add("pane");

        controller.SelectBtn.setOnAction(event -> toogleDeployPane(true));
        controller.StartBtn.setOnAction(event -> {
            MissionConfig mcfg = MissionDisplay.currentDisplayingMission;
            GameData.getCurrentData().createBattleFromConfig( getListDeployment(), mcfg);
        });
        controller.CancelBtn.setOnAction(event -> ((javafx.scene.layout.Pane) component.getParent()).getChildren().remove(component));

        controller.AllLeftBtn.setOnAction(event -> handleTransfer(controller.AllLeftBtn));
        controller.AllRightBtn.setOnAction(event -> handleTransfer(controller.AllRightBtn));
        controller.LeftBtn.setOnAction(event -> handleTransfer(controller.LeftBtn));
        controller.RightBtn.setOnAction(event -> handleTransfer(controller.RightBtn));
    }

    public void loadDisplayForMission(MissionConfig mcfg, javafx.scene.layout.Pane parent, double displayX, double displayY) {
        currentDisplayingMission = mcfg;
        controller.Description.setText(currentDisplayingMission.description);
        controller.Title.setText(currentDisplayingMission.name);
        controller.DeployPane.setVisible(false);

        controller.UnitAvailable.setCellFactory(param -> new UnitCell());
        controller.UnitCommitted.setCellFactory(param -> new UnitCell());
        controller.UnitAvailable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        controller.UnitCommitted.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        component.setLayoutX(displayX);
        component.setLayoutY(displayY);
        if(component.getParent() != parent) {
            parent.getChildren().add(component);
        }

        available.clear();
        selected.clear();
        available.addAll(GameData.getRoster());
    }

    public void toogleDeployPane(boolean toggling) {
//        if((controller.DeployPane.isVisible() && toggling) || (!controller.DeployPane.isVisible() && !toggling)) {
        if(controller.DeployPane.isVisible() == toggling) {
            controller.DeployPane.setVisible(false);
            return;
        }
        // Populate the available and command list
        controller.UnitAvailable.setItems(available);
        controller.UnitCommitted.setItems(selected);
        controller.DeployPane.setVisible(true);
    }

    public void handleTransfer(javafx.scene.control.Button button) {
        if(button == controller.AllRightBtn) {
            selected.addAll(available);
            available.clear();
        } else if(button == controller.AllLeftBtn) {
            available.addAll(selected);
            selected.clear();
        } else if(button == controller.RightBtn) {
            List<Unit> transfer = controller.UnitAvailable.getSelectionModel().getSelectedItems();
            available.removeAll(transfer);
            selected.addAll(transfer);
        } else if(button == controller.LeftBtn) {
            List<Unit> transfer = controller.UnitCommitted.getSelectionModel().getSelectedItems();
            selected.removeAll(transfer);
            available.addAll(transfer);
        } else {
            System.err.print("\nError: Unknown button call @handleTransfer");
        }
    }

    public void updateDeployingIconList(List<Unit> unitList) {
        List<Node> images = new ArrayList<>();
        int badgeSize = (int) controller.DeployingIconList.getHeight() - 4;
        for(Unit u:unitList)
            images.add(u.getUnitBadge(badgeSize));
        images.add(new Separator());
        for(Unit u:currentDisplayingMission.getEnemySquadList())
            images.add(u.getUnitBadge(badgeSize));
        controller.DeployingIconList.getChildren().removeAll();
        controller.DeployingIconList.getChildren().addAll(images);
    }

    public void updateDeployingIconList() {
        updateDeployingIconList(selected);
    }

    List<Unit> getListDeployment() {
        return new ArrayList<>();
    }

    public static MissionConfig currentDisplayingMission = null;
    public static final int unitBadgeSize = 20;

    static class UnitCell extends ListCell<Unit> {
        @Override
        public void updateItem(Unit unit, boolean empty) {
            super.updateItem(unit, empty);
            if(unit != null) {
                Label item = (Label) this.getGraphic();
                if(item != null) {
                    item.setText(unit.toString());
                } else {
                    item = new Label(unit.toString());
                }
                item.setGraphic(unit.getUnitBadge(unitBadgeSize));
                this.setGraphic(item);
            } else {
                this.setGraphic(null);
            }
        }
    }
}

class MissionDisplayController {
    @FXML Pane MissionImg;
    @FXML ProgressBar StrengthBar;
    @FXML javafx.scene.control.Label Description;
    @FXML javafx.scene.control.Label Title;
    @FXML HBox DeployingIconList;
    @FXML javafx.scene.control.Button CancelBtn;
    @FXML javafx.scene.control.Button SelectBtn;
    @FXML javafx.scene.control.Button StartBtn;
    @FXML javafx.scene.control.Button AutoBtn;
    @FXML javafx.scene.control.Button AllRightBtn;
    @FXML javafx.scene.control.Button RightBtn;
    @FXML javafx.scene.control.Button LeftBtn;
    @FXML javafx.scene.control.Button AllLeftBtn;
    @FXML javafx.scene.control.ListView UnitAvailable;
    @FXML javafx.scene.control.ListView UnitCommitted;

    @FXML
    Pane DeployPane;

    @FXML
    public void initialize() {
        System.out.println("Controller @MissionDisplay initialized.");
    }
}