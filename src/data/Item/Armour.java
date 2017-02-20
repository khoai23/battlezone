package data.Item;

import data.Item.Item;

import javax.json.JsonObject;

/**
 * Created by Quan on 12/23/2016.
 */
public class Armour implements Item {
    final int id;
    final int def;
    final int spd;
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

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public int getStock() {
        return stock;
    }
}
