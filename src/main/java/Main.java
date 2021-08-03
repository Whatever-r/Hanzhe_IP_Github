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
//		Server.register("ClimateGDPGHG", ClimateGDPGHG.class);
		Server.register("ClimateKaya", ClimateKaya.class);
		Server.run();

		String inpath = "data/projected-population-by-country.csv";
		HashMap<String, HashMap<Long, Long>> retHashHash;
		retHashHash = getPolulationList(inpath);
		println(retHashHash.keySet());
		println(retHashHash.get("DNK").keySet());
		println(retHashHash.get("CHN").get(0L));
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

