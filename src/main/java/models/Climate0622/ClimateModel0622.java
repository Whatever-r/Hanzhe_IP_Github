package models.Climate0622;

import static HZ_util.Print.*;
import org.apache.commons.math3.random.EmpiricalDistribution;
import simudyne.core.abm.AgentBasedModel;
import simudyne.core.abm.GlobalState;
import simudyne.core.abm.Group;
import simudyne.core.annotations.Constant;
import simudyne.core.annotations.ModelSettings;
import simudyne.core.annotations.Variable;
import simudyne.core.data.CSVSource;

//each marcoStep = 3 * timeStep = 3 MONTHS
@SuppressWarnings("CommentedOutCode")


@ModelSettings(macroStep = 1L, start = "2021-06-01T00:00:00Z", timeStep = 1L, timeUnit = "YEARS", ticks = 60L)
public class ClimateModel0622 extends AgentBasedModel<ClimateModel0622.Globals> {
	
	@Constant
	public int nbUN = 1;
//	@Constant
//	public int nbEconomy = 100;
//	public long seed = this.getConfig().getOrSetLong("core.prng-seed", System.nanoTime());
//	public SeededRandom prng = SeededRandom.create(seed);
	
	@Override
	public void init() {
//	createDoubleAccumulator("avgTemp", "Average Temperature");
		createDoubleAccumulator("varTempAccu", "Variation of Temperature");
		createDoubleAccumulator("avgGlobalTempAccu", "Average Global Temperature");
		createDoubleAccumulator("globalGDPAccu", "Global GDP");
		
		registerAgentTypes(Country.class, UN.class);
		registerLinkTypes(Links.ecoLink.class);
	}
	
	@Override
	public void setup() {
//		getDoubleAccumulator("avgTemp").add(18);
//		getDoubleAccumulator("varTemp").add(1.8);
//		SeededRandom prng = getContext().getPrng();
//		prng.generator.;
//		EmpiricalDistribution initGDP = getContext().getPrng().empiricalFromSource(new CSVSource("data/gdp-distribution.csv"));
		
		CSVSource CountryInitial = new CSVSource("data/GDP_by_Country_0622.csv");
		
		Group<UN> unGroup = generateGroup(UN.class, nbUN, un -> un.globalGDP = 0);
//		Group<Country> economyGroup = generateGroup(Country.class, nbEconomy,
//				eco -> {
//					eco.gdpValue = initGDP.sample();
////					base growth rate in % per month
//					eco.baseGrowth = (5 + getContext().getPrng().normal(0, 1).sample()) / 100.0 / 12;
////					impact of varTemp on the base growth rate in %
//					eco.impactOfD2DVariOnGrowth = (7.5 + getContext().getPrng().normal(0, 1).sample()) / 100.0;
//				});
		Group<Country> countryGroup = loadGroup(Country.class, CountryInitial,
				country -> {
//					random initial avg tempreature for now
					country.avgAnnualTemp = getContext().getPrng().discrete(-10, 30).sample();
					country.GDP = country.initGDP;
					println(country.GDP);
					country.compGrowth = country.initCompGrowth;
//					country.avgAnnualTempLast = country.avgAnnualTemp;
					
				}
		);
		unGroup.fullyConnected(countryGroup, Links.ecoLink.class);
		countryGroup.fullyConnected(unGroup, Links.ecoLink.class);
		super.setup();
		
	}
	
	@Override
	public void step() {
		super.step();
//		run(UN.sendTemp, Country.gdpGrowth, UN.updateGDP);
		run(Country.gdpGrowth, UN.updateGDP);
		getGlobals().varTemp += getContext().getPrng().normal(0, 0.01).sample();
		getDoubleAccumulator("varTempAccu").add(getGlobals().varTemp);
		getGlobals().avgTemp += getGlobals().avgTempStep;
		getDoubleAccumulator("avgGlobalTempAccu").add(getGlobals().avgTemp);
	}
	
	public class Globals extends GlobalState {
		@Variable(name = "Average GlobalTemperature")
		public double avgTemp = 18;
		@Variable
		public double avgTempStep = 0.1;
		@Variable(name = "D2D Variation of Temperature")
		public double varTemp = 1.5;
	}
}
