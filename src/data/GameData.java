package data;

import data.Item.Armour;
import data.Item.Item;
import data.Item.Weapon;
import data.Unit.Unit;

import javax.json.*;
import java.io.*;
import java.util.ArrayList;

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
    ArrayList<String> weaponsImageName;
    ArrayList<Armour> armours;
    ArrayList<String> armoursImageName;

    public GameData() {
        chapterName = "Death Bringer";
        chapterMaster = "Karkos";
        roster = new ArrayList<>();
        weapons = new ArrayList<>();
        weaponsImageName = new ArrayList<>();
        armours = new ArrayList<>();
        armoursImageName = new ArrayList<>();
        colorScheme = scheme_center;
        loadDefaultData();
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
            armoursImageName.clear();
            for(int i=0;i<armorArray.size();i++) {
                armours.add(new Armour(armorArray.getJsonObject(i),i));
                armoursImageName.add(armorArray.getJsonObject(i).getString("imgName"));
            }

            weapons.clear();
            for(int i=0;i<weaponArray.size();i++) {
                weapons.add(new Weapon(weaponArray.getJsonObject(i),i));
                weaponsImageName.add(weaponArray.getJsonObject(i).getString("imgName"));
            }
        } catch (FileNotFoundException e) {
            System.out.print("File not found @" + filePath + "\n");
        }
    }

    public ArrayList<String> getWeaponsImageName() {
        return weaponsImageName;
    }

    public ArrayList<String> getArmoursImageName() {
        return armoursImageName;
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
