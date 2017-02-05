package data;

/**
 * Created by Quan on 12/23/2016.
 */
public class Armour implements Item {
    final int Def;
    final int Spd;
    final boolean isTerminator;
    int stock;
    final String name;
    final String description;

    public Armour(int def,int spd, boolean term, int stk, String n, String desc) {
        Def = def;
        Spd = spd;
        isTerminator = term;
        stock = stk;
        name = n;
        description = desc;
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
