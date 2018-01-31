package data.Unit;

import data.GameData;
import data.Utility;

import javax.json.JsonArray;
import javax.json.JsonObject;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AscensionPath implements Serializable {
    public String name;
    double[] improvement;
    public int beginLvl;
    public int endLvl;
    public boolean isSpecialAscension;
    public int iconId;
    public int leaderIconId;
    public String refName;
    public String leaderRefName;
    public String unitBadge = null;
    TraitLvlCoupling[] traitAcquired;

    public AscensionPath(JsonObject obj, JsonObject secondary) {
        name = obj.getString("name");
        improvement = new double[] { obj.getJsonNumber("baseHP").doubleValue(),
                obj.getJsonNumber("baseBS").doubleValue(),
                obj.getJsonNumber("baseWS").doubleValue(),
                obj.getJsonNumber("baseI").doubleValue()};
        beginLvl = obj.getInt("startPoint");
        endLvl = obj.getInt("endPoint");
        iconId = obj.getInt("iconId");

        if(obj.containsKey("unitBadge"))
            unitBadge = obj.getString("unitBadge");
        else
            unitBadge = Utility.friendlyBadge;

        if(obj.containsKey("refName"))
            refName = obj.getString("refName");
        else
            refName = name;
        //refName = obj.getString("refName");
        if(obj.containsKey("leaderIconId"))
            leaderIconId = obj.getInt("leaderIconId");
        else
            leaderIconId = iconId;
        if(obj.containsKey("leaderName"))
            leaderRefName = obj.getString("leaderName");
        else
            leaderRefName = refName;

//        name = obj.getString("name");
        if(obj.containsKey("special")) {
            String specialType = obj.getString("special");
            isSpecialAscension = true;
            JsonArray tObjArr = secondary.getJsonArray(specialType);
            List<TraitLvlCoupling> traitList= new ArrayList<>();
            for(int i=0; i<tObjArr.size(); i++) {
                for(String tName:tObjArr.getJsonObject(i).getString("trait").split("\\|"))
                    traitList.add(new TraitLvlCoupling(tObjArr.getJsonObject(i).getInt("getAt"),
                            Trait.getReadTraitWithName(tName)));
            }
            traitAcquired = new TraitLvlCoupling[traitList.size()];
            traitList.toArray(traitAcquired);
        }
    }

    public AscensionPath(JsonObject obj) {
        this(obj, null);
    }

    public static void ascend(Astartes target, boolean automated, int ascendTo) {
        if(!lvlUpPossible(target) && ascendTo < 0) return;
        // The idea is that each warrior will have a path from which he can increase his relative skill.
        while(target.level * target.level <= target.exp || (ascendTo > -1 && target.level < ascendTo)) {
            if(GameData.getAscensionPathById(target.path).endLvl <= target.level) {
                int oldPath = target.path;
                if(GameData.getMiscSetting().fullyRandomizedAstartesStat || automated) {
                    AscensionPath newPath = GameData.getRandomAscensionPathAtLvl(target.level, false);
                    target.path = GameData.getAscensionPathIdx(newPath);
                } else {
                    // TODO remind player to choose path for each individual
                    AscensionPath newPath = GameData.getRandomAscensionPathAtLvl(target.level, false);
                    target.path = GameData.getAscensionPathIdx(newPath);
                }
                System.out.printf("\nChange path for brother %s, path %s to path %s, level %d", target.name, GameData.getAscensionPathById(oldPath).name,
                        GameData.getAscensionPathById(target.path).name, target.level);
            }
            handleChangeData(target.level,target);
            target.exp -= target.level * target.level;
            target.level++;
        }
        if(ascendTo > -1)
            target.exp = 0;
    }

    public static boolean lvlUpPossible(Astartes target) {
        return target.level * target.level <= target.exp;
    }

    static void handleChangeData(int levelUp, Astartes target) {
        AscensionPath asp = GameData.getAscensionPathById(target.path);
        target.baseStat[Astartes.basehp] += (int)(asp.improvement[Astartes.basehp] * (levelUp + 1)) - (int)(asp.improvement[Astartes.basehp] * levelUp);
        target.baseStat[Astartes.rangeAcc] += (int)(asp.improvement[Astartes.rangeAcc] * (levelUp + 1)) - (int)(asp.improvement[Astartes.rangeAcc] * levelUp);
        target.baseStat[Astartes.meleeAcc] += (int)(asp.improvement[Astartes.meleeAcc] * (levelUp + 1)) - (int)(asp.improvement[Astartes.meleeAcc] * levelUp);
        target.baseStat[Astartes.initiative] += (int)(asp.improvement[Astartes.initiative] * (levelUp + 1)) - (int)(asp.improvement[Astartes.initiative] * levelUp);

        if(asp.isSpecialAscension) {
            List<Trait> applicable = new ArrayList<>();
            Arrays.asList(asp.traitAcquired).forEach(traitLvlCoupling -> {
                if(traitLvlCoupling.lvl == levelUp) applicable.add(traitLvlCoupling.trait);
            });

            target.traits.add(Utility.getRandomItem(applicable));
        }
        //HP increase is independent on path.
        /*target.baseStat[Astartes.basehp] += (levelUp > 1) ? (levelUp > 6) ? 10 : 15 : 30;
        switch (target.path) {
            case path_omni: {
                // This path will increase both melee and ranged accuracy
                target.baseStat[Astartes.meleeAcc] += (levelUp > 1) ? (levelUp > 4) ? 5 : 10 : 15;
                target.baseStat[Astartes.rangeAcc] += (levelUp > 3) ?  5 : 10;
                break;
            }
            case path_ranged: {
                // This path will increase mostly ranged accuracy
                target.baseStat[Astartes.meleeAcc] += (levelUp > 1) ? (levelUp > 5) ? 3 : 0 : 12;
                target.baseStat[Astartes.rangeAcc] += (levelUp > 3) ?  8 : 16;
                break;
            }
            case path_melee: {
                // This path will increase mostly melee accuracy
                target.baseStat[Astartes.meleeAcc] += (levelUp > 2) ? (levelUp > 4) ? 4 : 12 : 13;
                target.baseStat[Astartes.rangeAcc] += (levelUp > 4) ?  5 : 3;
                break;
            }
            case path_command: {
                // This path will increase commanding capacity
                target.personalTrait = getRandomTrait(target.personalTrait,"com_");
                break;
            }
            default: {
                System.err.println("Wrong path detected, path " + target.path);
            }
        }*/
    }

    public String getNameOfPathFollower(boolean isLeader) {
        return isLeader ? leaderRefName : refName;
    }

    public int getIconOfPath(boolean isLeader) {
        return isLeader ? leaderIconId : iconId;
    }

    public static int getNeededExp(int level, int currentExp) {
        return getExpNeededForLvl(level) - currentExp;
    }
    public static int getExpNeededForLvl(int level) {
        return level * level;
    }

    //public static final int[] expRequired = {20,30,50,100,100,200};
    // recruit 0 -> brother 20 -> veteran 50 -> respected 100 -> ancient 200 -> venerable 300 -> legend 500

    // these are randomized on creation
    public static final int path_omni = 0;
    public static final int path_ranged = 1;
    public static final int path_melee = 2;
    public static final int path_command = 3;
    // these are decided randomly due to need
    public static final int path_psy = 4;
    public static final int path_tech = 5;
    public static final int path_med = 6;
    public static final int path_preach = 7;
}

class TraitLvlCoupling {
    public final int lvl;
    public final Trait trait;
    public TraitLvlCoupling(int l, Trait t) {
        lvl = l; trait = t;
    }
}