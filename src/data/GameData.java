package data;

import data.Battle.Battle;
import data.Item.*;
import data.StarMap.StarMap;
import data.Unit.*;

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
    List<Weapon> weapons;
    List<String> weaponsImageName;
    List<Armour> armours;
    List<String> armoursImageName;
    List<Accessory> accessories;
    List<VehicleType> variants;
    List<VehicleWeapon> vehicleWeapons;
    List<String> vehicleWeaponsImageName;
    List<VehicleChassis> vehicleChassus;
    List<Trait> traitList;
    Battle currentBattle = null;

    public GameData() {
        chapterName = "Death Bringer";
        chapterMaster = "Karkos";
        int[] baseStat = new int[]{40,90,90,8,2,9,2,2,Astartes.role_captain};
        you = new Astartes("Balzac", baseStat);
        you.level = 4;
        roster = new ArrayList<>();
        weapons = new ArrayList<>();
        weaponsImageName = new ArrayList<>();
        armours = new ArrayList<>();
        armoursImageName = new ArrayList<>();
        accessories = new ArrayList<>();
        variants = new ArrayList<>();
        vehicleWeapons = new ArrayList<>();
        vehicleWeaponsImageName = new ArrayList<>();
        vehicleChassus = new ArrayList<>();
        traitList = new ArrayList<>();
        colorScheme = scheme_quad;
        loadDefaultData();
        loadTestData();
    }

    public static String itemPath = "res/data/ItemData.json";
    public static String traitPath = "res/data/TraitData.json";

    public void loadDefaultData() {
        map = new StarMap();

        try {
            InputStream fis = new FileInputStream(itemPath);
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

            JsonArray vehicle_weapon = itemData.getJsonArray("vehicle_weapon");
            vehicleWeapons.clear();
            vehicleWeaponsImageName.clear();
            for(int i=0;i<vehicle_weapon.size();i++) {
                vehicleWeapons.add(new VehicleWeapon(vehicle_weapon.getJsonObject(i), i));
                vehicleWeaponsImageName.add(vehicle_weapon.getJsonObject(i).getString("imgName"));
            }

            JsonArray vehicle_chassis = itemData.getJsonArray("chassis");
            for(int i=0;i<vehicle_chassis.size();i++) {
                vehicleChassus.add(new VehicleChassis(vehicle_chassis.getJsonObject(i), i));
            }

            JsonArray accessories = itemData.getJsonArray("accessory");
            for(int i=0;i<accessories.size();i++) {
                this.accessories.add(new Accessory(accessories.getJsonObject(i), i));
            }

            reader = Json.createReader(new FileInputStream(traitPath));
            JsonArray traits = reader.readArray();
            for(int i=0;i<traits.size();i++) {
                traitList.add(new Trait(traits.getJsonObject(i), i));
            }

            reloadTraitOnWeapons();
        } catch (FileNotFoundException e) {
            System.out.print("File not found @" + itemPath + "\n");
        }
    }

    public void loadTestData() {
        Squad sqd = new Squad("Sqd 1");
        roster.add(sqd);
        sqd.members.add(new Astartes("Alande",new int[]{90,75,15,4,5,8,2,-1,Astartes.role_tactical}));
        sqd.members.add(new Astartes("Borien",new int[]{45,85,80,3,2,16,0,0,Astartes.role_sternguard}));

        Squad sqd2 = new Squad("Sqd 2");
        roster.add(sqd2);
        sqd2.members.add(new Astartes("Catharge",new int[]{40,50,50,6,0,14,3,1,Astartes.role_assault}));
        sqd2.members.add(new Astartes("Daniel",  new int[]{40,80,34,2,1,5,15,4,Astartes.role_librarian}));

        Vehicle predator = new Vehicle(2,1,true);
        Astartes gunner = new Astartes("Etufae",new int[]{35,60,80,6,3,0,0,-1,Astartes.role_devastator});
        gunner.hp = -10;
        predator.addCrewMember(gunner, false);
        predator.addCrewMember(new Astartes("Fragar",new int[]{40,50,80,3,4,0,0,-1,Astartes.role_devastator}), false);
        roster.add(predator);

        roster.add(new Vehicle(3,0));

        currentBattle = new Battle(0,roster,new ArrayList<>());

        currentBattle.move(sqd,3,4);

        Trait twinlinked = new Trait(new int[] {Trait.phase_nextAttack,Trait.traitType_single_enemy,Trait.predicate_attack_miss, -1,
                Trait.accuracy_offset, 10, -1}, 0);
        Trait spread_aux = new Trait(new int[] {Trait.phase_targetDecision,Trait.traitType_extra,Trait.attack_multiplier, -50,
                Trait.throwaway, -1, -1}, 1);
        Trait spread = new Trait(new int[] {Trait.phase_targetDecision, Trait.traitType_targetChange,Trait.trait_targetChange_attack_mult,
                100, 100, -1, 1}, 2);
        Trait bolt = new Trait(new int[] {Trait.phase_afterAttack, Trait.traitType_single_enemy,Trait.predicate_damage_inflicted_more,
                10, Trait.aadamage_multiplier, 100, -1}, 3);
//        sqd.members.get(1).traits.add(twinlinked);
        sqd.members.get(1).traits.add(spread);
    }

    public static List<String> getWeaponsImageName() {
        return GameData.getCurrentData().weaponsImageName;
    }

    public static List<String> getArmoursImageName() {
        return GameData.getCurrentData().armoursImageName;
    }

    public static List<String> getVehicleWeaponsImageName() { return GameData.getCurrentData().vehicleWeaponsImageName; }

    public static List<Unit> getRoster() {
        return GameData.getCurrentData().roster;
    }

    public static List<VehicleType> getVehiclesVariant() { return GameData.getCurrentData().variants; }

    public static List<VehicleChassis> getVehiclesChassus() { return GameData.getCurrentData().vehicleChassus; }

    public static List<Accessory> getAccessories() { return GameData.getCurrentData().accessories; }

    public List<Item> getAllItem() {
        ArrayList<Item> listItem = new ArrayList<>();
//        armours.toArray(listItem);
//        System.arraycopy(weapons.toArray(),0,listItem,armours.size(),weapons.size());
        listItem.addAll(armours);
        listItem.addAll(weapons);
        listItem.addAll(vehicleChassus);
        listItem.addAll(accessories);
        return listItem;
    }

    public Battle getCurrentBattle() {
        return currentBattle;
    }

    public static Armour getArmourById(int id) {
        if(id >= currentData.armours.size() || id < 0) return Armour.None;
        return currentData.armours.get(id);
    }

    public static Weapon getWeaponById(int id) {
        if(id >= currentData.weapons.size() || id < 0) return Weapon.None;
        return currentData.weapons.get(id);
    }

    public static Accessory getAccessoryById(int id) {
        if(id >= currentData.accessories.size() || id < 0) return Accessory.None;
        return currentData.accessories.get(id);
    }

    public static VehicleType getVehiclesVariantById(int id) {
        return GameData.getCurrentData().variants.get(id);
    }

    public static VehicleWeapon getVehiclesWeaponById(int id) {
        return GameData.getCurrentData().vehicleWeapons.get(id);
    }

    public static Trait getTraitById(int id) {
        if(id >= currentData.traitList.size() || id < 0) return Trait.None;
        return GameData.getCurrentData().traitList.get(id);
    }

    public Trait getTraitByName(String name) {
        for(Trait t:traitList) {
            if(t.getName().equals(name)) return t;
        }
        return Trait.None;
    }

    void reloadTraitOnWeapons() {
        Trait temp;
        for(Weapon wpn: weapons){
            for(String t:wpn.type.split(",")) {
                if(t.equals("melee") || t.equals("short") || t.equals("medium") || t.equals("long") || t.equals("extreme")) continue;
                temp = getTraitByName(t);
                if(temp == Trait.None) {
                    System.err.printf("\nCannot find trait %s for weapon %s on traitList",t,wpn.getName());
                } else {
                    wpn.traitList.add(temp);
                }
            }
        }
    }

    public static int scheme_monotone = 0;
    public static int scheme_center = 1;
    public static int scheme_half = 2;
    public static int scheme_quad = 3;
}
