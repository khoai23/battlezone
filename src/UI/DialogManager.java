package UI;

import data.GameData;
import data.Item.*;
import data.Unit.Astartes;
import data.Unit.Individual;
import data.Unit.Trait;
import data.Unit.Vehicle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Orientation;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.util.StringConverter;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static data.Unit.Trait.getArmorBindingTrait;

/**
 * Created by Quan on 7/1/2017.
 */
public class DialogManager {
    static Dialog<Individual> equipmentDialog = null;

    public static Dialog<Individual> getEquipmentDialog(Individual unit) {
        if(equipmentDialog == null) {
            equipmentDialog = new Dialog<>();
            ButtonType buttonTypeOk = new ButtonType("Done", ButtonBar.ButtonData.OK_DONE);
            ButtonType buttonTypeCancel = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
            equipmentDialog.getDialogPane().getButtonTypes().add(buttonTypeOk);
            equipmentDialog.getDialogPane().getButtonTypes().add(buttonTypeCancel);
//            equipmentDialog.setResizable(false);
        }
        equipmentDialog.setTitle(unit.isInfantry() ? "Change Equipment" : "Change Weaponry");

        equipmentDialog.getDialogPane().setContent(null);
        Individual clone;
        boxBinding.clear();
        if(unit.isInfantry()) {
            if (!(unit instanceof Astartes)) {
                System.err.print("\nError cannot convert unit");
                return null;
            }
            FlowPane mainPane = new FlowPane();
            Astartes bth = new Astartes((Astartes) unit);
            clone = bth;

            Pane display = new AnchorPane();
            display.setPrefHeight(200);
            display.setPrefWidth(150);
            display.setId("canvas");
            display.getChildren().addAll(bth.getUnitDisplay());

            GridPane pane = new GridPane();
            mainPane.getChildren().add(pane);
            mainPane.getChildren().add(display);

            Label label = new Label("Armor: ");
            ObservableList<Armour> armourList = FXCollections.observableList(GameData.getArmourList());
            ChoiceBox<Armour> armorChoice = new ChoiceBox<>(armourList);

//            pane.getChildren().addAll(label,armorChoice);
            pane.add(label, 1, 1);
            pane.add(armorChoice, 2, 1);

            // Weapon 1
            label = new Label("Weapon 1: ");
            ObservableList<Weapon> weaponListPrimary = FXCollections.observableList(GameData.getWeaponList());
            ChoiceBox<Weapon> weaponChoice1 = new ChoiceBox<>(FXCollections.observableList(weaponListPrimary));
            pane.add(label, 1, 2);
            pane.add(weaponChoice1, 2, 2);

            // Weapon 1
            label = new Label("Weapon 2: ");
            ObservableList<Weapon> weaponListSecondary = FXCollections.observableList(GameData.getWeaponList());
            ChoiceBox<Weapon> weaponChoice2 = new ChoiceBox<>(FXCollections.observableList(weaponListSecondary));
            pane.add(label, 1, 3);
            pane.add(weaponChoice2, 2, 3);

            // Accessory
            label = new Label("Accessory: ");
            ObservableList<Accessory> accessoryList = FXCollections.observableList(GameData.getAccessoryList());
            ChoiceBox<Accessory> accChoice = new ChoiceBox<>(FXCollections.observableList(accessoryList));
            pane.add(label, 1, 4);
            pane.add(accChoice, 2, 4);

            armorChoice.getSelectionModel().select(bth.getEquipment(Astartes.armour));
            weaponChoice1.getSelectionModel().select(bth.getEquipment(Astartes.weapon1));
            weaponChoice2.getSelectionModel().select(bth.getEquipment(Astartes.weapon2));
            accChoice.getSelectionModel().select(bth.getEquipment(Astartes.accessory));

            final ChoiceBox[] listChoiceBox = {armorChoice, weaponChoice1, weaponChoice2, accChoice};
            final ObservableList[] listDisplay = {armourList, weaponListPrimary, weaponListSecondary, accessoryList };

            /*armorChoice.setOnAction(actionEvent -> manageAstartesEquipment(listChoiceBox, 0, bth, display));

            weaponChoice1.setOnAction(actionEvent -> manageAstartesEquipment(listChoiceBox, 1, bth, display));

            weaponChoice2.setOnAction(actionEvent -> manageAstartesEquipment(listChoiceBox, 2, bth, display));

            accChoice.setOnAction(actionEvent -> manageAstartesEquipment(listChoiceBox, 3, bth, display));*/
            armorChoice.setOnAction(actionEvent -> repopulateBoxesAstartes(0, listChoiceBox, listDisplay, bth, display));

            weaponChoice1.setOnAction(actionEvent -> repopulateBoxesAstartes(1, listChoiceBox, listDisplay, bth, display));

            weaponChoice2.setOnAction(actionEvent -> repopulateBoxesAstartes(2, listChoiceBox, listDisplay, bth, display));

            accChoice.setOnAction(actionEvent -> repopulateBoxesAstartes(3, listChoiceBox, listDisplay, bth, display));

            openThread = false;
            /*accChoice.setOnAction(e -> {
                bth.changeEquipment(Astartes.accessory,
                        GameData.getAccessoryList().indexOf(accChoice.getSelectionModel().getSelectedItem()));
                display.getChildren().clear();
                display.getChildren().addAll(bth.getUnitDisplay());
            });

            armorChoice.setOnAction(e -> {
                Armour armour = armorChoice.getSelectionModel().getSelectedItem();

                bth.changeEquipment(Astartes.armour, GameData.getArmourList().indexOf(armour));
                display.getChildren().clear();
                display.getChildren().addAll(bth.getUnitDisplay());
            });

            weaponChoice1.setOnAction(e -> {
                Weapon wpn = weaponChoice1.getSelectionModel().getSelectedItem();
                List<Trait> requirement = new ArrayList<>(wpn.traitList);
                requirement.removeIf(Trait::isNotRequirementTrait);
                for(Trait req: requirement) {
                    if(req.getTraitDataRaw(Trait.traitData1) == Trait.noncom_required) {
//                        System.out.printf("\nTrait name %s, data %d %d %d",req.getName(), req.data[Trait.traitData1],
//                                req.data[Trait.traitData2], req.data[Trait.traitData3]);
                        switch (req.getTraitDataRaw(Trait.traitData2)) {
                            case Trait.noncom_require_accessory_specific:
                                accChoice.getSelectionModel().select(req.getTraitDataRaw(Trait.traitData3)); break;
                            case Trait.noncom_require_weapon_specific:
                                weaponChoice2.getSelectionModel().select(req.getTraitDataRaw(Trait.traitData3)); break;
                            case Trait.noncom_wearable_armor_Termi:
                                if(armorChoice.getSelectionModel().getSelectedItem().isTerminator()) return;
                                break;
                            case Trait.noncom_require_armor_Termi:
                                if(!armorChoice.getSelectionModel().getSelectedItem().isTerminator()) return;
                                break;
                            case Trait.noncom_require_armor_specific:
                                armorChoice.getSelectionModel().select(req.getTraitDataRaw(Trait.traitData3)); break;
                        }
                    }
                }

                bth.changeEquipment(Astartes.weapon1,
                        GameData.getWeaponList().indexOf(weaponChoice1.getSelectionModel().getSelectedItem()));
                display.getChildren().clear();
                display.getChildren().addAll(bth.getUnitDisplay());
            });

            weaponChoice2.setOnAction(e -> {
                Weapon wpn = weaponChoice2.getSelectionModel().getSelectedItem();
                List<Trait> requirement = new ArrayList<>(wpn.traitList);
                requirement.removeIf(Trait::isNotRequirementTrait);
                for(Trait req: requirement) {
                    if(req.getTraitDataRaw(Trait.traitData1) == Trait.noncom_required) {
//                        System.out.printf("\nTrait name %s, data %d %d %d",req.getName(), req.data[Trait.traitData1],
//                                req.data[Trait.traitData2], req.data[Trait.traitData3]);
                        switch (req.getTraitDataRaw(Trait.traitData2)) {
                            case Trait.noncom_require_accessory_specific:
                                accChoice.getSelectionModel().select(req.getTraitDataRaw(Trait.traitData3)); break;
                            case Trait.noncom_require_weapon_specific:
                                weaponChoice1.getSelectionModel().select(req.getTraitDataRaw(Trait.traitData3)); break;
                            case Trait.noncom_wearable_armor_Termi:
                                if(armorChoice.getSelectionModel().getSelectedItem().isTerminator()) return;
                                break;
                            case Trait.noncom_require_armor_Termi:
                                if(!armorChoice.getSelectionModel().getSelectedItem().isTerminator()) return;
                                break;
                            case Trait.noncom_require_armor_specific:
                                armorChoice.getSelectionModel().select(req.getTraitDataRaw(Trait.traitData3)); break;
                            case Trait.noncom_require_weapon_primary:
                                weaponChoice1.getSelectionModel().select(wpn); return;
                        }
                    }
                }

                bth.changeEquipment(Astartes.weapon2,
                        GameData.getWeaponList().indexOf(weaponChoice2.getSelectionModel().getSelectedItem()));
                display.getChildren().clear();
                display.getChildren().addAll(bth.getUnitDisplay());
            });*/

            equipmentDialog.getDialogPane().setContent(mainPane);
        } else if(unit instanceof Vehicle) {
            Vehicle veh = new Vehicle((Vehicle)unit);
            int now = veh.vehicleType().numOfWeapons();
            FlowPane mainPane = new FlowPane(Orientation.HORIZONTAL);
            Pane display = new AnchorPane();
            display.setPrefHeight(300);
            display.setPrefWidth(500);
            display.setId("canvas");

            GridPane pane = new GridPane();
            mainPane.getChildren().add(pane);
            mainPane.getChildren().add(display);

            Label label = new Label("Chassis: ");
            pane.add(label, 1, 1);
            pane.add(new Label(veh.vehicleType().chassis().getName()), 2, 1);


            if(now > 0) {
                label = new Label("Primary Weapon: ");
                ChoiceBox<int[]> variantChoice = new ChoiceBox<>(FXCollections.observableList(
                        GameData.getVehiclesVariantById(veh.getVehicleType()).getLoadOutData()
                ));
                variantChoice.setConverter(new StringConverter<int[]>() {
                    @Override
                    public String toString(int[] object) {
                        return GameData.getVehiclesWeaponById(object[0]).getName();
                    }

                    @Override
                    public int[] fromString(String string) {
                        return new int[2];
                    }
                });

                pane.add(label, 1, 2);
                pane.add(variantChoice, 2, 2);

                if(now > 1) {
                    label = new Label("Secondary Weapon: ");
                    final Label secondary = new Label();
                    variantChoice.setOnAction(event -> {
                        secondary.setText(GameData.getVehiclesWeaponById(variantChoice.getSelectionModel().getSelectedItem()[1]).getName());
                        veh.setLoadout(variantChoice.getSelectionModel().getSelectedIndex());
                        display.getChildren().clear();
                        display.getChildren().addAll(ImageHelper.getVehicleById(veh.getVehicleType(), veh.getLoadout(), veh.pintle, true));
                    });
                    pane.add(label, 1, 3);
                    pane.add(secondary, 2, 3);
                } else {
                    variantChoice.setOnAction(event -> {
                        veh.setLoadout(variantChoice.getSelectionModel().getSelectedIndex());
                        display.getChildren().clear();
                        display.getChildren().addAll(ImageHelper.getVehicleById(veh.getVehicleType(), veh.getLoadout(), veh.pintle, true));
                    });
                }

                variantChoice.getSelectionModel().select(veh.getLoadout());
            }

            if(veh.getPintleId() > -1) {
                label = new Label("Pintle: ");
                CheckBox pintle = new CheckBox();
                pintle.setSelected(veh.pintle);
                pintle.setOnAction(event -> {
                    veh.pintle = pintle.isSelected();
                    display.getChildren().clear();
                    display.getChildren().addAll(ImageHelper.getVehicleById(veh.getVehicleType(), veh.getLoadout(), veh.pintle, true));
                });
                pane.add(label, 1, 4);
                pane.add(pintle, 2, 4);
            }

            clone = veh;
            equipmentDialog.getDialogPane().setContent(mainPane);
        } else {
//            System.out.print("\nIncomplete");
            System.err.print("\nError cannot convert unit");
            clone = unit;
        }

        equipmentDialog.setResultConverter(button -> {
            if (button.getButtonData().equals(ButtonBar.ButtonData.OK_DONE)) return clone;
            else return unit;
        });

        return equipmentDialog;
    }

