package models.Deprecated.Climate;

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


@ModelSettings(macroStep = 3L, start = "2021-06-01T00:00:00Z", timeStep = 1L, timeUnit = "MONTHS", ticks = 60L)
public class ClimateModel extends AgentBasedModel<ClimateModel.Globals> {
	
	@Constant
	public int nbUN = 1;
	@Constant
	public int nbEconomy = 100;
//	public long seed = this.getConfig().getOrSetLong("core.prng-seed", System.nanoTime());
//	public SeededRandom prng = SeededRandom.create(seed);
	
	@Override
	public void init() {
//	createDoubleAccumulator("avgTemp", "Average Temperature");
		createDoubleAccumulator("varTempAccu", "Variation of Temperature");
		createDoubleAccumulator("globalGDP", "Global GDP");
		
		registerAgentTypes(Economy.class, UN.class);
		registerLinkTypes(Links.ecoLink.class);
	}
	
	@Override
	public void setup() {
//		getDoubleAccumulator("avgTemp").add(18);
//		getDoubleAccumulator("varTemp").add(1.8);
//		SeededRandom prng = getContext().getPrng();
//		prng.generator.;
		EmpiricalDistribution initGDP = getContext().getPrng().empiricalFromSource(new CSVSource("data/gdp-distribution.csv"));
		
		Group<UN> unGroup = generateGroup(UN.class, nbUN, un -> un.globalGDP = 1);
		Group<Economy> economyGroup = generateGroup(Economy.class, nbEconomy,
				eco -> {
					eco.gdpValue = initGDP.sample();
//					base growth rate in % per month
					eco.baseGrowth = (5 + getContext().getPrng().normal(0, 1).sample()) / 100.0 / 12;
//					impact of varTemp on the base growth rate in %
					eco.impactOfD2DVariOnGrowth = (7.5 + getContext().getPrng().normal(0, 1).sample()) / 100.0;
				});
		unGroup.fullyConnected(economyGroup, Links.ecoLink.class);
		economyGroup.fullyConnected(unGroup, Links.ecoLink.class);
		super.setup();
		
	}
	
	@Override
	public void step() {
		super.step();
//		SeededRandom prng = getContext().getPrng();
//		prng.generator.nextDouble();
		run(UN.sendTemp, Economy.gdpGrowth, UN.updateGDP);
		getGlobals().varTemp *= (1 + getContext().getPrng().normal(0, 0.1).sample());
		getDoubleAccumulator("varTempAccu").add(getGlobals().varTemp);
	}
	
	public static final class Globals extends GlobalState {
		@Variable(name = "Average Temperature")
		public double avgTemp = 18;
		@Variable(name = "D2D Variation of Temperature")
		public double varTemp = 1.5;
	}
}
