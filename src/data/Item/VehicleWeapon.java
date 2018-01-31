package data.Item;

import data.Unit.Trait;

import javax.json.JsonObject;
import java.util.List;

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
    public final String type;
    String description;
    public List<Trait> traitList = null;

    public VehicleWeapon(String name, int str, int spd, String type, String desc, int id) {
        this.id = id;
        this.name = name;
        this.str = str;
        this.spd = spd;
        this.type = type;
        this.description = desc;
        stock = 0;
        checkRange();
    }


    public VehicleWeapon(JsonObject obj, int id) {
        this(obj.getString("name"), obj.getInt("str"), obj.getInt("spd"),
                obj.getString("type"), obj.getString("desc"), id);
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

    @Override
    public List<Trait> getItemTraits() {
        return null;
    }

    public static VehicleWeapon None = new VehicleWeapon("None", -1, -1, "melee", "Nothing", -1);
}
