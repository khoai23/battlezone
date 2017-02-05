package data;

/**
 * Created by Quan on 12/23/2016.
 */
public class Weapon implements Item {
    final int Str;
    final int Spd;
    final int hand;
    int stock;
    final String name;
    final String description;

    public Weapon(int str,int spd, int hd, int stk, String n, String desc) {
        Str = str;
        Spd = spd;
        hand = hd;
        stock = stk;
        name = n;
        description = desc;
    }

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
