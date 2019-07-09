package experiments;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

public class CheckIfARXConsidersAsteriskAsNominalValue {

	private static String methods[] = { "arx_loss", "arx_nue" };
	private static String outputPath = "C:\\Users\\abampoulidis\\Desktop\\safedeed\\conferences\\tbd-demo-short\\repo\\output\\";

	public static void main(String args[]) throws Exception {
		for (String method : methods) {
			for (int experiment = 1; experiment <= 4; experiment++) {
				for (int k = 2; k <= 10; k++) {
					String input = outputPath + "case" + experiment + "\\" + method + "\\k_" + k + ".csv";

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
							System.out.println(method + "\t" + k + "\t" + entry.getKey());
						}
					}
				}
			}
		}
	}
}
