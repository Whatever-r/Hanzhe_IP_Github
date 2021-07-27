package models.ClimateKaya;

import simudyne.core.abm.Action;
import simudyne.core.abm.Agent;
import simudyne.core.annotations.Variable;
import simudyne.core.functions.SerializableConsumer;

import static HZ_util.Print.println;

public class SolarPV extends Agent<ClimateKaya.Globals> {
	private static Action<SolarPV> action(SerializableConsumer<SolarPV> s) {
		return Action.create(SolarPV.class, s);
	}
	
	double solarPrice2019 = 0.37725;
	
	@Variable(name = "Solar Price (2019 US$ per W)")
	double solarPrice = solarPrice2019;
	
	double expChangeRate = -0.121;
	double Ksquare = 0.153;
	double theta = 0.63;
	
	static Action<SolarPV> updatePrice = action(SolarPV::updatePrice);
	
	void updatePrice() {
		double tau = getContext().getTick();
		double Astar = tau + (tau * tau) / 44;
		double corre = 1 + theta * theta;
		double avg = Math.log(solarPrice2019) + tau * expChangeRate;
		double stdevSquare = Ksquare * Astar / corre;
		
		if (stdevSquare <= 0) stdevSquare = 0.0001;
		double exp = getPrng().normal(avg, Math.sqrt(stdevSquare)).sample();
		println(exp);
		solarPrice = Math.exp(exp);
		getDoubleAccumulator("SolarPrice").add(solarPrice);
	}
	
}
