package data.Battle;

import data.Utility;

import java.io.Serializable;
import java.util.Arrays;

/**
 * Created by Quan on 2/28/2017.
 * A field is a randomly generated terrain for a specific battle
 */
public
class Field implements Serializable {
    // Map are top-down left-right
    public int[][] fieldTerrain;

    public Field(int type) {
        if(type == type_rectangular) {
            fieldTerrain = new int[default_width][];
            for(int i=0;i<fieldTerrain.length;i++) {
                if(i%2==0) {
                    fieldTerrain[i] = new int[default_height-1];
                } else {
                    fieldTerrain[i] = new int[default_height];
                }
            }
        } else {
            // TODO hexagonal map
            System.out.println("Method uninitialized.");
        }
    }

    public Field(int type, int randomize) {
        this(type);
        if(randomize != randomize_none) {
            int obs,imps,size = default_height * default_width, choice;
            if(randomize == randomize_little) {
//                obs = default_height + (int)(Math.random() * default_height - Math.random() * default_height);
                obs = Utility.rollBetween(0, default_height * 2);
//                imps = (int)(Math.random() * default_height / 2);
                imps = Utility.rollBetween(0, default_height / 2);
            } else if(randomize == randomize_medium) {
//                obs = default_height * 2 + (int)(Math.random() * default_height - Math.random() * default_height);
                obs = Utility.rollBetween(default_height, default_height * 3);
//                imps = (int)(Math.random() * default_height / 2) + default_height / 2;
                imps = Utility.rollBetween(default_height / 2, default_height);
            } else {
//                obs = default_height * 4 + (int)(Math.random() * default_height - Math.random() * default_height);
                obs = Utility.rollBetween(default_height * 3, default_height * 5);
//                imps = (int)(Math.random() * default_height) + default_height;
                imps = Utility.rollBetween(default_height, default_height * 2);
            }
            for(;obs>0;obs--) {
//                choice = (int)(Math.random() * size);
                choice = Utility.rollBetween(0,size-1);
                if((choice / default_height)%2 == 0 && choice%default_height==default_height-1)
                    choice -= 1;
                fieldTerrain[choice/default_height][choice%default_height] = obstacles;
            }
            for(;imps>0;imps--) {
                choice = Utility.rollBetween(0,size-1);
                if((choice / default_height)%2 == 0 && choice%default_height==default_height-1)
                    choice -= 1;
                fieldTerrain[choice/default_height][choice%default_height] = impassable;
            }
        }
    }

    public static int lengthToPos(int xFrom, int yFrom, int xTo, int yTo) {
        // length not counting obstacles/impassable, for shooting
        int mov = (Math.abs(xFrom - xTo)+1) / 2;

        if(xTo % 2 == 0) {
            yFrom -= mov;
            if(yFrom > yTo) return Math.abs(xFrom-xTo) + Math.abs(yFrom-yTo);
            else            return Math.max(Math.abs(xFrom-xTo), Math.abs(yFrom-yTo));
        } else {
            yFrom += mov;
            if(yFrom < yTo) return Math.abs(xFrom-xTo) + Math.abs(yFrom-yTo);
            else            return Math.max(Math.abs(xFrom-xTo), Math.abs(yFrom-yTo));
        }
    }

    public int[][] getPossibleMovePosition(int speed, int posX, int posY) {
        // length counting obstacles/impassable, for moving
        // TODO optimize
        int[][] mapPosition;
        int tempX,tempY;

        mapPosition = new int[fieldTerrain.length][];
        for(int i=0;i<fieldTerrain.length;i++) {
            mapPosition[i] = new int[fieldTerrain[i].length];
            Arrays.fill(mapPosition[i], 99);
        }

        mapPosition[posX][posY] = 0;
        for(int i=0;i<speed;i++) {
            for(int j=0;j<fieldTerrain.length;j++)
                for(int k=0;k<fieldTerrain[j].length;k++) {
                    if(j%2==0) {
                        for(int l=0;l<6;l++) {
                            tempX = j + border_even[l][0]; tempY = k + border_even[l][1];
                            if(tempX >= 0 && tempX < fieldTerrain.length && tempY >=0 && tempY < fieldTerrain[tempX].length)
                                if(fieldTerrain[tempX][tempY] != impassable) {
                                    mapPosition[tempX][tempY] = Math.min(mapPosition[tempX][tempY],mapPosition[j][k] + 1 + fieldTerrain[tempX][tempY]);
                                }
                        }
                    } else {
                        for(int l=0;l<6;l++) {
                            tempX = j + border_odd[l][0]; tempY = k + border_odd[l][1];
                            if(tempX >= 0 && tempX < fieldTerrain.length && tempY >=0 && tempY < fieldTerrain[tempX].length)
                                if(fieldTerrain[tempX][tempY] != impassable) {
                                    mapPosition[tempX][tempY] = Math.min(mapPosition[tempX][tempY],mapPosition[j][k] + 1 + fieldTerrain[tempX][tempY]);
                                }
                        }
                    }
                }
        }

        return  mapPosition;
    }

    public int[][] getPossibleDeployPosition(boolean steelRain, int speed) {
        // Deployment map for different mode
        int[][] mapPosition;

        mapPosition = new int[fieldTerrain.length][];
        for(int i=0;i<fieldTerrain.length;i++) {
            mapPosition[i] = new int[fieldTerrain[i].length];
//            Arrays.fill(mapPosition[i], i < speed || steelRain ? 0 : 99);
            for(int f=0;f < fieldTerrain[i].length; f++)
                if(fieldTerrain[i][f] == Field.impassable) mapPosition[i][f] = 99;
                else mapPosition[i][f] = i < speed/2 || steelRain ? 0 : 99;
        }
        return  mapPosition;
    }

    static final int[][] border_odd = {{-1,-1},{-1,0},{0,-1},{0,1},{1,-1},{1,0}};
    static final int[][] border_even = {{-1,0},{-1,1},{0,-1},{0,1},{1,0},{1,1}};

    public static final int type_hexagon = 0;
    public static final int type_rectangular = 1;

    public static final int normal = 0;
    public static final int obstacles = 1;
    public static final int impassable = 2;
    public static final int occupied_friendly = 3;
    public static final int occupied_hostile = 4;
    public static final int occupied_both = 5;
    public static final int occupied_objective = 6;

    public static boolean isMovableSquare(int val) { return val > 10; }

    public static final int movable_friendly = 13;
    public static final int movable_hostile = 14;
    public static final int movable_both = 15;
    public static final int movable_objective = 16;

    public static final int randomize_none = 0;
    public static final int randomize_little = 1;
    public static final int randomize_medium = 2;
    public static final int randomize_high=3;

    static final int default_width = 15;
    static final int default_height = 10;
}