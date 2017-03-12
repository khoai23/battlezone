package data.Unit;

import data.Battle.AttackFormat;
import data.GameData;
import data.Item.VehicleType;
import data.TreeViewable;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Quan on 2/22/2017.
 */
public class Vehicle implements TreeViewable,Unit {
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
    }

    public Vehicle(int type, int loadout) {
        this(type,loadout,false);
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

        VehicleType currentType = GameData.getVehiclesVariantById(type);
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
    public boolean handleAttack(List<AttackFormat> attacks) {
        return false;
    }

    @Override
    public String toString() {
        return GameData.getVehiclesVariant().get(type).getName();
    }

    @Override
    public int getType() {
        return type;
    }

    public int getLoadout() { return loadout; }

    public Astartes[] getCrew() { return this.crew.toArray(new Astartes[this.crew.size()]); }

    public boolean addCrewMember(Astartes mbn, boolean check) {
        if(check && crew.size() >= GameData.getVehiclesVariantById(type).getCrew()) return false;

        crew.add(mbn);
        return true;
    }

    public boolean addCrewMember(Astartes mbn) {
        return this.addCrewMember(mbn,true);
    }

    public Astartes[] removeAllCrewMember() {
        Astartes[] listDepositedCrew = crew.toArray(new Astartes[crew.size()]);
        crew.clear();
        return listDepositedCrew;
    }

    public int getArmourValue() {
        return GameData.getVehiclesVariantById(type).getArmor();
    }

    public int getPintleId () {
        return GameData.getVehiclesVariantById(type).getPintle();
    }

    @Override
    public int getIconId() {
        return 0;
    }
}
