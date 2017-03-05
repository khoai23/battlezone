package data.Item;

import javax.json.JsonObject;
import java.io.Serializable;

/**
 * Created by Quan on 3/5/2017.
 */
public class VehicleChassis implements Serializable {
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

    public int getStock() { return stock; }
}
