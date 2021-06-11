package models.Vehicle2;

import simudyne.core.abm.Action;
import simudyne.core.abm.Agent;
import simudyne.core.annotations.Variable;
import simudyne.core.functions.SerializableConsumer;


public class Household extends Agent<VehicleModel.Globals> {
	
	public static Action<Household> newVehicle =
			action(
					h -> {
						if (h.ownVehicle == null) {
							double icePrice = h.getMessageOfType(Messages.vInfo.class).icePrice;
							double iceCost = h.getMessageOfType(Messages.vInfo.class).iceCost;
							double evPrice = h.getMessageOfType(Messages.vInfo.class).evPrice;
							double evCost = h.getMessageOfType(Messages.vInfo.class).evCost;
							if (h.purchaseBudget >= icePrice) {
								if (h.purchaseBudget < evPrice) {
									h.ownVehicle = new Vehicle(false, icePrice, iceCost);
//									h.getLongAccumulator("nbICE").add(1);
									h.getLinks(Links.vLink.class).send(Messages.buyICE.class);
								} else {
									if ((evCost / iceCost) <= (1.0 / 3)) {
										h.ownVehicle = new Vehicle(true, evPrice, evCost);
//										h.getLongAccumulator("nbEV").add(1);
										h.getLinks(Links.vLink.class).send(Messages.buyEV.class);
									}
								}
							} else if (h.purchaseBudget >= evPrice) {
								h.ownVehicle = new Vehicle(true, evPrice, evCost);
								h.getLinks(Links.vLink.class).send(Messages.buyEV.class);
//								h.getLongAccumulator("nbEV").add(1);
							}
						}
					}
			);
	public static Action<Household> replaceVehicle =
			action(
					h -> {
						if (h.ownVehicle != null) {
							double iceCost = h.getMessageOfType(Messages.vInfo.class).iceCost;
							double icePrice = h.getMessageOfType(Messages.vInfo.class).icePrice;
							double evPrice = h.getMessageOfType(Messages.vInfo.class).evPrice;
							double evCost = h.getMessageOfType(Messages.vInfo.class).evCost;
//							if (h.getPrng().discrete(1, 1).sample() == 1) {
//								println("change");
//								h.ownVehicle = null;
//								h.ownVehicle = new Vehicle(true, evPrice, evCost);
//								h.getLongAccumulator("nbEV").add(1);
//								h.getLongAccumulator("nbICE").add(-1);
//							} else
							if (h.ownVehicle.isEV == false) {
								if (evCost < 1) {
									h.ownVehicle = null;
									h.ownVehicle = new Vehicle(true, evPrice, evCost);
//									h.getLongAccumulator("nbEV").add(1);
//									h.getLongAccumulator("nbICE").add(-1);
									h.getLinks(Links.vLink.class).send(Messages.sellICE.class);
									h.getLinks(Links.vLink.class).send(Messages.buyEV.class);
								}
							}
						}
					});
	@Variable
	public double purchaseBudget;
	Vehicle ownVehicle;
	
	static Action<Household> action(SerializableConsumer<Household> consumer) {
		return Action.create(Household.class, consumer);
	}
	
	public static Action<Household> newVehicle2() {
		return action(
				h -> {
					if (h.ownVehicle == null) {
						h.hasMessageOfType(
								Messages.vInfo.class,
								message -> {
									double icePrice = message.icePrice;
									double iceCost = message.iceCost;
									double evPrice = message.evPrice;
									double evCost = message.evCost;
									if (h.purchaseBudget >= icePrice) {
										if (h.purchaseBudget < evPrice) {
											h.ownVehicle = new Vehicle(false, icePrice, iceCost);
											h.getLinks(Links.vLink.class).send(Messages.buyICE.class);
										} else {
											if (evCost / iceCost <= 1.0 / 3) {
												h.ownVehicle = new Vehicle(true, evPrice, evCost);
												h.getLinks(Links.vLink.class).send(Messages.buyEV.class);
											}
										}
									} else if (h.purchaseBudget >= evPrice) {
										h.ownVehicle = new Vehicle(true, evPrice, evCost);
										h.getLinks(Links.vLink.class).send(Messages.buyEV.class);
									}
								}
						);
					}
				}
		);
	}
	
	public static class Vehicle {
		boolean isEV;
		//int efficiency;
		double price;
		double unitCost;
		
		public Vehicle(boolean isEV, double price, double unitCost) {
			this.isEV = isEV;
			this.price = price;
			this.unitCost = unitCost;
//			double energyCost = isEV ? getGlobals().ElecPrice : getGlobals().FuelPrice;
//			this.unitCost = efficiency * energyCost;
		}
	}
	
}
