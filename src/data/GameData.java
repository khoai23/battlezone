package data;

import UI.Main;
import UI.MainScene;
import data.Battle.Battle;
import data.Battle.Field;
import data.Battle.MissionConfig;
import data.Item.*;
import data.StarMap.StarMap;
import data.Unit.*;
import javafx.application.Platform;

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
    List<EnemyIndividual> enemyBaseIndividual;
    List<EnemySquadConfig> enemyBaseSquad;
    List<MissionConfig> missionList;
    List<Trait> traitList;
    List<AscensionPath> statList;
    public Setting setting = new Setting();
    Battle currentBattle = null;

    public GameData() {
        // Needed 'you', hence a placeholder
        int[] baseStat = new int[]{40,90,90,8,2,9,2,2,Astartes.role_captain};
        you = new Astartes("Balzac", baseStat);
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
        enemyBaseIndividual = new ArrayList<>();
        enemyBaseSquad = new ArrayList<>();
        missionList = new ArrayList<>();
        statList = new ArrayList<>();
        colorScheme = scheme_quad;
        loadDefaultData();
        Platform.runLater(() -> {
            System.out.println("loadTestData waiter initialized");
            loadTestData();
        });
    }

    public static String itemPath = "res/data/ItemData.json";
    public static String traitPath = "res/data/TraitData.json";
    public static String enemyPath = "res/data/EnemyData.json";
    public static String defaultDataPath = "res/data/AstartesStat.json";

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
            Trait.initialization = new ArrayList<>();
            for(int i=0;i<traits.size();i++) {
                Trait.initialization.add(new Trait(traits.getJsonObject(i), i));
            }
            traitList = Trait.initialization;

            reloadTraitOnWeapons();
            reloadTraitOnAccessory();

            reader = Json.createReader(new FileInputStream(enemyPath));
            JsonObject enemyData = reader.readObject();
            JsonArray enemyIndv = enemyData.getJsonArray("individual");
            EnemyIndividual.initialization = new ArrayList<>();
            for(int i=0;i<enemyIndv.size();i++) {
                EnemyIndividual.initialization.add(new EnemyIndividual(enemyIndv.getJsonObject(i), i));
            }
            enemyBaseIndividual = EnemyIndividual.initialization;

            JsonArray enemySqdConf = enemyData.getJsonArray("unit");
            EnemySquadConfig.initialization = new ArrayList<>();
            for(int i=0;i<enemySqdConf.size();i++) {
                EnemySquadConfig.initialization.add(new EnemySquadConfig(enemySqdConf.getJsonObject(i), i));
            }

            JsonArray missionConf = enemyData.getJsonArray("mission");
            for(int i=0;i<missionConf.size();i++) {
                missionList.add(new MissionConfig(missionConf.getJsonObject(i), i));
            }
            enemyBaseSquad = EnemySquadConfig.initialization;

            reader = Json.createReader(new FileInputStream(defaultDataPath));
            JsonObject defaultData = reader.readObject();
            for(int i=0;i<defaultData.getJsonArray("stat").size();i++) {
                statList.add(new AscensionPath(defaultData.getJsonArray("stat").getJsonObject(i),
                        defaultData.getJsonObject("special_progression")));
            }

            if(Utility.nameList == null) {
                JsonArray nameGeneratorList = defaultData.getJsonArray("names");
                Utility.nameList = new String[nameGeneratorList.size()];
                for(int i=0;i<Utility.nameList.length;i++)
                    Utility.nameList[i] = nameGeneratorList.getString(i);
            }


            EnemyIndividual.initialization = null;
            Trait.initialization = null;
            EnemySquadConfig.initialization = null;
        } catch (FileNotFoundException e) {
            System.out.print("\nFile not found @" + itemPath);
        }
    }

    public void loadTestData() {
        chapterName = "Death Bringer";
        chapterMaster = "Karkos";
        you = Utility.createAstartes("Balzac", new int[]{2,9,2,2}, 55, "Command", true);

//        AstartesSquad sqd = //new AstartesSquad("Sqd 1");
//                Utility.createAstartesSquad(null);
//        roster.add(sqd);
////        sqd.tryAddMember(new Astartes("Alande",new int[]{90,75,15,4,5,8,2,-1,Astartes.role_tactical}));
////        sqd.tryAddMember(new Astartes("Borien",new int[]{45,85,80,3,2,16,0,0,Astartes.role_sternguard}));
//
//        AstartesSquad sqd2 = new AstartesSquad("Sqd 2");
//        roster.add(sqd2);
////        sqd2.tryAddMember(new Astartes("Catharge",new int[]{40,50,50,6,0,14,3,1,Astartes.role_assault}));
//        sqd2.tryAddMember(Utility.createAstartes("Catharge", new int[]{0,14,3,1}, 22, -1, false));
//        //sqd2.tryAddMember(new Astartes("Daniel",  new int[]{40,80,34,2,1,5,15,4,Astartes.role_librarian}));
//        sqd2.tryAddMember(Utility.createAstartes("Daniel", new int[]{1,5,15,4}, 40, -1, false));
//
//        Vehicle predator = new Vehicle(2,1,true);
//        Astartes gunner = new Astartes("Etufae",new int[]{35,60,80,6,3,0,0,-1,Astartes.role_devastator});
//        gunner.setHp(-10);
//        predator.addCrewMember(gunner, false);
//        predator.addCrewMember(new Astartes("Fragar",new int[]{40,50,80,3,4,0,0,-1,Astartes.role_devastator}), false);
//        roster.add(predator);
//
//        roster.add(new Vehicle(3,0));

        roster.addAll(Utility.createRandomCompany(null));

//        MissionConfig currentMission = missionList.get(0);
//        List<Unit> frSqd = new ArrayList<>();
//        frSqd.add(roster.get(0));
//        frSqd.add(sqd);
//        frSqd.add(sqd2);
//        frSqd.add(predator);

//        currentBattle = new Battle(Field.type_rectangular + 10 * Field.randomize_medium,
//                frSqd, currentMission.getEnemySquadList(), Battle.mode_encounterBattle);
        MissionConfig firstMission = missionList.get(0);
        data.StarMap.System systemChosen = Utility.getRandomItem(map.systems);
        map.addMissionForSystem(systemChosen, firstMission);
        System.out.printf("\nAdded mission: %s at system %s", firstMission.name, systemChosen.name);

//        MainScene.runningScene.showField();
        MainScene.runningScene.checkAndUpdateTab(MainScene.runningScene.controller.StatusTab);
        MainScene.runningScene.checkAndUpdateTab(MainScene.runningScene.controller.BattleTab);
//        currentBattle.runActionLoop();
    }

    public static List<String> getWeaponsImageName() {
        return GameData.getCurrentData().weaponsImageName;
    }

    public static List<String> getArmoursImageName() {
        return GameData.getCurrentData().armoursImageName;
    }

    public static List<String> getVehicleWeaponsImageName() { return GameData.getCurrentData().vehicleWeaponsImageName; }

    public static List<Unit> getRoster() { return GameData.getCurrentData().roster; }

    public static List<VehicleType> getVehiclesVariant() { return GameData.getCurrentData().variants; }

    public static List<VehicleChassis> getVehiclesChassus() { return GameData.getCurrentData().vehicleChassus; }

    public static List<Accessory> getAccessoryList() { return GameData.getCurrentData().accessories; }

    public static List<Trait> getTraitList() { return GameData.getCurrentData().traitList; }

    public static List<Armour> getArmourList() { return currentData.armours; }

    public static List<Weapon> getWeaponList() { return currentData.weapons; }

    public static List<String> getCombatBadgeList() {
        List<String> result = new ArrayList<>();
        result.add(Utility.friendlyBadge);
        result.add(Utility.hostileBadge);

        // All Friendly squad badges
        for(AscensionPath asp: currentData.statList) {
            if(asp.unitBadge != null && !result.contains(asp.unitBadge)) result.add(asp.unitBadge);
        }

        for(VehicleType veh: currentData.variants) {
            if(veh.unitBadge != null && !result.contains(veh.unitBadge)) result.add(veh.unitBadge);
        }

        for(EnemySquadConfig econf: currentData.enemyBaseSquad) {
            if(econf.unitBadge != null && !result.contains(econf.unitBadge)) result.add(econf.unitBadge);
        }

        return result;
    }

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

    public void setCurrentBattle(Battle b) {
        currentBattle = b;
    }

    public static Armour getArmourById(int id) {
        if(id >= currentData.armours.size() || id < 0) return Armour.None;
        return currentData.armours.get(id);
    }

    public static int getArmourIdByName(String name) {
        for(int i=0;i<currentData.armoursImageName.size();i++) {
            if(currentData.armoursImageName.get(i).equals(name) || currentData.armours.get(i).getName().equals(name))
                return i;
        }
        return -1;
    }

    public static Weapon getWeaponById(int id) {
        if(id >= currentData.weapons.size() || id < 0) return Weapon.None;
        return currentData.weapons.get(id);
    }

    public static int getWeaponIdByName(String name) {
        for(int i=0;i<currentData.weaponsImageName.size();i++) {
            if(currentData.weaponsImageName.get(i).equals(name) || currentData.weapons.get(i).getName().equals(name))
                return i;
        }
        return -1;
    }

    public static Accessory getAccessoryById(int id) {
        if(id >= currentData.accessories.size() || id < 0) return Accessory.None;
        return currentData.accessories.get(id);
    }

    public static int getAccessoryIdByName(String name) {
        for(int i=0;i<currentData.accessories.size();i++) {
            if(currentData.accessories.get(i).imgName.equals(name) || currentData.accessories.get(i).getName().equals(name))
                return i;
        }
        return -1;
    }

    public static VehicleType getVehiclesVariantById(int id) {
        return GameData.getCurrentData().variants.get(id);
    }

    public static int getVehiclesIdByName(String name) {
        for(int i=0;i<currentData.variants.size();i++) {
            if(currentData.variants.get(i).getName().equals(name))
                return i;
        }
        return -1;
    }

    public static VehicleWeapon getVehiclesWeaponById(int id) {
        try {
            return GameData.getCurrentData().vehicleWeapons.get(id);
        } catch(IndexOutOfBoundsException e) {
            return VehicleWeapon.None;
        }
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

    public static EnemyIndividual getBaseIndividual(int id) {
        return GameData.getCurrentData().enemyBaseIndividual.get(id);
    }

    public static EnemySquadConfig getBaseSquad(int id) {
        return GameData.getCurrentData().enemyBaseSquad.get(id);
    }

    public static EnemySquadConfig getSquadByRefName(String refname) {
        for(EnemySquadConfig esc: GameData.getCurrentData().enemyBaseSquad)
            if(esc.name.equals(refname)) return esc;
        return null;
    }

    public static int getSquadIdByRefName(String refname) {
        return GameData.getCurrentData().enemyBaseSquad.indexOf(getSquadByRefName(refname));
    }

    public static Setting getMiscSetting() {
        return GameData.getCurrentData().setting;
    }

    public static AscensionPath getAscensionPathById(int id) {
        return GameData.getCurrentData().statList.get(id);
    }

    public static AscensionPath getRandomAscensionPathAtLvl(int lvl, boolean includeSpecial) {
        List<AscensionPath> list = getAllAscensionPathAtLvl(lvl, includeSpecial);
        if(list.size() == 0) {
            System.err.printf("Cannot get list of path for %d, includeSpecial %s",lvl, includeSpecial);
            return null;
        } else {
            return list.get(Utility.rollBetween(0, list.size()-1));
        }
    }

    public static List<AscensionPath> getAllAscensionPathAtLvl(int lvl, boolean includeSpecial) {
        List<AscensionPath> list = new ArrayList<>(GameData.getCurrentData().statList);
        list.removeIf(path -> (path.endLvl <= lvl || path.beginLvl > lvl) || (!includeSpecial && path.isSpecialAscension));
        return list;
    }

    public static int getAscensionPathIdx(AscensionPath asp) {
        return GameData.getCurrentData().statList.indexOf(asp);
    }

    public static int getAscensionPathIdx(String aspName) {
        List<AscensionPath> statList = GameData.getCurrentData().statList;
        for(int i=0; i<statList.size(); i++) {
            if(statList.get(i).name.equals(aspName))
                return i;
        }
        return -1;
    }

    void reloadTraitOnWeapons() {
        Trait temp;
        List<Item> listAllWeapons = new ArrayList<>(weapons);
        listAllWeapons.addAll(vehicleWeapons);
        for(Item wpn: listAllWeapons){
            String[] listNameTrait;
            if(wpn instanceof Weapon) {
                listNameTrait = ((Weapon) wpn).type.split(",");
            } else {
                listNameTrait = ((VehicleWeapon) wpn).type.split(",");
            }
            List<Trait> loT = new ArrayList<>();
            for(String t: listNameTrait) {
                if(t.equals("melee") || t.equals("short") || t.equals("medium") || t.equals("long") || t.equals("extreme")) continue;
                temp = getTraitByName(t);
                if(temp == Trait.None) {
                    System.err.printf("\nCannot find trait %s for weapon %s on traitList",t,wpn.getName());
                } else {
                    loT.add(temp);
                }
            }
            if(wpn instanceof Weapon) {
                ((Weapon) wpn).traitList = loT;
            } else {
                ((VehicleWeapon) wpn).traitList = loT;
            }
        }
    }

    void reloadTraitOnAccessory() {
        Trait temp;
        for(Accessory acc: accessories) {
            for(String t:acc.getTrait().split(",")) {
                temp = getTraitByName(t);
                if(temp == Trait.None) {
                    System.err.printf("\nCannot find trait %s for accessory %s on traitList",t,acc.getName());
                } else {
                    acc.traitList.add(temp);
                }
            }
        }
    }

    public Battle createBattleFromConfig(List<Unit> friendly, MissionConfig missionConfig) {
        if(currentBattle != null) {
            System.err.print("\nBattle already initialized. Please recheck");
            // TODO null battle after finishing and return null here as well.
            return currentBattle;
        }

        currentBattle = new Battle(Field.type_rectangular + 10 * Field.randomize_medium,
                friendly, missionConfig.getEnemySquadList(), missionConfig.missionType);

        // MainScene.runningScene.showField();
        MainScene.openBattleTab();
        MainScene.runningScene.checkAndUpdateTab(MainScene.runningScene.controller.BattleTab);
        currentBattle.runActionLoop();

        return currentBattle;
    }

    public static int scheme_monotone = 0;
    public static int scheme_center = 1;
    public static int scheme_half = 2;
    public static int scheme_quad = 3;
}
