package data.Battle;

import data.Unit.Unit;
import data.Utility;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Quan on 3/22/2017.
 * A simple BattleAI that moves randomly toward all direction and
 * attack any enemy unit in sight after that
 */
public class BasicBattleAI implements BattleAI {
    @Override
    public boolean controlUnit(Battle battle, Deployment unit, List<Deployment> opposition) {
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
