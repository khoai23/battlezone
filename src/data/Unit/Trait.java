package data.Unit;

import data.GameData;
import data.Utility;

import javax.json.JsonArray;
import javax.json.JsonObject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Quan on 3/8/2017.
 *
 * Clarifications:
 * + All phase vs before phase is difference between modifying numbers of attacks and not.
 * Before phase should not be allowed to change numbers of attacks unless you are an ass
 * and want your game to fail
 * + Normal trait will activate at correct phase(0), correct target(1), check for a
 * set up predicate (2+3), modify the data if the predicate is satisfied (4+5), then can
 * link to another AUX trait (6) that only contain data modification if target modifiers
 * or to another normal trait if in any other phase
 * + Noncom trait will be hardcoded into the file using correct phase (0), ignore target(1)
 * and with type(2) plus variable (3-4). Variable(3) can be strings or number in some case.
 */
public class Trait {
    public int id;
    int[] data;
    String name = "trait?";
    String description;

    public Trait(int[] data, int id) {
        this.data = data;
        this.id = id;
    }

    public Trait(JsonObject obj, int id) {
        this.id = id;
        data = new int[7];
        name = obj.getString("name");
        JsonArray data = obj.getJsonArray("numberData");
        if(data != null)
            for(int i=0;i<7;i++) {
                this.data[i] = data.getInt(i, -1);
            }
        else {
            convertStringToTraitList(obj.getString("stringData"));
        }
        description = obj.getString("desc");
    }

    void convertStringToTraitList(String data) {
        String[] traitData = data.split("\\|");
        if(traitData.length<7) {
            System.err.println("Wrong data (notEnoughEntry) " + data + " id = " + this.id);
            return;
        }

        this.data[traitPhase] = getTraitPhaseFromString(traitData[0]);

        this.data[traitType] = getTraitTypeFromString(traitData[1]);

        if(this.data[traitPhase] == phase_targetDecision) {
            // targetDecision are organized differently: phase|type|offsetType|range1|range2|(ifUseThingOtherThanDefTar)|aux
            this.data[traitData1] = getTargetModifierFromString(traitData[2]);
            this.data[traitData2] = getNumberFromString(traitData[3]);
            this.data[traitData3] = getNumberFromString(traitData[4]);
            this.data[bindToOther] = getNumberFromString(traitData[6]);
            return;
        } else if(this.data[traitPhase] == phase_nonCombat) {
            // trait do not affect into combat phase, instead influence other aspect of unit
            // phase|type|value1|value2 only
            this.data[traitData1] = getNoncomTraitFromString(traitData[2]);
            this.data[traitData2] = getNumberFromString(traitData[3]);
            this.data[traitData3] = getNumberFromString(traitData[4]);
            return;
        }
        if(this.data[traitType] == traitType_extra) {
            // Only extra can have two modifier and no predicate
            // They are used to supplement firemode and targeting ONLY
            this.data[traitData1] = getModifierFromString(traitData[2]);
        } else {
            this.data[traitData1] = getPredicateFromString(traitData[2]);
        }

        this.data[traitData2] = getNumberFromString(traitData[3]);
        this.data[traitData3] = getModifierFromString(traitData[4]);
        this.data[traitData4] = getNumberFromString(traitData[5]);
        this.data[bindToOther] = getTraitNumberFromString(traitData[6]);
    }

    int getTraitPhaseFromString(String input) {
        switch (input) {
            case "target": return phase_targetDecision;
            case "all": return phase_allAttacks;
            case "before": return phase_beforeAttack;
            case "after": return phase_afterAttack;
            case "next": return phase_nextAttack;
            case "noncom": return phase_nonCombat;

            default: return -1;
        }
    }

    int getTraitTypeFromString(String input) {
        switch (input) {
            case "single": return traitType_single_enemy;
            case "self": return traitType_single_self;
            case "squad_off": return traitType_squad_self;
            case "squad_def": return traitType_squad_enemy;
            case "vehicle_off": return traitType_vehicle_self;
            case "vehicle_def": return traitType_vehicle_enemy;
            case "friendly": return traitType_all_friendly;
            case "hostile": return traitType_all_hostile;
            case "all": return traitType_everyone;
            case "extra": return traitType_extra;

            default: return -1;
        }
    }

