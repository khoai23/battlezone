package data.Battle;

import data.Utility;

import java.util.ArrayList;
import java.util.List;
/**
 * Created by Quan on 3/22/2017.
 * An Defensive BattleAI that maintain close contact with other friendly units
 * and attack only with numerical superiority
 */
public class DefensiveBattleAI implements BattleAI {
    public static int defaultContactRange = 4;

    @Override
    public boolean controlUnit(Battle battle, Deployment unit, List<Deployment> opposition, List<Deployment> friendly) {
        List<Deployment> trueFriendly = new ArrayList<>(friendly);
        trueFriendly.remove(unit);

        int check = 3;
        for(Deployment enemy: opposition) {
            if(Field.lengthToPos(unit.posX, unit.posY, enemy.posX, enemy.posY) < defaultContactRange) {
                check = 0;
                break;
            } else if(Field.lengthToPos(unit.posX, unit.posY, enemy.posX, enemy.posY) < 3 * defaultContactRange) {
                check = 2;
                break;
            }
        }

        int[][] moveMap = battle.displayTerrainWithPath(unit);
        Coordinate current = new Coordinate(unit.posX, unit.posY);
        if(check == 3) {
            // If every enemy is too far away, choose the general direction by a random enemy
            Deployment target = opposition.get(Utility.rollBetween(0, opposition.size() - 1));
            int dirX = target.posX < unit.posX ? -1 : 1, dirY = target.posY < unit.posY ? -1 : 1;
            // check the map for the furthest the unit can go toward such direction
            for (int i = (dirX == 1) ? 0 : moveMap.length - 1; i != unit.posX && check == 3; i -= dirX)
                for (int j = (dirY == 1) ? 0 : moveMap[i].length - 1; j != unit.posY && check == 3; j -= dirY) {
                    if (Field.isMovableSquare(moveMap[i][j])) {
                        if (battle.move(unit, i, j, true, unit.unit.getMovement())) {
                            battle.movedDuringMovement = !(i == current.x && j == current.y);
                            check = -1;
                        }
                    }
                }
        } else if(check == 2) {
            // If there are enemy close by, go to ground
            int currentCounter = 0;
            int[][] pureTerrain = battle.displayPureTerrain();
            for(int i=0;i<moveMap.length;i++)
                for (int j=0;j<moveMap[i].length;j++) {
                    if(Field.isMovableSquare(moveMap[i][j]) && (pureTerrain[i][j] == Field.obstacles)) {
                        // go to ground with the largest friendly nearby
                        if(getNearbyFriendly(new Coordinate(i,j), trueFriendly) > currentCounter) {
                            current = new Coordinate(i,j);
                            currentCounter = getNearbyFriendly(new Coordinate(i,j), trueFriendly);
                        }
                    }
                }
            battle.movedDuringMovement = !(unit.posX == current.x && unit.posY == current.y);
            battle.move(unit, current.x, current.y, true, unit.unit.getMovement());
        } else {
            // If enemy within shooting range, stand ground or run
            int nof = getNearbyFriendly(new Coordinate(unit.posX, unit.posY), trueFriendly);
            int noe = 0;
            Deployment target = opposition.get(0);
            for(Deployment enemy: opposition) {
                if(Field.lengthToPos(unit.posX, unit.posY, enemy.posX, enemy.posY) < defaultContactRange) {
                    noe++;
                }
            }
            // If too many enemy, execute tactical advance in opposite direction
            if(noe > nof) {
                int dirX = target.posX < unit.posX ? -1 : 1, dirY = target.posY < unit.posY ? -1 : 1;
                for (int i = (dirX == -1) ? 0 : moveMap.length - 1; i != unit.posX && check != -1; i -= dirX)
                    for (int j = (dirY == -1) ? 0 : moveMap[i].length - 1; j != unit.posY && check != -1; j -= dirY) {
                        if (Field.isMovableSquare(moveMap[i][j])) {
                            if (battle.move(unit, i, j, true, unit.unit.getMovement())) {
                                battle.movedDuringMovement = !(i == current.x && j == current.y);
                                check = -1;
                            }
                        }
                    }
            }
        }

        // whatever happened, try shooting anything in range
        for(Deployment d: opposition) {
            if(battle.attack(unit, d, true, unit.unit.getMaxRange(), battle.movedDuringMovement)) {
                battle.movedDuringMovement = false;
                return true;
            }
        }

        return true;
    }

    int getNearbyFriendly(Coordinate self, List<Deployment> friendly, int searchRange) {
        int count = 0;
        for(Deployment fr: friendly)
            if(Field.lengthToPos(self.x, self.y, fr.posX, fr.posY) < searchRange)
                count++;
        return count;
    }

    int getNearbyFriendly(Coordinate self, List<Deployment> friendly) {
        return getNearbyFriendly(self, friendly, defaultContactRange);
    }
}