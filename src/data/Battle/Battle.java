package data.Battle;

import UI.ImageHelper;
import UI.MainScene;
import data.GameData;
import data.Unit.Unit;
import data.Utility;
import javafx.application.Platform;
import javafx.scene.image.ImageView;

import java.io.Serializable;
import java.lang.System;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Quan on 2/25/2017.
 * Class controlling data for a generated battle
 */
public class Battle implements Serializable {
    public Field terrain;
    public ArrayList<Deployment> friendly;
    public ArrayList<Deployment> hostile;
    public ArrayList<Deployment> carousel;

    public BattleAI ai;
    int gameMode;
    Deployment target = null;

    // TerrainType decide randomization in first digit and terrainType in second
    public Battle(int terrainType, List<Unit> friendly, List<Unit> hostile) {
        if(terrainType < 0) {
            // negative input, use random map
            terrainType = Field.type_rectangular + 10 * Utility.rollBetween(Field.randomize_none, Field.randomize_high);
        }
        terrain = new Field(terrainType % 10, terrainType / 10);
        this.friendly = new ArrayList<>();
        this.friendly.addAll(friendly.stream().map(Deployment::new).collect(Collectors.toList()));
        this.hostile = new ArrayList<>();
        this.hostile.addAll(hostile.stream().map(Deployment::new).collect(Collectors.toList()));

        this.carousel = new ArrayList<>(this.friendly);
        this.carousel.addAll(this.hostile);
        this.currentDeployment = this.carousel.get(0);

        ai = new BasicBattleAI();
//        MainScene.runningScene.showField();
    }

    // Mode decide game type in its last digit, followed by 1/2 var
    public Battle(int terrainType, List<Unit> friendly, List<Unit> hostile, int mode) {
        this(terrainType, friendly, hostile);
        gameMode = mode % 10;
        deployByGameMode(mode % 10, (mode / 10) % 10, mode / 100);
    }

    public static final int mode_normalBattle = 0;
    public static final int mode_steelRain = 1;
    public static final int mode_encounterBattle = 2;
    public static final int mode_defendPosition = 3;
    public static final int mode_attackPosition = 4;
    public static final int mode_defendUnit = 5;
    public static final int mode_destroyUnit = 6;
    public static final int mode_captureUnit = 7;

    void deployByGameMode(int mode, int var1, int var2) {
        switch (mode) {
            case mode_normalBattle:
            case mode_steelRain:
                break;

            case mode_attackPosition:
            case mode_defendPosition:
                target = new Deployment(null);
                if(var1 <= 0 || var2 <= 0) {
                    // no value, randomize
                    var1 = Utility.rollBetween(0, terrain.fieldTerrain.length-1);
                }
                target.setPos(var1, var2);
                break;

            case mode_defendUnit:
                target = friendly.get(var1);
                break;
            case mode_destroyUnit:
            case mode_captureUnit:
                target = hostile.get(var1);
                break;

            case mode_encounterBattle:
                recalculateOrder();
                currentDeployment = carousel.get(0);
                break;
        }

        // Encounter Battle will deploy both armies randomly, while other phase will only deploy hostile
        if(gameMode == mode_encounterBattle) {
            friendly.forEach(u -> deploySingleUnitRandomly(u, 0));
        } else {
            deploymentPhaseOn = true;
        }

        if(gameMode <= mode_encounterBattle) {
            hostile.forEach(u -> deploySingleUnitRandomly(u, 0));
        } else {
            hostile.forEach(u -> deploySingleUnitRandomly(u, 2));
        }
    }

    void deploySingleUnitRandomly(Deployment deployment, int range) {
        if(deployment.posX != -1 && deployment.posY != -1) // deployed
            return;
        int posX, posY;
        do {
            if(range == 0) { // all
                posX = Utility.rollBetween(0, terrain.fieldTerrain.length - 1);
            } else if(range == 1) { // friendly territories
                posX = Utility.rollBetween(0, terrain.fieldTerrain.length / 2);
            } else { // hostile territories
                posX = Utility.rollBetween(terrain.fieldTerrain.length / 2, terrain.fieldTerrain.length - 1);
            }
            posY = Utility.rollBetween(0, terrain.fieldTerrain[posX].length - 1);
        } while (terrain.fieldTerrain[posX][posY] == Field.impassable);
        move(deployment, posX, posY, false, deployment.unit.getMovement());
    }

