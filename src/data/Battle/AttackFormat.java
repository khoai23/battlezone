package data.Battle;

import data.Item.VehicleWeapon;
import data.Item.Weapon;
import data.Unit.Astartes;
import data.Unit.Trait;
import data.Unit.Vehicle;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Quan on 2/28/2017.
 */
public class AttackFormat {
    public int strength;
    public int accuracy;
    public int time;
    public int range = 0;
    public String traits;
    public String preferredTarget;
    public List<Trait> traitList = new ArrayList<>();

    public static AttackFormat createAttack(int str,int acc, int range, int time, String trt, String pref) {
        AttackFormat att = new AttackFormat();
        att.strength = str;
        att.accuracy = acc;
        att.range = range;
        att.time = time;
        att.traits = trt;
        att.preferredTarget = pref;
        return att;
    }

    public static AttackFormat createAttack(Weapon wpn, int acc, String pref) {
        return createAttack(wpn,acc,"",pref);
    }

    public static AttackFormat createAttack(Weapon wpn, int acc, String adnTrait, String pref) {
        AttackFormat att = new AttackFormat();
        att.strength = wpn.str;
        att.accuracy = acc;
        att.time = wpn.spd;
        att.range = wpn.getRange();
        att.traits = wpn.getType() + (adnTrait.equals("") ? "" : "," + adnTrait);
        att.preferredTarget = pref;
        return att;
    }

    public static AttackFormat createAttack(Weapon wpn, Astartes ast, int range) {
        AttackFormat att = new AttackFormat();
        att.strength = wpn.str;
        att.accuracy = (range==0) ? ast.getMeleeAccuracy() : ast.getRangeAccuracy();
        att.time = wpn.spd;
        att.range = wpn.getRange();
        att.traitList.addAll(ast.traits);
        att.traitList.addAll(wpn.traitList);
        return att;
    }

    public static AttackFormat createAttack(VehicleWeapon wpn, int acc, String pref) {
        AttackFormat att = new AttackFormat();
        att.strength = wpn.str;
        att.accuracy = acc;
        att.time = wpn.spd;
        att.range = wpn.getRange();
        att.traits = wpn.getType();
        att.preferredTarget = pref;
        return att;
    }
}
