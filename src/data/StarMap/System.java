package data.StarMap;

/**
 * Created by Quan on 2/20/2017.
 */
public class System {
    public String name;
    public float posX;
    public float poxY;
    public Planet[] listPlanet;

    public System(String name) {
        this.name = name;
        int planetNum = (int)Math.floor(Math.random() * 4) + 1;
        listPlanet = new Planet[planetNum];
        for(int i=0;i<planetNum;i++) {
            listPlanet[i] = new Planet(i+1);
        }
    }
}

class Planet {
    public final int designation;
    public int habitat;
    public long population;
    public String extra;

    public Planet(int number) {
        designation = number;
        // planet type forge and fortress are not randomized
        habitat = (int)Math.floor(Math.random() * 5);

    }

    // TODO a roman number generator
    public static String getName(String name, String designation) {
        return name + " " + designation;
    }

    public static final int habitat_type_dead=0;
    public static final int habitat_type_death=1;
    public static final int habitat_type_feral=2;
    public static final int habitat_type_agri=3;
    public static final int habitat_type_hive=4;
    public static final int habitat_type_forge=5;
    public static final int habitat_type_fortress=6;
}