    // Units killed/added during the duration of the game require a reshuffle of orders
    void recalculateOrder() {
        // remove killed and calculate the ordering speed
        for(Iterator<Deployment> it = carousel.iterator(); it.hasNext(); ) {
            Deployment unit = it.next();
            if(unit.unit.getStrength() <= 0) {
                unit.unitBadge.setOpacity(0.0);
                it.remove();
            }
        }

        // carousel.removeIf(d -> d.unit.getStrength()<=0);
        carousel.sort(Deployment::compareInitiative);
    }

    public Deployment currentDeployment = null;
    boolean deploymentPhaseOn = false;
    public Deployment getNextDeployment(Deployment d) {
        Deployment res;
        int pos = carousel.indexOf(d);
        if(!deploymentPhaseOn || hostile.contains(d)) {
            recalculateOrder();
        }
        if(d.unit.getStrength() <= 0) {
            d.unitBadge.setOpacity(0.0);
            // Killed themselves, no modification to the ordering
            int counter = 0;
            do {
                counter++;
                if(pos == carousel.size()-1) pos = 0; else pos++;
            } while (carousel.get(pos).unit.getStrength()<=0 || counter < carousel.size());
            if(counter == carousel.size()) return null;
            res = carousel.get(pos);
        } else {
//            recalculateOrder();
            pos = carousel.indexOf(d);
            if(pos == carousel.size()-1) res = carousel.get(0);
            else res = carousel.get(pos+1);
        }
        System.out.printf("\nNextDeployment called, unit %s at %d %d", res.unit, res.posX, res.posY);
        this.currentDeployment = res;
        if(friendly.contains(res))
            ImageHelper.getWaitAnimation(ImageHelper.friendlyWait, res.posX, res.posY);
        else if(hostile.contains(res))
            ImageHelper.getWaitAnimation(ImageHelper.hostileWait, res.posX, res.posY);
        else
            ImageHelper.getWaitAnimation(ImageHelper.neutralWait, res.posX, res.posY);
        return res;
    }

    // Display the pure terrain
    public int[][] displayPureTerrain() {
        return terrain.fieldTerrain;
    }

    // Display the terrain with units on them
    public int[][] displayTerrain() {
        // clone the map
        int[][] data;
        data = new int[terrain.fieldTerrain.length][];
        for(int i=0;i<terrain.fieldTerrain.length;i++) {
            data[i] = Arrays.copyOf(terrain.fieldTerrain[i],terrain.fieldTerrain[i].length);
        }

        // put image size
        for(Deployment d:friendly) {
            if(d.posX>=0 && d.posY>=0 && d.posX < terrain.fieldTerrain.length && d.posY<terrain.fieldTerrain[d.posX].length && d.unit.getStrength()>0) {
                data[d.posX][d.posY] = Field.occupied_friendly;
            }
        }

        for(Deployment d:hostile) {
            if(d.posX>=0 && d.posY>=0 && d.posX < terrain.fieldTerrain.length && d.posY<terrain.fieldTerrain[d.posX].length && d.unit.getStrength()>0) {
                if(data[d.posX][d.posY] == Field.occupied_friendly) data[d.posX][d.posY] = Field.occupied_both;
                else data[d.posX][d.posY] = Field.occupied_hostile;
            }
        }

        if(target != null) {
            // Game Mode with an objective
            data[target.posX][target.posY] = Field.occupied_objective;
        }

        return data;
    }

    public int[][] displayTerrainWithPath(Deployment chosen) {
        final int spd = chosen.unit.getMovement();
        int[][] data = displayTerrain();
        // TODO make jumppack deployment as mini steelrain through this.
        int[][] moveMap = (chosen.isOnField() && chosen.unit.getStrength() > 0) ?
            terrain.getPossibleMovePosition(spd,chosen.posX,chosen.posY) :
            terrain.getPossibleDeployPosition(gameMode == mode_steelRain, spd);
        for(int i=0;i<data.length;i++)
            for(int j=0;j<data[i].length;j++) {
                if(moveMap[i][j] <= spd) {
                    data[i][j] = getMovableTile(data[i][j]);
                }
            }
        return data;
    }

    // Try to move unit into position (newX,newY)
    public boolean move(Unit unit, int newX, int newY, boolean recheck, int remainingSpeed) {
        // Search for unit
        Deployment deployment = searchForUnit(unit);
        return deployment != null && move(deployment, newX, newY, recheck, remainingSpeed);
    }

