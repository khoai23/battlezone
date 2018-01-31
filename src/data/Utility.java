package data;

import UI.Main;
import UI.MainScene;
import data.Battle.AttackFormat;
import data.Item.VehicleType;
import data.Unit.*;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.paint.Paint;
import javafx.scene.text.Text;

import javax.json.JsonObject;
import java.util.*;
import java.util.List;
import java.util.concurrent.SynchronousQueue;

/**
 * Created by Quan on 3/6/2017.
 * Utility handle codes that are too problematic to be left in where they should be
 */
public class Utility {
    public static int rollBetween(int from, int to) {
        if(from == to) return from;
        return from + (int)Math.floor(Math.random() * (to-from+1));
    }

    public static <T> T getRandomItem(List<T> list) {
        if(list.size() == 0) return null;
        else return list.get(rollBetween(0,list.size()-1));
    }

    public static <T> T getRandomItem(T[] list) {
        if(list.length == 0) return null;
        else return list[rollBetween(0,list.length-1)];
    }

    public static boolean rollForPercent(int percentage) {
        return Math.random() < (float)(percentage) / 100;
    }

    public static List<AttackFormat> handleLaunchingAttack(Astartes bth, int range, boolean moved) {
        List<AttackFormat> attacksMade = bth.getAttack(range);
        for(AttackFormat att:attacksMade) {
            if(att.traits.contains("hot") && rollForPercent(4)) {
                // Overload plasma
               // bth.getHp() -= att.strength * rollBetween(50,75) / 100 - bth.getArmourValue();
            }
            if(moved) {
                if(att.traits.contains("assault")) att.accuracy -= 5;
                else if(att.traits.contains("heavy")) return new ArrayList<>();
                else att.accuracy -= 25;
            }
        }
        return (bth.getHp() < 0) ? new ArrayList<>() : attacksMade;
    }

    public static List<AttackFormat> handleLaunchingAttack(Vehicle veh, int range, boolean moved) {
        List<AttackFormat> attacksMade = veh.getAttack(range);
        boolean impededAccuracy = false;
        int fotmPenalty = moved ? 15 : 0;

        for (Astartes crw:veh.getCrew()) if(crw.personalTrait.contains("dri_stable")) fotmPenalty = (fotmPenalty==0) ? fotmPenalty : 5;

        for(AttackFormat att:attacksMade) {
            if(att.traits.contains("hot") && rollForPercent(4)) {
                // Overload plasma
                //veh.getHp() -= att.strength * rollBetween(50,75) / 100 - veh.getArmourValue();
            }
            if(att.traits.contains("pintle")) impededAccuracy = true;
        }

        if(impededAccuracy || fotmPenalty>0)
            for(AttackFormat att:attacksMade) att.accuracy -= (impededAccuracy ? 10 : 0) + fotmPenalty;

        return (veh.getHp() < 0) ? new ArrayList<>() : attacksMade;
    }

    public static boolean handleAttackOnVehicle(AttackFormat att, Vehicle target) {
        int[] data = {att.strength,att.accuracy,att.time};
        int vehicleArmor = target.getArmourValue();
        String[] listTrait = att.traits.split(",");
        List<String> laterUsedTrait = new ArrayList<>();
        for(String trait:listTrait) {
            // before applying trait
            switch (trait) {
                case "missile": case "splash": case "pintle":
                case "spread": case "sponson":
                case "req_pack": case "no_acc": break;
                case "melta":   if(vehicleArmor >= 35) data[0] += 10; break;
                case "ordnance": laterUsedTrait.add(trait);
                case "aoe":     data[1] += 10; break;
                case "coaxial": data[1] += 5; break;
                case "automated": data[1] = 80; break;
                case "rotary":  if(data[0]-vehicleArmor<10) data[0] += 10; data[2] /= 2; break;
                default:        laterUsedTrait.add(trait);
            }
        }

        boolean twinLinkedBonus = false;
        int actualDamage;

        for(;data[2]>0;data[2]--) {
            // check for hit/miss and add bonus
            if(!rollForPercent(data[1] + (twinLinkedBonus ? 10 : 0))) {
                if(laterUsedTrait.contains("twin-linked")) twinLinkedBonus = true;
                continue;
            }

            actualDamage = data[0] + rollBetween(-data[0]/10,data[0]/10) - vehicleArmor;

            for (String trait : laterUsedTrait) {
                // traits applied after firing
                switch (trait) {
                    case "bolt":  if (actualDamage > 10) actualDamage *= 2; break;
                    case "flame": if (actualDamage > 0) actualDamage *= rollBetween(4, 5);
                                  else actualDamage = data[0] / 10; break;
                    case "ordnance": actualDamage +=10;
                        //for(Astartes crw:target.getCrew()) crw.getHp() -= 10;
                        break;
                }
            }

            //target.getHp() -= actualDamage;
        }

        for(String trait: laterUsedTrait) {
            // traits applied after all attacks are made
            switch (trait) {
                case "concussive":
                case "poison": target.status = trait; break;
            }
        }

        // Check vehicle destroyed
        return target.getHp() <= 0;
    }

