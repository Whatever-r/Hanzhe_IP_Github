package models.Climate0622;

import simudyne.core.graph.Message;

public class Messages {
	public static class gdpValue extends Message.Double {
	}
	
	public static class temperature extends Message {
		double avgTemp;
		double varTemp;
	}
}
