package models.Climate0622;

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
//		EmpiricalDistribution initGDP = getContext().getPrng().empiricalFromSource(new CSVSource("data/gdp-distribution.csv"));
		CSVSource CountryInitial = new CSVSource("data/countryInit.csv");
		
		Group<UN> unGroup = generateGroup(UN.class, nbUN, un -> un.globalGDP = 0);

//		Init Country agents at 2020 w/ CSV source data
		Group<Country> countryGroup = loadGroup(Country.class, CountryInitial,
				country -> {
//					random initial avg tempreature for now
//					country.avgAnnuTemp = getContext().getPrng().discrete(-10, 30).sample();
//					country.GDP = country.initGDP;
					println(country.name);
					println(country.GDP);
					println(country.avgAnnuTemp);
//					country.compGrowth = Ωcountry.initCompGrowth;
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
//		if (getContext().getTick() == 0) {
//			AgentStatistics<Country> countryAgentStatistics = stats(Country.class);
//			countryAgentStatistics.field("Sum", country -> country.GDP);
//			AgentStatisticsResult<Country> countryAgentStatisticsResult = countryAgentStatistics.get();
//			getDoubleAccumulator("globalGDPAccu").add(countryAgentStatisticsResult.getField("Sum").getSum());
//		}
//		Within each step, Country receives Temoerature data,
//		Produce GDP growth incl. the temperature impact
//		run(UN.sendTemp, Country.gdpGrowth, UN.updateGDP);
		run(Country.gdpGrowth, UN.updateGDP);
		getGlobals().varTemp += getContext().getPrng().normal(0, 0.01).sample();
		getDoubleAccumulator("varTempAccu").add(getGlobals().varTemp);
		getGlobals().avgTemp += getGlobals().avgTempStep;
		getDoubleAccumulator("avgGlobalTempAccu").add(getGlobals().avgTemp);
	}
	
	public static final class Globals extends GlobalState {
		@Variable(name = "Average Global Temperature")
		public double avgTemp = 15.64;
		@Variable(name = "Average Global Warming Step")
		public double avgTempStep = 0.1;
		@Variable(name = "D2D Variation of Temperature")
		public double varTemp = 1.5;
	}
}
