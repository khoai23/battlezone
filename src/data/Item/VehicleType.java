package data.Item;

import data.GameData;
import data.Utility;

import javax.json.JsonArray;
import javax.json.JsonObject;
import java.io.Serializable;
import java.util.ArrayList;
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
    int pintle;
    int crew;
    int[][] loadOutData;
    String description;
    public String unitBadge = null;
    int carry;

    public VehicleType(JsonObject obj, int id) {
        this.id = id;
        name = obj.getString("name");
        chassis = obj.getInt("chassis");
        description = obj.getString("desc");
        speed = obj.getInt("spd");
        carry = obj.getInt("carry");
        crew = obj.getInt("crew");
        armor = obj.getInt("armor");
        defaultHp = obj.getInt("hp");
        pintle = obj.getInt("pintle");
        JsonArray weapon_set = obj.getJsonArray("weapons");
        if(obj.containsKey("unitBadge"))
            unitBadge = obj.getString("unitBadge");
        else
            unitBadge = Utility.friendlyBadge;
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

    public int getPintle() {
        return pintle;
    }

    public int getChassis() {
        return chassis;
    }

    public VehicleChassis chassis() {
        return GameData.getVehiclesChassus().get(chassis);
    }

    public int getLoadoutPrimary(int loadout) {
        if(loadOutData == null || loadOutData.length <= loadout || loadout <= -1) return -1;
        return loadOutData[loadout][0];
    }

    public int getLoadoutSecondary(int loadout) {
        if(loadOutData == null || loadOutData.length <= loadout || loadout <= -1 || loadOutData[loadout].length <= 1) return -1;
        return loadOutData[loadout][1];
    }

    public List<int[]> getLoadOutData() {
        try {
            return Arrays.asList(loadOutData) ;
        } catch (NullPointerException e) {
            return new ArrayList<>();
        }
    }

    public int getCrew() {
        return crew;
    }

    public int getArmor() {
        return armor;
    }

    public int getDefaultHp() { return defaultHp; }

    public String getDescription() {
        return description;
    }

    public boolean checkLoadoutViable(int loadout) {
        return loadout == -1 || (loadOutData.length > loadout && loadout >= 0);
    }

    public int numOfWeapons() {
        if(loadOutData.length == 0) return 0;
        else return loadOutData[0].length;
    }
}
