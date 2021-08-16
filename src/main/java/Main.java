import models.ClimateKaya.ClimateKaya;
import simudyne.core.exec.runner.ModelRunner;
import simudyne.core.exec.runner.RunnerBackend;
import simudyne.core.exec.runner.definition.BatchDefinitionsBuilder;
import simudyne.nexus.Server;

import static HZ_util.Print.println;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


public class Main {
	public static void main(String[] args) {

		//Easier switch than commenting out
		boolean bypassConsole = true;

		if (!bypassConsole) {
			Server.register("ClimateKaya", ClimateKaya.class);
			Server.run();

			String inpath = "data/projected-population-by-country.csv";
			HashMap<String, HashMap<Long, Long>> retHashHash;
			retHashHash = getPolulationList(inpath);
			println(retHashHash.keySet());
			println(retHashHash.get("DNK").keySet());
			println(retHashHash.get("CHN").get(0L));
		} else {
			try {
				RunnerBackend runnerBackend = RunnerBackend.create();
				ModelRunner modelRunner = runnerBackend.forModel(ClimateKaya.class);
				BatchDefinitionsBuilder runDefinitionBuilder =
						BatchDefinitionsBuilder.create()
								.forRuns(10) // a required field, must be greater than 0.
								.forTicks(80); // a required field, must be greater than 0.

				modelRunner.forRunDefinitionBuilder(runDefinitionBuilder);
				// To run the model and wait for it to complete
				modelRunner.run();
			} catch (RuntimeException e) {
				System.out.println(Arrays.toString(e.getStackTrace()));
				e.printStackTrace();
			}
		}
	}

	
	/**
	 * Helper, Read Population Projection by Country into Hashmap<code, Hashmap<year, value>>
	 */
	public static HashMap<String, HashMap<Long, Long>> getPolulationList(String path) {
		HashMap<String, HashMap<Long, Long>> retHashMap = new HashMap<>();
		try {
			BufferedReader reader = new BufferedReader(new FileReader(path));
			String[] headtilte = reader.readLine().
					split(",");// header line
			String line;
			while ((line = reader.readLine()) != null) {
				HashMap<Long, Long> itemMap = new HashMap<>();
				String[] itemArray = line.split(",");
				for (int i = 1; i < itemArray.length; i++) {
					itemMap.put(Long.parseLong(headtilte[i]), Long.parseLong(itemArray[i]));
				}
				retHashMap.put(itemArray[0], itemMap);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return retHashMap;
	}
}

