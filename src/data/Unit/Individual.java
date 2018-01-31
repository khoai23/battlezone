package data.Unit;

import data.Battle.AttackFormat;

import java.util.List;

/**
 * Created by Quan on 3/13/2017.
 */
public interface Individual {
    int getHp();
    int getArmourValue();
    List<AttackFormat> getAttack(int range);
    boolean setHp(int value);
    int getFullHp();
    boolean isInfantry();
    float getInitiative();
    int getMaxRange();
    String toString();
    List<Trait> getIndividualOffensiveTrait();
    List<Trait> getIndividualDefensiveTrait();
}
