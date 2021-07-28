package models.ClimateKaya;

import simudyne.core.abm.Action;
import simudyne.core.abm.Agent;
import simudyne.core.annotations.Constant;
import simudyne.core.annotations.Variable;
import simudyne.core.functions.SerializableConsumer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Country extends Agent<ClimateKaya.Globals> {
	@Constant(name = "C/R Code")
	String code;
	@Constant(name = "Name of Country/Region")
	String name;
	@Constant(name = "G7 Member")
	boolean G7;
	@Constant(name = "G20 Member")
	boolean G20;
	@Constant(name = "OECD Member")
	boolean OECD;
	
	@Variable(initializable = true, name = "Compound Growth Rate%") // Base Compound Growth in %
	double compGrowth;
	@Variable(initializable = true, name = "Average Annual Temp by Country")
	double avgAnnuTemp;
	@Constant(name = "Ratio of Local Temp Growth to Global Avg.")
	double tempStepRatio;
	
	@Variable(initializable = true, name = "GDP by Country USD")
	double gdp;
	@Variable(initializable = true, name = "Population")
	double population;
	@Variable(name = "Step GDP per Capita")
	double gdpPercapitaSimutaneous;
	/**
	 * GDP per Capita parameters
	 */
	@Constant(name = "Initial GDP per Capita $ per person")
	double gdpPerCapita;
	@Constant(name = "GDP per Capita Count")
	double gdpPerCapitaCount;
	@Variable(initializable = true, name = "GDP per Capita Mu")
	double gdpPerCapitaMu;
	@Variable(initializable = true, name = "GDP per Capita K^2")
	double gdpPerCapitaK2;
	/**
	 * Energy per GDP parameters
	 */
	@Constant(name = "Energy per GDP kWh/$")
	double energyPerGdp;
	@Constant(name = "Energy per GDP Count")
	double energyPerGdpCount;
	@Variable(initializable = true, name = "Energy per GDP Mu")
	double energyPerGdpMu;
	@Variable(initializable = true, name = "Energy per GDP K^2")
	double energyPerGdpK2;
	/**
	 * Emission per Energy parameters
	 */
	@Constant(name = "Emission per Energy tCO2e/TWh")
	double emisPerEnergy;
	@Constant(name = "Emission per Energy Count")
	double emisPerEnergyCount;
	@Variable(initializable = true, name = "Emission per Energy Mu")
	double emisPerEnergyMu;
	@Variable(initializable = true, name = "Emission per Energy K^2")
	double emisPerEnergyK2;
	
	@Variable(initializable = true, name = "Unit GHG Emission / GDP, tCO2e/$M ")
	double unitGHG;
	
	
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
	
	static Action<Country> gdpGrowth =
			action(
					country -> {
//						country.climateImpactedGdpGrowth();
						country.KayaGdp();
						country.sendGDPToUN();
						country.sendGHGToUN(country.calcEmission());
						country.updateAvgTemp();
					}
			);
	
	void sendGDPToUN() {
		getLinks(Links.UNLink.class).send(Messages.gdpValue.class, gdp);
	}
	
	void sendGHGToUN(double emission) {
		getLinks(Links.UNLink.class).send(Messages.ghgEmission.class, emission);
	}
	
	void updateAvgTemp() {
		avgAnnuTemp += getGlobals().avgTempStep * tempStepRatio;
	}
	
	void KayaGdp() {
		/**GDP per Capita * Population projection*/
		long year = getContext().getTick();
		this.gdpPercapitaSimutaneous = getGdpPerCapita();
		population = getGlobals().populationHash.get(code).get(year);
		/**Marginal Warming impact w.r.t to local annual average temperature*/
		double localTempStep = getGlobals().avgTempStep * tempStepRatio;
		double avgTempImpact = (-0.001375 * avgAnnuTemp + 0.01125) * localTempStep;
		avgTempImpact += getPrng().normal(0, 0.01).sample();
		this.gdp = this.gdpPercapitaSimutaneous * population * (1 - avgTempImpact);
	}
	
	void climateImpactedGdpGrowth() {
		/**Marginal Warming impact w.r.t to local annual average temperature*/
		double localTempStep = getGlobals().avgTempStep * tempStepRatio;
		double avgTempImpact = (-0.001375 * avgAnnuTemp + 0.01125) * localTempStep;
		avgTempImpact += getPrng().normal(0, 0.01).sample();
		double coeff = 1 + (compGrowth + avgTempImpact);
		this.gdp *= coeff;
	}
	
	double calcEmission() {
		double energyTWh = gdp * getEnergyPerGdp() / Math.pow(10, 9);
		return energyTWh * getEmisPerEnergy();
	}
	
	double getGdpPerCapita() {
		double tau = getContext().getTick();
		double Astar = tau + (tau * tau) / gdpPerCapitaCount;
		double avg = Math.log(gdpPerCapita) / Math.log(2) + tau * gdpPerCapitaMu;
		double stdevSquare = gdpPerCapitaK2 * Astar;
		if (stdevSquare <= 0) stdevSquare = 0.000001;
		double exp = getPrng().normal(avg, Math.sqrt(stdevSquare)).sample();
//		println(exp);
		return Math.pow(2, exp);
	}
	
	double getEnergyPerGdp() {
		double tau = getContext().getTick();
		double Astar = tau + (tau * tau) / energyPerGdpCount;
		double avg = Math.log(energyPerGdp) / Math.log(2) + tau * energyPerGdpMu;
		double stdevSquare = energyPerGdpK2 * Astar;
		if (stdevSquare <= 0) stdevSquare = 0.000001;
		double exp = getPrng().normal(avg, Math.sqrt(stdevSquare)).sample();
//		println(exp);
		return Math.pow(2, exp);
	}
	
	double getEmisPerEnergy() {
		double tau = getContext().getTick();
		double Astar = tau + (tau * tau) / emisPerEnergyCount;
		double avg = Math.log(emisPerEnergy) + tau * emisPerEnergyMu;
		double stdevSquare = emisPerEnergyK2 * Astar;
		if (stdevSquare <= 0) stdevSquare = 0.000001;
		double exp = getPrng().normal(avg, Math.sqrt(stdevSquare)).sample();
//		println(exp);
		return Math.pow(2, exp);
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
			List<Double> doubleList = new ArrayList<>();
			list.forEach(msg -> doubleList.add(msg.getBody()));
			
			Collections.sort(doubleList);
			if (doubleList.get(0) >= unitGHG) return;
			
			for (int i = 1; i < doubleList.size(); i++) {
				if (doubleList.get(i) >= unitGHG && doubleList.get(i - 1) < unitGHG) {
					unitGHG = doubleList.get(i - 1);
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




