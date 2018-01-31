package data.Battle;

import data.Unit.EnemySquad;
import data.Unit.EnemySquadConfig;
import data.Unit.Unit;
import data.Utility;

import javax.json.JsonArray;
import javax.json.JsonObject;
import java.util.ArrayList;
import java.util.List;

public class MissionConfig {
    int id;
    public String name;
    public String description;
    public String refname;
    public int [][] composition;
    public int missionType;
    public int missionWeight;

    public MissionConfig(JsonObject obj, int id) {
        this.id = id;
        this.name = obj.getString("name");
        this.refname = obj.getString("refname");
        this.description = obj.getString("description");
        JsonArray comp = obj.getJsonArray("composition");
        composition = new int[comp.size()][];
        for(int i=0;i<composition.length;i++) {
            composition[i] = new int[3];
            composition[i][indId] = EnemySquadConfig.getSquadIdByName(comp.getJsonArray(i).getString(indId));
            composition[i][indLower] = comp.getJsonArray(i).getInt(indLower);
            if(comp.getJsonArray(i).size() <= 2)
                composition[i][indUpper] = composition[i][indLower];
            else
                composition[i][indUpper] = comp.getJsonArray(i).getInt(indUpper);
        }
        //desc = obj.getString("description");
        missionType = obj.getInt("missionType");
        missionWeight = obj.getInt("weight");
        //System.out.printf("\nMission loaded: %s, %s, %s, %d-%d",name,refname,description,missionType,missionWeight);
    }

    public List<Unit> getEnemySquadList() {
        List<Unit> list = new ArrayList<>();
        for (int[] aComposition : composition) {
            for(int i = Utility.rollBetween(aComposition[indLower], aComposition[indUpper]);i>0;i--)
                list.add(new EnemySquad(aComposition[indId]));
        }
        return list;
    }

    static int indId = 0;
    static int indLower = 1;
    static int indUpper = 2;
}
