package data.Unit;

import UI.ImageHelper;
import data.Battle.AttackFormat;
import data.GameData;
import data.Item.VehicleType;
import data.Item.VehicleWeapon;
import data.TreeViewable;
import data.Utility;
import javafx.scene.image.ImageView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Quan on 2/22/2017.
 */
public class Vehicle implements TreeViewable, Unit, Individual {
    final int type;
    int loadout;
    public int hp;
    public boolean pintle;
    public String status;
    List<Astartes> crew;

    public Vehicle(int type, int loadout, boolean pintle) {
        this.type = type;
        this.loadout = loadout;
        this.pintle = pintle;
        this.crew = new ArrayList<>();
        this.hp = GameData.getVehiclesVariant().get(type).getDefaultHp();
    }

    public Vehicle(int type, int loadout) {
        this(type,loadout,false);
    }

    public Vehicle(Vehicle target) {
        this(target.type,target.loadout, target.pintle);
        this.crew = target.crew;
        this.hp = target.hp;
    }

    @Override
    public int getStrength() {
        return hp;
    }

    @Override
    public int getMovement() {
        return GameData.getVehiclesVariant().get(type).getSpeed();
    }

    @Override
    public List<AttackFormat> getAttack(int range) {
        if(crew.isEmpty()) return new ArrayList<>();

        VehicleType currentType = vehicleType();
        int primary = currentType.getLoadoutPrimary(loadout);
        int secondary = currentType.getLoadoutSecondary(loadout);
        int pintle = !this.pintle ? -1 : currentType.getPintle();

        List<AttackFormat> attacks = new ArrayList<>();
        if(primary > -1) {
            attacks.add(AttackFormat.createAttack(GameData.getVehiclesWeaponById(primary), crew.get(crew.size() - 1).getRangeAccuracy(), "soft"));
        }
        if(secondary > -1) {
            attacks.add(AttackFormat.createAttack(GameData.getVehiclesWeaponById(secondary),crew.get(crew.size()-1).getRangeAccuracy(),"soft"));
        }
        if(pintle > -1) {
            attacks.add(AttackFormat.createAttack(GameData.getVehiclesWeaponById(pintle),crew.get(crew.size()-1).getRangeAccuracy(),"soft"));
        }
        return attacks;
    }

    @Override
    public int getMaxRange() {
        int maxRange;

        VehicleType currentType = vehicleType();
        int primary = currentType.getLoadoutPrimary(loadout);
        maxRange = GameData.getVehiclesWeaponById(primary).getRange();
        int secondary = currentType.getLoadoutSecondary(loadout);
        maxRange = Math.max(GameData.getVehiclesWeaponById(secondary).getRange(),maxRange);
        int pintle = !this.pintle ? -1 : currentType.getPintle();
        if(pintle > -1) {
            maxRange = Math.max(GameData.getVehiclesWeaponById(pintle).getRange(),maxRange);
        }

        return maxRange;
    }

    @Override
    public String toString() {
        return GameData.getVehiclesVariant().get(type).getName();
    }

    @Override
    public int getType() {
        return Utility.speaker_friendly;
    }

    public int getVehicleType() {
        return type;
    }
    
    public VehicleType vehicleType() { return GameData.getVehiclesVariantById(type); }

    @Override
    public float getInitiative() {
        if(crew.isEmpty()) return 0;
        return crew.get(crew.size() - 1).getInitiative();
    }

    @Override
    public String getDebugString() {
        return toString() + '(' + this.getHp() + '/' + GameData.getVehiclesVariant().get(type).getDefaultHp() + ")\n";
    }

    @Override
    public ImageView getUnitBadge(int badgeSize) {
        String badge = GameData.getVehiclesVariantById(type).unitBadge;
        return ImageHelper.getBadgeByName(badge, true, badgeSize);
    }

    public int getLoadout() { return loadout; }

    public void setLoadout(int loadout) { if(vehicleType().checkLoadoutViable(loadout)) this.loadout = loadout; }
    
    public String getLoadoutString() {
        VehicleType vehicleType = vehicleType();

        int vehId = vehicleType.getLoadoutPrimary(loadout);
        String data = "Primary: " + GameData.getVehiclesWeaponById(vehId).getName();
        vehId = vehicleType.getLoadoutSecondary(loadout);
        if(vehId >= 0) {
            data += ", Secondary: " + GameData.getVehiclesWeaponById(vehId).getName();
        }
        vehId = vehicleType.getPintle();
        if(vehId >= 0 && pintle) {
            data += ", Pintle: " + getPintle().getName();
        }

        return data;
    }

    public List<Astartes> getCrew() { return this.crew; }

    public String getCrewString() {
        final String[] crewString = {""};
        crew.forEach(cm -> crewString[0] += (crewString[0].equals("")) ? cm.toString() : ", " + cm.toString());
        return crewString[0];
    }

    public boolean addCrewMember(Astartes mbn, boolean check) {
        if(check && crew.size() >= vehicleType().getCrew()) return false;

        crew.add(mbn);
        return true;
    }

    public boolean addCrewMember(Astartes mbn) {
        return this.addCrewMember(mbn,true);
    }

    public List<Astartes> removeAllCrewMember() {
        ArrayList<Astartes> result = new ArrayList<>(crew);
        crew.clear();
        return result;
    }

    public int getArmourValue() {
        return vehicleType().getArmor();
    }

    public int getPintleId() {
        return vehicleType().getPintle();
    }
    
    public VehicleWeapon getPintle() { return GameData.getVehiclesWeaponById(getPintleId()); }

    @Override
    public int getIconId() {
        return 0;
    }

    @Override
    public int getHp() {
        return hp;
    }

    @Override
    public int getFullHp() {
        return GameData.getVehiclesVariant().get(type).getDefaultHp();
    }

    @Override
    public boolean setHp(int value) {
        hp = value;
        return hp < 0;
    }

    @Override
    public boolean isInfantry() {
        return false;
    }

    @Override
    public List<Trait> getIndividualOffensiveTrait() {
        List<Trait> listTraits = new ArrayList<>();
        if(crew.isEmpty()) return listTraits;
        listTraits.addAll(crew.get(crew.size()-1).traits);

        listTraits.removeIf(Trait::isNotOffensiveVehicleTrait);
        return listTraits;
    }

    @Override
    public List<Trait> getIndividualDefensiveTrait() {
        List<Trait> listTraits = new ArrayList<>();
        if(crew.isEmpty()) return listTraits;
        listTraits.addAll(crew.get(0).traits);

        listTraits.removeIf(Trait::isNotOffensiveVehicleTrait);
        return listTraits;
    }

    // Crew will list driver->gunner
}
