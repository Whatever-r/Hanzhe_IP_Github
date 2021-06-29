package models.Deprecated.Vehicle2;

import static HZ_util.Print.*;

import simudyne.core.abm.Action;
import simudyne.core.abm.Agent;
import simudyne.core.annotations.Variable;
import simudyne.core.functions.SerializableConsumer;

public class Brand extends Agent<VehicleModel.Globals> {
	public static Action<Brand> updatePurchase =
			action(
					brand -> {
						int buyEVs = brand.getMessagesOfType(Messages.buyEV.class).size();
						int buyICEs = brand.getMessagesOfType(Messages.buyICE.class).size();
						int sellICEs = brand.getMessagesOfType(Messages.sellICE.class).size();
						println("buyEVs = " + buyEVs);
						brand.getLongAccumulator("nbEV").add(buyEVs);
						brand.getLongAccumulator("nbICE").add(buyICEs - sellICEs);
					}
			);
	public static Action<Brand> techImprove =
			action(
					b -> {
						b.evPrice -= b.evPrice > b.evPriceDrop * 2 ? b.evPriceDrop : 0;
						b.evCost -= b.evCost > b.evCosteDrop * 2 ? b.evCosteDrop : 0;
						b.icePrice -= b.icePrice > b.icePriceDrop * 2 ? b.icePriceDrop : 0;
						b.iceCost -= b.iceCost > b.iceCostDrop * 2 ? b.iceCostDrop : 0;
						b.sendPriceInfo();
					});
	public static Action<Brand> sendPrice =
			action(
					b -> b.getLinks(Links.vLink.class)
							.send(Messages.vInfo.class,
									(m, l) -> {
										m.evPrice = b.evPrice;
										m.evCost = b.evCost;
										m.icePrice = b.icePrice;
										m.iceCost = b.iceCost;
									}
							));
	@Variable
	public double evPrice = 60000;
	@Variable
	public double evCost = 6;
	@Variable
	public double icePrice = 30000;
	@Variable
	public double iceCost = 12;
	public double icePriceDrop = 100;
	public double iceCostDrop = 0.1;
	double evPriceDrop = 400;
	double evCosteDrop = 0.1;
	
	private static Action<Brand> action(SerializableConsumer<Brand> consumer) {
		return Action.create(Brand.class, consumer);
	}
	
	void sendPriceInfo() {
		getLinks(Links.vLink.class).send(
				Messages.vInfo.class,
				(m, l) -> {
					m.evPrice = evPrice;
					m.evCost = evCost;
					m.icePrice = icePrice;
					m.iceCost = iceCost;
				}
		);
	}
	
	
}
