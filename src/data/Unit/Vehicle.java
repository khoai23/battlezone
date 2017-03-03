package data.Unit;

import data.Battle.AttackFormat;
import data.GameData;
import data.TreeViewable;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Quan on 2/22/2017.
 */
public class Vehicle implements TreeViewable,Unit {
    final int type;
    int loadout;
    int hp;
    List<Astartes> crew;

    public Vehicle(int type, int loadout) {
        this.type = type;
        this.loadout = loadout;
    }

    @Override
    public int getStrength() {
        return hp;
    }

    @Override
    public int getMovement() {
        return GameData.getVehiclesVariant().get(type).getSpeed();
    }

    @Override
    public List<AttackFormat> getAttack(int range) {
        return new ArrayList<>();
    }

    @Override
    public boolean handleAttack(List<AttackFormat> attacks) {
        return false;
    }

    @Override
    public String toString() {
        return GameData.getVehiclesVariant().get(type).getName();
    }

    @Override
    public int getType() {
        return type;
    }

    @Override
    public int getIconId() {
        return 0;
    }
}
