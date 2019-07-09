package experiments;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NumberOfDistinctValues {

	private static String input = "C:\\Users\\abampoulidis\\Desktop\\safedeed\\conferences\\tbd-demo-short\\repo\\input\\adult_9.csv";
	private static String separator = ";";

	public static void main(String args[]) throws Exception {
		Map<Integer, String> fieldIndecesFieldNamesMap = new HashMap<Integer, String>();
		Map<Integer, List<String>> fieldIndecesValuesMap = new HashMap<Integer, List<String>>();

		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(input), "UTF8"));

		String header[] = br.readLine().split(separator);
		for (int i = 0; i < header.length; i++) {
			fieldIndecesFieldNamesMap.put(i, header[i]);
		}

		String line;
		while ((line = br.readLine()) != null) {
			String fields[] = line.split(separator);
			
			for (int i = 0; i < fields.length; i++) {
				List<String> values;
				
				if (fieldIndecesValuesMap.containsKey(i)) {
					values = fieldIndecesValuesMap.get(i);
				} else {
					values = new ArrayList<String>();
				}
				
				if (!values.contains(fields[i])) {
					values.add(fields[i]);
				}
				
				fieldIndecesValuesMap.put(i, values);
			}
		}

		br.close();
		
		for (Map.Entry<Integer, List<String>> entry : fieldIndecesValuesMap.entrySet()) {
			System.out.println(fieldIndecesFieldNamesMap.get(entry.getKey()) + "\t" + entry.getValue().size());
		}
	}
}