    int getPredicateFromString(String input) {
        switch (input) {
            case "every": return predicate_every;
            case "random": return predicate_random_chance;
            case "aad_more": return predicate_damage_inflicted_more;
            case "aad_less": return predicate_damage_inflicted_less;
            case "hit": return predicate_attack_hit;
            case "miss": return predicate_attack_miss;
            case "switch": return predicate_isInfantry;
            case "first": return predicate_firstAttack;
            case "switch_auto": return predicate_chooseBetterDamage;
            case "armor_less": return predicate_enemy_armor_less;
            case "armor_more": return predicate_enemy_armor_more;
            case "moved": return predicate_onMoving;
            case "notMoved": return predicate_hadnotMoved;
            case "range_more": return predicate_range_more;
            case "range_less": return predicate_range_less;

            case "sqdSize_more": return predicate_squad_alive_more;
            case "sqdSize_less": return predicate_squad_alive_less;
            case "enSqdSize_more": return predicate_enemysquad_alive_more;
            case "enSqdSize_less": return predicate_enemysquad_alive_less;

            default: return throwaway;
        }
    }

    int getModifierFromString(String input) {
        switch (input) {
            case "spd_mult": return attack_multiplier;
            case "spd_off": return attack_offset;
            case "acc_mult": return accuracy_multiplier;
            case "acc_off": return accuracy_offset;
            case "str_mult": return damage_multiplier;
            case "str_off": return damage_offset;
            case "armor_mult": return armor_multiplier;
            case "armor_off": return armor_offset;
            case "dmg_mult": return aadamage_multiplier;
            case "dmg_off": return aadamage_offset;
            case "enemy_dmg": return hp_enemy_offset;
            case "self_dmg": return hp_self_offset;

            default: return throwaway;
        }
    }

    int getTargetModifierFromString(String input) {
        switch (input) {
            case "inc_target_mult": return trait_targetChange_attack_mult;
            case "inc_target_offset": return trait_targetChange_attack_offset;
            case "dec_target_mult": return trait_targetChange_defend_mult;
            case "dec_target_offset": return trait_targetChange_defend_offset;
            default: return -1;
        }
    }

    int getNoncomTraitFromString(String input) {
        switch (input) {
            case "speed": return noncom_speed;
            case "ignoreObstacle": return noncom_igObs;
            case "required": return noncom_required;
            case "emptySlot": return noncom_emptySlot;
            default: return -1;
        }
    }

    int getNumberFromString(String input) {
        try {
            return Integer.parseInt(input);
        } catch (NumberFormatException e) {
            System.err.println("Error parsing number @ traitId = " + this.id);
            return -1;
        }
    }

    int getTraitNumberFromString(String input) {
        try {
            return Integer.parseInt(input);
        } catch (NumberFormatException e) {
            System.out.printf("\nFinding trait by name %s",input);
            // Assume None is found, it is never taken in the list and thus is safe
            return initialization.indexOf(getReadTraitWithName(input));
        }
    }

    public static final int traitPhase = 0;
    public static final int traitType =  1;
    public static final int traitData1 = 2;
    public static final int traitData2 = 3;
    public static final int traitData3 = 4;
    public static final int traitData4 = 5;
    public static final int bindToOther =6;

    /**
     * Change attack data on launching attack
     * example: bolt have x2 setHp if setHp inflicted >10 per shot
     * -> phase afterAttack, single, pred setHp inflicted more, 10, aadamage multiplier, 100, -1
     * rotary automatically switch between normal and -50% shot& +10 setHp, use an aux rotaryAlt trait
     * -> phase beforeAttack, single, pred chooseBetterDamage, rotaryAlt id
     * -> rotaryAtt ?
     * */

    public static final int phase_targetDecision = 0;
    public static final int phase_allAttacks = 1;
    public static final int phase_beforeAttack = 2;
    public static final int phase_afterAttack = 3;
    public static final int phase_nextAttack = 4;
    public static final int phase_nonCombat = 5;

    public static final int traitType_ = 0;
    public static final int traitType_targetChange = 0;
    public static final int traitType_single_self = 1;
    public static final int traitType_single_enemy = 2;
    public static final int traitType_squad_self = 3;
    public static final int traitType_squad_enemy = 4;
    public static final int traitType_vehicle_self = 5;
    public static final int traitType_vehicle_enemy = 6;
    public static final int traitType_all_friendly = 7;
    public static final int traitType_all_hostile = 8;
    public static final int traitType_everyone = 9;
    public static final int traitType_extra = 10;

