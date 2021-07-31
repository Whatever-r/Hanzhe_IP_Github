package models.ClimateKaya;

import simudyne.core.graph.Message;

public class Messages {
	public static class gdpValue extends Message.Double {
	}
	
	public static class ghgEmission extends Message.Double {
	}
	
	public static class unitGHG extends Message.Double {
	}
	
	public static class EnergyPerGdpMsg extends Message.Double {
	}
	
	public static class EmisPerEnergyMsg extends Message.Double {
	
	}

	public static class Technology extends Message{
		double energyPerGdpMsg;
		double emisPerEnergyMsg;
		
	}
	
	public static class isGroupMem extends Message {
		boolean isG7;
		boolean isG20;
		boolean isOECD;
	}
	
	public static class temperature extends Message {
		double avgTemp;
		double varTemp;
	}
}
