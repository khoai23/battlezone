package data.Unit;

import javax.json.JsonArray;
import javax.json.JsonObject;
import java.io.Serializable;

/**
 * Created by Quan on 3/24/2017.
 *
 * Squad configurations are read from EnemyData.json
 */
public class EnemySquadConfig implements Serializable {
    int id;
    public String name;
    public int [][] composition;
    public String desc;

    public EnemySquadConfig(JsonObject obj, int id) {
        this.id = id;
        this.name = obj.getString("name");
        if(obj.containsKey("refComposition")) {
            JsonArray comp = obj.getJsonArray("refComposition");
            composition = new int[comp.size()][];
            for(int i=0;i<composition.length;i++) {
                composition[i] = new int[2];
                composition[i][indId] = EnemyIndividual.getIndividualByRefName(comp.getJsonArray(i).getString(indId));
                composition[i][indNum] = comp.getJsonArray(i).getInt(indNum);
            }
        } else {
            JsonArray comp = obj.getJsonArray("composition");
            composition = new int[comp.size()][];
            for(int i=0;i<composition.length;i++) {
                composition[i] = new int[2];
                composition[i][indId] = comp.getJsonArray(i).getInt(indId);
                composition[i][indNum] = comp.getJsonArray(i).getInt(indNum);
            }
        }
        desc = obj.getString("description");
    }

    public static int indId = 0;
    public static int indNum = 1;
}