    public static final int trait_targetChange_attack_offset = 0;
    public static final int trait_targetChange_defend_offset = 1;
    public static final int trait_targetChange_attack_mult = 2;
    public static final int trait_targetChange_defend_mult = 3;

    public static final int predicate_enemy_armor_more = 0; // predicate that return yes/no
    public static final int predicate_enemy_armor_less = 1;
    public static final int predicate_damage_inflicted_more = 2;
    public static final int predicate_damage_inflicted_less = 3;
    public static final int predicate_attack_hit = 4;
    public static final int predicate_attack_miss = 5;
    public static final int predicate_onMoving = 6;
    public static final int predicate_hadnotMoved = 7;
    public static final int predicate_firstAttack = 8;
    public static final int predicate_every = 9;
    public static final int predicate_random_chance = 10;
    public static final int predicate_isInfantry = 11;
    public static final int predicate_chooseBetterDamage = 12;
    public static final int predicate_range_more = 13;
    public static final int predicate_range_less = 14;

    public static final int predicate_underIsBoolean = 30;

    public static final int predicate_squad_alive_more  = 30; // predicate that generate value
    public static final int predicate_squad_alive_less  = 31;
    public static final int predicate_enemysquad_alive_more  = 32;
    public static final int predicate_enemysquad_alive_less  = 33;

    public static final int damage_multiplier   = 0; // multiplier are made on base 100
    public static final int damage_offset       = 1;
    public static final int accuracy_multiplier = 2;
    public static final int accuracy_offset     = 3;
    public static final int attack_multiplier   = 4;
    public static final int attack_offset       = 5;
    public static final int aadamage_multiplier = 6; // afterArmorDamage
    public static final int aadamage_offset     = 7;
    public static final int armor_multiplier    = 8;
    public static final int armor_offset        = 9;
    public static final int hp_self_offset      = 10;
    public static final int hp_enemy_offset     = 11;

    public static final int noncom_speed  = 0;
    public static final int noncom_igObs  = 1;
    public static final int noncom_required = 2;
    public static final int noncom_emptySlot = 3;
    public static final int noncom_paired = 4;
    public static final int noncom_split = 4;
    public static final int noncom_ = 1;

    public static final int noncom_require_armor_Termi = 0;
    public static final int noncom_require_armor_normal = 1;
    public static final int noncom_require_armor_scout = 2;
    public static final int noncom_require_armor_specific = 3;
    public static final int noncom_require_weapon_primary = 4;
    public static final int noncom_require_weapon_specific = 5;
    public static final int noncom_require_accessory_specific = 6;

    public boolean ofTargetDecisionPhase() {
        return data[traitPhase] == phase_targetDecision;
        //return (data[traitType] == traitType_offensive_data_self && data[traitData1] == trait_special && data[traitData2]<=special_multi);
    }

    boolean isBooleanPredicate(int predicate) {
        return predicate < predicate_underIsBoolean;
    }

    public boolean ofBeforeAllAttackPhase() {
        return data[traitPhase] == phase_allAttacks;
    }

    public boolean ofBeforeEachAttackPhase() {
        return data[traitPhase] == phase_beforeAttack;
    }

    public boolean ofAfterEachAttackPhase() {
        return data[traitPhase] == phase_afterAttack;
    }

    public boolean ofBeforeNextAttackPhase() {
        return data[traitPhase] == phase_nextAttack;
    }

    boolean checkPredicateBeforeHit(int predicate, int value, int[] dataSet, boolean moved, boolean isFirstAttack, boolean isInfantry) {
        switch (predicate) {
            case predicate_every: return true;
            case predicate_random_chance: return Utility.rollForPercent(value);
            case predicate_enemy_armor_more: return dataSet[Utility.def_arm] > value;
            case predicate_enemy_armor_less: return dataSet[Utility.def_arm] < value;
            case predicate_hadnotMoved: return !moved;
            case predicate_onMoving: return moved;
            case predicate_firstAttack: return isFirstAttack;
            case predicate_isInfantry: return isInfantry;
            case predicate_chooseBetterDamage:
                //TODO fix this one immediately
                Trait replacementUse = GameData.getTraitById(value);
                int[] ifReplace = replacementUse.checkForReplacement(dataSet,false);
                return (ifReplace[Utility.atk_str]-ifReplace[Utility.def_arm]) * ifReplace[Utility.atk_spd] * ifReplace[Utility.atk_acc]
                        > (dataSet[Utility.atk_str]-dataSet[Utility.def_arm]) * dataSet[Utility.atk_spd] * dataSet[Utility.atk_acc];

            case predicate_range_more: return dataSet[Utility.range] > value;
            case predicate_range_less: return dataSet[Utility.range] < value;
            default: {
                System.err.println("Wrong predicate detected: " + predicate + "/" + value);
                return false;
            }
        }
    }

