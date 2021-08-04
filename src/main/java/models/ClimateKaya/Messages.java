package models.ClimateKaya;

import simudyne.core.graph.Message;

public class Messages {
	public static class GdpValue extends Message.Double {
	}
	
	public static class GhgEmission extends Message.Double {
	}
	
	public static class GdpPerCapitaMsg extends Message.Double {
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
	
}
