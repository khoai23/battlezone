package data.Item;

import data.Item.Item;
import data.StarMap.*;
import data.Unit.Trait;

import javax.json.JsonObject;
import java.lang.System;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Quan on 12/23/2016.
 */
public class Weapon implements Item {
    final int id;
    public final int str;
    public final int spd;
    public final int hand;
    int stock;
    final String name;
    final String description;
    public final String type;
    public List<Trait> traitList = new ArrayList<>();
    int range;

    public Weapon(int id, int str,int spd, int hd, int stk, String n, String desc, String tp) {
        this.id = id;
        this.str = str;
        this.spd = spd;
        hand = hd;
        stock = stk;
        name = n;
        description = desc;
        type = tp;
        checkRange();
    }

    public Weapon(JsonObject obj, int id) {
        this.id = id;
        str = obj.getInt("str");
        spd = obj.getInt("spd");
        hand = obj.getInt("hand");
        name = obj.getString("name");
        description = obj.getString("desc");
        type = obj.getString("type");
        stock = 0;
        checkRange();
    }

    public boolean useDefaultArm() {
        return (hand <= 2 && !type.contains("melee") && !name.contains("Sniper")) ||
                (name.contains("Missile") || name.contains("Fist") || name.contains("Claw"));
    }

    public boolean neitherArms() {
        return (hand == 3 && !name.contains("Missile"));
    }

    void checkRange() {
        if(type.contains("extreme")) {
            range = 4;
        } else if(type.contains("long")) {
            range = 3;
        } else if(type.contains("medium")) {
            range = 2;
        } else if(type.contains("short")) {
            range = 1;
        } else if(type.contains("melee")) {
            range = 0;
        } else {
            System.out.println("Error getting correct range for weapon name " + name);
            range = 0;
        }
    }

    public int getRange() { return range; }

    @Override
    public int getId() { return id; }

    @Override
    public String getType() {
        return "Weapon";
    }

    @Override
    public String getName() {
        return name;
    }

    public String toString() {
        return name;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public int getStock() {
        return stock;
    }

    public static final Weapon None = new Weapon(-1,0,0,0,0,"Empty","","");
}
