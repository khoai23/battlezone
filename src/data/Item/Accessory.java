package data.Item;

import data.Unit.Trait;

import javax.json.JsonObject;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Quan on 3/7/2017.
 */
public class Accessory implements Item {
    int id;
    String trait;
    String name;
    String description;
    public String imgName = "";
    public boolean showWithBackpack=false;
    public boolean showBeforeBackpack;
    public List<Trait> traitList = new ArrayList<>();
    int stock;

    public Accessory(int id, String name, String trait, String description, boolean showWithBackpack, boolean showBeforeBackpack) {
        this.id = id;
        this.name = name;
        this.trait = trait;
        this.description = description;
        this.showWithBackpack = showWithBackpack;
        this.showBeforeBackpack = showBeforeBackpack;
    }

    public Accessory(int id, String name, String trait, String description) {
        this(id,name,trait,description,false,false);
    }

    public Accessory(JsonObject obj, int id) {
        this.id = id;
        name = obj.getString("name");
        trait = obj.getString("trait");
        imgName = obj.getString("imgName");
        description = obj.getString("desc");
        if(obj.get("useBeforeBackpack") != null) {
            showWithBackpack = true;
            showBeforeBackpack = obj.getBoolean("useBeforeBackpack");
        }
    }
    @Override
    public int getId() {
        return id;
    }

    @Override
    public String getType() {
        return "Accessory";
    }

    @Override
    public String getName() {
        return name;
    }

    public String toString() {
        return name;
    }

    @Override
    public String getDescription() {
        return description;
    }

    public String getTrait() {
        return trait;
    }

    @Override
    public int getStock() {
        return stock;
    }

    public static Accessory None = new Accessory(-1,"None","","",true,true);
}
