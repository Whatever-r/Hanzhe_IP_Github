package models.ClimateKaya;

import static HZ_util.Print.println;

import simudyne.core.abm.AgentBasedModel;
import simudyne.core.abm.GlobalState;
import simudyne.core.abm.Group;
import simudyne.core.annotations.Constant;
import simudyne.core.annotations.ModelSettings;
import simudyne.core.annotations.Variable;
import simudyne.core.data.CSVSource;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.HashMap;



//each marcoStep = 3 * timeStep = 3 MONTHS
@SuppressWarnings("CommentedOutCode")
@ModelSettings(macroStep = 1L, start = "2019-06-01T00:00:00Z", timeStep = 1L, timeUnit = "YEARS", ticks = 60L)
public class ClimateKaya extends AgentBasedModel<ClimateKaya.Globals> {
	
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
		@Variable(name = "D2D Variation of Temperature")
		public double varTemp = 1.5;
		
		public HashMap<String, HashMap<Integer, Integer>> populationHash = null;
	}
	
	/**Read Population CSV*/
	
	@Override
	public void init() {
		createDoubleAccumulator("avgGlobalTempAccu", "Average Global Temperature");
		getDoubleAccumulator("avgGlobalTempAccu").add(getGlobals().avgTemp);
		createDoubleAccumulator("globalGDPAccu", "Global GDP");
		getDoubleAccumulator("globalGDPAccu").add(Double.NaN);
		createDoubleAccumulator("globalGHGAccu", "Global GHG");
		getDoubleAccumulator("globalGHGAccu").add(Double.NaN);
		
		createDoubleAccumulator("SolarPrice", "Solar Price W/US$2019");
		getDoubleAccumulator("SolarPrice").add(0.37725);
		
		registerAgentTypes(Country.class, UN.class, SolarPV.class);
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
		CSVSource CountryInitial = new CSVSource("data/Kaya Metric.csv");
		getGlobals().populationHash = getPolulationList("data/projected-population-by-country.csv");
		Group<UN> unGroup = generateGroup(UN.class, nbUN, un -> un.globalGDP = 0);
//		Init Country agents at 2020 w/ CSV source data
		Group<Country> countryGroup = loadGroup(Country.class, CountryInitial,
				country -> {
					 println(getGlobals().populationHash.get(country.code)+
							 "\t"+getGlobals().populationHash.get(country.code).get(0));
				}
		);
		Group<SolarPV> solarPVGroup = generateGroup(SolarPV.class, 1);
		
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
		run(SolarPV.updatePrice);
		getGlobals().avgTemp += getGlobals().avgTempStep;
		getDoubleAccumulator("avgGlobalTempAccu").add(getGlobals().avgTemp);
	}
	
	/**
	 * Helper, Read Population Projection by Country into Hashmap<code, Hashmap<year, value>>
	 */
	public static HashMap<String, HashMap<Integer, Integer>> getPolulationList(String path) {
		HashMap<String, HashMap<Integer, Integer>> retHashMap = new HashMap<>();
		try {
			BufferedReader reader = new BufferedReader(new FileReader(path));
			String[] headtilte = reader.readLine().
					split(",");// header line
			String line;
			while ((line = reader.readLine()) != null) {
				HashMap<Integer, Integer> itemMap = new HashMap<>();
				String[] itemArray = line.split(",");
				for (int i = 1; i < itemArray.length; i++) {
					itemMap.put(Integer.parseInt(headtilte[i]), Integer.parseInt(itemArray[i]));
				}
				retHashMap.put(itemArray[0], itemMap);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return retHashMap;
	}
	
}
