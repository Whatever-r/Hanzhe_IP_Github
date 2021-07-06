import models.ClimateGDPGHG.ClimateGDPGHG;
import models.Tokyo.TokyoModel;
import simudyne.nexus.Server;

public class Main {
	public static void main(String[] args) {
		Server.register("ClimateGDPGHG", ClimateGDPGHG.class);
//		Server.register("Climate", ClimateModel.class);
		Server.register("Tokyo", TokyoModel.class);
		Server.run();
	}
}
