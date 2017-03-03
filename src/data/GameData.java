package data;

import data.Battle.Battle;
import data.Item.Armour;
import data.Item.Item;
import data.Item.VehicleType;
import data.Item.Weapon;
import data.StarMap.StarMap;
import data.Unit.Astartes;
import data.Unit.Squad;
import data.Unit.Unit;
import data.Unit.Vehicle;

import javax.json.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

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
    public Astartes you;
    public StarMap map;
    ArrayList<Unit> roster;
    ArrayList<Weapon> weapons;
    ArrayList<String> weaponsImageName;
    ArrayList<Armour> armours;
    ArrayList<String> armoursImageName;
    ArrayList<VehicleType> variants;
    Battle currentBattle = null;

    public GameData() {
        chapterName = "Death Bringer";
        chapterMaster = "Karkos";
        int[] baseStat = new int[]{40,90,90,8,2,7,2,0,Astartes.role_captain};
        you = new Astartes("Balzac", baseStat);
        roster = new ArrayList<>();
        weapons = new ArrayList<>();
        weaponsImageName = new ArrayList<>();
        armours = new ArrayList<>();
        armoursImageName = new ArrayList<>();
        variants = new ArrayList<>();
        colorScheme = scheme_quad;
        loadDefaultData();
        loadTestData();
    }

    public static String filePath = "res/data/ItemData.json";

    public void loadDefaultData() {
        map = new StarMap();

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

            JsonArray vehicle_type = itemData.getJsonArray("vehicle_type");
            variants.clear();
            for(int i=0;i<vehicle_type.size();i++) {
                variants.add(new VehicleType(vehicle_type.getJsonObject(i), i));
            }
        } catch (FileNotFoundException e) {
            System.out.print("File not found @" + filePath + "\n");
        }
    }

    public void loadTestData() {
        Squad sqd = new Squad("Sqd 1");
        roster.add(sqd);
        sqd.members.add(new Astartes("Alande",new int[]{90,80,15,4,5,2,3,0,2}));
        sqd.members.add(new Astartes("Borien",new int[]{45,60,80,3,2,11,1,0,2}));

        sqd = new Squad("Sqd 2");
        roster.add(sqd);
        sqd.members.add(new Astartes("Catharge",new int[]{40,50,50,6,0,8,10,0,2}));

        roster.add(new Vehicle(2,0));

        currentBattle = new Battle(0,roster,new ArrayList<>());

        currentBattle.move(sqd,2,4);
    }

    public static List<String> getWeaponsImageName() {
        return GameData.getCurrentData().weaponsImageName;
    }

    public static List<String> getArmoursImageName() {
        return GameData.getCurrentData().armoursImageName;
    }

    public static ArrayList<Unit> getRoster() {
        return GameData.getCurrentData().roster;
    }

    public static List<VehicleType> getVehiclesVariant() { return GameData.getCurrentData().variants; }

    public Item[] getAllItem() {
        Item[] listItem = new Item[armours.size() + weapons.size()];
        armours.toArray(listItem);
        System.arraycopy(weapons.toArray(),0,listItem,armours.size(),weapons.size());
        return listItem;
    }

    public Battle getCurrentBattle() {
        return currentBattle;
    }

    public static Armour getArmourById(int id) {
        if(id >= currentData.armours.size()) id=0;
        return currentData.armours.get(id);
    }

    public static Weapon getWeaponById(int id) {
        if(id >= currentData.weapons.size()) id=0;
        return currentData.weapons.get(id);
    }


    public static int scheme_monotone = 0;
    public static int scheme_center = 1;
    public static int scheme_half = 2;
    public static int scheme_quad = 3;
}
