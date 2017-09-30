package data.Unit;

import data.Battle.AttackFormat;
import data.GameData;
import data.Utility;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Quan on 3/22/2017.
 *
 * An enemy squad created from read config in EnemyData.json
 */
public class EnemySquad implements Squad {
    int squadTypeId;
    List<EnemyIndividual> members;

    public EnemySquad(int squadType) {
        this(GameData.getBaseSquad(squadType));
        squadTypeId = squadType;
    }

    EnemySquad(EnemySquadConfig conf) {
        members = new ArrayList<>();
        for(int[] comp : conf.composition) {
            for(int i=0;i<comp[EnemySquadConfig.indNum];i++) {
                members.add(new EnemyIndividual(GameData.getBaseIndividual(comp[EnemySquadConfig.indId]),EnemyIndividual.variation_much));
            }
        }
    }

    @Override
    public List<Individual> getMembers() {
        return new ArrayList<>(members);
    }

    @Override
    public List<Trait> getSquadOffensiveTraits() {
        return new ArrayList<>();
    }

    @Override
    public List<Trait> getSquadDefensiveTraits() {
        return new ArrayList<>();
    }

    @Override
    public int getStrength() {
        int counter = 0;
        for(Individual bth:members) if(bth.getHp()>0) counter++;
        return counter;
    }

    @Override
    public int getMovement() {
        return 2;
    }

    @Override
    public List<AttackFormat> getAttack(int range) {
        List<AttackFormat> list = new ArrayList<>();
        for(EnemyIndividual en:members) list.addAll(en.getAttack(range));
        return list;
    }

    @Override
    public int getMaxRange() {
        int maxRange = 0;
        for(EnemyIndividual en:members) maxRange = Math.max(maxRange,en.getMaxRange());
        return maxRange;
    }

    @Override
    public int getType() {
        return Utility.speaker_hostile;
    }

    @Override
    public float getInitiative() {
        float sum = 0;
        for(EnemyIndividual bth:members) sum += bth.getInitiative();
        return sum / members.size();
    }

    @Override
    public String getDebugString() {
        String data = this.toString() + '\n';
        for(EnemyIndividual bth:members) data += bth.toString() + '|' + bth.getHp() + '\n';
        return data;
    }

    @Override
    public int getIconId() {
        return 0;
    }

    @Override
    public String toString() {
        return GameData.getBaseSquad(squadTypeId).name;
    }
}

