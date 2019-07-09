package experiments;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;

public class AverageExecutionTimes {

	private static String outputPath = "C:\\Users\\abampoulidis\\Desktop\\safedeed\\conferences\\tbd-demo-short\\repo\\output\\";
	private static String output = outputPath + "avg_exec_time.csv";
	private static String methods[] = { "arx_nue", "prioprivacy", "prioprivacy_one_thread" };

	public static void main(String args[]) throws Exception {
		BufferedWriter bw = new BufferedWriter(
				new OutputStreamWriter(new FileOutputStream(output), StandardCharsets.UTF_8));
		
		bw.write("method,experiment,avg_time\n");
		
		for (String method : methods) {
			for (int experiment = 1; experiment <= 4; experiment++) {
				double total = 0.0;
				
				String input = outputPath + "case" + experiment + "\\" + method + "_exec_time.csv";
				
				BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(input), "UTF8"));
				
				String line = br.readLine();
				while ((line = br.readLine()) != null) {
					total += Double.parseDouble(line.split(",")[1]);
				}
				
				br.close();
				
				bw.write(method + "," + experiment + "," + (total / 9) + "\n");
			}
		}

		bw.close();
	}
}
