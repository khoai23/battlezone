package data.Item;

import data.Item.Item;
import data.Unit.Trait;

import javax.json.JsonObject;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Quan on 12/23/2016.
 */
public class Armour implements Item {
    final int id;
    public final int def;
    public final int spd;
    final boolean isTerminator;
    int stock;
    final String name;
    final String description;

    public Armour(int id,int def,int spd, boolean term, int stk, String n, String desc) {
        this.id = id;
        this.def = def;
        this.spd = spd;
        isTerminator = term;
        stock = stk;
        name = n;
        description = desc;
    }

    public Armour(JsonObject obj, int id) {
        this.id = id;
        def = obj.getInt("def");
        spd = obj.getInt("spd");
        isTerminator = obj.getBoolean("isTermi");
        name = obj.getString("name");
        description = obj.getString("desc");
        stock = 0;
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public String getType() {
        return "Armour";
    }

    @Override
    public String getName() {
        return name;
    }

    public String toString() { return name; }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public int getStock() {
        return stock;
    }

    @Override
    public List<Trait> getItemTraits() { return null; }

    public boolean isTerminator() { return isTerminator; }

    public static Armour None = new Armour(-1,0,0,false,0,"None","");
}
