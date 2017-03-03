package data.Unit;

import UI.ImageHelper;
import data.Battle.AttackFormat;
import data.GameData;
import data.Item.Weapon;
import data.TreeViewable;
import javafx.scene.image.ImageView;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Quan on 12/28/2016.
 */
public class Astartes implements TreeViewable, Serializable {
    private String name;
    private int[] equipment;
    // armour,hand1,hand2,accessory
    private int[] baseStat;
    // wound, bs, ws, i
    public short level = 0;
    public int exp = 0;
    public int role = 0;
    public int hp = 1;

    public Astartes(String name, int[] all) {
        this.name = name;
        if(all.length>=4)
            baseStat = Arrays.copyOfRange(all,0,4);
        if(all.length>=8)
            equipment = Arrays.copyOfRange(all,4,8);
        if(all.length>=9)
            role = all[8];
        hp = baseStat[basehp];
    }

    public String toString() {
//        if(role <=4 )
            return "Brother " + this.name;
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
                "";
    }

    public String expToString() {
        return "Lvl " + level + "[" + exp + "/" + 100 + "]";
    }

    public static ArrayList<ImageView> display = new ArrayList<>();
    public ArrayList<ImageView> getUnitDisplay() {
        ArrayList<ImageView> all = display;
        display.clear();
        all.addAll(Arrays.asList(ImageHelper.getArmourImageById(equipment[Astartes.armour])));
        // TODO add battle scarring(armour_9) and terminator profile
        ImageView[] weapon = ImageHelper.getWeaponImageById(equipment[Astartes.weapon1],true);
        Weapon profile = GameData.getWeaponById(equipment[Astartes.weapon1]);
        if(weapon.length>0) {
            all.addAll(Arrays.asList(weapon));
            if(!profile.useDefaultArm())
                all.get(2).setImage(null);
            if(profile.neitherArms())
                all.get(1).setImage(null);
        }
        weapon = ImageHelper.getWeaponImageById(equipment[Astartes.weapon2], false);
        profile = GameData.getWeaponById(equipment[Astartes.weapon2]);
        if(weapon.length>0) {
            all.addAll(Arrays.asList(weapon));
            if(!profile.useDefaultArm())
                all.get(1).setImage(null);
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
            if(mainhand.getRange()==0) {
                attacksMade.add(AttackFormat.createAttack(mainhand.str, getMeleeAccuracy(), mainhand.spd, mainhand.getType(), "soft"));
            }
            if(offhand.getRange()==0) {
                attacksMade.add(AttackFormat.createAttack(offhand.str, getMeleeAccuracy(), offhand.spd, offhand.getType(), "soft"));
            }
        } else {
            if(mainhand.getRange()>=range) {
                attacksMade.add(AttackFormat.createAttack(mainhand.str, getRangeAccuracy(), mainhand.spd, mainhand.getType(), "soft"));
            }
            if(offhand.getRange()>=range) {
                attacksMade.add(AttackFormat.createAttack(offhand.str, getRangeAccuracy(), offhand.spd, offhand.getType(), "soft"));
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
        return val;
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
