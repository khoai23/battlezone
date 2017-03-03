package data.Item;

import javax.json.JsonArray;
import javax.json.JsonObject;
import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Quan on 2/22/2017.
 */
public class VehicleType implements Serializable {
    int id;
    String name;
    int chassis;
    int speed;
    int armor;
    int defaultHp;
    int[][] loadOutData;
    String description;
    int carry;

    public VehicleType(JsonObject obj, int id) {
        this.id = id;
        name = obj.getString("name");
        chassis = obj.getInt("chassis");
        description = obj.getString("desc");
        speed = obj.getInt("spd");
        carry = obj.getInt("carry");
        armor = obj.getInt("armor");
        defaultHp = obj.getInt("hp");
        JsonArray weapon_set = obj.getJsonArray("weapons");
        int weaponNum = weapon_set.size();
        if( weaponNum == 0) return;
        int numOfVariant = weapon_set.getJsonArray(0).size();
        loadOutData = new int[numOfVariant][weaponNum];
        for(int i=0;i<numOfVariant;i++) {
            for(int j=0;j<weaponNum;j++) {
                loadOutData[i][j] = weapon_set.getJsonArray(j).getInt(i);
            }
        }
    }

    public int getSpeed() {
        return speed;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }
}
