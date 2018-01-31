package data.Unit;

import data.Battle.AttackFormat;
import data.Utility;

import javax.json.JsonArray;
import javax.json.JsonObject;
import java.util.ArrayList;
import java.util.List;

public class EnemyIndividual implements Individual {
    String name;
    int id; int fullHp;
    int hp; int armor; int initiative;
    int meleeAtkStr; int rangeAtkStr;
    int meleeAtkSpd; int rangeAtkSpd;
    int meleeAtkAcc; int rangeAtkAcc;
    int rangeMax;
    public String refName = null;

    public EnemyIndividual(JsonObject obj, int id) {
        this.id = id;
        name = obj.getString("name");
        fullHp = hp = obj.getInt("hp");
        armor = obj.getInt("armor");
        initiative = obj.getInt("init");
        JsonArray weapon = obj.getJsonArray("melee");
        meleeAtkStr = weapon.getInt(0); meleeAtkSpd = weapon.getInt(1); meleeAtkAcc = weapon.getInt(2);
        weapon = obj.getJsonArray("ranged");
        rangeAtkStr = weapon.getInt(0); rangeAtkSpd = weapon.getInt(1); rangeAtkAcc = weapon.getInt(2);
        rangeMax = weapon.getInt(3);
        refName = obj.getString("refname");
    }

    public EnemyIndividual(EnemyIndividual base, int variation) {
        this.name = base.name;
        this.hp = base.hp;
        this.hp += Utility.rollBetween(-variation * hp / 4, variation * hp / 4);
        this.armor = base.armor + Utility.rollBetween(-variation * 2, variation * 2);
        this.initiative = base.initiative + Utility.rollBetween(-variation, variation);
        this.meleeAtkStr = base.meleeAtkStr; this.meleeAtkStr += Utility.rollBetween(-variation * meleeAtkStr / 6, variation * meleeAtkStr / 6);
        this.rangeAtkStr = base.rangeAtkStr; this.rangeAtkStr += Utility.rollBetween(-variation * rangeAtkStr / 4, variation * rangeAtkStr / 4);
        this.meleeAtkAcc = base.meleeAtkAcc; this.meleeAtkAcc += Utility.rollBetween(-(variation+1) * 3, (variation+1) * 3);
        this.rangeAtkAcc = base.rangeAtkAcc; this.rangeAtkAcc += Utility.rollBetween(-(variation+1) * 5, (variation+1) * 5);
        this.meleeAtkSpd = base.meleeAtkSpd;
        this.rangeAtkSpd = base.rangeAtkSpd;
        this.rangeMax = base.rangeMax;
    }

    public static int variation_none = 0;
    public static int variation_small = 1;
    public static int variation_much = 3;

    @Override
    public int getHp() {
        return hp;
    }

    @Override
    public int getFullHp() {
        return fullHp;
    }

    @Override
    public int getArmourValue() {
        return armor;
    }

    @Override
    public List<AttackFormat> getAttack(int range) {
        List<AttackFormat> attacks = new ArrayList<>();
        if(range == 0) {
            attacks.add(AttackFormat.createAttack(meleeAtkStr,meleeAtkAcc,range,meleeAtkSpd,"",""));
        } else if(range <= rangeMax) {
            attacks.add(AttackFormat.createAttack(rangeAtkStr,rangeAtkAcc,range,rangeAtkSpd,"",""));
        }

        return attacks;
    }

    @Override
    public boolean setHp(int value) {
        hp = value;
        return value < 0;
    }

    @Override
    public boolean isInfantry() {
        return true;
    }

    @Override
    public float getInitiative() {
        return initiative;
    }

    @Override
    public int getMaxRange() {
        return rangeMax;
    }

    @Override
    public List<Trait> getIndividualOffensiveTrait() {
        return new ArrayList<>();
    }

    @Override
    public List<Trait> getIndividualDefensiveTrait() {
        return new ArrayList<>();
    }

    @Override
    public String toString() {
       return name;
    }

    public static List<EnemyIndividual> initialization = null;
    public static int getIndividualByRefName(String ref) {
        for (int i=0;i<initialization.size();i++) {
            if(initialization.get(i).refName.equals(ref)) return i;
        }
        return -1;
    }
}