    public boolean move(Deployment dep, int newX, int newY, boolean recheck, int speed) {
        // recheck must be disabled for deploying on board first turn, and should be disabled for good.
        if(recheck && terrain.getPossibleMovePosition(speed,dep.posX,dep.posY)[newX][newY] > speed) {
            MainScene.addToVoxLog(Utility.debugMessage(String.format("%s at %d %d cannot move to %d %d",dep.unit,dep.posX,dep.posY,newX,newY)));
            return false;
        }
        MainScene.addToVoxLog(Utility.debugMessage(String.format("%s moved to %d %d",dep.unit,newX,newY)));
        System.out.printf("\nSetting unit %s at %d %d", dep.unit, newX, newY);
        dep.setPos(newX,newY);
        if(recheck)
            ImageHelper.getWaitAnimation(-1, newX, newY);
        return true;
    }

    public boolean move(Unit unit, int newX, int newY) {
        return this.move(unit,newX,newY,false,0);
    }

    public boolean attack(Unit attacker, Unit defender, boolean recheck, int range, boolean moved) {
        Deployment att = searchForUnit(attacker);
        Deployment def = searchForUnit(defender);
        return !(att == null || def == null) && attack(att, def, recheck, range, moved);
    }

    public boolean attack(Deployment att, Deployment def, boolean recheck, int range, boolean moved) {
        System.out.printf("\n%s try attack %s with maxRange %d, while range %d", att.unit, def.unit, range, Field.lengthToPos(att.posX,att.posY,def.posX,def.posY));
        if(recheck && Field.lengthToPos(att.posX,att.posY,def.posX,def.posY) > range) {
            MainScene.addToVoxLog(Utility.debugMessage(String.format("%s cannot attack %s with maxRange %d, actual distance %d", att.unit, def.unit, range, Field.lengthToPos(att.posX,att.posY,def.posX,def.posY))));
            return false;
        }
//        att.unit.handleAttack(def.unit.getAttack(range));
        Utility.handleUnitOnUnitCombat(att.unit,def.unit, Field.lengthToPos(att.posX,att.posY,def.posX,def.posY), moved);
        return true;
    }


    public boolean attack(Unit attacker, Unit defender, int range, boolean moved) {
        return attack(attacker,defender,false,range,moved);
    }

    // The handleClick method try to parse what happen when you click a square and
    // respond with a resulting map for that action (if valid)
    private boolean isSelected = false;
    private boolean isMovement = false;
    boolean movedDuringMovement = false;
    public int[][] handleClick(int itemX, int itemY) {
        // TODO disallow shooting on contested square
        if(!isSelected || currentDeployment == null) return null;
        Deployment clicked = clickOnHostile(itemX,itemY);
        boolean tryAction;
        if(clicked != null) {
            // Click on an enemy unit
            if(isMovement) {
                // Try assault: move & attack all at once
                if(currentDeployment.posX != itemX || currentDeployment.posY != itemY) {
                    tryAction = move(currentDeployment, itemX, itemY, true, currentDeployment.unit.getMovement());
                    movedDuringMovement = tryAction;
                } else {
                    tryAction = true;
                }

                if(tryAction) {
                    // if move succeeded, attack
                    System.err.printf("\nAssaulting enemy unit %s on square %d %d",clicked.unit, itemX, itemY);
                    MainScene.addToVoxLog(Utility.debugMessage(String.format("Assaulting enemy unit %s on square %d %d",clicked.unit, itemX, itemY)));
                    attack(currentDeployment, clicked, false, 0, movedDuringMovement);
                    getNextDeployment(currentDeployment);
                    Platform.runLater(this::runActionLoop);
                } else {
                    System.err.printf("\nCannot assault on square %d %d", itemX, itemY);
                }
                return tryAction ? displayTerrain() : null;
            } else {
                // Try attack that unit in attack phase
                tryAction = attack(currentDeployment,clicked,true,currentDeployment.unit.getMaxRange(),movedDuringMovement);
                if(tryAction) {
                    getNextDeployment(currentDeployment);
                    Platform.runLater(this::runActionLoop);
                } else {
                    System.err.printf("\nTarget %s out of range.", clicked.unit);
                }
                return tryAction ? displayTerrain() : null;
            }
        } else {
            // Click on empty square
            if(isMovement) {
                if(currentDeployment.posX != itemX || currentDeployment.posY != itemY) {
                    tryAction = move(currentDeployment,itemX,itemY,true,currentDeployment.unit.getMovement());
                    movedDuringMovement = true;
                } else {
                    MainScene.addToVoxLog(Utility.debugMessage("Unit "+ currentDeployment.unit.toString() +" abandoned movement phase."));
                    tryAction = true;
                    movedDuringMovement = false;
                }
                if(tryAction) {
                    // Move succeeded, switch to attack phase
                    isMovement = false;
                    return displayTerrain();
                } else {
                    System.err.printf("\nUnit %s cannot move to square %d %d", currentDeployment.unit,itemX,itemY);
                    return null;
                }
            } else {
                // If select self, abandon firing phase
                if(currentDeployment.posX == itemX && currentDeployment.posY == itemY) {
                    MainScene.addToVoxLog(Utility.debugMessage("Abandon firing phase."));
                    System.err.printf("\nUnit %s abandon firing phase.",currentDeployment.unit);
                    getNextDeployment(currentDeployment);
                    Platform.runLater(this::runActionLoop);
                    return displayTerrain();
                }
                MainScene.addToVoxLog(Utility.debugMessage("Clicked a not-enemy square during combat phase."));
                System.err.println("Clicked a not-enemy square during combat phase");
                return null;
            }
        }
    }

