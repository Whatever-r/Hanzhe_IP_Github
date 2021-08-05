package models.ClimateKaya;

import simudyne.core.abm.Action;
import simudyne.core.abm.Agent;
import simudyne.core.annotations.Constant;
import simudyne.core.annotations.Variable;
import simudyne.core.functions.SerializableConsumer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Country extends Agent<ClimateKaya.Globals> {
	// At most 1x GDP adoption for every 5 year
	long gdpEvolvePeriod = 5;
	// Even possibility to initiate 1st adoption
	int[] gdpStart = new int[(int) gdpEvolvePeriod];
	double[] gdpPoss = new double[(int) gdpEvolvePeriod];
	int gdpStartTick;
	// At most 1x tech adoption for every 3 year
	long techEvolvePeriod = 3;
	int[] epgStart = new int[(int) techEvolvePeriod];
	double[] epgPoss = new double[(int) techEvolvePeriod];
	int epgStartTick;
	int[] epeStart = new int[(int) techEvolvePeriod];
	double[] epePoss = new double[(int) techEvolvePeriod];
	int epeStartTick;
	
	void initEvoStart() {
		gdpStart = getStart(gdpEvolvePeriod);
		gdpPoss = getPoss(gdpEvolvePeriod);
		gdpStartTick = getPrng().enumeratedInteger(gdpStart, gdpPoss).sample();
		epgStart = getStart(techEvolvePeriod);
		epgPoss = getPoss(techEvolvePeriod);
		epgStartTick = getPrng().enumeratedInteger(epgStart, epgPoss).sample();
	}
	
	double[] getPoss(long period) {
		double[] ret = new double[(int) period];
		Arrays.fill(ret, 1.0 / period);
		return ret;
	}
	
	int[] getStart(long period) {
		int[] ret = new int[(int) period];
		for (int i = 0; i < ret.length; i++)
			ret[i] = i;
		return ret;
	}
	
	;
	
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
	
	@Variable(initializable = true, name = "Average Annual Temp by Country")
	double avgAnnuTemp;
	@Constant(name = "Ratio of Local Temp Growth to Global Avg.")
	double tempStepRatio;
	@Variable(initializable = true, name = "GDP by Country USD")
	double gdp;
	@Variable(initializable = true, name = "Population")
	double population;
	/**
	 * GDP per Capita parameters
	 */
	@Variable(initializable = true, name = "Initial GDP per Capita $ per person")
	double gdpPerCapitaRef;
	double gdpPerCapitaStep;
	// Parameter for prediction
	@Constant(name = "GDP per Capita Count")
	double gdpPerCapitaCount;
	@Variable(initializable = true, name = "GDP per Capita Mu")
	double gdpPerCapitaMu;
	@Variable(initializable = true, name = "GDP per Capita K^2")
	double gdpPerCapitaK2;
	// GDP pp Evolve related
	long gdpPerCapitaLU = 0;    // Last update year
	double gdpPerCapitaTarget = gdpPerCapitaStep;
	double gdpPerCapitaEvoCoeff = gdpPerCapitaMu;
	boolean gdpPerCapitaInProgress = false;
	/**
	 * Energy per GDP parameters
	 */
	@Variable(initializable = true, name = "Energy per GDP kWh/$")
	double energyPerGdpRef;
	double energyPerGdpStep;
	// Parameter for prediction
	@Constant(name = "Energy per GDP Count")
	double energyPerGdpCount;
	@Variable(initializable = true, name = "Energy per GDP Mu")
	double energyPerGdpMu;
	@Variable(initializable = true, name = "Energy per GDP K^2")
	double energyPerGdpK2;
	// Tech Evolve related
	long energyPerGdpLU = 0;                          // Last update year
	double energyPerGdpTarget = energyPerGdpStep;     // target adoption value
	double energyPerGdpAdoptCoeff = energyPerGdpMu;   // avg exponential adoption coeff
	boolean energyPerGdpInProgress = false;           // adoption in progress or not
	/**
	 * Emission per Energy parameters
	 */
	@Variable(initializable = true, name = "Emission per Energy tCO2e/TWh")
	double emisPerEnergyRef;
	double emisPerEnergyStep;
	@Constant(name = "Emission per Energy Count")
	double emisPerEnergyCount;
	@Variable(initializable = true, name = "Emission per Energy Mu")
	double emisPerEnergyMu;
	@Variable(initializable = true, name = "Emission per Energy K^2")
	double emisPerEnergyK2;
	// Tech Evolve related
	long emisPerEnergyLU = 0;                           // Last update year
	double emisPerEnergyTarget = emisPerEnergyStep;     // target adoption value
	double emisPerEnergyAdoptCoeff = emisPerEnergyMu;   // avg exponential adoption coeff
	boolean emisPerEnergyInProgress = false;            // adoption in progress or not
	
	
	private static Action<Country> action(SerializableConsumer<Country> consumer) {
		return Action.create(Country.class, consumer);
	}
	
	/**
	 * Prune Inter-country Link by group
	 */
	static Action<Country> SendGroup =
			action(Country::sendGroupInfo);
	
	void sendGroupInfo() {
		getLinks(Links.INTLLink.class).send(Messages.isGroupMem.class,
				((isGroupMem, interLink) -> {
					isGroupMem.isG7 = G7;
					isGroupMem.isG20 = G20;
					isGroupMem.isOECD = OECD;
				}));
	}
	
	static Action<Country> PruneLink =
			action(Country::prune);
	
	void prune() {
		getMessagesOfType(Messages.isGroupMem.class).forEach(msg -> {
			if (!msg.isG7) removeLinksTo(msg.getSender(), Links.G7Link.class);
			if (!msg.isG20) removeLinksTo(msg.getSender(), Links.G20Link.class);
			if (!msg.isOECD) removeLinksTo(msg.getSender(), Links.OECDLink.class);
		});
	}
	
	/**
	 * Calc GDP, Emission
	 */
	static Action<Country> GdpGrowth =
			action(
					country -> {
//						update Avg Temp
						country.UpdateAvgTemp();
//						update Step Variables
						country.UpdateStepVar();
//						calc GDP w/o Marginal Loss
						country.KayaGdp();
//						calc & send Emission
						country.SendGHGToUN(country.calcEmission());
//						calc Lossed GDP & Send
						country.SendGDPToUN(country.MarginalGdpLossCoeff());
					}
			);
	
	void SendGDPToUN(double lossCoeff) {
		getLinks(Links.UNLink.class).send(Messages.GdpValue.class, gdp * lossCoeff);
	}
	
	void SendGHGToUN(double emission) {
		getLinks(Links.UNLink.class).send(Messages.GhgEmission.class, emission);
	}
	
	void UpdateAvgTemp() {
		avgAnnuTemp += getGlobals().avgTempStep * tempStepRatio;
	}
	
	double MarginalGdpLossCoeff() {
		/**Marginal Warming impact w.r.t to local annual average temperature*/
		double localTempStep = getGlobals().avgTempStep * tempStepRatio;
		double avgTempImpact = (-0.001375 * avgAnnuTemp + 0.01125) * localTempStep;
		avgTempImpact += getPrng().normal(0, 0.015).sample();
		return (1 + avgTempImpact);
	}
	
	void UpdateStepVar() {
		getGdpPerCapita();
		getEnergyPerGdp();
		getEmisPerEnergy();
	}
	
	void KayaGdp() {
		/**GDP per Capita * Population projection * Marginal Loss*/
		long year = getContext().getTick();
		population = getGlobals().populationHash.get(code).get(year);
		this.gdp = this.gdpPerCapitaStep * population;
	}
	
	double calcEmission() {
		//                 USD * kWh per USD / 10ˆ9 to Trillion Wh
		double energyTWh = gdp * energyPerGdpStep / Math.pow(10, 9);
		//   Trillion Wh * tCO2e per TWh
		return energyTWh * emisPerEnergyStep;
	}
	
	//USD per capita
	void getGdpPerCapita() {
		long currYear = getContext().getTick();
		double tau = currYear - gdpPerCapitaLU;
//		if is in progress of adopting from other country
		if (gdpPerCapitaInProgress && tau <= gdpEvolvePeriod) {
			double avg = Math.log(gdpPerCapitaRef) / Math.log(2) + tau * gdpPerCapitaEvoCoeff;
			double stdev = Math.max(0.000001, gdpPerCapitaEvoCoeff / 50);
			double exp = getPrng().normal(avg, stdev).sample();
			this.gdpPerCapitaStep = Math.pow(2, exp);
			//at the end of adoption, update the ref value
			if (tau == gdpEvolvePeriod) {
				gdpPerCapitaRef = gdpPerCapitaStep;
				gdpPerCapitaInProgress = false;
				getLongAccumulator("gdpPerCapitaFin").add(1L);
			}
		} else {
			double Astar = tau + (tau * tau) / gdpPerCapitaCount;
			double avg = Math.log(gdpPerCapitaRef) / Math.log(2) + tau * gdpPerCapitaMu;
			//avoid 0 stdev error
			double stdevSquare = Math.max(0.000001, gdpPerCapitaK2 * Astar);
			double exp = getPrng().normal(avg, Math.sqrt(stdevSquare)).sample();
			this.gdpPerCapitaStep = Math.pow(2, exp);
		}
	}
	
	//kWh per USD
	void getEnergyPerGdp() {
		if (!getGlobals().techBoolean) {
			return;
		}
		long currYear = getContext().getTick();
		double tau = currYear - energyPerGdpLU;
		// Improve the parameter aiming to achieve the target value at the end of period
		if (energyPerGdpInProgress && tau <= techEvolvePeriod) {
			double avg = Math.log(energyPerGdpRef) / Math.log(2) + tau * energyPerGdpAdoptCoeff;
			double stdev = Math.max(0.000001, energyPerGdpAdoptCoeff / 50);
			double exp = getPrng().normal(avg, stdev).sample();
			this.energyPerGdpStep = Math.pow(2, exp);
//			at the end of adoption, update the ref value
			if (tau == techEvolvePeriod) {
				energyPerGdpRef = energyPerGdpStep;
				energyPerGdpInProgress = false;
				getLongAccumulator("energyPerGdpFin").add(1L);
			}
		} else {
			double Astar = tau + (tau * tau) / energyPerGdpCount;
			double avg = Math.log(energyPerGdpRef) / Math.log(2) + tau * energyPerGdpMu;
			double stdevSquare = Math.max(0.000001, energyPerGdpK2 * Astar);
			double exp = getPrng().normal(avg, Math.sqrt(stdevSquare)).sample();
			this.energyPerGdpStep = Math.pow(2, exp);
		}
	}
	
	//	tCO2e per Trillion Wh
	void getEmisPerEnergy() {
		if (!getGlobals().techBoolean) {
			return;
		}
		long currYear = getContext().getTick();
		double tau = currYear - emisPerEnergyLU;
//		if is in technology adoption period
		if (emisPerEnergyInProgress && tau <= techEvolvePeriod) {
			double avg = Math.log(emisPerEnergyRef) / Math.log(2) + tau * emisPerEnergyAdoptCoeff;
			double stdev = Math.max(0.000001, emisPerEnergyAdoptCoeff / 50);
			double exp = getPrng().normal(avg, stdev).sample();
			this.emisPerEnergyStep = Math.pow(2, exp);
//			at the end of adoption, update the ref value
			if (tau == techEvolvePeriod) {
				emisPerEnergyInProgress = false;
				emisPerEnergyRef = emisPerEnergyStep;
				getLongAccumulator("emisPerEnergyFin").add(1L);
			}
		} else {
			double Astar = tau + (tau * tau) / emisPerEnergyCount;
			double avg = Math.log(emisPerEnergyRef) / Math.log(2) + tau * emisPerEnergyMu;
			double stdevSquare = emisPerEnergyK2 * Astar;
			if (stdevSquare <= 0) stdevSquare = 0.000001;
			double exp = getPrng().normal(avg, Math.sqrt(stdevSquare)).sample();
			this.emisPerEnergyStep = Math.pow(2, exp);
		}
	}
	
	/**
	 * Share & Improve GDP per Capita
	 */
	static Action<Country> ShareGdpPerCapita = action(Country::ShareGdpPerCapita);
	static Action<Country> ImproveGdpPerCapita = action(Country::ImproveGdpPerCapita);
	
	void ShareGdpPerCapita() {
		long currTick = getContext().getTick();
		if (!getGlobals().gdpShareOpt)
			return;
		if (currTick != gdpStartTick && currTick <= gdpEvolvePeriod)
			return;
		if (getGlobals().techShareOpt == 1)
			getLinks(Links.G7Link.class).send(Messages.GdpPerCapitaMsg.class, gdpPerCapitaStep);
		else if (getGlobals().techShareOpt == 2)
			getLinks(Links.G20Link.class).send(Messages.GdpPerCapitaMsg.class, gdpPerCapitaStep);
		else if (getGlobals().techShareOpt == 3)
			getLinks(Links.INTLLink.class).send(Messages.GdpPerCapitaMsg.class, gdpPerCapitaStep);
	}
	
	void ImproveGdpPerCapita() {
		if (!gdpPerCapitaInProgress && hasMessageOfType(Messages.GdpPerCapitaMsg.class)) {
//		Read & Sort
			List<Messages.GdpPerCapitaMsg> gdpMsgList = getMessagesOfType(Messages.GdpPerCapitaMsg.class);
			List<Double> gdpPerCapitaList = new ArrayList<>();
			gdpMsgList.forEach(gdp -> gdpPerCapitaList.add(gdp.getBody()));
			Collections.sort(gdpPerCapitaList);
			// Get expected GDP pp level after the evolution period
			// and set corresponding values
			// Target value is the minimal one which is better than the expected self-developmemt result
			long currTick = getContext().getTick();
			if (currTick == gdpStartTick || currTick - gdpPerCapitaLU > gdpEvolvePeriod) {
				double gdpPerCapitaExpect = Math.pow(2, Math.log(gdpPerCapitaStep) / Math.log(2)
						+ gdpEvolvePeriod * gdpPerCapitaMu);
				gdpPerCapitaTarget = gdpPerCapitaExpect;
				for (int i = 1; i < gdpPerCapitaList.size(); i++) {
					if (gdpPerCapitaList.get(i) >= gdpPerCapitaExpect && gdpPerCapitaList.get(i - 1) < gdpPerCapitaExpect) {
						gdpPerCapitaTarget = gdpPerCapitaList.get(i);
						gdpPerCapitaEvoCoeff = (Math.log(gdpPerCapitaTarget) - Math.log(gdpPerCapitaStep))
								/ (Math.log(2) * gdpEvolvePeriod);
						gdpPerCapitaLU = currTick;
						gdpPerCapitaRef = gdpPerCapitaStep;
						gdpPerCapitaInProgress = true;
						getLongAccumulator("gdpPerCapitaAccu").add(1L);
						break;
					}
				}
			}
		}
	}
	
	/**
	 * Share & Improve Technology
	 */
	static Action<Country> ShareTech = action(Country::ShareTech);
	static Action<Country> ImproveTech = action(Country::ImproveTech);
	
	//Share EmisPerEnergy & EnergyPerGDP to other countries
	void ShareTech() {
		if (!getGlobals().techBoolean)
			return;
		if (getGlobals().techShareOpt == 1)
			getLinks(Links.G7Link.class).send(
					Messages.Technology.class, (m, l) -> {
						m.emisPerEnergyMsg = emisPerEnergyStep;
						m.energyPerGdpMsg = energyPerGdpStep;
					});
		
		else if (getGlobals().techShareOpt == 2)
			getLinks(Links.G20Link.class).send(
					Messages.Technology.class, (m, l) -> {
						m.emisPerEnergyMsg = emisPerEnergyStep;
						m.energyPerGdpMsg = energyPerGdpStep;
					});
		
		else if (getGlobals().techShareOpt == 3)
			getLinks(Links.INTLLink.class).send(
					Messages.Technology.class, (m, l) -> {
						m.emisPerEnergyMsg = emisPerEnergyStep;
						m.energyPerGdpMsg = energyPerGdpStep;
					});
	}
	
	//Acquire EmisPerEnergy & EnergyPerGDP value, get target value and set own tech-improvement step
	void ImproveTech() {
		if (hasMessageOfType(Messages.Technology.class)) {
//		Read & Sort
			List<Messages.Technology> techMsgList = getMessagesOfType(Messages.Technology.class);
			List<Double> emisPerEnergyList = new ArrayList<>();
			List<Double> energyPerGdpList = new ArrayList<>();
			techMsgList.forEach(tech -> {
				emisPerEnergyList.add(tech.emisPerEnergyMsg);
				energyPerGdpList.add(tech.energyPerGdpMsg);
			});
			Collections.sort(emisPerEnergyList);
			Collections.sort(energyPerGdpList);
			// Get expected technology level after the evolution period
			// Target value is the minimal one which is better than the
			// expected self-developmemt result after the period
			long currTick = getContext().getTick();
			if (!emisPerEnergyInProgress &&
					(currTick == epeStartTick || currTick - emisPerEnergyLU > techEvolvePeriod)) {
				double emisPerEnergyExpect = Math.pow(2, Math.log(emisPerEnergyStep) / Math.log(2)
						+ techEvolvePeriod * emisPerEnergyMu);
				emisPerEnergyTarget = emisPerEnergyExpect;
				for (int i = 1; i < emisPerEnergyList.size(); i++) {
					if (emisPerEnergyList.get(i) >= emisPerEnergyExpect && emisPerEnergyList.get(i - 1) < emisPerEnergyExpect) {
						emisPerEnergyTarget = emisPerEnergyList.get(i - 1);
						//assume avg exponential improvement coeff
						emisPerEnergyAdoptCoeff = (Math.log(emisPerEnergyTarget) - Math.log(emisPerEnergyStep))
								/ (Math.log(2) * techEvolvePeriod);
						//
						emisPerEnergyLU = currTick;
						emisPerEnergyRef = emisPerEnergyStep;
						emisPerEnergyInProgress = true;
						getLongAccumulator("emisPerEnergyAccu").add(1L);
						break;
					}
				}
			}
			if (!energyPerGdpInProgress &&
					(currTick == epgStartTick || currTick - energyPerGdpLU > techEvolvePeriod)) {
				double energyPerGdpExpect = Math.pow(2, Math.log(energyPerGdpStep) / Math.log(2)
						+ techEvolvePeriod * energyPerGdpMu);
				energyPerGdpTarget = energyPerGdpExpect;
				for (int i = 1; i < energyPerGdpList.size(); i++) {
					if (energyPerGdpList.get(i) >= energyPerGdpExpect && energyPerGdpList.get(i - 1) < energyPerGdpExpect) {
						energyPerGdpTarget = energyPerGdpList.get(i - 1);
						energyPerGdpAdoptCoeff = (Math.log(energyPerGdpTarget) - Math.log(energyPerGdpStep))
								/ (Math.log(2) * techEvolvePeriod);
						energyPerGdpLU = currTick;
						energyPerGdpRef = energyPerGdpStep;
						energyPerGdpInProgress = true;
						getLongAccumulator("energyPerGdpAccu").add(1L);
						break;
					}
				}
			}
		}
	}
}





