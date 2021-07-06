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
@ModelSettings(macroStep = 1L, start = "2020-06-01T00:00:00Z", timeStep = 1L, timeUnit = "YEARS", ticks = 60L)
public class ClimateGDPGHG extends AgentBasedModel<ClimateGDPGHG.Globals> {
	
	@Constant
	public int nbUN = 1;
	
	public static final class Globals extends GlobalState {
		@Variable(name = "Average Global Temperature")
		public double avgTemp = 15.64;
		@Constant(name = "Average Global Warming Step")
		public double avgTempStep = 0.1;
		@Constant(name = "Tech Improve")
		public double techImprove = 1;
		@Constant(name = "Share Tech, 0-N 1-G7 2-G20 3-ITNL")
		public int unitGHGShare = 0;
		//		@Constant(name = "growth decay coeff.")
//		public double decay = 0.001;
		@Variable(name = "D2D Variation of Temperature")
		public double varTemp = 1.5;
	}
	
	@Override
	public void init() {

//	createDoubleAccumulator("avgTemp", "Average Temperature");
//		createDoubleAccumulator("varTempAccu", "Variation of Temperature");
		createDoubleAccumulator("avgGlobalTempAccu", "Average Global Temperature");
		getDoubleAccumulator("avgGlobalTempAccu").add(getGlobals().avgTemp);
		createDoubleAccumulator("globalGDPAccu", "Global GDP");
		getDoubleAccumulator("globalGDPAccu").add(Double.NaN);
		createDoubleAccumulator("globalGHGAccu", "Global GHG");
		getDoubleAccumulator("globalGHGAccu").add(Double.NaN);
		
		registerAgentTypes(Country.class, UN.class);
		registerLinkTypes(Links.UNLink.class,
				Links.InterLink.class,
				Links.G7Link.class,
				Links.G20Link.class,
				Links.OECDLink.class
		);
		
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
		countryGroup.partitionConnected(unGroup, Links.UNLink.class);
		countryGroup.fullyConnected(countryGroup, Links.InterLink.class);
		countryGroup.fullyConnected(countryGroup, Links.G7Link.class);
		countryGroup.fullyConnected(countryGroup, Links.G20Link.class);
		countryGroup.fullyConnected(countryGroup, Links.OECDLink.class);
		super.setup();
		
	}
	
	@Override
	public void step() {
		super.step();
		if (getContext().getTick() == 0) {
			run(Country.sendGroup, Country.pruneLink);
		}
//		Within each step, Country receives Temoerature data,
//		Produce GDP growth incl. the temperature impact
		run(Country.gdpGrowth, UN.updateStat);
		run(Country.shareTech, Country.improveTech);
		getGlobals().avgTemp += getGlobals().avgTempStep;
		getDoubleAccumulator("avgGlobalTempAccu").add(getGlobals().avgTemp);
	}
	
	
}
