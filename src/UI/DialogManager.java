package UI;

import data.GameData;
import data.Item.*;
import data.Unit.Astartes;
import data.Unit.Individual;
import data.Unit.Trait;
import data.Unit.Vehicle;
import javafx.collections.FXCollections;
import javafx.geometry.Orientation;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.util.StringConverter;

import java.util.ArrayList;
import java.util.List;

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
        if(unit.isInfantry()) {
            if (!(unit instanceof Astartes)) {
                System.err.print("\nError cannot convert unit");
                return null;
            }
            FlowPane mainPane = new FlowPane();
            Astartes bth = new Astartes(unit.toString(), ((Astartes) unit).getCloneArray());
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
            ChoiceBox<Armour> armorChoice = new ChoiceBox<>(FXCollections.observableList(GameData.getArmourList()));
            armorChoice.getSelectionModel().select(bth.getEquipment(Astartes.armour));

//            pane.getChildren().addAll(label,armorChoice);
            pane.add(label, 1, 1);
            pane.add(armorChoice, 2, 1);

            // Weapon 1
            label = new Label("Weapon 1: ");
            ChoiceBox<Weapon> weaponChoice1 = new ChoiceBox<>(FXCollections.observableList(GameData.getWeaponList()));
            weaponChoice1.getSelectionModel().select(bth.getEquipment(Astartes.weapon1));
            pane.add(label, 1, 2);
            pane.add(weaponChoice1, 2, 2);

            // Weapon 1
            label = new Label("Weapon 2: ");
            ChoiceBox<Weapon> weaponChoice2 = new ChoiceBox<>(FXCollections.observableList(GameData.getWeaponList()));
            weaponChoice2.getSelectionModel().select(bth.getEquipment(Astartes.weapon2));
            pane.add(label, 1, 3);
            pane.add(weaponChoice2, 2, 3);

            // Accessory
            label = new Label("Accessory: ");
            ChoiceBox<Accessory> accChoice = new ChoiceBox<>(FXCollections.observableList(GameData.getAccessories()));
            accChoice.getSelectionModel().select(bth.getEquipment(Astartes.accessory));
            pane.add(label, 1, 4);
            pane.add(accChoice, 2, 4);

            final ChoiceBox[] listChoiceBox = {armorChoice, weaponChoice1, weaponChoice2, accChoice};

            armorChoice.setOnAction(actionEvent -> manageAstartesEquipment(listChoiceBox, 0, bth, display));

            weaponChoice1.setOnAction(actionEvent -> manageAstartesEquipment(listChoiceBox, 1, bth, display));

            weaponChoice2.setOnAction(actionEvent -> manageAstartesEquipment(listChoiceBox, 2, bth, display));

            accChoice.setOnAction(actionEvent -> manageAstartesEquipment(listChoiceBox, 3, bth, display));
            /*accChoice.setOnAction(e -> {
                bth.changeEquipment(Astartes.accessory,
                        GameData.getAccessories().indexOf(accChoice.getSelectionModel().getSelectedItem()));
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
                            case Trait.noncom_require_armor_normal:
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
                            case Trait.noncom_require_armor_normal:
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

    static void manageAstartesEquipment(ChoiceBox[] listChoiceBox, int choice, Astartes target, Pane display) {
        if(listChoiceBox.length < 3 || choice >= 4) {
            System.err.println("Wrong input, exiting process.");
            return;
        }

        // Force convert
        Item chosenItem = (Item)listChoiceBox[choice].getSelectionModel().getSelectedItem();

        if(chosenItem instanceof Armour && choice == 0) {
            // Armor is force cleared, TODO only force clear with different types
            target.changeEquipment(new int[]{listChoiceBox[choice].getSelectionModel().getSelectedIndex(), -1, -1, -1});
            for(ChoiceBox box: listChoiceBox)
                if(box != listChoiceBox[choice])
                    box.getSelectionModel().clearSelection();
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
                        case Trait.noncom_require_armor_normal:
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
            target.changeEquipment(Astartes.accessory, GameData.getAccessories().indexOf(chosenItem));
        } else {
            System.err.println("There is a screwup somewhere down the line, choice " + choice);
            return;
        }

        System.out.printf("\nChange event fired for choice %d, selection %d, listData %s", choice + 1,
                listChoiceBox[choice].getSelectionModel().getSelectedIndex(), target.equipmentToString());

        display.getChildren().clear();
        display.getChildren().addAll(target.getUnitDisplay());
    }
}
