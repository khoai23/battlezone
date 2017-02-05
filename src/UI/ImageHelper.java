package UI;

import data.GameData;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;

/**
 * Created by Quan on 12/27/2016.
 */
public class ImageHelper {
    public static void init() {
        rootIcon = new Image("file:res/texture/all_icon.png");

        avatarImage = new Image[8][7];

        for(int i=0;i<8;i++) {
            for (int j=0; j<7;j++) {
//                avatarImage[i][j] = new Image("file:res/texture/view_unit/spr_" + "aquila" + "_colors_" + j + ".png");
                avatarImage[i][j] = new Image("file:res/texture/view_unit/" + getArmourNameById(i) + "_" + j + ".png");
            }
        }

        vehicleImage = new Image[5][];

        // rhino chassis with top hatch in 7
        vehicleImage[0] = loadPrefixedImage("view_vehicle/rhino_",7);
        // predator turret lasc/auto
        vehicleImage[1] = loadPrefixedImage("view_vehicle/pred_top_",6);
        // predator sponsons lasc/hb
        vehicleImage[2] = loadPrefixedImage("view_vehicle/pred_spon_",6);
        // predator pintle storm bolter
        vehicleImage[3] = loadPrefixedImage("view_vehicle/pred_pintle_",3);
        // razorback top mount hb/mm/lasc
        vehicleImage[4] = loadPrefixedImage("view_vehicle/raz_top_",9);
        // ??
//        vehicleImage[3] = loadPrefixedImage("raz_top_",9);
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

    public static String getArmourNameById(int id) {
        switch (id) {
            case aquilaArmour: return "aquila";
            case corvusArmour: return "corvus";
            case errantArmour: return "errant";
            case ironArmour: return "iron";
            case ironArmour_alt: return "iron2";
            case indomitusArmour: return "indomitus";
            case tartarosArmour: return "tartaros";
            case tartarosArmour_alt: return "tartaros2";

            case artificerArmour: return "artificer";

            default:
                return "aquila";
        }
    }

    private static ImageView[] unitImageList = null;
    public static ImageView[] getArmourImageById(int id) {
        ImageView[] list;
        if(unitImageList == null) {
            list = new ImageView[7];
            for(int i=0;i<7;i++)
                list[i] = new ImageView();
            unitImageList = list;
        } else {
            list = unitImageList;
        }
        for(int i=0;i<7;i++) {
            list[i].setImage(avatarImage[id][i]);
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
                    list[i].setOpacity(1.0);
                    list[i].setEffect(secondaryColor);
                } else {
                    list[i].setOpacity(0.0);
                }
            }
        }
        return list;
    }

    private static ImageView[] vehiclesImageList = null;
    public static ImageView[] getVehicles(int id, int type, int deco) {
        ImageView[] list;
        if (vehiclesImageList == null) {
            list = new ImageView[8];
            for (int i = 0; i < 8; i++)
                list[i] = new ImageView();
            vehiclesImageList = list;
        } else {
            list = vehiclesImageList;
        }

        if(id == rhino || id == razorback || id == predator) {
            // wheels
            int counter = 0;
            list[counter++].setImage(vehicleImage[0][1]);
            // predator/razorback top
            if(id == razorback) {
                list[counter++].setImage(vehicleImage[4][type*3]);

                list[counter].setImage(vehicleImage[4][type*3+1]);
                list[counter++].setEffect(primaryColor);

                list[counter].setImage(vehicleImage[4][type*3+2]);
                list[counter++].setEffect(weaponColor);
            } else if(id == predator) {
                // turret
                list[counter++].setImage(vehicleImage[1][type*3]);

                list[counter].setImage(vehicleImage[1][type*3+1]);
                list[counter++].setEffect(primaryColor);

                list[counter].setImage(vehicleImage[1][type*3+2]);
                list[counter++].setEffect(secondaryColor);
            }

            // chassis
            list[counter].setImage(vehicleImage[0][0]);
            list[counter++].setEffect(primaryColor);

            if(id == predator) {
                // sponson
                list[counter++].setImage(vehicleImage[2][type*3]);

                list[counter].setImage(vehicleImage[2][type*3+1]);
                list[counter++].setEffect(primaryColor);

                list[counter].setImage(vehicleImage[2][type*3+2]);
                list[counter++].setEffect(weaponColor);
            } else{
                // extra secondary color
                list[counter].setImage(vehicleImage[0][3+deco]);
                list[counter++].setEffect(secondaryColor);
            }

            if(id==rhino || id==razorback) {
                // top hatch
                list[counter].setImage(vehicleImage[0][6]);
                list[counter].setEffect(primaryColor);
            }

            for(int i=0;i<list.length;i++) {
                list[i].setFitHeight(150);
                list[i].setFitWidth(250);
                list[i].setOpacity((i>counter) ? 0.0 : 1.0);
            }
        }

        return list;
    }

    private static Image rootIcon = null;

    private static Image[][] avatarImage = null;

    private static Image[][] vehicleImage = null;

    public static Node getIconById(int id) {
        Rectangle2D rectangle = getIconRectById(id);
        ImageView cropper = new ImageView(rootIcon);
        cropper.setPreserveRatio(true);
        cropper.setFitHeight(rectangle.getHeight() * 30 / 70);
        cropper.setViewport(rectangle);
        cropper.setEffect(currentColor);
        return cropper;
    }

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
     * @return the ColorAdjust that can be used in setEffect
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

}
