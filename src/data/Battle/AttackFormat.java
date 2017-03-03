package data.Battle;

/**
 * Created by Quan on 2/28/2017.
 */
public class AttackFormat {
    public int strength;
    public int accurracy;
    public int time;
    public String traits;
    public String preferedTarget;

    public static AttackFormat createAttack(int str,int acc, int time, String trt, String pref) {
        AttackFormat att = new AttackFormat();
        att.strength = str;
        att.accurracy = acc;
        att.time = time;
        att.traits = trt;
        att.preferedTarget = pref;
        return att;
    }
}