    public static boolean handleUnitOnUnitCombat(Unit attacker, Unit defender, int range, boolean attackerMoved) {
        List<Individual> attackers = new ArrayList<>();
        List<Individual> defenders = new ArrayList<>();
        List<Trait> squadTraits = new ArrayList<>();
        if(attacker instanceof Squad) {
            attackers.addAll(((Squad)attacker).getMembers());
            attackers.removeIf(individual -> individual.getHp()<0);
            squadTraits.addAll(((Squad)attacker).getSquadOffensiveTraits());
        } else if(attacker instanceof Individual) {
            attackers.add((Individual)attacker);
        } else {
            System.err.println("Error parsing attacker");
            return false;
        }

        if(defender instanceof Squad) {
            defenders.addAll(((Squad)defender).getMembers());
            squadTraits.addAll(((Squad)defender).getSquadDefensiveTraits());
        } else if(defender instanceof Individual) {
            defenders.add((Individual)defender);
        } else {
            System.err.println("Error parsing defender");
            return false;
        }
        int damageCounter = 0;

        for(Individual a:attackers) {
            List<AttackFormat> atkList = a.getAttack(range);
            tempData[atk_hp] = a.getHp(); tempData[atk_size] = attackers.size();
            for (AttackFormat atk : atkList) {
                attackToData(atk, tempData);
                tempData[atk_tar] = 1;
                moved = attackerMoved;
                // Apply for firing decision - targets of the attack
                List<Trait> applied = new ArrayList<>(atk.traitList);
//                applied.forEach(p -> System.out.print(p.getName()));
                applied.addAll(squadTraits);
                applied.removeIf(p -> !p.ofTargetDecisionPhase());
                for (Trait t : applied) t.handleChangeTargetNumber(tempData);
                printCurrentData();

                // Apply for firing data - stats of the attack
                applied = new ArrayList<>(atk.traitList);
                applied.removeIf(p -> !p.ofBeforeAllAttackPhase());
                for (Trait t : applied) t.handleChangeBeforeHit(tempData, attackerMoved, false, true);
                // firing data returned to atk
                atk.time = tempData[atk_spd];
                dataToAttack(atk, tempData);
                a.setHp(tempData[atk_hp]);
                if(a.getHp() < 0) { continue; }
                printCurrentData();

                // choose targets from list
                List<Individual> targets;
                defenders.removeIf(p -> (p.getHp() < 0));
                if (randomTargeting) {
                    Collections.shuffle(defenders);
                }
                targets = defenders.subList(0, Math.min(tempData[atk_tar], defenders.size()));
                if (targets.isEmpty()) {
                    System.err.print("\nNo target found. Most likely squad annihilated.");
                    return true;
                }

                // begin firing
                for (int i = 0; i < tempData[atk_spd] && !targets.isEmpty(); i++) {
                    Individual target = targets.get(i % targets.size());
                    tempData[def_arm] = target.getArmourValue();
                    tempData[def_hp] = target.getHp();
                    tempData[def_size] = defenders.size();
                    // accuracy penalty due to movement
                    if(attackerMoved) tempData[atk_acc] -= 25;
                    // Apply for firing data - stats of the one attack
                    applied = new ArrayList<>(atk.traitList);
                    applied.addAll(squadTraits);
                    applied.addAll(target.getIndividualDefensiveTrait());
                    applied.removeIf(p -> !p.ofBeforeEachAttackPhase());
                    for (Trait t : applied) t.handleChangeBeforeHit(tempData, attackerMoved, i == 0, target.isInfantry());
                    printCurrentData();
                    // Doing the attack

                    applied = new ArrayList<>(atk.traitList);
                    applied.addAll(squadTraits);
                    applied.addAll(target.getIndividualDefensiveTrait());
                    applied.removeIf(p -> !p.ofAfterEachAttackPhase());
                    boolean hit = false;
                    if (rollForPercent(tempData[atk_acc])) {
                        tempData[atk_str] += rollBetween(-tempData[atk_str]/10,tempData[atk_str]/10);
                        tempData[atk_str] = Math.max(tempData[atk_str] - tempData[def_arm],0);
                        for (Trait t : applied) t.handleChangeAfterHit(tempData, true, target.isInfantry());
                        tempData[def_hp] -= tempData[atk_str];
                        hit = true;

                        System.out.printf("\nAttack fired, setHp %d",tempData[atk_str]);
                        if(GameData.getMiscSetting().show_damage) {
                            MainScene.addToVoxLog(damageDealt(attacker.getType(), defender.getType(),
                                    a.toString(), target.toString(), tempData[atk_str]));
                        }
                            damageCounter += tempData[atk_str];
                    } else {
                        for (Trait t : applied) t.handleChangeAfterHit(tempData, false, target.isInfantry());

                        if(GameData.getMiscSetting().show_damage) {
                            MainScene.addToVoxLog(attackMissed(attacker.getType(), defender.getType(),
                                    a.toString(), target.toString()));
                        }
                        System.out.print("\nAttack missed.");
                    }
                    printCurrentData();
                    target.setHp(tempData[def_hp]);
                    a.setHp(tempData[atk_hp]);
                    if (target.getHp() < 0) targets.remove(target);

                    // Reload the attackData from the original AttackFormat, and apply the next
                    attackToData(atk, tempData);
                    applied = new ArrayList<>(atk.traitList);
                    applied.addAll(squadTraits);
                    applied.addAll(target.getIndividualDefensiveTrait());
                    applied.removeIf(p -> !p.ofBeforeNextAttackPhase());
                    for (Trait t : applied) t.handleChangeAfterHit(tempData, hit, target.isInfantry());
                    printCurrentData();
                }
                if(a.getHp() < 0) {
                    System.out.println("Attacker " + a.toString() + " suffered fatal damage during attacking phase.");
                    break;
                }
            }
        }

        if(defender instanceof Individual || defender instanceof Squad) {
            if(defender.getStrength() <= 0) {
                MainScene.addToVoxLog(unitDestroyedMessage(attacker.getType(),defender.getType(),attacker.toString(),defender.toString()));
                return false;
            } else {
                MainScene.addToVoxLog(unitDamagedMessage(attacker.getType(),defender.getType(),attacker.toString(),defender.toString(),damageCounter));
                return defender.getStrength() > 0;
            }
        } else {
            System.err.println("Error parsing defender.");
            return false;
        }
    }

