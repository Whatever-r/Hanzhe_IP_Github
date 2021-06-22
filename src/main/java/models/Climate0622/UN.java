package models.Climate0622;

import simudyne.core.abm.Action;
import simudyne.core.abm.Agent;
import simudyne.core.annotations.Variable;
import simudyne.core.functions.SerializableConsumer;

import java.util.List;

public class UN extends Agent<ClimateModel0622.Globals> {
	static Action<UN> sendTemp =
			action(
					UN::sendTempInfo
			);
	
	static Action<UN> updateGDP =
			action(
					un -> {
						un.updateGlobalGDP();
						un.updateAccumulator();
					}
			);
	
//	@Variable public double maxAvgTempRise = 1.5;
	@Variable
	public double globalGDP;
	
	private static Action<UN> action(SerializableConsumer<UN> consumer) {
		return Action.create(UN.class, consumer);
	}
	
	void sendTempInfo() {
		double avgTemp = getGlobals().avgTemp;
//		double avgTemp = getDoubleAccumulator("avgTemp").value();
		double varTemp = getGlobals().varTemp;
//		double varTemp = getDoubleAccumulator("varTemp").value();
		getLinks(Links.ecoLink.class)
				.send(
						Messages.temperature.class,
						(m, l) -> {
							m.avgTemp = avgTemp;
							m.varTemp = varTemp;
						});
	}
	
	void updateGlobalGDP() {
		List<Messages.gdpValue> gdpValueList = getMessagesOfType(Messages.gdpValue.class);
		calcGlobalGDP(gdpValueList);
	}
	
	void calcGlobalGDP(List<Messages.gdpValue> gdpValueList) {
		this.globalGDP = 0;
		gdpValueList.forEach(
				gdpValue -> this.globalGDP += gdpValue.getBody()
		);
	}
	
	void updateAccumulator() {
		getDoubleAccumulator("globalGDPAccu").add(this.globalGDP);
	}
}
