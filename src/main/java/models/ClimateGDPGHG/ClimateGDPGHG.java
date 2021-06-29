package models.ClimateGDPGHG;

import simudyne.core.abm.AgentBasedModel;
import simudyne.core.abm.GlobalState;
import simudyne.core.abm.Group;
import simudyne.core.annotations.Constant;
import simudyne.core.annotations.ModelSettings;
import simudyne.core.annotations.Variable;
import simudyne.core.data.CSVSource;

import static HZ_util.Print.println;

//each marcoStep = 3 * timeStep = 3 MONTHS
@SuppressWarnings("CommentedOutCode")
@ModelSettings(macroStep = 1L, start = "2021-06-01T00:00:00Z", timeStep = 1L, timeUnit = "YEARS", ticks = 60L)
public class ClimateGDPGHG extends AgentBasedModel<ClimateGDPGHG.Globals> {
	
	@Constant
	public int nbUN = 1;
	
	
	@Override
	public void init() {

//	createDoubleAccumulator("avgTemp", "Average Temperature");
//		createDoubleAccumulator("varTempAccu", "Variation of Temperature");
		createDoubleAccumulator("avgGlobalTempAccu", "Average Global Temperature");
		createDoubleAccumulator("globalGDPAccu", "Global GDP");
		createDoubleAccumulator("globalGHGAccu", "Global GHG");
		
		registerAgentTypes(Country.class, UN.class);
		registerLinkTypes(Links.ecoLink.class);
	}
	
	@Override
	public void setup() {
//		EmpiricalDistribution initGDP = getContext().getPrng().empiricalFromSource(new CSVSource("data/gdp-distribution.csv"));
		CSVSource CountryInitial = new CSVSource("data/countryInit.csv");
		Group<UN> unGroup = generateGroup(UN.class, nbUN, un -> un.globalGDP = 0);
//		Init Country agents at 2020 w/ CSV source data
		Group<Country> countryGroup = loadGroup(Country.class, CountryInitial,
				country -> {
					println(country.code + "\t" + country.GDP);
					println(country.avgAnnuTemp + "\t" + country.tempStepRatio);
				}
		);
		unGroup.fullyConnected(countryGroup, Links.ecoLink.class);
		countryGroup.fullyConnected(unGroup, Links.ecoLink.class);
		super.setup();
		
	}
	
	@Override
	public void step() {
		super.step();
//		Within each step, Country receives Temoerature data,
//		Produce GDP growth incl. the temperature impact
		run(Country.gdpGrowth, UN.updateStat);
		getGlobals().avgTemp += getGlobals().avgTempStep;
		getDoubleAccumulator("avgGlobalTempAccu").add(getGlobals().avgTemp);
	}
	
	public static final class Globals extends GlobalState {
		@Variable(name = "Average Global Temperature")
		public double avgTemp = 15.64;
		@Constant(name = "Average Global Warming Step")
		public double avgTempStep = 0.1;
		@Variable(name = "D2D Variation of Temperature")
		public double varTemp = 1.5;
	}
}