    public static void attackToData(AttackFormat atk, int[] dataSet) {
        dataSet[atk_str] = atk.strength;
        dataSet[atk_acc] = atk.accuracy;
        dataSet[atk_spd] = atk.time;
//        dataSet[atk_tar] = 1;
    }

    public static void dataToAttack(AttackFormat atk, int[] dataSet) {
        atk.strength = dataSet[atk_str];
        atk.accuracy = dataSet[atk_acc];
//        atk.time = tempData[atk_spd];
    }

    public static void printCurrentData() {
        System.out.printf("\nSTR %d - ACC %d - SPD %d - TAR %d - HP %d - EARM %d - ESZ %d - EHP %d - " + moved,tempData[atk_str],tempData[atk_acc],
                tempData[atk_spd],tempData[atk_tar],tempData[atk_hp],tempData[def_arm],tempData[def_size],tempData[def_hp]);
    }

    public static FlowPane createLine(String speakerName, int speaker, String line) {
        if(!GameData.getMiscSetting().show_message) return null;
        FlowPane pane = new FlowPane();
        Text spk = new Text(speakerName + ": ");
        colorize(spk,speaker);
        pane.getChildren().add(spk);
        pane.getChildren().add(new Text(line));
        return pane;
    }

    public static FlowPane damageDealt(int atkType, int defType, String atkStr, String defStr, int damage) {
        FlowPane pane = new FlowPane();
        Text other = new Text("Attack: ");
        colorize(other,speaker_other);
        Text atk = new Text(atkStr);
        colorize(atk,atkType);
        Text def = new Text(defStr);
        colorize(def,defType);
        Text middle = new Text(" dealt " + damage + " damage to ");
        pane.getChildren().addAll(other,atk,middle,def);
        return pane;
    }

    public static FlowPane attackMissed(int atkType, int defType, String atkStr, String defStr) {
        FlowPane pane = new FlowPane();
        Text other = new Text("Attack: ");
        colorize(other,speaker_other);
        Text atk = new Text(atkStr);
        colorize(atk,atkType);
        Text def = new Text(defStr);
        colorize(def,defType);
        Text middle = new Text(" missed an attack on ");
        pane.getChildren().addAll(other,atk,middle,def);
        return pane;
    }