    /**
     * @deprecated by repopulateBoxesAstartes
    * */
    static void manageAstartesEquipment(ChoiceBox[] listChoiceBox, int choice, Astartes target, Pane display) {
        if(listChoiceBox.length < 3 || choice >= 4) {
            System.err.println("Wrong input, exiting process.");
            return;
        }

        // Force convert
        Item chosenItem = (Item)listChoiceBox[choice].getSelectionModel().getSelectedItem();

        if(chosenItem instanceof Armour && choice == 0) {
            if(GameData.getArmourById(target.getEquipment(Astartes.armour)).isTerminator()
                    != ((Armour) chosenItem).isTerminator()) {
                // Armor is force cleared if types are different
                target.changeEquipment(new int[]{listChoiceBox[choice].getSelectionModel().getSelectedIndex(), -1, -1, -1});
                for(ChoiceBox box: listChoiceBox)
                    if(box != listChoiceBox[choice])
                        box.getSelectionModel().clearSelection();
            }
        } else if(chosenItem instanceof Weapon && choice < 3 && choice > 0) {
            Weapon wpn = (Weapon)chosenItem;
//            System.out.println("Weapon selected " + wpn.getName());
            List<Trait> requirement = new ArrayList<>(wpn.traitList);
            requirement.removeIf(Trait::isNotRequirementTrait);
            for(Trait req: requirement) {
                if(req.getTraitDataRaw(Trait.traitData1) == Trait.noncom_required) {
                    switch (req.getTraitDataRaw(Trait.traitData2)) {
                        case Trait.noncom_require_accessory_specific:
                            listChoiceBox[3].getSelectionModel().select(req.getTraitDataRaw(Trait.traitData3)); break;
                        case Trait.noncom_require_weapon_specific:
                            // Selecting the other hand
                            listChoiceBox[choice == 1 ? 2 : 1].getSelectionModel().select(req.getTraitDataRaw(Trait.traitData3)); break;
                        case Trait.noncom_wearable_armor_Termi:
                            if(((Armour)listChoiceBox[0].getSelectionModel().getSelectedItem()).isTerminator()) {
                                // Revert to what was originally being wielded before the selection
                                listChoiceBox[choice].getSelectionModel().select(target.getEquipment(choice));
                                return;
                            }
                            break;
                        case Trait.noncom_require_armor_Termi:
                            if(!((Armour)listChoiceBox[0].getSelectionModel().getSelectedItem()).isTerminator()) {
                                listChoiceBox[choice].getSelectionModel().select(target.getEquipment(choice));
                                return;
                            }
                            break;
                        case Trait.noncom_require_armor_specific:
                            listChoiceBox[0].getSelectionModel().select(req.getTraitDataRaw(Trait.traitData3)); break;
                        case Trait.noncom_require_weapon_primary:
                            if(choice == 2) { // Secondary selected, switching to primary
                                listChoiceBox[1].getSelectionModel().select(chosenItem);
                                return;
                            }
                    }
                }
            }

            target.changeEquipment(choice, GameData.getWeaponList().indexOf(wpn));
        } else if(chosenItem instanceof Accessory && choice == 3) {
            target.changeEquipment(Astartes.accessory, GameData.getAccessoryList().indexOf(chosenItem));
        } else {
            System.err.println("There is a screwup somewhere down the line, choice " + choice);
            return;
        }

        System.out.printf("\nChange event fired for choice %d, selection %d, listData %s", choice + 1,
                listChoiceBox[choice].getSelectionModel().getSelectedIndex(), target.equipmentToString());

        display.getChildren().clear();
        display.getChildren().addAll(target.getUnitDisplay());
    }

