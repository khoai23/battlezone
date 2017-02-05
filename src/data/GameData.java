package data;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by Quan on 12/23/2016.
 */
public class GameData implements Serializable {
    static GameData currentData = null;
    public static GameData getCurrentData() {
        if(GameData.currentData == null) {
            GameData.currentData = new GameData();
        }
        return GameData.currentData;
    }

    public String chapterName;
    public String chapterMaster;
    public int colorScheme;
    ArrayList<Unit> roster;
    ArrayList<Weapon> weapons;
    ArrayList<Armour> armours;

    public GameData() {
        chapterName = "Death Bringer";
        chapterMaster = "Karkos";
        roster = new ArrayList<Unit>();
        weapons = new ArrayList<Weapon>();
        armours = new ArrayList<Armour>();
        colorScheme = scheme_center;
    }

    public static int scheme_monotone = 0;
    public static int scheme_center = 1;
    public static int scheme_half = 2;
    public static int scheme_quad = 3;
}