    public static FlowPane unitDestroyedMessage(int atkType, int defType, String atkStr, String defStr) {
        FlowPane pane = new FlowPane();
        Text other = new Text("Destroyed: ");
        colorize(other,speaker_other);
        Text atk = new Text(atkStr);
        colorize(atk,atkType);
        Text def = new Text(defStr);
        colorize(def,defType);
//        Text end = new Text(".");
        Text middle = new Text(" destroyed by ");
        pane.getChildren().addAll(other,def,middle,atk);
        return pane;
    }

    public static FlowPane unitDamagedMessage(int atkType, int defType, String atkStr, String defStr, int damage) {
        FlowPane pane = new FlowPane();
        Text other = new Text("Damaged: ");
        colorize(other,speaker_other);
        Text atk = new Text(atkStr);
        colorize(atk,atkType);
        Text def = new Text(defStr);
        colorize(def,defType);
//        Text end = new Text(".");
        Text middle = new Text(" dealt a total of " + damage + " damage to ");
        pane.getChildren().addAll(other,atk,middle,def);
        return pane;
    }

    public static FlowPane debugMessage(String string) {
        if(!GameData.getMiscSetting().show_debug) return null;
        FlowPane pane = new FlowPane();
        Text other = new Text("Debug:");
        colorize(other,speaker_other);
        Text middle = new Text(string);
        pane.getChildren().addAll(other,middle);
        return pane;
    }

    static Text colorize(Text text, int type) {
        switch (type) {
            case speaker_narrator: text.setFill(Paint.valueOf("black")); break;
            case speaker_friendly: text.setFill(Paint.valueOf("green")); break;
            case speaker_hostile: text.setFill(Paint.valueOf("red")); break;
            case speaker_neutral: text.setFill(Paint.valueOf("yellow")); break;
            default: text.setFill(Paint.valueOf("olive")); break;
        }
        return text;
    }

    public static Label[] dataToLabel(Individual unit, Label[] listLabel) {
        if(listLabel.length < 10) {
            System.err.println("Insufficient label input, exiting.");
            return listLabel;
        }

        if(unit instanceof Astartes) {
            Astartes bth = (Astartes) unit;

            listLabel[0].setText("Role: " + bth.getRole());
            listLabel[1].setText("Stat: " + bth.statToString());
            listLabel[2].setText("Wargears: " + bth.equipmentToString());
            listLabel[3].setText("");
            listLabel[4].setText("Level: " + bth.expToString());
            listLabel[6].setText("Chapter Traits: N/A");
            listLabel[7].setText("Personal Traits: " + convertTraitListToString(bth.traits));
        } else if(unit instanceof Vehicle) {
            Vehicle veh = (Vehicle) unit;

            listLabel[0].setText("Type: " + veh.getDebugString());
            listLabel[1].setText("Chassis: " + veh.vehicleType().chassis().getName());
            listLabel[2].setText("Loadout: " + veh.getLoadoutString());
            listLabel[3].setText("");
            listLabel[4].setText("Crew: " + veh.getCrewString());
            listLabel[6].setText("Chapter Traits: N/A");
            listLabel[7].setText("Unit Traits: ");
        } else {
            System.err.println("Individual type not implemented @dataToLabel.");
            return listLabel;
        }

        return listLabel;
    }

    static String convertTraitListToString(List<Trait> traitList) {
        if(traitList.size() == 0) return "None";
        else {
            final String[] all = {""};
            traitList.forEach(t -> all[0] += t.getName());
            return all[0];
        }
    }

    public static final int speaker_narrator = 0;
    public static final int speaker_friendly = 1;
    public static final int speaker_hostile = 2;
    public static final int speaker_neutral = 3;
    public static final int speaker_other = 4;



