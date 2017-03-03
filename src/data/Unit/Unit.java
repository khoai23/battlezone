package data.Unit;

import data.Battle.AttackFormat;
import data.TreeViewable;

import java.io.Serializable;
import java.util.List;

/**
 * Created by Quan on 12/23/2016.
 */
public interface Unit extends Serializable, TreeViewable {
    // An unit may be a squad or a vehicle, as they are the individual in each battle
    int getStrength();
    int getMovement();
    List<AttackFormat> getAttack(int range);
    boolean handleAttack(List<AttackFormat> attacks);
    int getType();
}
