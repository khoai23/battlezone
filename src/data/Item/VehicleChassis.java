package data.Item;

import data.Unit.Trait;

import javax.json.JsonObject;
import java.io.Serializable;
import java.util.List;

/**
 * Created by Quan on 3/5/2017.
 */
public class VehicleChassis implements Item {
    int id;
    public String name;
    public String description;
    public String imgName;
    int stock;

    public VehicleChassis(JsonObject obj, int id) {
        this.id = id;
        name = obj.getString("name");
        description = obj.getString("desc");
        imgName = obj.getString("imgName");
        stock = 0;
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public String getType() {
        return "Chassis";
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getDescription() {
        return description;
    }

    public int getStock() { return stock; }

    @Override
    public List<Trait> getItemTraits() {
        return null;
    }
}
