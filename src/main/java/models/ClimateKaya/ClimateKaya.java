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
@ModelSettings(macroStep = 1L, start = "2020-06-01T00:00:00Z", timeStep = 1L, timeUnit = "YEARS", ticks = 80L)
public class ClimateKaya extends AgentBasedModel<ClimateKaya.Globals> {
	
	@Constant
	public int nbUN = 1;
	
	public static final class Globals extends GlobalState {
		@Variable(name = "Average Global Temperature")
		public double avgTemp = 15.64;
		@Constant(name = "Average Global Warming Step")
		public double avgTempStep = 0.02;
		@Constant(name = "Share Tech 0-N 1-G7 2-G20 3-ITNL")
		public int techShareOpt;
		@Constant(name = "Share GDP pp")
		public boolean gdpShareOpt;
		//Population Projection HashMap
		public HashMap<String, HashMap<Long, Long>> populationHash = null;
	}
	
	
	@Override
	public void init() {
		//Temp, GDP, GHG accumulator & initial value
		createDoubleAccumulator("avgGlobalTempAccu", "Average Global Temperature");
		getDoubleAccumulator("avgGlobalTempAccu").add(getGlobals().avgTemp);
		createDoubleAccumulator("globalGDPAccu", "Global GDP");
		getDoubleAccumulator("globalGDPAccu").add(76139917568890.90);
		createDoubleAccumulator("globalGHGAccu", "Global GHG");
		getDoubleAccumulator("globalGHGAccu").add(27833931834.0);
		// Count no. of technology & GDP adoption
		createLongAccumulator("energyPerGdpAccu", "Count of Energy Per GDP Adoption");
		createLongAccumulator("energyPerGdpFin", "Count of Energy Per GDP - Finish");
		createLongAccumulator("emisPerEnergyAccu", "Count of Emission Per Energy Adoption");
		createLongAccumulator("emisPerEnergyFin", "Count of Emission Per Energy - Finish");
		createLongAccumulator("gdpPerCapitaAccu", "Count of GDP Per Capita Adoption");
		createLongAccumulator("gdpPerCapitaFin", "Count of GDP Per Capita - Finish");
		registerAgentTypes(Country.class, UN.class);
		registerLinkTypes(Links.UNLink.class,
				Links.INTLLink.class,
				Links.G7Link.class,
				Links.G20Link.class,
				Links.OECDLink.class
		);
		
	}
	
	@Override
	public void setup() {
		CSVSource CountryInitial = new CSVSource("data/Kaya Metric.csv");
		getGlobals().populationHash = getPolulationList("data/projected-population-by-country.csv");
		Group<UN> unGroup = generateGroup(UN.class, nbUN, un -> un.globalGDP = 0);
//		Init Country agents at 2020 w/ CSV source data
		Group<Country> countryGroup = loadGroup(Country.class, CountryInitial,
				country -> {
					country.gdpPerCapitaStep = country.gdpPerCapitaRef;
					country.emisPerEnergyStep = country.emisPerEnergyRef;
					country.energyPerGdpStep = country.energyPerGdpRef;
					country.initEvoStart();
					println(country.code + "\t" + getGlobals().populationHash.get(country.code).get(0L));
				}
		);
		countryGroup.partitionConnected(unGroup, Links.UNLink.class);
		countryGroup.fullyConnected(countryGroup, Links.INTLLink.class);
		countryGroup.fullyConnected(countryGroup, Links.G7Link.class);
		countryGroup.fullyConnected(countryGroup, Links.G20Link.class);
		countryGroup.fullyConnected(countryGroup, Links.OECDLink.class);
		super.setup();
	}
	
	@Override
	public void step() {
		super.step();
		if (getContext().getTick() == 0) {
			run(Country.SendGroup, Country.PruneLink);
		}
//		Within each step, Country receives Temoerature data,
//		Produce GDP growth incl. the temperature impact
		run(Country.GdpGrowth, UN.UpdateStat);
		run(Country.SendGDP, Country.ImproveGDP);
		run(Country.SendTech, Country.ImproveTech);
		//Randomized global warming step with expected 1.6C warming in 80 years
		double tempStep = getContext().getPrng().normal(getGlobals().avgTempStep, 0.01).sample();
		getGlobals().avgTemp += tempStep;
		getDoubleAccumulator("avgGlobalTempAccu").add(getGlobals().avgTemp);
	}
	
	/**
	 * Helper, Read Population Projection by Country into Hashmap<country code, Hashmap<year, value>>
	 */
	public static HashMap<String, HashMap<Long, Long>> getPolulationList(String path) {
		HashMap<String, HashMap<Long, Long>> retHashMap = new HashMap<>();
		try {
			BufferedReader reader = new BufferedReader(new FileReader(path));
			String[] headtilte = reader.readLine().
					split(",");// header line
			String line;
			while ((line = reader.readLine()) != null) {
				HashMap<Long, Long> itemMap = new HashMap<>();
				String[] itemArray = line.split(",");
				for (int i = 1; i < itemArray.length; i++) {
					itemMap.put(Long.parseLong(headtilte[i]), Long.parseLong(itemArray[i]));
				}
				retHashMap.put(itemArray[0], itemMap);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return retHashMap;
	}
	
}