    // Tooltips will show data related to the square that is currently being hovered on
    public static void handleTooltipDisplay(int x, int y) {
        String result;
        Battle currentBattle = GameData.getCurrentData().getCurrentBattle();
        StringBuilder resultBuilder = new StringBuilder();
        for(Deployment d:currentBattle.carousel) {
            if(d.posX == x && d.posY == y) resultBuilder.append(GameData.getMiscSetting().tooltip_show_compo ? d.unit.getDebugString() : d.unit.toString());
        }
        result = resultBuilder.toString();
        if(result.equals("")) result = "Empty Square";
        if(currentBattle.currentDeployment != null && GameData.getMiscSetting().tooltip_distance) {
            result += "\nDistance: " + Field.lengthToPos(currentBattle.currentDeployment.posX,currentBattle.currentDeployment.posY,x,y);
        }
        MainScene.updateTooltip(result);
    }

    boolean actionBeingPerformed = false;
    public void runActionLoop() {
        if(actionBeingPerformed) return;
        isMovement = true;
        movedDuringMovement = false;
        if(checkBattleCondition() != 0) {
            MainScene.updateBattleResult(checkBattleCondition() == 1 ? "You won!" : "You lost.");
            return;
        }
        actionBeingPerformed = true;

        if(friendly.contains(currentDeployment)) {
            // If controllable, initialize movement and show path
            ImageHelper.updateFromIntMap(displayTerrainWithPath(currentDeployment));
            isSelected = true;
        } else {
            // IF not, let AI handle unit
            ImageHelper.updateFromIntMap(displayTerrain());
            isSelected = false;
            Utility.waitForEnemy(1.0, arg -> {
                runningEnemyAction(currentDeployment);
                getNextDeployment(currentDeployment);
                runActionLoop();
            });
        }
        actionBeingPerformed = false;
    }