    int checkPredicateReturningValue(int predicate, int value, int sqdSize, int enemySqdSize) {
        switch (predicate) {
            case predicate_squad_alive_less: if(sqdSize < value) return value - sqdSize; break;
            case predicate_squad_alive_more: if(sqdSize > value) return sqdSize - value; break;
            case predicate_enemysquad_alive_less: if(enemySqdSize < value) return value - enemySqdSize; break;
            case predicate_enemysquad_alive_more: if(enemySqdSize > value) return enemySqdSize - value; break;

            default: {
                System.err.println("Wrong predicate detected: " + predicate + "/" + value);
                return 0;
            }
        }
        return 0;
    }

    boolean checkPredicateAfterHit(int predicate, int value, int[] dataSet, boolean hit, boolean isInfantry) {
        switch (predicate) {
            case predicate_every: return true;
            case predicate_random_chance: return Utility.rollForPercent(value);
            case predicate_attack_hit: return hit;
            case predicate_attack_miss: return !hit;
            case predicate_damage_inflicted_more: return dataSet[Utility.atk_str] > value;
            case predicate_damage_inflicted_less: return dataSet[Utility.atk_str] < value;
            case predicate_isInfantry: return isInfantry;

            default: {
                System.err.println("Wrong predicate detected: " + predicate + "/" + value);
                return false;
            }
        }
    }

    void modifyStat(int target, int value, int[] dataSet) {
        switch (target) {
            case damage_multiplier: dataSet[Utility.atk_str] = dataSet[Utility.atk_str] * (100 + value) / 100; break;
            case damage_offset: dataSet[Utility.atk_str] = Math.max(dataSet[Utility.atk_str] + value, 0); break;

            case accuracy_multiplier: dataSet[Utility.atk_acc] = dataSet[Utility.atk_acc] * (100 + value) / 100; break;
            case accuracy_offset: dataSet[Utility.atk_acc] = Math.max(dataSet[Utility.atk_acc] + value, 0); break;

            case attack_multiplier: dataSet[Utility.atk_spd] = dataSet[Utility.atk_spd] * (100 + value) / 100; break;
            case attack_offset: dataSet[Utility.atk_spd] = Math.max(dataSet[Utility.atk_spd] + value, 0); break;

            case aadamage_multiplier: dataSet[Utility.atk_str] = dataSet[Utility.atk_str] * (100 + value) / 100; break;
            case aadamage_offset: dataSet[Utility.atk_str] = Math.max(dataSet[Utility.atk_str] + value, 0); break;

            case armor_multiplier: dataSet[Utility.def_arm] = dataSet[Utility.def_arm] * (100 + value) / 100; break;
            case armor_offset: dataSet[Utility.def_arm] = Math.max(dataSet[Utility.def_arm] + value, 0); break;

            case hp_self_offset: dataSet[Utility.atk_hp] -= value; break;
            case hp_enemy_offset: dataSet[Utility.def_hp] -= value; break;

            default: {
                System.err.printf("ModifyStatFunction failed, data %d %d",target,value);
            }
        }
    }

    int[] checkForReplacement(int[] dataSet, boolean reuseDataArray) {
        // Reuse array for extra usage
        if(data[traitType] != traitType_extra) {
            System.err.printf("\nWrong usage of check for replacement.");
            Utility.printCurrentData();
            return new int[]{0,0,0,0,0,0,0,0};
        }
        int[] copy = Arrays.copyOf(dataSet,dataSet.length);
        if(reuseDataArray) copy = dataSet;
        modifyStat(data[traitData1],data[traitData2],copy);
        modifyStat(data[traitData3],data[traitData4],copy);
        return copy;
    }

