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
    public boolean controlUnit(Battle battle, Deployment unit, List<Deployment> opposition, List<Deployment> friendly) {
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
        int largerX = unit.posX > closestEnemy.posX ? unit.posX : closestEnemy.posX;
        int smallerX = unit.posX < closestEnemy.posX ? unit.posX : closestEnemy.posX;
        if(largerX == smallerX) { largerX += 2; smallerX -= 2; }
        int largerY = unit.posY > closestEnemy.posY ? unit.posY : closestEnemy.posY;
        int smallerY = unit.posY < closestEnemy.posY ? unit.posY : closestEnemy.posY;
        if(largerY == smallerY) { largerY += 2; smallerY -= 2; }

        boolean finished = false;
        for(int i = largerX; i > smallerX && !finished; i--)
            for (int j = smallerY; j < largerY; j++) {
                if(Field.isMovableSquare(moveMap[i][j]))
                    if(battle.move(unit,i,j,true,unit.unit.getMovement())) {
                        battle.movedDuringMovement = true;
                        finished = true;
                        break;
                    }
            }

        for(Deployment d: opposition) {
            if(battle.attack(unit, d, true, unit.unit.getMaxRange(), battle.movedDuringMovement)) {
                battle.movedDuringMovement = false;
                return true;
            }
        }

        return true;
    }
}