package data.Battle;

import java.util.List;

/**
 * Created by Quan on 3/22/2017.
 * Interface for a class responsible for moving an unit
 * this class should be able to be used to automate friendly movements as well
 */
public interface BattleAI {
    boolean controlUnit(Battle battle, Deployment unit, List<Deployment> opposition, List<Deployment> friendly);
}