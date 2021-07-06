package models.ClimateGDPGHG;

import simudyne.core.abm.Action;
import simudyne.core.abm.Agent;
import simudyne.core.annotations.Constant;
import simudyne.core.annotations.Variable;
import simudyne.core.functions.SerializableConsumer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static HZ_util.Print.println;

public class Country extends Agent<ClimateGDPGHG.Globals> {
	
	@Constant(name = "G7 Member")
	boolean G7;
	@Constant(name = "G20 Member")
	boolean G20;
	@Constant(name = "OECD Member")
	boolean OECD;
	
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
	
	static Action<Country> sendGroup =
			action(Country::sendGroupInfo);
	
	void sendGroupInfo() {
		getLinks(Links.InterLink.class).send(Messages.isGroupMem.class,
				((isGroupMem, interLink) -> {
					isGroupMem.isG7 = G7;
					isGroupMem.isG20 = G20;
					isGroupMem.isOECD = OECD;
				}));
	}
	
	static Action<Country> pruneLink =
			action(Country::prune);
	
	void prune() {
		getMessagesOfType(Messages.isGroupMem.class).forEach(
				msg -> {
					if (!msg.isG7) removeLinksTo(msg.getSender(), Links.G7Link.class);
					if (!msg.isG20) removeLinksTo(msg.getSender(), Links.G20Link.class);
					if (!msg.isOECD) removeLinksTo(msg.getSender(), Links.OECDLink.class);
				}
		);
		
	}
	
	static Action<Country> sendGDP =
			action(country -> country.getLinks(Links.UNLink.class).send(Messages.gdpValue.class, country.GDP));
	
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
		getLinks(Links.UNLink.class).send(Messages.gdpValue.class, GDP);
	}
	
	void sendGHGToUN() {
		getLinks(Links.UNLink.class).send(Messages.ghgEmission.class, GDP * unitGHG);
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
	
	void gdpGrowth(double localTempStep) {
//		Marginal Warming impact w.r.t to local annual average temperature
		double avgTempImpact = (-0.001375 * avgAnnuTemp + 0.01125) * localTempStep;
		avgTempImpact += getPrng().normal(0, 0.01).sample();
		double coeff = 1 + (compGrowth + avgTempImpact);
		println(coeff);
		this.GDP *= coeff;
	}
	
	static Action<Country> shareTech = action(Country::shareTech);
	static Action<Country> improveTech = action(Country::improveUnitGHG);
	
	
	void improveUnitGHG() {
		if (hasMessageOfType(Messages.unitGHG.class)) {
			List<Messages.unitGHG> list = getMessagesOfType(Messages.unitGHG.class);
//			AtomicReference<Double> techImport = new AtomicReference<>(unitGHG);
//			AtomicReference<Double> tempDiff = new AtomicReference<>(unitGHG);
//			list.forEach(msg -> {
//				double msgVal = msg.getBody();
//				double temp = unitGHG - msgVal;
//				if (temp >= 0 && temp < tempDiff.get()) {
//					tempDiff.set(temp);
//					techImport.set(msgVal);
//				}
//			});
//			unitGHG = techImport.get();
			List<Double> doubles = new ArrayList<>();
			list.forEach(msg -> {
				doubles.add(msg.getBody());
			});
			
			Collections.sort(doubles);
			if (doubles.get(0) >= unitGHG) return;
			
			for (int i = 1; i < doubles.size(); i++) {
				if (doubles.get(i) >= unitGHG && doubles.get(i - 1) < unitGHG) {
					unitGHG = doubles.get(i - 1);
					return;
				}
			}
		}
	}
	
	void shareTech() {
		unitGHG *= getGlobals().techImprove;
		
		if (getGlobals().unitGHGShare == 1)
			getLinks(Links.G7Link.class).send(Messages.unitGHG.class, unitGHG);
		if (getGlobals().unitGHGShare == 2)
			getLinks(Links.G20Link.class).send(Messages.unitGHG.class, unitGHG);
		if (getGlobals().unitGHGShare == 3)
			getLinks(Links.InterLink.class).send(Messages.unitGHG.class, unitGHG);
	}
	
}





