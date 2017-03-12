package data.Unit;

import UI.ImageHelper;
import data.Battle.AttackFormat;
import data.GameData;
import data.Item.Accessory;
import data.Item.Weapon;
import data.TreeViewable;
import data.Utility;
import javafx.scene.image.ImageView;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * Created by Quan on 12/28/2016.
 */
public class Astartes implements TreeViewable, Serializable {
    public String status = "";
    protected String name;
    protected int[] equipment;
    // armour,hand1,hand2,accessory
    protected int[] baseStat;
    // wound, bs, ws, i
    public short level = 0;
    public int exp = 0;
    public int role = 0;
    public int hp = 5;
    protected int path;
    public String personalTrait;
    public List<Trait> traits = new ArrayList<>();

    public Astartes(String name, int[] all) {
        this.name = name;
        if(all.length>=4)
            baseStat = Arrays.copyOfRange(all,0,4);
        if(all.length>=8)
            equipment = Arrays.copyOfRange(all,4,8);
        if(all.length>=9)
            role = all[8];
        hp = baseStat[basehp];
        personalTrait = "";
        path = Utility.rollBetween(0,4);
    }

    public String toString() {
        return ((level > 3) ? "Venerable " : "") + getRole() + " " + this.name;
    }

    public String getRole() {
        switch (role) {
            case role_recruit:
            case role_neophyte:
                return "Recruit";
            case role_librarian:
                return "Librarian";
            case role_apothecary:
                return "Apothecary";
            case role_techmarine:
                return "Techmarine";
            case role_captain:
                return "Brother-Captain";
            case role_chaptermaster:
                return "Chapter Master";
            default:
                return "Brother";
        }
    }

    @Override
    public int getIconId() {
        switch (this.role) {
            case role_recruit:
            case role_neophyte:
                return ImageHelper.normalIcon;
            case role_devastator:
                return ImageHelper.devastatorIcon;
            case role_assault:
                return ImageHelper.assaultIcon;
            case role_tactical:
                return ImageHelper.tacticalIcon;
            case role_sternguard:
            case role_vanguard:
                return ImageHelper.eliteIcon;
            case role_honourguard:
                return ImageHelper.honourIcon;
            case role_captain:
                return ImageHelper.captainIcon;
            case role_chaptermaster:
                return ImageHelper.devastatorIcon;
            case role_apothecary:
                return ImageHelper.apothecaryIcon;
            case role_librarian:
                return ImageHelper.librarianIcon;
            case role_chaplain:
                return ImageHelper.chaplainIcon;
            case role_techmarine:
                return ImageHelper.cogIcon;
            default:
                return ImageHelper.normalIcon;
        }
    }

    public String statToString() {
        return "[W]" + baseStat[basehp] + " [BS]" + baseStat[rangeAcc] + " [WS]" + baseStat[meleeAcc] + " [I]" + baseStat[initiative];
    }

    public String equipmentToString() {
        return GameData.getArmourById(equipment[armour]).getName() + ", " +
                GameData.getWeaponById(equipment[weapon1]).getName() + ", "  +
                GameData.getWeaponById(equipment[weapon2]).getName() + ", "  +
                GameData.getAccessoryById(equipment[accessory]).getName();
    }

    public String expToString() {
        return "Lvl " + level + "[" + exp + "/" + AscensionPath.expRequired[level] + "]";
    }

    public static ArrayList<ImageView> display = new ArrayList<>();
    public ArrayList<ImageView> getUnitDisplay() {
        ArrayList<ImageView> all = display;
        display.clear();
//        all.addAll(Arrays.asList(ImageHelper.getArmourImageById(equipment[Astartes.armour])));
        ImageView[] armourImage = ImageHelper.getArmourImageById(equipment[Astartes.armour]);
        ImageView[] accessoryImage = ImageHelper.getAccessoryImageById(equipment[Astartes.accessory]);
        if(!GameData.getAccessoryById(equipment[Astartes.accessory]).showWithBackpack
            || GameData.getAccessoryById(equipment[Astartes.accessory]).showBeforeBackpack) {
            all.addAll(Arrays.asList(accessoryImage));
        }
        if(GameData.getAccessoryById(equipment[Astartes.accessory]).showWithBackpack){
            all.add(armourImage[9]); all.add(armourImage[10]);
        }
        int backImagesNum = all.size();
        all.addAll(Arrays.asList(Arrays.copyOfRange(armourImage,0,9)));
        if(hp <= 0) {
            // no weapon and show damage
            all.add(armourImage[11]);
            return all;
        }

        if(GameData.getAccessoryById(equipment[Astartes.accessory]).showWithBackpack) {
            if(!GameData.getAccessoryById(equipment[Astartes.accessory]).showBeforeBackpack)
                all.addAll(Arrays.asList(accessoryImage));
        }

        // TODO add terminator profile
        ImageView[] weapon = ImageHelper.getWeaponImageById(equipment[Astartes.weapon1],true);
        Weapon profile = GameData.getWeaponById(equipment[Astartes.weapon1]);
        if(weapon.length>0) {
            all.addAll(Arrays.asList(weapon));
            if(!profile.useDefaultArm())
                all.get(backImagesNum + 2).setImage(null);
            if(profile.neitherArms())
                all.get(backImagesNum + 1).setImage(null);
        }
        weapon = ImageHelper.getWeaponImageById(equipment[Astartes.weapon2], false);
        profile = GameData.getWeaponById(equipment[Astartes.weapon2]);
        if(weapon.length>0) {
            all.addAll(Arrays.asList(weapon));
            if(!profile.useDefaultArm())
                all.get(backImagesNum + 1).setImage(null);
            // no need for checking neither arms, due to 3-hand going w1 means no weapon in w2
        }

        return all;
    }

