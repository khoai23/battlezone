package data.Unit;

import UI.ImageHelper;
import data.TreeViewable;
import data.Unit.Unit;

/**
 * Created by Quan on 12/27/2016.
 */
public class Squad implements Unit, TreeViewable {
    private String squadName;

    public Squad(String name) {
        squadName = name;
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
        return 0;
    }

    @Override
    public int getAttack() {
        return 0;
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
