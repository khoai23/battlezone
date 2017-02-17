package data;

import javax.json.JsonObject;

/**
 * Created by Quan on 12/23/2016.
 */
public class Weapon implements Item {
    final int id;
    final int str;
    final int spd;
    final int hand;
    int stock;
    final String name;
    final String description;

    public Weapon(int id, int str,int spd, int hd, int stk, String n, String desc) {
        this.id = id;
        this.str = str;
        this.spd = spd;
        hand = hd;
        stock = stk;
        name = n;
        description = desc;
    }

    public Weapon(JsonObject obj, int id) {
        this.id = id;
        str = obj.getInt("str");
        spd = obj.getInt("spd");
        hand = obj.getInt("hand");
        name = obj.getString("name");
        description = obj.getString("desc");
        stock = 0;
    }

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

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public int getStock() {
        return stock;
    }
}
