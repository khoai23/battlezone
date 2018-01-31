package data;

import java.io.Serializable;

public class Setting implements Serializable {
    // Randomize the warpath for individual Astartes instead of allowing the player to select
    // Affect AscensionPath
    public boolean fullyRandomizedAstartesStat = true;

    // Do not allow you to redistribute wargears, appoint squad leaders and specialist
    public boolean autonomicWarriors = false;

    // Squad denoted by leader's name instead of numbering
    public boolean personalSquad = false;

    // Normal hex icon size, default 64
    public int battleHexSize = 64;
    // Normal badge icon size, default 48
    public int badgeSize = 40;

    // Display setting of the combat tab
    // Affect Utility, BattleTab and VoxPane
    public boolean show_debug = false;
    public boolean show_damage = false;
    public boolean show_message = true;
    public boolean tooltip_distance = false;
    public boolean tooltip_show_compo = false;
}
