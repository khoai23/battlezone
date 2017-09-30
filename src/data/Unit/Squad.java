package data.Unit;

import java.util.List;

/**
 * Created by Quan on 3/14/2017.
 *
 * A Squad is a number of Individuals codified into one single Unit
 */
public interface Squad extends Unit {
    List<Individual> getMembers();
    List<Trait> getSquadOffensiveTraits();
    List<Trait> getSquadDefensiveTraits();
}
