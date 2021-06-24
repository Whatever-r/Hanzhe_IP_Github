package models.Climate0622;

import simudyne.core.abm.Action;
import simudyne.core.abm.Agent;
import simudyne.core.annotations.Constant;
import simudyne.core.annotations.Variable;
import simudyne.core.functions.SerializableConsumer;

import static HZ_util.Print.println;

public class Country extends Agent<ClimateModel0622.Globals> {
	
	@Constant(name = "Name of Country/Region")
	String Country;
	
	@Variable(initializable = true, name = "GDP by Country")
	double GDP;
	
	@Variable(initializable = true, name = "Compound Growth Rate%") // Base Compound Growth in %
	double compGrowth;
	
	@Variable(initializable = true, name = "Average Annual Temp by Country")
	double avgAnnuTemp;
	
	//impact on growth rate in %
	@Variable(name = "impact of D2D Temp. Var. By Country")
	public double impactOfD2DVariOnGrowth;
	
	private static Action<Country> action(SerializableConsumer<Country> consumer) {
		return Action.create(Country.class, consumer);
	}
	
//	static Action<Country> sendGDP =
//			action(country -> country.getLinks(Links.ecoLink.class).send(Messages.gdpValue.class, country.GDP));
	
	static Action<Country> gdpGrowth =
			action(
					country -> {
						country.climateImpactedGrowth();
						country.avgAnnuTemp += country.getGlobals().avgTempStep;
						country.sendGDPToUN();
					}
			);
	
	void sendGDPToUN() {
		getLinks(Links.ecoLink.class).send(Messages.gdpValue.class, GDP);
	}
	
	void climateImpactedGrowth() {
//		if (hasMessageOfType(Messages.temperature.class)) {
//			double avgTemp = getMessagesOfType(Messages.temperature.class).get(0).avgTemp;
//			avgTemp = getGlobals().avgTemp;
//			double varTemp = getMessagesOfType(Messages.temperature.class).get(0).varTemp;
			double varTemp = getGlobals().varTemp;
			gdpGrowth(varTemp, getGlobals().avgTempStep);//, avgAnnualTempLast);
//			avgAnnualTempLast = avgAnnualTemp;
//		}
	}
	
	
	void gdpGrowth(double varTemp, double avgTempStep) {//, double avgTempLast) {
//		double avgTempVar = avgTemp - avgTempLast;
		double avgTempImpact = -0.001625 * avgTempStep + 0.00875;
		avgTempImpact += getPrng().normal(0, 0.1).sample();
		double coeff = 1 + (compGrowth + avgTempImpact);
		println(coeff);
		this.GDP *= coeff;
	}
	
	
}



