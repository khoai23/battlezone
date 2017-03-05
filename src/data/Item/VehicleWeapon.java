package data.Item;

import javax.json.JsonObject;

/**
 * Created by Quan on 2/22/2017.
 */
public class VehicleWeapon implements Item {
    int id;
    String name;
    public int str;
    public int spd;
    int stock;
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
    }

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
