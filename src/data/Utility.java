package data;

import UI.Main;
import UI.MainScene;
import data.Battle.AttackFormat;
import data.Unit.*;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.paint.Paint;
import javafx.scene.text.Text;

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

    public static boolean rollForPercent(int percentage) {
        return Math.random() < (float)(percentage) / 100;
    }

    public static List<AttackFormat> handleLaunchingAttack(Astartes bth, int range, boolean moved) {
        List<AttackFormat> attacksMade = bth.getAttack(range);
        for(AttackFormat att:attacksMade) {
            if(att.traits.contains("hot") && rollForPercent(4)) {
                // Overload plasma
                bth.hp -= att.strength * rollBetween(50,75) / 100 - bth.getArmourValue();
            }
            if(moved) {
                if(att.traits.contains("assault")) att.accuracy -= 5;
                else if(att.traits.contains("heavy")) return new ArrayList<>();
                else att.accuracy -= 25;
            }
        }
        return (bth.hp < 0) ? new ArrayList<>() : attacksMade;
    }

    public static List<AttackFormat> handleLaunchingAttack(Vehicle veh, int range, boolean moved) {
        List<AttackFormat> attacksMade = veh.getAttack(range);
        boolean impededAccuracy = false;
        int fotmPenalty = moved ? 15 : 0;

        for (Astartes crw:veh.getCrew()) if(crw.personalTrait.contains("dri_stable")) fotmPenalty = (fotmPenalty==0) ? fotmPenalty : 5;

        for(AttackFormat att:attacksMade) {
            if(att.traits.contains("hot") && rollForPercent(4)) {
                // Overload plasma
                veh.hp -= att.strength * rollBetween(50,75) / 100 - veh.getArmourValue();
            }
            if(att.traits.contains("pintle")) impededAccuracy = true;
        }

        if(impededAccuracy || fotmPenalty>0)
            for(AttackFormat att:attacksMade) att.accuracy -= (impededAccuracy ? 10 : 0) + fotmPenalty;

        return (veh.hp < 0) ? new ArrayList<>() : attacksMade;
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
                        for(Astartes crw:target.getCrew()) crw.hp -= 10;
                        break;
                }
            }

            target.hp -= actualDamage;
        }

        for(String trait: laterUsedTrait) {
            // traits applied after all attacks are made
            switch (trait) {
                case "concussive":
                case "poison": target.status = trait; break;
            }
        }

        // Check vehicle destroyed
        return target.hp <= 0;
    }


    public static boolean handleAttackOnSquad(AttackFormat att, AstartesSquad sqd) {
        int[] data = {att.strength,att.accuracy,att.time};
        int estimatedArmor = sqd.getEstimatedArmor();
        int estimateHp = sqd.getEstimatedHp();
        String[] listTrait = att.traits.split(",");
        List<String> laterUsedTrait = new ArrayList<>();
        for(String trait:listTrait) {
            // before applying trait
            switch (trait) {
                // wpnTrait
                case "pintle":
                case "req_pack": case "no_acc": break;
                case "ordnance":
                case "aoe":     data[1] += 10; laterUsedTrait.add(trait); break;
                case "coaxial": data[1] += 5; break;
                case "automated": data[1] = 80; break;
                case "missile": data[0] /= 2; data[2] *= 2; laterUsedTrait.add(trait); break;
                case "rotary":
                case "spread":
                    if(data[0]-estimatedArmor > estimateHp / (data[2] * 3 / 5) ) {
                    data[0] += 10; data[2] /= 2; laterUsedTrait.add(trait); } break;
                default:        laterUsedTrait.add(trait);
            }
        }

        boolean twinLinkedBonus = false;
        int numOfTarget = 1;
        boolean isAOE = false;
        for (Iterator<String> trait = laterUsedTrait.iterator();trait.hasNext();) {
            switch (trait.next()) {
                case "rotary":
                case "spread": numOfTarget = 10; trait.remove(); break;
                case "sponsons": data[3] /=2; numOfTarget = 2; trait.remove(); break;
                case "splash": numOfTarget = rollBetween(1,3); trait.remove(); break;

                case "aoe": case "missile": trait.remove();
                case "ordnance": isAOE = true; break;
            }
        }
        int actualDamage;
        if(!isAOE) {
            numOfTarget = Math.min(Math.min(numOfTarget, sqd.members.size()), data[2]);
        } else {
            numOfTarget = rollBetween(sqd.members.size() * 4 / 5, sqd.members.size());
            data[2] = numOfTarget;
        }

        List<Astartes> targets = new ArrayList<>(sqd.members);
        targets.removeIf(p -> (p.hp<0));
        if(randomTargeting) {
            Collections.shuffle(targets);
        }
        targets = targets.subList(0,numOfTarget);
        if(targets.isEmpty()) {
            System.err.println("No target found. Most likely squad annihilated.");
            return true;
        }

        Astartes bth;
        for(;data[2]>0;data[2]--) {
            bth = targets.get(data[2] % numOfTarget);
            int accuracy = data[1];
            int damage = data[0];

            if(bth.getAccessoryTrait().equals("c_field")) {
                accuracy -= 30;
            }

            for(String defTrait:bth.personalTrait.split("\\|")) {
                switch (defTrait) {
                    case "com_fearless": damage += damage * 15 / 100; break;
                    case "spe_nimble": damage += damage * 15 / 100; break;
                }
            }

            // check for hit/miss and add bonus
            if(!rollForPercent(data[1] + (twinLinkedBonus ? 10 : 0))) {
                if(laterUsedTrait.contains("twin-linked")) twinLinkedBonus = true;
                continue;
            }

            actualDamage = data[0] + rollBetween(-data[0]/10,data[0]/10) - bth.getArmourValue();
            if(actualDamage < 0) actualDamage = 0;

            for (String trait : laterUsedTrait) {
                // traits applied after firing
                switch (trait) {
                    case "bolt":  if (actualDamage > 10) actualDamage *= 2; break;
                    case "flame": if (actualDamage > 0) actualDamage *= rollBetween(4, 5);
                    else actualDamage = data[0] / 10; break;
                    case "ordnance": actualDamage +=10;
                        break;
                    case "concussive":
                    case "poison": bth.status = trait; break;
                }
            }

            bth.hp -= actualDamage;
            if(bth.personalTrait.contains("spe_bloodthirsty") && rollForPercent(30)) bth.hp += 10;
            if(bth.personalTrait.contains("spe_stoic")) bth.status = "";
        }

        // Check squad destroyed
        for(Astartes b:sqd.members) {
            if(b.hp > 0) return false;
        }
        return true;
    }

    public static String filterPersonalTraitForAttack(String original) {
        List<String> traitList = Arrays.asList(original.split("\\|"));
        traitList.removeIf(p -> (!p.contains("spe_") && !p.contains("com_")));
        return String.join(",",traitList);
    }

    public static boolean handleSquadAttackSquad(AstartesSquad attacker, AstartesSquad defender, int range, boolean attackerMoved) {
        for(Astartes a:attacker.members) {
            List<AttackFormat> atkList = a.getAttack(range);
            tempData[atk_hp] = a.hp; tempData[atk_size] = attacker.members.size();
            for (AttackFormat atk : atkList) {
                attackToData(atk, tempData);
                moved = attackerMoved;
                // Apply for firing decision - targets of the attack
                List<Trait> applied = new ArrayList<>(atk.traitList);
                applied.removeIf(p -> !p.ofTargetDecisionPhase());
                for (Trait t : applied) t.handleChangeTargetNumber(tempData);
                printCurrentData();

                // Apply for firing data - stats of the attack
                applied = new ArrayList<>(atk.traitList);
                applied.removeIf(p -> !p.ofBeforeAllAttackPhase());
                for (Trait t : applied) t.handleChangeBeforeHit(tempData, attackerMoved, false, true);
                // firing data returned to atk
                dataToAttack(atk, tempData);
                atk.time = tempData[atk_spd];
                a.hp = tempData[atk_hp];
                if(a.hp < 0) { continue; }
                printCurrentData();

                // choose targets from list
                List<Astartes> targets = new ArrayList<>(defender.members);
                targets.removeIf(p -> (p.hp < 0));
                if (randomTargeting) {
                    Collections.shuffle(targets);
                }
                targets = targets.subList(0, Math.min(tempData[atk_tar], targets.size()));
                if (targets.isEmpty()) {
                    System.err.println("No target found. Most likely squad annihilated.");
                    return true;
                }

                // begin firing
                for (int i = 0; i < atk.time && !targets.isEmpty(); i++) {
                    Astartes target = targets.get(i % targets.size());
                    tempData[def_arm] = target.getArmourValue();
                    tempData[def_hp] = target.hp;
                    tempData[def_size] = defender.members.size();
                    // Apply for firing data - stats of the one attack
                    applied = new ArrayList<>(atk.traitList);
                    applied.removeIf(p -> !p.ofBeforeEachAttackPhase());
                    for (Trait t : applied) t.handleChangeBeforeHit(tempData, attackerMoved, i == 0, true);
                    printCurrentData();
                    // Doing the attack

                    applied = new ArrayList<>(atk.traitList);
                    applied.removeIf(p -> !p.ofAfterEachAttackPhase());
                    boolean hit = false;
                    if (rollForPercent(tempData[atk_acc])) {
                        tempData[atk_str] += rollBetween(-tempData[atk_str]/10,tempData[atk_str]/10);
                        tempData[atk_str] = tempData[atk_str] - tempData[def_arm];
                        for (Trait t : applied) t.handleChangeAfterHit(tempData, true, true);
                        tempData[def_hp] -= Math.max(tempData[atk_str],0);
                        hit = true;

                        System.out.print("\nAttack fired, setHp " + tempData[atk_str]);
                    } else {
                        for (Trait t : applied) t.handleChangeAfterHit(tempData, false, true);

                        System.out.print("\nAttack missed.");
                    }
                    printCurrentData();
                    target.hp = tempData[def_hp];
                    a.hp = tempData[atk_hp];
                    if (target.hp < 0) targets.remove(target);

                    // Reload the attackData from the original AttackFormat, and apply the next
                    attackToData(atk, tempData);
                    applied = new ArrayList<>(atk.traitList);
                    applied.removeIf(p -> !p.ofBeforeNextAttackPhase());
                    for (Trait t : applied) t.handleChangeAfterHit(tempData, hit, true);
                    printCurrentData();
                }
                if(a.hp < 0) {
                    System.out.println("\nAttacker " + a.toString() + " suffered fatal setHp during attacking phase.");
                    break;
                }
            }
        }

        for(Astartes b:defender.members) {
            if(b.hp > 0) return false;
        }
        return true;
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
                    System.err.println("No target found. Most likely squad annihilated.");
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

                        System.out.print("\nAttack fired, setHp " + tempData[atk_str]);
                        if(show_damage) {
                            MainScene.addToVoxLog(damageDealt(attacker.getType(), defender.getType(),
                                    a.toString(), target.toString(), tempData[atk_str]));
                        }
                            damageCounter += tempData[atk_str];
                    } else {
                        for (Trait t : applied) t.handleChangeAfterHit(tempData, false, target.isInfantry());

                        if(show_damage) {
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
        if(!show_message) return null;
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
        if(!show_debug) return null;
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

    public static boolean show_debug = false;
    public static boolean show_damage = false;
    public static boolean show_message = true;
    public static boolean tooltip_distance = false;
    public static boolean tooltip_show_compo = false;

    public static void waitForEnemy(double second) {
        // TODO implement
        try {
            Thread.sleep((long)(second * 1000));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
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
}