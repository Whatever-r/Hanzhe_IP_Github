import models.Climate0622.ClimateModel0622;
import models.Climate.ClimateModel;
import simudyne.nexus.Server;

public class Main {
	public static void main(String[] args) {
		Server.register("Climate0622", ClimateModel0622.class);
		Server.register("Climate", ClimateModel.class);
//		Server.register("Tokyo", TokyoModel.class);
		Server.run();
	}
}
