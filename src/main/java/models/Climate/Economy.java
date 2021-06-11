package models.Climate;

//import static HZ_util.Print.*;

import simudyne.core.abm.Action;
import simudyne.core.abm.Agent;
import simudyne.core.annotations.Variable;
import simudyne.core.functions.SerializableConsumer;

import java.util.List;

public class Economy extends Agent<ClimateModel.Globals> {
	static Action<Economy> gdpGrowth =
			action(
					eco -> {
						eco.climateImpactedGrowth();
						eco.getLinks(Links.ecoLink.class).send(Messages.gdpValue.class, eco.gdpValue);
					}
			);
	
	//impact on growth rate in %
	@Variable
	public double impactOfD2DVariOnGrowth;
	//	gdp Value in USD
	@Variable
	public double gdpValue;
	//	base Growth rate in %
	@Variable
	public double baseGrowth;
	
	private static Action<Economy> action(SerializableConsumer<Economy> consumer) {
		return Action.create(Economy.class, consumer);
	}
	
	void climateImpactedGrowth() {
		if (hasMessageOfType(Messages.temperature.class)) {
//			double avgTemp = getMessagesOfType(Messages.temperature.class).get(0).avgTemp;
			double varTemp = getMessagesOfType(Messages.temperature.class).get(0).varTemp;
			gdpGrowth(varTemp);
			
		}
	}
	
	void gdpGrowth(double varTemp) {
		double coeff = 1+(baseGrowth * 1-(varTemp * impactOfD2DVariOnGrowth));
//		println(coeff);
		this.gdpValue *= coeff;
	}
	
	
}