    static void repopulateBoxesAstartes(int boxSelected, ChoiceBox[] listChoiceBox, ObservableList[] displayList, Astartes target, Pane display) {
        if(boxSelected > displayList.length || boxSelected < 0)
            return;
        // Only let one thread run at a time, except for the primary switch
        if(openThread) {
            System.err.printf("\nThread already opened, breaking from box %d with target %s.",boxSelected, target);
            return;
        }
        openThread = true;

        // remove all related binding
        for(int[] bind:popBinding(boxSelected)) {
            switch (bind[1]) {
                case 1:
                case 2:
                    listChoiceBox[bind[1]].setItems(FXCollections.observableList(GameData.getWeaponList()));
                    break;
                case 3:
                    listChoiceBox[bind[1]].setItems(FXCollections.observableList(GameData.getAccessoryList()));
                    break;
                case 0:
                    listChoiceBox[bind[1]].setItems(FXCollections.observableList(GameData.getArmourList()));
                    System.err.printf("Bad practice - bind armor detected.");
                    break;
                default:
                    System.err.printf("Wrong binding value (%d %d).", bind[0],bind[1]);
            }
        }
        Object selected = (listChoiceBox[boxSelected].getSelectionModel().getSelectedItem());
        if(selected == null)
            return;
        List<Trait> itemTraitList;
        Item selectedItem = (Item) selected;
        if(boxSelected == 0) {
            Armour currentlyWearing = GameData.getArmourById(target.getEquipment(Astartes.armour));
            if(currentlyWearing.isTerminator() != ((Armour)selectedItem).isTerminator()) {
                // Different armor, reset all equipment
                List<Item> validItemList;
                // Armor, check the others for validity
                if(((Armour)selectedItem).isTerminator()) {
                    System.out.print("\nSpecial armour selected");
                    // Only get armor-compatible items
                    for(int i=1;i<4;i++) {
                        Item currentWieldingItem = (Item) listChoiceBox[i].getSelectionModel().getSelectedItem();
                        if(i==3) {
                            validItemList = new ArrayList<>(listChoiceBox[i].getItems());
                        } else {
                            validItemList = new ArrayList<>(GameData.getWeaponList());
                        }
                        // Normal item cannot be worn with terminator armor
                        validItemList.removeIf(item -> !Trait.haveRequirementTrait(item.getItemTraits()));
                        // Check if requirement trait do have either require or wearable path
                        //for(Item item:validItemList) {
                        for(Iterator<Item> iterator = validItemList.iterator(); iterator.hasNext();) {
                            itemTraitList = iterator.next().getItemTraits();
                            itemTraitList.removeIf(Trait::isNotRequirementTrait);
                            boolean removable = true;
                            for(Trait t: itemTraitList) {
                                if(t.getTraitDataRaw(Trait.traitData2) == Trait.noncom_require_armor_Termi
                                        || t.getTraitDataRaw(Trait.traitData2) == Trait.noncom_wearable_armor_Termi){
                                    removable = false;
                                    break;
                                }
                            }
                            if(removable) iterator.remove();
                        }
                        // Re-adding all valid items
                        //displayList[i].clear();
                        listChoiceBox[i].setItems(FXCollections.observableList(validItemList));
                        if(validItemList.contains(currentWieldingItem))
                            listChoiceBox[i].getSelectionModel().select(currentWieldingItem);
                    }
                } else {
                    // get all items for default
                    for (int i = 1; i < 4; i++) {
                        Item currentWieldingItem = (Item) listChoiceBox[i].getSelectionModel().getSelectedItem();
                        if (i == 3) {
                            validItemList = new ArrayList<>(GameData.getAccessoryList());
                        } else {
                            validItemList = new ArrayList<>(GameData.getWeaponList());
                        }
                        // Normal item be worn with terminator armor
                        // validItemList.removeIf(item -> !Trait.haveRequirementTrait(item.getItemTraits()));
                        // Check if requirement trait does not have require terminator
                        //for(Item item:validItemList) {
                        for (Iterator<Item> iterator = validItemList.iterator(); iterator.hasNext(); ) {
                            itemTraitList = iterator.next().getItemTraits();
                            for (Trait t : itemTraitList) {
                                // has an requirement trait with require path
                                if (!t.isNotRequirementTrait() && t.getTraitDataRaw(Trait.traitData2) == Trait.noncom_require_armor_Termi) {
                                    iterator.remove();
                                    break;
                                }
                            }
                        }
                        // Re-adding all valid items
                        //displayList[i].clear();
                        listChoiceBox[i].setItems(FXCollections.observableList(validItemList));
                        if(validItemList.contains(currentWieldingItem))
                            listChoiceBox[i].getSelectionModel().select(currentWieldingItem);
                    }
                }
            }
            target.changeEquipment(Astartes.armour, (Item)(listChoiceBox[0].getSelectionModel().getSelectedItem()));
        } else {
            target.changeEquipment(boxSelected, (Item)(listChoiceBox[boxSelected].getSelectionModel().getSelectedItem()));

            itemTraitList = selectedItem.getItemTraits();
            itemTraitList.removeIf(Trait::isNotRequirementTrait);
            boolean checkedOtherWeapon=false;
            for(Trait t: itemTraitList) {
                int restriction = t.getTraitDataRaw(Trait.traitData2);
                if(restriction == Trait.noncom_require_accessory_specific && boxSelected != 3) {
                    int accID = t.getTraitDataRaw(Trait.traitData3);
                    Item reqAcc = GameData.getAccessoryById(accID);
                    List<Item> accessories = new ArrayList<>();
                    accessories.add(reqAcc);
                    //displayList[3].clear();
                    //displayList[3].add(reqAcc);
                    listChoiceBox[3].setItems(FXCollections.observableList(accessories));
                    listChoiceBox[3].getSelectionModel().select(0);
                    target.changeEquipment(Astartes.accessory, reqAcc);
                    pushBinding(boxSelected, Astartes.accessory);
                    System.out.printf("\nRequire accessory selected, item %s, target %s", selectedItem.getName(), target.equipmentToString());
                } else if(restriction == Trait.noncom_require_weapon_specific || restriction == Trait.noncom_must_be_paired) {
                    if(boxSelected == 3) continue;
                    // Ignore if accessory requirement
                    int otherHand = (boxSelected==Astartes.weapon1) ? Astartes.weapon2 : Astartes.weapon1;

                    int weaId = t.getTraitDataRaw(Trait.traitData3);
                    Item reqWea = (restriction == Trait.noncom_require_weapon_specific) ? GameData.getWeaponById(weaId) : selectedItem;
                    List<Item> weapons = new ArrayList<>();
                    weapons.add(reqWea);
                    listChoiceBox[otherHand].setItems(FXCollections.observableList(weapons));
                    listChoiceBox[otherHand].getSelectionModel().select(0);
                    // TODO have multiple bound weapon
                    //if(!checkedOtherWeapon)
                    //    displayList[otherHand].clear();
                    //displayList[otherHand].add(reqWea);
                    //listChoiceBox[otherHand].setValue(reqWea);
                    target.changeEquipment(otherHand, reqWea);
                    //checkedOtherWeapon = true;
                    pushBinding(boxSelected, otherHand);
                    System.out.printf("\nRequire weapon specific selected, item %s, current box %d, change box %d, target %s", selectedItem.getName(), boxSelected,
                            otherHand, target.equipmentToString());
                } else if(restriction == Trait.noncom_require_weapon_primary && boxSelected == 2) {
                    // Remove accessory and secondary weapon beforehand
                    listChoiceBox[3].getSelectionModel().clearSelection();
                    target.changeEquipment(Astartes.accessory, Accessory.None);
                    listChoiceBox[2].getSelectionModel().clearSelection();
                    target.changeEquipment(Astartes.weapon2, Weapon.None);
                    openThread = false;
                    target.changeEquipment(Astartes.weapon1, selectedItem);
                    System.out.printf("\nRequire weapon primary selected, item %s, target %s", selectedItem.getName(), target.equipmentToString());
                    listChoiceBox[1].getSelectionModel().select(selectedItem);
                    // repopulateBoxesAstartes(Astartes.weapon1, listChoiceBox, displayList, target, display);
                    return;
                } else if(restriction == Trait.noncom_must_not_use_slot) {
                    int slotBlocked = t.getTraitDataRaw(Trait.traitData3);
                    if(slotBlocked == restriction || slotBlocked < 0 || slotBlocked >= 4) continue;
                    listChoiceBox[slotBlocked].getSelectionModel().clearSelection();
                    target.changeEquipment(slotBlocked, -1);
                    pushBinding(boxSelected, slotBlocked);
                    System.out.printf("\nRequire no item selected, item %s, target %s", selectedItem.getName(), target.equipmentToString());
                }
            }
        }

//         Armor-specific trait check, used no matter what selection
//        int armorIdx = GameData.getArmourList().indexOf(selectedItem);
//        for(int i=1;i<4;i++) {
//            displayList[i].removeIf(item -> {
//                Trait armorConcern = Trait.getArmorBindingTrait(((Item)item).getItemTraits(), armorIdx);
//                return armorConcern != Trait.None && armorConcern.getTraitDataRaw(Trait.traitData3) != armorIdx;
//            });
//            if(!displayList[i].contains(listChoiceBox[i].getSelectionModel().getSelectedItem()))
//                listChoiceBox[i].getSelectionModel().clearSelection();
//        }
//
//        for(ChoiceBox box:listChoiceBox)
//            System.out.printf("\nbox item %s", ((Item)box.getSelectionModel().getSelectedItem()).getName());

        display.getChildren().clear();
        display.getChildren().addAll(target.getUnitDisplay());

        System.out.printf("\nChange event fired for choice %d, selection %d, listData %s", boxSelected + 1,
                listChoiceBox[boxSelected].getSelectionModel().getSelectedIndex(), target.equipmentToString());
        openThread = false;
    }


    static List<int[]> popBinding(int itemChanged) {
        List<int[]> result = new ArrayList<>();
        for(int[] bind:boxBinding)
            if(bind[0]==itemChanged) result.add(bind);
        boxBinding.removeAll(result);
        return result;
    }

    static void pushBinding(int itemChanged, int boxLocked) {
        boxBinding.add(new int[]{itemChanged, boxLocked});
    }
    // List to check for when change item
    static List<int[]> boxBinding = new ArrayList<>();
    static boolean openThread = false;
}
