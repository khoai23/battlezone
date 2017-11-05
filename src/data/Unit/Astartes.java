package data.Unit;

import UI.ImageHelper;
import data.Battle.AttackFormat;
import data.GameData;
import data.Item.Accessory;
import data.Item.Armour;
import data.Item.Weapon;
import data.TreeViewable;
import data.Utility;
import javafx.scene.image.ImageView;

import javax.json.JsonArray;
import javax.json.JsonObject;
import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * Created by Quan on 12/28/2016.
 */
public class Astartes implements TreeViewable, Serializable, Individual {
    public String status = "";
    public String name;
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
        switch (role) {
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
            // no weapon and show setHp
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

            if(mainhand.getRange()==range) {
                attacksMade.add(AttackFormat.createAttack(mainhand, this, range));
            }
            if(offhand.getRange()==range) {
                attacksMade.add(AttackFormat.createAttack(offhand, this, range));
            }

//            attacksMade.add(AttackFormat.createAttack(mainhand, this, 0));
//            attacksMade.add(AttackFormat.createAttack(offhand, this, 0));
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
//        if(getAccessoryTrait().equals("stable")) {
//            val += 5;
//        }
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

    @Override
    public int getHp() {
        return hp;
    }

    @Override
    public boolean setHp(int value) {
        hp = value;
        return hp < 0;
    }

    @Override
    public boolean isInfantry() {
        return true;
    }

    @Override
    public float getInitiative() {
        return baseStat[initiative] + GameData.getArmourById(baseStat[armour]).spd;
    }

    @Override
    public int getMaxRange() {
        return Math.max(GameData.getWeaponById(equipment[weapon1]).getRange(),GameData.getWeaponById(equipment[weapon2]).getRange());
    }

    @Override
    public List<Trait> getIndividualOffensiveTrait() {
        List<Trait> listTraits = new ArrayList<>(traits);

        Accessory acc = GameData.getAccessoryById(equipment[accessory]);
        if(acc != Accessory.None) {
            listTraits.addAll(acc.traitList);
        }

        listTraits.removeIf(Trait::isNotOffensiveTrait);
        return listTraits;
    }

    @Override
    public List<Trait> getIndividualDefensiveTrait() {
        List<Trait> listTraits = new ArrayList<>(traits);

        Accessory acc = GameData.getAccessoryById(equipment[accessory]);
        if(acc != Accessory.None) {
            listTraits.addAll(acc.traitList);
        }

        listTraits.removeIf(Trait::isNotDefensiveTrait);
        return listTraits;
    }

    public void changeEquipment(int type, int eq) {
        equipment[type] = eq;
//        System.out.println("Equipment change called, new eq " + equipmentToString());
    }

    public int getEquipment(int type) { return equipment[type]; }

    public void changeEquipment(int[] newEquipment) {
        if(newEquipment.length == 4)
            equipment = newEquipment;
        else if(newEquipment.length == 9) {
            equipment = new int[4];
            System.arraycopy(newEquipment,4,equipment,0,4);
        } else
            System.err.printf("\nWrong input @%s, array len %d", toString(), newEquipment.length);
    }

    public int[] getCloneArray() {
        int[] clone = new int[9];
        System.arraycopy(baseStat,0,clone,0,4);
        System.arraycopy(equipment,0,clone,4,4);
        clone[8] = role;
        return clone;
    }
}