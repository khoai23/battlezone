package data;

import java.io.Serializable;

public class Setting implements Serializable {
    // Randomize the warpath for individual Astartes instead of allowing the player to select
    // Affect AscensionPath
    public boolean fullyRandomizedAstartesStat = true;

    // Display setting of the combat tab
    // Affect Utility, BattleTab and VoxPane
    public boolean show_debug = false;
    public boolean show_damage = false;
    public boolean show_message = true;
    public boolean tooltip_distance = false;
    public boolean tooltip_show_compo = false;
}
