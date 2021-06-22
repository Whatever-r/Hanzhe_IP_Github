package models.Climate0622;

//import static HZ_util.Print.*;

import simudyne.core.abm.AgentBasedModel;
import simudyne.core.abm.GlobalState;
import simudyne.core.abm.Group;
import simudyne.core.rng.SeededRandom;
import simudyne.core.abm.Action;
import simudyne.core.abm.Agent;
import simudyne.core.annotations.Constant;
import simudyne.core.annotations.Variable;
import simudyne.core.annotations.ModelSettings;
import simudyne.core.functions.SerializableConsumer;

public class Country extends Agent<ClimateModel0622.Globals> {
	
	@Constant
	String Country;
	@Constant
	double initGDP;
	@Variable(name = "GDP by Country")
	double GDP;
	@Constant // Base Compound Growth in %
	double initCompGrowth;
	@Variable(name = "Compound Growth Rate%") // Base Compound Growth in %
	double compGrowth;
	@Variable(name = "Average Annual Temp by Country")
	double avgAnnualTemp;
	
//	double avgAnnualTempLast;
	
	//impact on growth rate in %
	@Variable
	public double impactOfD2DVariOnGrowth;
	
	private static Action<Country> action(SerializableConsumer<Country> consumer) {
		return Action.create(Country.class, consumer);
	}
	
	static Action<Country> gdpGrowth =
			action(
					country -> {
						country.climateImpactedGrowth();
						country.avgAnnualTemp += country.getGlobals().avgTempStep;
						country.getLinks(Links.ecoLink.class).send(Messages.gdpValue.class, country.GDP);
					}
			);
	
	void climateImpactedGrowth() {
		if (hasMessageOfType(Messages.temperature.class)) {
//			double avgTemp = getMessagesOfType(Messages.temperature.class).get(0).avgTemp;
//			avgTemp = getGlobals().avgTemp;
			double varTemp = getMessagesOfType(Messages.temperature.class).get(0).varTemp;
			gdpGrowth(varTemp, getGlobals().avgTempStep);//, avgAnnualTempLast);
//			avgAnnualTempLast = avgAnnualTemp;
		}
	}
	
	
	void gdpGrowth(double varTemp, double avgTempStep){//, double avgTempLast) {
//		double avgTempVar = avgTemp - avgTempLast;
		double avgTempImpact = -0.001625 * avgTempStep + 0.00875;
//		avgTempImpact += getPrng().normal(0,0.1 ).sample();
 		double coeff = 1 + (compGrowth + avgTempImpact);
//		println(coeff);
		this.GDP *= coeff;
	}
	
	
}



