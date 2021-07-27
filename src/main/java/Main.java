import models.ClimateGDPGHG.ClimateGDPGHG;
import models.ClimateKaya.ClimateKaya;
import simudyne.nexus.Server;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Main {
	public static void main(String[] args) {
		Server.register("ClimateGDPGHG", ClimateGDPGHG.class);
		Server.register("ClimateKaya", ClimateKaya.class);
//		Server.register("Climate", ClimateModel.class);
//		Server.register("Tokyo", TokyoModel.class);
		Server.run();
		List<HashMap<String, Object>> retHashMap = new ArrayList<HashMap<String, Object>>();
		retHashMap = getcsvTableList(inpath);
		for (HashMap k : retHashMap) {
			System.out.println(k);
		}
		
	}
	
	public static List<HashMap<String, Object>> getcsvTableList(String path) {
		List<HashMap<String, Object>> retHashMap = new ArrayList<HashMap<String, Object>>();
		try {
			BufferedReader reader = new BufferedReader(new FileReader(path));
			String[] headtilte = reader.readLine().split("\\|");// 第一行信息，为标题信息，不用，如果需要，注释掉
			String line = null;
			while ((line = reader.readLine()) != null) {
				HashMap<String, Object> itemMap = new HashMap<String, Object>();
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

