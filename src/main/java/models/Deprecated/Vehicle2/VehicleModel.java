package models.Deprecated.Vehicle2;

import org.apache.commons.math3.random.EmpiricalDistribution;
import simudyne.core.abm.AgentBasedModel;
import simudyne.core.abm.GlobalState;
import simudyne.core.abm.Group;
import simudyne.core.annotations.Constant;
import simudyne.core.annotations.Input;
import simudyne.core.annotations.ModelSettings;
import simudyne.core.annotations.Variable;
import simudyne.core.data.CSVSource;
import simudyne.core.rng.SeededRandom;

//@ModelSettings(macroStep = 3) // each step represents a quarter
//@ModelSettings(macroStep = 1)
@ModelSettings(macroStep = 3L, start = "2021-06-01T00:00:00Z", timeStep = 1L, timeUnit = "MONTHS", ticks = 100L)
public class VehicleModel extends AgentBasedModel<VehicleModel.Globals> {
	@Constant(name = "Number of Households")
	public int nbHouseholds = 100;
	
	@Override
	public void init() {
		createLongAccumulator("nbEV", "Number of EVs");
		createLongAccumulator("nbICE", "Number of Fuel Vehicles");
		
		registerAgentTypes(Household.class, Brand.class);
		registerLinkTypes(Links.vLink.class);
	}
	
	@Override
	public void setup() {
		SeededRandom prng = getContext().getPrng();
		
		EmpiricalDistribution budgetDist =
				prng.empiricalFromSource(new CSVSource("data/purchase-budget.csv"));
		
		Group<Household> householdGroup =
				generateGroup(
						Household.class,
						nbHouseholds,
						house -> house.purchaseBudget = budgetDist.sample(1)[0]);
		Group<Brand> brandGroup = generateGroup(Brand.class, 1);
		
//		householdGroup.fullyConnected(brandGroup, Links.vLink.class);
//		brandGroup.fullyConnected(householdGroup, Links.vLink.class);
		householdGroup.partitionConnected(brandGroup, Links.vLink.class);
		brandGroup.partitionConnected(householdGroup, Links.vLink.class);
		
		super.setup();
	}
	
	@Override
	public void step() {
		super.step();
		run(Brand.sendPrice, Household.newVehicle, Brand.updatePurchase);
		run(Brand.techImprove, Household.replaceVehicle, Brand.updatePurchase);
//		run(Household.replaceVehicle);
//				Brand.updatePurchase
	
	}
	
	public static final class Globals extends GlobalState {
		@Input(name = "dummy Globals Input")
		public double dummyInput = 0.1;
		@Variable(name = "Dummy Global State")
		public double dummyGS = 0;
	}
}
