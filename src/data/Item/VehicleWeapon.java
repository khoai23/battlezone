package data.Item;

import javax.json.JsonObject;

/**
 * Created by Quan on 2/22/2017.
 *
 * A VehicleWeapon is exclusive to a Vehicle
 * TODO add components for the weapon
 */
public class VehicleWeapon implements Item {
    int id;
    String name;
    public int str;
    public int spd;
    int stock;
    int range;
    String type;
    String description;

    public VehicleWeapon(JsonObject obj, int id) {
        this.id = id;
        name = obj.getString("name");
        str = obj.getInt("str");
        spd = obj.getInt("spd");
        type = obj.getString("type");
        description = obj.getString("desc");
        stock = 0;
        checkRange();
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
    public int getId() {
        return id;
    }

    @Override
    public String getType() {
        // TODO fix proper type for these module
        return "Module";
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
