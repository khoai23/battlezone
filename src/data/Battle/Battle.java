package data.Battle;

import data.StarMap.*;
import data.Unit.Unit;

import java.io.Serializable;
import java.lang.System;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * Created by Quan on 2/25/2017.
 */
public class Battle implements Serializable {
    public Field terrain;
    public ArrayList<Deployment> friendly;
    public ArrayList<Deployment> hostile;

    public Battle(int type, ArrayList<Unit> friendly, ArrayList<Unit> hostile) {
        terrain = new Field(Field.type_rectangular, Field.randomize_high);
        this.friendly = new ArrayList<>();
        this.friendly.addAll(friendly.stream().map(Deployment::new).collect(Collectors.toList()));
        this.hostile = new ArrayList<>();
        this.hostile.addAll(hostile.stream().map(Deployment::new).collect(Collectors.toList()));
    }

    public int[][] displayPureTerrain() {
        return terrain.fieldTerrain;
    }

    public int[][] displayTerrain() {
        // clone the map
        int[][] data;
        data = new int[terrain.fieldTerrain.length][];
        for(int i=0;i<terrain.fieldTerrain.length;i++) {
            data[i] = Arrays.copyOf(terrain.fieldTerrain[i],terrain.fieldTerrain[i].length);
        }

        // put image size
        for(Deployment d:friendly) {
            if(d.posX>=0 && d.posY>=0 && d.posX < terrain.fieldTerrain.length && d.posY<terrain.fieldTerrain[d.posX].length) {
                data[d.posX][d.posY] = Field.occupied_friendly;
            }
        }
        for(Deployment d:hostile) {
            if(d.posX>=0 && d.posY>=0 && d.posX < terrain.fieldTerrain.length && d.posY<terrain.fieldTerrain[d.posX].length) {
                if(data[d.posX][d.posY] == Field.occupied_friendly) data[d.posX][d.posY] = Field.occupied_both;
                else data[d.posX][d.posY] = Field.occupied_hostile;
            }
        }

        return data;
    }

    public int[][] displayTerrainWithPath(Deployment chosen) {
        final int spd = chosen.unit.getMovement();
        int[][] data = displayTerrain();
        int[][] moveMap = terrain.getPossibleMovePosition(spd,chosen.posX,chosen.posY);
        for(int i=0;i<data.length;i++)
            for(int j=0;j<data[i].length;j++) {
                if(moveMap[i][j] <= spd && data[i][j] != Field.occupied_hostile)
                    data[i][j] = Field.occupied_friendly;
            }
        return data;
    }

    public boolean move(Unit unit, int newX, int newY, boolean recheck, int remainingSpeed) {
        // Search for unit
        Deployment deployment = searchForUnit(unit);
        return deployment != null && move(deployment, newX, newY, recheck, remainingSpeed);
    }

    public boolean move(Deployment dep, int newX, int newY, boolean recheck, int speed) {
        // recheck must be disabled for deploying on board first turn, and should be disabled for good.
        if(recheck && terrain.getPossibleMovePosition(speed,dep.posX,dep.posY)[newX][newY] > speed)
            return false;
        dep.setPos(newX,newY);
        return true;
    }

    public boolean move(Unit unit, int newX, int newY) {
        return this.move(unit,newX,newY,false,0);
    }

    public boolean attack(Unit attacker, Unit defender, boolean recheck, int range) {
        Deployment att = searchForUnit(attacker);
        Deployment def = searchForUnit(defender);
        return !(att == null || def == null) && attack(att, def, recheck, range);
    }

    public boolean attack(Deployment att, Deployment def, boolean recheck, int range) {
        if(recheck && Field.lengthToPos(att.posX,att.posY,def.posX,def.posY) > range)
            return false;
        att.unit.handleAttack(def.unit.getAttack(range));
        return true;
    }

    public boolean attack(Unit attacker, Unit defender, int range) {
        return attack(attacker,defender,false,range);
    }

    Deployment searchForUnit(Unit unit) {
        for(Deployment d:friendly)
            if(d.unit == unit) {
                return d;
            }

        for(Deployment d:hostile)
            if(d.unit == unit) {
                return d;
            }

        return null;
    }

    boolean isSelected = false;
    Deployment currentSelected = null;
    public int[][] handleClick(int itemX, int itemY) {
        Deployment d = clickOnFriendly(itemX,itemY);
        if(!isSelected) {
            if(d==null) return null; // clicking unknown item
            // recolor map
            isSelected = true;
            currentSelected = d;
            return displayTerrainWithPath(currentSelected);
        } else {
            if(d==null) {
                // Move / Attack / Assault, for now exclude assault
                Deployment en = clickOnHostile(itemX,itemY);
                if(en == null) {
                    if(currentSelected == null) {
                        System.out.print("Nulled currentSelected, escaping.");
                        return null;
                    }
                    // try Move
                    move(currentSelected,itemX,itemY,true,currentSelected.unit.getMovement());
                } else {
                    // try attack
                    // TODO assault & check attack with max range
                    attack(currentSelected,en,false,0);
                }
            } else {
                if(d!=currentSelected) {
                    currentSelected = d;
                }
                // cancel, if click unit again
            }
            isSelected = false;
            currentSelected = null;
            System.err.println("Reverting to normal terrain.");
            return displayTerrain();
        }
    }

    public Deployment clickOnFriendly(int itemX, int itemY) {
        for(Deployment d:friendly) {
            if(d.posX == itemX && d.posY == itemY) return d;
        }
        return null;
    }

    public Deployment clickOnHostile(int itemX, int itemY) {
        for(Deployment d:hostile) {
            if(d.posX == itemX && d.posY == itemY) return d;
        }
        return null;
    }
}

class Deployment implements Serializable {
    public int posX = -1;
    public int posY = -1;
    public Unit unit;

    public Deployment(Unit unit) {
        this.unit = unit;
    }

    public void setPos(int x, int y) {
        posX = x; posY = y;
    }
}