    public int checkBattleCondition() {
        boolean cond = true;
        switch (gameMode) {
            // end when either side destroyed
            case mode_encounterBattle:
            case mode_normalBattle:
            case mode_steelRain:
                for(Deployment d:hostile) if(d.isOnField()) { cond = false; break; }
                if(cond) return 1;
                cond = true;
                for(Deployment d:friendly) if(d.isOnField()) { cond = false; break; }
                if(cond) return -1;
                break;
            // end when your unit land on target or side destroyed
            case mode_attackPosition:
                for(Deployment d:hostile) if(d.isOnField()) { cond = false; break; }
                for(Deployment d:friendly) if(d.posX == target.posX && d.posY == target.posY) { cond = true; break;}
                if(cond) return 1;
                cond = true;
                for(Deployment d:friendly) if(d.isOnField()) { cond = false; break; }
                if(cond) return -1;
                break;
            // end when enemy unit land on target or side destroyed
            case mode_defendPosition:
                for(Deployment d:friendly) if(d.isOnField()) { cond = false; break; }
                for(Deployment d:hostile) if(d.posX == target.posX && d.posY == target.posY) { cond = true; break;}
                if(cond) return -1;
                cond = true;
                for(Deployment d:hostile) if(d.isOnField()) { cond = false; break; }
                if(cond) return 1;
                break;
            // end when target enemy unit destroyed
            case mode_destroyUnit:
                if(target.unit.getStrength() < 0) return 1;
                for(Deployment d:friendly) if(d.isOnField()) { cond = false; break; }
                if(cond) return -1;
                break;
            // end when target friendly unit destroyed
            case mode_defendUnit:
                if(target.unit.getStrength() < 0) return -1;
                for(Deployment d:hostile) if(d.isOnField()) { cond = false; break; }
                if(cond) return 1;
                break;
            // end when target enemy unit *touched*
            case mode_captureUnit:
                for(Deployment d:friendly) if(d.posX == target.posX && d.posY == target.posY) return 1;

                for(Deployment d:hostile) if(d.isOnField()) { cond = false; break; }
                if(cond) return -1;
                cond = true;
                for(Deployment d:friendly) if(d.isOnField()) { cond = false; break; }
                if(cond) return -1;
                break;
        }
        return 0;
    }

    // Running action made by a decided AI
    public boolean runningEnemyAction(Deployment en) {
        return ai.controlUnit(this,en,friendly, hostile);
    }

    // Find an unit's deployment
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

    // Find if your click landed on a friendly unit
    public Deployment clickOnFriendly(int itemX, int itemY) {
        for(Deployment d:friendly) {
            if(d.posX == itemX && d.posY == itemY) return d;
        }
        return null;
    }

    // Find if your click landed on a hostile unit
    public Deployment clickOnHostile(int itemX, int itemY) {
        for(Deployment d:hostile) {
            if(d.posX == itemX && d.posY == itemY) return d;
        }
        return null;
    }

    static int getMovableTile(int tileValue) {
        switch (tileValue) {
            case Field.occupied_hostile:
                return Field.movable_hostile;
            case Field.occupied_both:
                return Field.movable_both;
            case Field.occupied_objective:
                return Field.movable_objective;
            case Field.occupied_friendly:
                return Field.occupied_friendly;
            default:
                return Field.movable_friendly;
        }
    }

    public List<ImageView> getDisplay(float hexSize) {
        // Get map display
        List<ImageView> displayElements = new ArrayList<>(ImageHelper.getMapFromIntMap(this.displayTerrain(), hexSize));
        // Get the wait anim
        displayElements.add(
                ImageHelper.getWaitAnimation(ImageHelper.hostileWait, this.currentDeployment.posX, this.currentDeployment.posY));

        // Get deployment display
        for(Deployment d: carousel)
            if(d.unitBadge != null) {
                ImageView badge = d.unitBadge;
                displayElements.add(d.unitBadge);
                ImageHelper.moveUnitToPosition(d.unitBadge, d.posX, d.posY);

                badge.setOnMouseClicked(event -> {
                    System.out.printf("\nBadge clicked for unit %s",d.unit.getDebugString());
                    int[][] newMap = GameData.getCurrentData().getCurrentBattle().handleClick(d.posX,d.posY);
                    if(newMap!=null) ImageHelper.updateFromIntMap(newMap);
                });
                badge.setOnMouseEntered(event -> {
                    Battle.handleTooltipDisplay(d.posX,d.posY);
                });
            }

        return displayElements;
    }


}

class Deployment implements Serializable {
    public int posX = -1;
    public int posY = -1;
    public Unit unit;
    public ImageView unitBadge = null;

    public Deployment(Unit unit) {
        this.unit = unit;
        if(unit != null)
            unitBadge = unit.getUnitBadge(GameData.getCurrentData().setting.badgeSize);
    }

    public void setPos(int x, int y) {
        ImageHelper.moveUnitToPosition(unitBadge, x, y);
        posX = x; posY = y;
    }

    public static int compareInitiative(Deployment d1, Deployment d2) {
        return Float.compare(d1.unit.getInitiative(), d2.unit.getInitiative());
    }

    public boolean isOnField() {
        return !(posX < 0 || posY < 0 || unit.getStrength() <= 0);
    }
}

class Coordinate {
    public int x;
    public int y;

    public Coordinate(int x, int y) {
        this.x = x;
        this.y = y;
    }
}