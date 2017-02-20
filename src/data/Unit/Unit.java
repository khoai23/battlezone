package data.Unit;

/**
 * Created by Quan on 12/23/2016.
 */
public interface Unit {
    // An unit may be a squad or a vehicle, as they are the individual in each battle
    public int getStrength();
    public int getMovement();
    public int getAttack();
    public int getType();
}
