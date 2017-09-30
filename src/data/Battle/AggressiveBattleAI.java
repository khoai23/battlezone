package data.Battle;

import data.Utility;

import java.util.ArrayList;
import java.util.List;
/**
 * Created by Quan on 3/22/2017.
 * An Aggressive BattleAI that moves toward nearest opposing enemy and attack
 * as much as they can
 */
public class AggressiveBattleAI implements BattleAI {
    @Override
    public boolean controlUnit(Battle battle, Deployment unit, List<Deployment> opposition) {
        // If can assault, do assault
        Deployment closestEnemy = opposition.get(0);
        for(Deployment enemy: opposition) {
            if(!battle.move(unit,enemy.posX,enemy.posY,true,unit.unit.getMovement())) {
                battle.movedDuringMovement = true;
                battle.attack(unit, enemy, false, unit.unit.getMaxRange(), battle.movedDuringMovement);
            }

            if(Field.lengthToPos(unit.posX, unit.posY, closestEnemy.posX, closestEnemy.posY) >
                    Field.lengthToPos(unit.posX, unit.posY, enemy.posX, enemy.posY))
                closestEnemy = enemy;
        }

        // If cannot assault, move as close as possible to the nearest enemy and try shooting


        int[][] moveMap = battle.displayTerrainWithPath(unit);
        List<Coordinate> listPlace = new ArrayList<>();
        for(int i=0;i<moveMap.length;i++)
            for (int j=0;j<moveMap[i].length;j++) {
                if(moveMap[i][j] == Field.occupied_friendly || moveMap[i][j] == Field.occupied_hostile) {
                    listPlace.add(new Coordinate(i, j));
                }
            }

        Coordinate cord;
        battle.movedDuringMovement = true;
        do {
            // Random move to a square
            cord = listPlace.get(Utility.rollBetween(0, listPlace.size()-1));
            if(unit.posX == cord.x && unit.posY == cord.y) {
                battle.movedDuringMovement = false;
                break;
            }
        } while (!battle.move(unit,cord.x,cord.y,true,unit.unit.getMovement()));

        for(Deployment d: opposition) {
            if(battle.attack(unit, d, true, unit.unit.getMaxRange(), battle.movedDuringMovement)) {
                battle.movedDuringMovement = false;
                return true;
            }
        }

        return true;
    }
}

