package UI;

import data.GameData;
import data.Item.VehicleChassis;
import data.Item.VehicleType;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;

import java.lang.System;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Quan on 12/27/2016.
 */
public class ImageHelper {
    public static void init() {
        rootIcon = new Image("file:res/texture/all_icon.png");

        backgroundImage = new Image("file:res/texture/bg_star.jpg");

        starImage = loadPrefixedImage("starmap/star_",6);

        planetImage = loadPrefixedImage("starmap/star_",24);

        // TODO add contested (purple)
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
            // armor default with 9 parts
            armorImage[i] = loadPrefixedImage("view_unit/" + getArmourNameById(i) + "_",9);
        }

        int weaponNum = GameData.getWeaponsImageName().size();
        weaponImage = new Image[weaponNum][];
        for(int i=0;i<weaponNum;i++) {
            // armor default with 8 parts
            weaponImage[i] = loadPrefixedImage("view_weapon/weapon_" + getWeaponNameById(i) + "_",5);
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
    public static ImageView getBackgroundImage() {
        return new ImageView(backgroundImage);
    }

    /**
     * Getting the corresponding star by id
     * */
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

        if(id<weaponlist.size() && !weaponlist.get(id).equals("")) {
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
        List<String> weaponlist = GameData.getVehicleWeaponsImageName();

        if(id<weaponlist.size() && !weaponlist.get(id).equals("")) {
            return weaponlist.get(id);
        }

        return "";
    }

    /**
     * Get the corresponding vehicle weapon name by id, used for loading resource
     * @param id weapon id
     * @return string
     * */
    public static String getChassisById(int id) {
        List<VehicleChassis> weaponlist = GameData.getVehiclesChassus();

        if(id<weaponlist.size() && !weaponlist.get(id).imgName.equals("")) {
            return weaponlist.get(id).imgName;
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
            list = new ImageView[9];
            for(int i=0;i<9;i++)
                list[i] = new ImageView();
            unitImageList = list;
        } else {
            list = unitImageList;
        }
        for(int i=0;i<9;i++) {
            list[i].setImage(armorImage[id][i]);
            list[i].setFitWidth(150);
            list[i].setFitHeight(200);

            // color using scheme, with 4-7 as secondary distribution
            if(i==0 || i==1 || (i==2 && (GameData.getCurrentData().colorScheme == GameData.scheme_monotone ||
                            GameData.getCurrentData().colorScheme == GameData.scheme_center))
                    ) {
                list[i].setEffect(primaryColor);
            } else if(i==2) { // && colorscheme = half/quad
                list[i].setEffect(secondaryColor);
            } else if(i==3) {
                list[i].setEffect(ornamentColor);
            } else if(i==4) {
                list[i].setEffect(pauldronColor);
            } else {
                if(i - 5 == GameData.getCurrentData().colorScheme) {
                    list[i].setVisible(true);
                    list[i].setEffect(secondaryColor);
                } else {
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

    private static ImageView[] vehiclesImageList = null;
    /**
     * Get the vehicle's images
     * @param id vehicle id
     * @param type vehicle type
     * @return array of ImageView
     * */
    public static ImageView[] getVehicleByIdOld(int id, int type) {
        ImageView[] list;
        if (vehiclesImageList == null) {
            list = new ImageView[9];
            for (int i = 0; i < 9; i++)
                list[i] = new ImageView();
            vehiclesImageList = list;
        } else {
            list = vehiclesImageList;
        }

        if(id == rhino || id == razorback || id == predator) {
            int counter = 0;
            // top hatches/turret mount
            if(id == razorback) {
                list[counter++].setImage(vehicleWeaponImage[4][type*3]);

                list[counter].setImage(vehicleWeaponImage[4][type*3+1]);
                list[counter++].setEffect(primaryColor);

                list[counter].setImage(vehicleWeaponImage[4][type*3+2]);
                list[counter++].setEffect(weaponColor);
            } else if(id == predator) {
                // turret
                list[counter++].setImage(vehicleWeaponImage[1][type*3]);

                list[counter].setImage(vehicleWeaponImage[1][type*3+1]);
                list[counter++].setEffect(primaryColor);

                list[counter].setImage(vehicleWeaponImage[1][type*3+2]);
                list[counter++].setEffect(secondaryColor);
            } else /*if(id == rhino)*/ {
                list[counter++].setImage(vehicleWeaponImage[0][3]);
                list[counter].setImage(vehicleWeaponImage[0][4]);
                list[counter++].setEffect(secondaryColor);
            }

            // chassis with coloring
            list[counter++].setImage(vehicleWeaponImage[0][0]);
            list[counter].setImage(vehicleWeaponImage[0][1]);
            list[counter++].setEffect(primaryColor);
            list[counter].setImage(vehicleWeaponImage[0][2]);
            list[counter++].setEffect(secondaryColor);

            if(id == predator) {
                // sponson
                list[counter++].setImage(vehicleWeaponImage[2][type*3]);

                list[counter].setImage(vehicleWeaponImage[2][type*3+1]);
                list[counter++].setEffect(primaryColor);

                list[counter].setImage(vehicleWeaponImage[2][type*3+2]);
                list[counter++].setEffect(weaponColor);
            }

            for(int i=0;i<list.length;i++) {
                list[i].setFitHeight(150);
                list[i].setFitWidth(250);
                list[i].setOpacity((i>counter) ? 0.0 : 1.0);
            }
        }

        return list;
    }

    public static ImageView[] getVehicleById(int id, int loadout, boolean pintle) {
        ImageView[] list;
        if (vehiclesImageList == null) {
            list = new ImageView[3 + 4 + 4 + 4]; // pintle 3, loadout top(4), chassis 4/6, loadout bottom(4)
            for (int i = 0; i < 15; i++) {
                list[i] = new ImageView();
                list[i].setFitHeight(150);
                list[i].setFitWidth(250);
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

    public static ImageView[] getWeaponById(int id) {
        ImageView[] list = new ImageView[5];

        for (int i = 0; i < 5; i++) {
            list[i] = new ImageView();
            list[i].setImage(weaponImage[id][i]);
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
            case 0: cssStyle = "land-empty"; break;
            case 1: cssStyle = "land-obstacle"; break;
            case 2: cssStyle = "land-impassable"; break;
            case 3: cssStyle = "land-friendly"; break;
            case 4: cssStyle = "land-hostile"; break;
            case 5: cssStyle = "land-contested"; break;
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
                final int itemX = i; final int itemY = j;
                temp.setOnMouseClicked(event -> {
                    System.out.println("Position clicked " + itemX + " " + itemY);
                    int[][] newMap = GameData.getCurrentData().getCurrentBattle().handleClick(itemX,itemY);
                    if(newMap!=null) updateFromIntMap(newMap);
                });
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

    public static boolean isHeavyWeapon(int id) {
        return false;
    }

    private static Image rootIcon = null;

    private static Image backgroundImage = null;

    private static Image[] starImage = null;

    private static Image[] planetImage = null;

    private static Image[] combatMapImage = null;

    private static Image[][] armorImage = null;

    private static Image[][] weaponImage = null;

    private static Image[][] vehicleWeaponImage = null;

    private static Image[][] vehicleChassisImage = null;

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

    // Armor list
    public static final int aquilaArmour = 0;
    public static final int corvusArmour = 1;
    public static final int errantArmour = 2;
    public static final int ironArmour = 3;
    public static final int ironArmour_alt = 4;
    public static final int maximusArmour = 5;
    public static final int indomitusArmour = 6;
    public static final int tartarosArmour = 7;
    public static final int tartarosArmour_alt = 8;
    public static final int artificerArmour = 9;

    // Vehicle list
    public static final int rhino = 0;
    public static final int razorback = 1;
    public static final int predator = 2;
    public static final int land_raider = 3;

    // Vehicle type
    public static final int Razorback_HB = 0;
    public static final int Razorback_MM = 1;
    public static final int Razorback_L = 2;
    public static final int Predator_Annihilator = 0;
    public static final int Predator_Destructor = 1;

    public static final int square_empty=0;
    public static final int square_impassable=1;
    public static final int square_objective=2;
    public static final int square_friendly=3;
    public static final int square_hostile=4;
}
