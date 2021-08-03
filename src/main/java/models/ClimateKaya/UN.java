package models.ClimateKaya;

import simudyne.core.abm.Action;
import simudyne.core.abm.Agent;
import simudyne.core.functions.SerializableConsumer;

import java.util.List;

public class UN extends Agent<ClimateKaya.Globals> {
	
	static Action<UN> UpdateStat =
			action(
					un -> {
						un.updateGlobalGDP();
						un.updateGlobalGHG();
						un.updateAccumulator();
					}
			);
	
	public double globalGDP = 0;
	public double globalGHG = 0;
	
	private static Action<UN> action(SerializableConsumer<UN> consumer) {
		return Action.create(UN.class, consumer);
	}
	
	void updateGlobalGDP() {
		List<Messages.GdpValue> gdpValueList = getMessagesOfType(Messages.GdpValue.class);
		calcGlobalGDP(gdpValueList);
	}
	
	void calcGlobalGDP(List<Messages.GdpValue> gdpValueList) {
		this.globalGDP = 0;
		gdpValueList.forEach(
				gdpValue -> this.globalGDP += gdpValue.getBody()
		);
	}
	
	void updateGlobalGHG() {
		List<Messages.GhgEmission> ghgEmissionList = getMessagesOfType(Messages.GhgEmission.class);
		calcGlobalGHG(ghgEmissionList);
	}
	
	void calcGlobalGHG(List<Messages.GhgEmission> ghgEmissionList) {
		this.globalGHG = 0;
		ghgEmissionList.forEach(
				ghgEmission -> this.globalGHG += ghgEmission.getBody()
		);
	}
	
	void updateAccumulator() {
		getDoubleAccumulator("globalGDPAccu").add(this.globalGDP);
		getDoubleAccumulator("globalGHGAccu").add(this.globalGHG);
	}
}