    public void handleChangeTargetNumber(int[] dataSet) {
        if(data[traitType] != traitType_targetChange) {
            System.err.println("\nStrange traits mixed in.");
            Utility.printCurrentData();
        }
        int externalValue = data[traitData4] < 0 ? dataSet[Utility.atk_tar] : dataSet[data[traitData4]];
        switch (data[traitData1]) {
            case trait_targetChange_attack_offset: dataSet[Utility.atk_tar] += Utility.rollBetween(data[traitData2],data[traitData3]); break;
            case trait_targetChange_defend_offset: dataSet[Utility.atk_tar] -= Utility.rollBetween(data[traitData2],data[traitData3]); break;
            case trait_targetChange_attack_mult: dataSet[Utility.atk_tar] =
                    externalValue * Utility.rollBetween(100 + data[traitData2],100 + data[traitData3]) / 100; break;
            case trait_targetChange_defend_mult: dataSet[Utility.atk_tar] =
                    externalValue * Utility.rollBetween(100 - data[traitData2],100 - data[traitData3]) / 100; break;
        }
        if(dataSet[Utility.atk_tar] <= 1) dataSet[Utility.atk_tar] = 1;
        if(data[bindToOther] >=0) {
            Trait binded = GameData.getTraitById(data[bindToOther]);
            binded.checkForReplacement(dataSet,true);
        }
    }

    public void handleChangeBeforeHit(int[] dataSet, boolean isMoved, boolean isFirstAttack, boolean isInfantry) {
        int numericResult = isBooleanPredicate(data[traitData1]) ? 0 : checkPredicateReturningValue(
                data[traitData1], data[traitData2], dataSet[Utility.atk_size], dataSet[Utility.def_size]
        );
        if(checkPredicateBeforeHit(data[traitData1],data[traitData2],dataSet,isMoved,isFirstAttack,isInfantry) || numericResult > 0){
            if(numericResult > 0) {
                modifyStat(data[traitData3],data[traitData4] * numericResult,dataSet);
            } else {
                modifyStat(data[traitData3],data[traitData4],dataSet);
            }
            if(data[bindToOther] >= 0) {
                // There is new trait
                Trait binded = GameData.getTraitById(data[bindToOther]);
                binded.handleChangeBeforeHit(dataSet,isMoved,isFirstAttack,isInfantry);
            }
        }
    }

    public void handleChangeAfterHit(int[] dataSet, boolean hit, boolean isInfantry) {
        if(checkPredicateAfterHit(data[traitData1], data[traitData2], dataSet, hit, isInfantry)) {
            modifyStat(data[traitData3],data[traitData4],dataSet);
            if(data[bindToOther] >= 0) {
                // There is new trait
                Trait binded = GameData.getTraitById(data[bindToOther]);
                binded.handleChangeAfterHit(dataSet, hit, isInfantry);
            }
        }
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public int getTraitDataRaw(int pos) { return data[pos]; }

    // Since I am using lots of removeIf, this is useful
    public boolean isNotOffensiveTrait() {
        return data[traitPhase] == phase_nonCombat || !(data[traitType] == traitType_single_self);
    }

    public boolean isNotOffensiveSquadTrait() {
        return data[traitPhase] == phase_nonCombat || !(data[traitType] == traitType_squad_self);
    }

    public boolean isNotOffensiveVehicleTrait() {
        return data[traitPhase] == phase_nonCombat || !(data[traitType] == traitType_vehicle_self);
    }

    public boolean isNotDefensiveTrait() {
        return data[traitPhase] == phase_nonCombat || !(data[traitType] == traitType_single_enemy);
    }

    public boolean isNotDefensiveSquadTrait() {
        return data[traitPhase] == phase_nonCombat || !(data[traitType] == traitType_squad_enemy);
    }

    public boolean isNotDefensiveVehicleTrait() {
        return data[traitPhase] == phase_nonCombat || !(data[traitType] == traitType_vehicle_enemy);
    }

    public boolean isNotRequirementTrait() {
        return data[traitPhase] != phase_nonCombat || data[traitData1] != noncom_required;
    }

    public static boolean haveRequirementTrait(List<Trait> traitList) {
        for (Trait t:traitList) if(!t.isNotRequirementTrait())
            return true;
        return false;
    }

    public static final Trait None = new Trait(new int[]{0,0,0,0,0,0,0},-1);
    public static final int throwaway = 99;

    public static ArrayList<Trait> initialization = null;
    public static Trait getReadTraitWithName(String name) {
        for(Trait trait:initialization) {
            if(trait.name.equals(name)) return trait;
        }
        return None;
    }
}
