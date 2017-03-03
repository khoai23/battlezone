package data.Unit;

import UI.ImageHelper;
import data.Battle.AttackFormat;
import data.TreeViewable;
import data.Unit.Unit;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Quan on 12/27/2016.
 */
public class Squad implements Unit {
    private String squadName;
    public ArrayList<Astartes> members;

    public Squad(String name) {
        squadName = name;
        members = new ArrayList<>();
    }

    public Squad() {
        this("Zatheas");
    }

    @Override
    public int getStrength() {
        return 0;
    }

    @Override
    public int getMovement() {
        return 2;
    }

    @Override
    public List<AttackFormat> getAttack(int range) {
        List<AttackFormat> list = new ArrayList<>();
        for(Astartes a:members) list.addAll(a.getAttack(range));
        return list;
    }

    @Override
    public boolean handleAttack(List<AttackFormat> attacks) {
        return false;
    }

    @Override
    public int getType() {
        return 0;
    }

    @Override
    public String toString() {
        return "Squad " + this.squadName;
    }

    public int getIconId() {
        return ImageHelper.squadIcon;
    }
}
