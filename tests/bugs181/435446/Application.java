package de.scrum_master.app;

import java.util.HashMap;
import java.util.Map;

public class Application {
	static Map/*<String, Map<Integer, String>>*/ languages = new HashMap<>();
	
	static {
		Map<Integer, String> englishNumbers = new HashMap<>();
		englishNumbers.put(11, "eleven");
		englishNumbers.put(12, "twelve");
		englishNumbers.put(13, "thirteen");
		languages.put("EN", englishNumbers);

		Map<Integer, String> germanNumbers = new HashMap<>();
		germanNumbers.put(11, "elf");
		germanNumbers.put(12, "zwölf");
		germanNumbers.put(13, "dreizehn");
		languages.put("DE", germanNumbers);

		
	}
	
	public static void main(String[] args) {
		languages.entrySet().stream().forEach((language) -> {
//			String languageCode = language.getKey();
			Map/*<Integer, String>*/ numbers =  (Map)((Map.Entry)language).getValue();
//			System.out.println("Language code = " + languageCode);
			numbers.entrySet().stream().forEach((number) -> {
//				int numericValue = number.getKey();
//				String textualValue = number.getValue();
//				System.out.println("  " + numericValue + " -> " + textualValue);
			});
		});
	}
}