    public List<AttackFormat> getAttack(int range) {
        if(hp <= 0)  return new ArrayList<>();
        List<AttackFormat> attacksMade = new ArrayList<>();
        Weapon mainhand = GameData.getWeaponById(equipment[weapon1]);
        Weapon offhand = GameData.getWeaponById(equipment[weapon2]);
        if(range==0) {
            attacksMade.add(AttackFormat.createAttack(mainhand, this, 0));
            attacksMade.add(AttackFormat.createAttack(offhand, this, 0));
        } else {
            if(mainhand.getRange()>=range) {
                attacksMade.add(AttackFormat.createAttack(mainhand, this, range));
            }
            if(offhand.getRange()>=range) {
                attacksMade.add(AttackFormat.createAttack(offhand, this, range));
            }
        }
        return attacksMade;
    }

    public int getMeleeAccuracy() {
        int val = baseStat[meleeAcc];
        // 3 hands are cumbersome status, 4 are overloaded
        // todo take account of terminator armors
        if(GameData.getWeaponById(equipment[weapon1]).hand + GameData.getWeaponById(equipment[weapon2]).hand >=3 && level < 7)
            val -= 10;
        return val;
    }

    public int getRangeAccuracy() {
        int val = baseStat[rangeAcc];
        // todo take account of terminator armors
        if(GameData.getWeaponById(equipment[weapon1]).hand + GameData.getWeaponById(equipment[weapon2]).hand >=3 &&
                (level < 7 && GameData.getWeaponById(equipment[weapon1]).hand != 3))
            val -= 15;
        if(getAccessoryTrait().equals("stable")) {
            val += 5;
        }
        switch (status) {
            case "": break;
            case "concussive": val -= 30; break;
            case "disoriented": val -= 10; break;
        }
        return val;
    }

    public int getArmourValue() {
        int val = GameData.getArmourById(equipment[armour]).def;

        if(getAccessoryTrait().equals("r_field")) {
            val += 15;
        }

        return val;
    }

    public String getAccessoryTrait() {
        return GameData.getAccessoryById(equipment[accessory]).getTrait();
    }

    public List<Trait> getTraitsOnAttack() {
        List<Trait> listTraits = new ArrayList<>(traits);

        Accessory acc = GameData.getAccessoryById(equipment[accessory]);
        if(acc != Accessory.None) {
            listTraits.addAll(acc.traitList);
        }

        listTraits.removeIf(Trait::isNotOffensiveTrait);
        return listTraits;
    }

    public List<Trait> getTraitsOnDefend() {
        List<Trait> listTraits = new ArrayList<>(traits);

        Accessory acc = GameData.getAccessoryById(equipment[accessory]);
        if(acc != Accessory.None) {
            listTraits.addAll(acc.traitList);
        }

        listTraits.removeIf(Trait::isNotDefensiveTrait);
        return listTraits;
    }

    // role decide hp/+acc bonus.
    public static final int role_recruit=0;
    public static final int role_neophyte=1;
    public static final int role_devastator=2;
    public static final int role_assault=3;
    public static final int role_tactical=4;
    public static final int role_sternguard=5;
    public static final int role_vanguard=6;
    public static final int role_honourguard=7;
    public static final int role_captain=8;
    public static final int role_chaptermaster=9;
    public static final int role_apothecary=10;
    public static final int role_librarian=11;
    public static final int role_chaplain=12;
    public static final int role_techmarine=13;

    public static final int armour=0;
    public static final int weapon1=1;
    public static final int weapon2=2;
    public static final int accessory=3;

    public static final int basehp=0;
    public static final int rangeAcc=1;
    public static final int meleeAcc=2;
    public static final int initiative=3;
}

class AscensionPath {
    public static void ascend(Astartes target) {
        // The idea is that the warrior will have a path from which he can increase his relative skill.
        while(target.level < expRequired.length && target.exp >= expRequired[target.level]) {
            handleChangeData(target.level+1,target);
            target.exp -= expRequired[target.level];
            target.level++;
        }
    }

    static void handleChangeData(int levelUp, Astartes target) {
        //HP increase is independent on path.
        target.baseStat[Astartes.basehp] += (levelUp > 1) ? (levelUp > 6) ? 10 : 15 : 30;
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
        }
    }

    public static final int[] expRequired = {20,30,50,100,100,200};
    // recruit 0 -> brother 20 -> veteran 50 -> respected 100 -> ancient 200 -> venerable 300 -> legend 500

    static String getRandomTrait(String existingTrait, String prefix) {
        // TODO add the traitlist JSON file
        List<String> traitList = new ArrayList<>();
        int traitNum;
        do {
//            traitNum = (int)Math.floor(Math.random() * traitList.size());
            traitNum = Utility.rollBetween(0,traitList.size());
        } while (!traitList.get(traitNum).contains(prefix) || existingTrait.contains(traitList.get(traitNum)));

        if(existingTrait.equals("")) return traitList.get(traitNum);
        return existingTrait + "|" + traitList.get(traitNum);
    }

    // these are randomized on creation
    public static final int path_omni = 0;
    public static final int path_ranged = 1;
    public static final int path_melee = 2;
    public static final int path_command = 3;
    // these are decided randomly due to need
    public static final int path_mech = 4;
    public static final int path_tech = 5;
    public static final int path_medi = 6;
    public static final int path_preach = 7;
}
