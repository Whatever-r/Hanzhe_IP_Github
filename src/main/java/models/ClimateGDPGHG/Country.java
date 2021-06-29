package models.ClimateGDPGHG;

import simudyne.core.abm.Action;
import simudyne.core.abm.Agent;
import simudyne.core.annotations.Constant;
import simudyne.core.annotations.Variable;
import simudyne.core.functions.SerializableConsumer;

import static HZ_util.Print.println;

public class Country extends Agent<ClimateGDPGHG.Globals> {
	
	@Constant(name = "C/R Code")
	String code;
	@Constant(name = "Name of Country/Region")
	String name;
	
	@Variable(initializable = true, name = "GDP by Country, $M")
	double GDP;
	
	@Variable(initializable = true, name = "Compound Growth Rate%") // Base Compound Growth in %
	double compGrowth;
	
	@Variable(initializable = true, name = "Unit GHG Emission / GDP, tCO2e/$M ")
	double unitGHG;
	
	@Variable(initializable = true, name = "Average Annual Temp by Country")
	double avgAnnuTemp;
	
	@Constant(name = "Ratio of Local Temp Growth to Global Avg.")
	double tempStepRatio;
	
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
						country.updateAvgTemp();
						country.sendGDPToUN();
						country.sendGHGToUN();
					}
			);
	
	void sendGDPToUN() {
		getLinks(Links.ecoLink.class).send(Messages.gdpValue.class, GDP);
	}
	
	void sendGHGToUN() {
		getLinks(Links.ecoLink.class).send(Messages.ghgEmission.class, GDP * unitGHG);
	}
	
	void updateAvgTemp() {
		avgAnnuTemp += getGlobals().avgTempStep * tempStepRatio;
	}
	
	
	void climateImpactedGrowth() {
//		if (hasMessageOfType(Messages.temperature.class)) {
//			double avgTemp = getMessagesOfType(Messages.temperature.class).get(0).avgTemp;
//			avgTemp = getGlobals().avgTemp;
		double varTemp = getGlobals().varTemp;
		gdpGrowth(getGlobals().avgTempStep * tempStepRatio);
	}
	
	void gdpGrowth(double localTempStep) {//, double avgTempLast) {
//		Marginal Warming impact w.r.t to local annual average temperature
		double avgTempImpact = (-0.001375 * avgAnnuTemp + 0.01125) * localTempStep;
		avgTempImpact += getPrng().normal(0, 0.01).sample();
		double coeff = 1 + (compGrowth + avgTempImpact);
		println(coeff);
		this.GDP *= coeff;
	}
	
	
}



