package data.Battle;

import data.Item.VehicleWeapon;
import data.Item.Weapon;
import data.Unit.Vehicle;

/**
 * Created by Quan on 2/28/2017.
 */
public class AttackFormat {
    public int strength;
    public int accuracy;
    public int time;
    public String traits;
    public String preferredTarget;

    public static AttackFormat createAttack(int str,int acc, int time, String trt, String pref) {
        AttackFormat att = new AttackFormat();
        att.strength = str;
        att.accuracy = acc;
        att.time = time;
        att.traits = trt;
        att.preferredTarget = pref;
        return att;
    }

    public static AttackFormat createAttack(Weapon wpn, int acc, String pref) {
        AttackFormat att = new AttackFormat();
        att.strength = wpn.str;
        att.accuracy = acc;
        att.time = wpn.spd;
        att.traits = wpn.getType();
        att.preferredTarget = pref;
        return att;
    }

    public static AttackFormat createAttack(VehicleWeapon wpn, int acc, String pref) {
        AttackFormat att = new AttackFormat();
        att.strength = wpn.str;
        att.accuracy = acc;
        att.time = wpn.spd;
        att.traits = wpn.getType();
        att.preferredTarget = pref;
        return att;
    }
}
