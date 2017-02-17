package data;

import javax.json.*;
import java.io.*;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by Quan on 12/23/2016.
 */
public class GameData implements Serializable {
    static GameData currentData = null;
    public static GameData getCurrentData() {
        if(GameData.currentData == null) {
            GameData.currentData = new GameData();
        }
        return GameData.currentData;
    }

    public String chapterName;
    public String chapterMaster;
    public int colorScheme;
    ArrayList<Unit> roster;
    ArrayList<Weapon> weapons;
    ArrayList<Armour> armours;

    public GameData() {
        chapterName = "Death Bringer";
        chapterMaster = "Karkos";
        roster = new ArrayList<Unit>();
        weapons = new ArrayList<Weapon>();
        armours = new ArrayList<Armour>();
        colorScheme = scheme_center;
    }

    public static String filePath = "res/data/ItemData.json";

    public void loadDefaultData() {
        try {
            InputStream fis = new FileInputStream(filePath);
            JsonReader reader = Json.createReader(fis);
            JsonObject itemData = reader.readObject();

            JsonArray armorArray = itemData.getJsonArray("armour");
            JsonArray weaponArray = itemData.getJsonArray("weapon");

            armours.clear();
            for(int i=0;i<armorArray.size();i++) {
                armours.add(new Armour(armorArray.getJsonObject(i),i));
            }

            weapons.clear();
            for(int i=0;i<weaponArray.size();i++) {
                weapons.add(new Weapon(weaponArray.getJsonObject(i),i));
            }
        } catch (FileNotFoundException e) {
            System.out.print("File not found @" + filePath+"\n");
        }
    }

    public Item[] getAllItem() {
        Item[] listItem = new Item[armours.size() +weapons.size()];
        armours.toArray(listItem);
        System.arraycopy(weapons.toArray(),0,listItem,armours.size(),weapons.size());
        return listItem;
    }

    public static int scheme_monotone = 0;
    public static int scheme_center = 1;
    public static int scheme_half = 2;
    public static int scheme_quad = 3;
}
