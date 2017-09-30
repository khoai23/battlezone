package data.Battle;

import java.util.List;

/**
 * Created by Quan on 3/22/2017.
 * Interface for a class responsible for moving an unit
 */
public interface BattleAI {
    boolean controlUnit(Battle battle, Deployment unit, List<Deployment> opposition);
}