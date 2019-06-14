package org.deidentifier.arx.experiments;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

public class CheckIfARXConsidersAsteriskAsNominalValue {

	private static String qualityMetrics[] = { "Loss", "Non-Uniform Entropy" };
	private static String datasetPath = "C:\\Users\\abampoulidis\\Desktop\\safedeed\\conferences\\tbd-demo-short\\output\\case4\\arx\\";

	public static void main(String args[]) throws Exception {
		for (int QIsSize = 9; QIsSize <= 9; QIsSize++) {
			for (int k = 2; k <= 10; k++) {
				for (String metric : qualityMetrics) {
					String input = datasetPath + metric + "_QIs_" + QIsSize + "_k_" + k + ".csv";
					
					BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(input), "UTF8"));

					Map<String, Integer> rowKanonymityMap = new HashMap<String, Integer>();

					String line = br.readLine();
					while ((line = br.readLine()) != null) {
						int count;
						if (rowKanonymityMap.containsKey(line)) {
							count = rowKanonymityMap.get(line) + 1;
						} else {
							count = 1;
						}

						rowKanonymityMap.put(line, count);
					}

					br.close();

					for (Map.Entry<String, Integer> entry : rowKanonymityMap.entrySet()) {
						if (entry.getValue() < k) {
							System.out.println(metric + "_QIs_" + QIsSize + "_k_" + k + "\t" + entry.getKey());
						}
					}

				}
			}
		}
	}
}
