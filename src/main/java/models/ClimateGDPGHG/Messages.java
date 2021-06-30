package models.ClimateGDPGHG;

import simudyne.core.graph.Message;

public class Messages {
	public static class gdpValue extends Message.Double {
	}
	
	public static class ghgEmission extends Message.Double {
	}
	
	public static class unitGHG extends Message.Double {
	}
	
	//	public static class isGroupMem extends Message.Boolean{}
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
