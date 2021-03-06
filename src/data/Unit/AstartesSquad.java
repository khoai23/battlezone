package data.Unit;

import UI.ImageHelper;
import data.Battle.AttackFormat;
import data.GameData;
import data.Utility;
import javafx.scene.image.ImageView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Quan on 12/27/2016.
 * A basic squad under your command
 */
public class AstartesSquad implements Squad {
    private String squadName;


    protected ArrayList<Astartes> members;
    Astartes leader = null;
    protected int restriction = -1;

    public AstartesSquad(String name) {
        squadName = name;
        members = new ArrayList<>();
    }

    public AstartesSquad(String name, AscensionPath asp) {
        this(name);
        setRestriction(asp);
    }

    public AstartesSquad() {
        this("Zatheas");
    }

    public void setRestriction(AscensionPath asp) {
        setRestriction(GameData.getAscensionPathIdx(asp));
    }

    public void setRestriction(int idx) {
        restriction = idx;
    }

    public boolean assignLeader(Astartes a) {
        if(!members.contains(a)) return false;
        leader = a;
        return true;
    }

    @Override
    public int getStrength() {
        int counter = 0;
        for(Astartes bth:members) if(bth.getHp()>0) counter++;
        return counter;
    }

    public boolean tryAddMember(Astartes newbie) {
        if(restriction == -1 || newbie.path == restriction) {
            members.add(newbie);
            return true;
        }
        return false;
    }

    @Override
    public int getMovement() {
        return 2;
    }

    @Override
    public List<AttackFormat> getAttack(int range) {
        List<AttackFormat> list = new ArrayList<>();
        for(Astartes a:members) list.addAll(a.getAttack(range));
        return list;
    }

    @Override
    public int getMaxRange() {
        int maxRange = 0;
        for(Astartes a:members) maxRange = Math.max(maxRange,a.getMaxRange());
        return maxRange;
    }

    @Override
    public int getType() {
        return Utility.speaker_friendly;
    }

    @Override
    public float getInitiative() {
        float sum = 0;
        for(Astartes bth:members) sum += bth.getInitiative();
        return sum / members.size();
    }

    @Override
    public String getDebugString() {
        String data = "Squad " + squadName + ":\n";
        for(Astartes bth:members) data += bth.toString() + ' ' + bth.statToString() + '\n';
        return data;
    }

    @Override
    public ImageView getUnitBadge(int badgeSize) {
        String badge = (restriction < 0) ? Utility.friendlyBadge :
                GameData.getAscensionPathById(restriction).unitBadge;
        return ImageHelper.getBadgeByName(badge, true, badgeSize);
//        return ImageHelper.getBadgeByName(Utility.friendlyBadge, true);
    }

    @Override
    public String toString() {
        return "AstartesSquad " + this.squadName;
    }

    public int getIconId() {
        return ImageHelper.squadIcon;
    }

    public int getEstimatedArmor() {
        int total = 0;
        for (Astartes bth:members) total+=bth.getArmourValue();
        return total/members.size();
    }

    public int getEstimatedHp() {
        int total = 0;
        for (Astartes bth:members) total+=bth.hp;
        return total/members.size();
    }

    @Override
    public List<Individual> getMembers() {
        return new ArrayList<>(members);
    }

    public List<Trait> getSquadOffensiveTraits() {
        if(leader == null) return new ArrayList<>();
        // AstartesSquad trait is only taken from the squad leader
        List<Trait> traitList = new ArrayList<>(leader.traits);
        traitList.removeIf(Trait::isNotOffensiveSquadTrait);
        return traitList;
    }

    public List<Trait> getSquadDefensiveTraits() {
        if(leader == null) return new ArrayList<>();
        // AstartesSquad trait is only taken from the squad leader
        List<Trait> traitList = new ArrayList<>(leader.traits);
        traitList.removeIf(Trait::isNotDefensiveSquadTrait);
        return traitList;
    }
}
