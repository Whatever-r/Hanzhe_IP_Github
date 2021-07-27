import models.ClimateGDPGHG.ClimateGDPGHG;
import models.ClimateKaya.ClimateKaya;
import simudyne.nexus.Server;

import static HZ_util.Print.println;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


public class Main {
	public static void main(String[] args) {
		Server.register("ClimateGDPGHG", ClimateGDPGHG.class);
		Server.register("ClimateKaya", ClimateKaya.class);
//		Server.register("Climate", ClimateModel.class);
//		Server.register("Tokyo", TokyoModel.class);
		Server.run();
		String inpath = "data/projected-population-by-country.csv";
		List<HashMap<String, Object>> retHashMap;

		HashMap<String, HashMap<Double, Double>> retHashHash;
		retHashHash = getPolulationList(inpath);
		println(retHashHash.keySet());
		println(retHashHash.get("DNK").keySet());
		println(retHashHash.get("DNK").get(0));
	}
	
	/**
	 * Helper, Read Population Projection by Country into Hashmap<code, Hashmap<year, value>>
	 */
	public static HashMap<String, HashMap<Double, Double>> getPolulationList(String path) {
		HashMap<String, HashMap<Double, Double>> retHashMap = new HashMap<>();
		try {
			BufferedReader reader = new BufferedReader(new FileReader(path));
			String[] headtilte = reader.readLine().
					split(",");// header line
			String line;
			while ((line = reader.readLine()) != null) {
				HashMap<Double, Double> itemMap = new HashMap<>();
				String[] itemArray = line.split(",");
				for (int i = 1; i < itemArray.length; i++) {
					itemMap.put(Double.parseDouble(headtilte[i]), Double.parseDouble(itemArray[i]));
				}
				retHashMap.put(itemArray[0], itemMap);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return retHashMap;
	}
	
	public static List<Map<String, String>> readWithMap(String path) {
		Pattern pattern = Pattern.compile(",");
		List<Map<String, String>> namefreq = null;
		try (BufferedReader in = new BufferedReader(new FileReader(path))) {
			namefreq = in.lines()
					.skip(1)
					.map(line -> pattern.split(line)) // or pattern::split
					.map(line -> {
						Map<String, String> map = new HashMap<>();
						map.put("NAME_OF_FIRST_COLUMN", line[0]);
						map.put("NAME_OF_SECOND_COLUMN", line[1]);
						// ... (etc)
						return map;
					})
					.collect(Collectors.toList());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return namefreq;
	}
	
	public static List<HashMap<String, Object>> getcsvTableList(String path) {
		List<HashMap<String, Object>> retHashMap = new ArrayList<>();
		try {
			BufferedReader reader = new BufferedReader(new FileReader(path));
			String[] headtilte = reader.readLine().
					split("\\|");// 第一行信息，为标题信息，不用，如果需要，注释掉
			String line;
			while ((line = reader.readLine()) != null) {
				HashMap<String, Object> itemMap = new HashMap<>();
				String[] itemArray = line.split("\\|");
				for (int i = 0; i < itemArray.length; i++) {
					itemMap.put(headtilte[i], itemArray[i]);
				}
				retHashMap.add(itemMap);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return retHashMap;
	}
}

