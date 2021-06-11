import models.Climate.ClimateModel;
import simudyne.nexus.Server;

public class Main {
	public static void main(String[] args) {
		Server.register("Climate", ClimateModel.class);
		Server.run();
	}
}