    public static void waitForEnemy(double second) {
        // TODO implement actual wait time and second thread
        try {
            Thread.sleep((long)(second * 1000));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void waitForEnemy(long milisecond, java.util.function.Consumer function) {
        try {
            new java.util.Timer().schedule(
                new java.util.TimerTask() {
                    @Override
                    public void run() {
                        function.accept(null);
                    }
                }, milisecond);
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }
    }

    public static void waitForEnemy(double second, java.util.function.Consumer function) {
        long milisecond = (long) second * 1000;
        waitForEnemy(milisecond, function);
    }

    public static List<Unit> createRandomCompany(JsonObject structure) {
        // TODO make object for this one
        List<Unit> rosterAddon = new ArrayList<>();

        rosterAddon.add(createAstartesSquad(structure));
        rosterAddon.add(createAstartesSquad(structure));
        rosterAddon.add(createAstartesSquad(structure));

        ((AstartesSquad)(rosterAddon.get(0))).setRestriction(GameData.getAscensionPathIdx("Tactical"));

        int aquila = GameData.getArmourIdByName("aquila");
        int[] driverLoadout = new int[] {aquila, 0, -1, -1};
        List<Astartes> rhinoDriver = new ArrayList<>();
                rhinoDriver.add(createAstartes(getRandomName(), driverLoadout, 10, "Devastator", false));

        rosterAddon.add(createVehicle("Rhino", rhinoDriver));

        rosterAddon.add(createVehicle("Land Speeder", null));

        rosterAddon.add(createVehicle("Vindicator", null));
        return rosterAddon;
    }

    public static Vehicle createVehicle(String name, List<Astartes> crew) {
        int vehicleId = GameData.getVehiclesIdByName(name);
        if(vehicleId < 0)
            return null;
        VehicleType choices = GameData.getVehiclesVariantById(vehicleId);
        int rollChoice = choices.getLoadOutData().size() > 0 ? rollBetween(0, choices.getLoadOutData().size()-1) : -1;
        Vehicle result = new Vehicle(vehicleId, rollChoice);
        if(crew != null)
            for(Astartes bth:crew) result.addCrewMember(bth, true);
        return result;
    }

    public static AstartesSquad createAstartesSquad(JsonObject structure) {
        // TODO make object for this one, too
        AstartesSquad newSquad = new AstartesSquad("");
        int aquila = GameData.getArmourIdByName("aquila");
        int bolter = GameData.getWeaponIdByName("bolter");
        int boltpistol = GameData.getWeaponIdByName("bpistol");
        int chainsword = GameData.getWeaponIdByName("chainsword");
        int jumppack = GameData.getAccessoryIdByName("jumppack");
        int heavyBolter = GameData.getWeaponIdByName("hbolt");
        int devpack = GameData.getAccessoryIdByName("devpack");
        int[][] loadout = new int[][] {
                {aquila,bolter,chainsword,-1},
                {aquila, chainsword, boltpistol, jumppack},
                {aquila, heavyBolter, -1, devpack}
        };
        for(int i=0;i<defaultSquadSize;i++) {
            newSquad.tryAddMember(createAstartes(getRandomName(), loadout[rollBetween(0,2)], 10, -1, false));
        }

        return newSquad;
    }

    public static Astartes createAstartes(String name, int[] equipment, int level, int path, boolean isLeader) {
        int[] stats = new int[8];
        // Generate random value to stats
        for (int i = 0; i < 4; i++) {
            stats[i] = rollBetween(-5, 5);
            if(i==3) // initiative roll
                stats[i] = rollBetween(-1, 1);
            if(equipment.length > i)
                stats[i+4] = equipment[i];
        }
        Astartes result = new Astartes(name, stats);
        result.level = -1;
        result.setPath(0);
        AscensionPath.ascend(result, true, level);
        if(path > -1) {
            result.setPath(path);
        }
        result.setHp(result.getFullHp());
        result.isPathLeader = isLeader;
        return result;
    }

    public static Astartes createAstartes(String name, int[] equipment, int level, String path, boolean isLeader) {
        int getPathIdx = GameData.getAscensionPathIdx(path);
        return createAstartes(name, equipment, level, getPathIdx, isLeader);
    }

    public static Astartes createAstartes(String name, int[] equipment, int level, String path) {
        return createAstartes(name, equipment, level, path, false);
    }

    public static int[] tempData = new int[10];
    public static final int atk_str = 0;
    public static final int atk_acc = 1;
    public static final int atk_spd = 2;
    public static final int atk_tar = 3;
    public static final int atk_hp = 4;
    public static final int atk_size = 5;
    public static final int def_arm = 6;
    public static final int def_size = 7;
    public static final int def_hp = 8;
    public static final int range = 9;
    public static boolean moved;

    public static boolean randomTargeting = true;
    public static final int defaultSquadSize = 10;

    public static String[] nameList = null;
    public static String getRandomName() {
        if(nameList == null) return "Zathreas";
        return nameList[rollBetween(0,nameList.length-1)];
    }

    public static final String friendlyBadge = "default_friendly";
    public static final String hostileBadge = "default_hostile";
}