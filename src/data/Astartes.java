package data;

import UI.ImageHelper;

import java.util.Arrays;

/**
 * Created by Quan on 12/28/2016.
 */
public class Astartes implements TreeViewable {
    private String name;
    private int[] equipment;
    // armour,hand1,hand2,accessory
    private int[] baseStat;
    // wound, bs, ws, i
    public int role=0;

    public Astartes(String name, int[] all) {
        this.name = name;
        if(all.length>=4)
            baseStat = Arrays.copyOfRange(all,0,4);
        if(all.length>=8)
            equipment = Arrays.copyOfRange(all,4,8);
        if(all.length>=9)
            role = all[8];
    }

    public String toString() {
        return "Squad " + this.name;
    }

    @Override
    public int getIconId() {
        switch (this.role) {
            case role_recruit:
            case role_neophyte:
                return ImageHelper.normalIcon;
            case role_devastator:
                return ImageHelper.devastatorIcon;
            case role_assault:
                return ImageHelper.assaultIcon;
            case role_tactical:
                return ImageHelper.tacticalIcon;
            case role_sternguard:
            case role_vanguard:
                return ImageHelper.eliteIcon;
            case role_honourguard:
                return ImageHelper.honourIcon;
            case role_captain:
                return ImageHelper.captainIcon;
            case role_chaptermaster:
                return ImageHelper.devastatorIcon;
            case role_apothecary:
                return ImageHelper.apothecaryIcon;
            case role_librarian:
                return ImageHelper.librarianIcon;
            case role_chaplain:
                return ImageHelper.chaplainIcon;
            case role_techmarine:
                return ImageHelper.cogIcon;
            default:
                return ImageHelper.normalIcon;
        }
    }

    // role are supposed to confer bonus.
    public static final int role_recruit=0;
    public static final int role_neophyte=1;
    public static final int role_devastator=2;
    public static final int role_assault=3;
    public static final int role_tactical=4;
    public static final int role_sternguard=5;
    public static final int role_vanguard=6;
    public static final int role_honourguard=7;
    public static final int role_captain=8;
    public static final int role_chaptermaster=9;
    public static final int role_apothecary=10;
    public static final int role_librarian=11;
    public static final int role_chaplain=12;
    public static final int role_techmarine=13;
}
