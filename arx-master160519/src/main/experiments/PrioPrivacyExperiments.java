package experiments;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import prioprivacy.PrioPrivacy;

public class PrioPrivacyExperiments {

	private static String generalizationsPath = "C:\\Users\\abampoulidis\\Desktop\\safedeed\\conferences\\tbd-demo-short\\repo\\generalization_hierarchies\\our\\";
	private static String input = "C:\\Users\\abampoulidis\\Desktop\\safedeed\\conferences\\tbd-demo-short\\repo\\input\\adult_9.csv";
	private static String outputPath = "C:\\Users\\abampoulidis\\Desktop\\safedeed\\conferences\\tbd-demo-short\\repo\\output\\";
	private static String header = "sex;salary-class;race;workclass;marital-status;occupation;education;native-country;age";

	public static void main(String args[]) throws Exception {
		Map<Integer, Map<Integer, List<String>>> levelsQIsRulesMap = createLevelsQIsRulesMap(generalizationsPath);
		
		Map<Integer, List<String>> QIsDomainsMap = createQIsDomainsMap();

		Map<Integer, String> dataset = createDataset(input);

		for (int experiment = 2; experiment <= 4; experiment++) {
			Map<Integer, List<Integer>> priorities = getPriorities(experiment);

			String execTimeOutput = outputPath + "case" + experiment + "\\prioprivacy_exec_time.csv";

			BufferedWriter bw = new BufferedWriter(
					new OutputStreamWriter(new FileOutputStream(execTimeOutput), StandardCharsets.UTF_8));

			bw.write("k,minutes\n");

			for (int k = 2; k <= 10; k++) {System.out.println(experiment + "\t" + k);
				long startTime = System.currentTimeMillis();

				Map<Integer, String> anonymizedDataset = PrioPrivacy.Algorithm(priorities, levelsQIsRulesMap, dataset,
						k, QIsDomainsMap);

				long endTime = System.currentTimeMillis();

				bw.write(k + "," + ((endTime - startTime) / 60000.0) + "\n");

				String output = outputPath + "case" + experiment + "\\prioprivacy\\k_" + k + ".csv";

				writeDataset(output, header, anonymizedDataset);
			}

			bw.close();
		}
	}

	private static Map<Integer, Map<Integer, List<String>>> createLevelsQIsRulesMap(String generalizationsPath)
			throws Exception {
		Map<Integer, Map<Integer, List<String>>> levelsQIsRulesMap = new HashMap<Integer, Map<Integer, List<String>>>();

		File directory = new File(generalizationsPath);
		File[] files = directory.listFiles();

		if (files != null) {
			for (File file : files) {
				String name = file.getName();
				name = name.substring(0, name.indexOf("."));

				int level = Integer.parseInt(name.split("_")[0]);
				int QI = Integer.parseInt(name.split("_")[1]);

				List<String> rules = new ArrayList<String>();

				BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF8"));

				String line;
				while ((line = br.readLine()) != null) {
					rules.add(QI + ":" + line);
				}

				br.close();

				Map<Integer, List<String>> QIsRulesMap;
				if (!levelsQIsRulesMap.containsKey(level)) {
					QIsRulesMap = new HashMap<Integer, List<String>>();
				} else {
					QIsRulesMap = levelsQIsRulesMap.get(level);
				}

				QIsRulesMap.put(QI, rules);

				levelsQIsRulesMap.put(level, QIsRulesMap);
			}
		}

		return levelsQIsRulesMap;
	}

	private static Map<Integer, List<String>> createQIsDomainsMap() throws Exception {
		Map<Integer, List<String>> QIsDomainsMap = new HashMap<Integer, List<String>>();
		
		QIsDomainsMap.put(0, Arrays.asList(("Male,Female").split(",")));
		QIsDomainsMap.put(1, Arrays.asList(("<=50K,>50K").split(",")));
		QIsDomainsMap.put(2, Arrays.asList(("Amer-Indian-Eskimo,Asian-Pac-Islander,Black,Other,White").split(",")));
		QIsDomainsMap.put(3, Arrays.asList(
				("Private,Self-emp-not-inc,Self-emp-inc,Federal-gov,Local-gov,State-gov,Without-pay").split(",")));
		QIsDomainsMap.put(4, Arrays.asList(
				("Married-civ-spouse,Married-AF-spouse,Divorced,Never-married,Separated,Widowed,Married-spouse-absent")
						.split(",")));
		QIsDomainsMap.put(5, Arrays.asList(
				("Tech-support,Craft-repair,Prof-specialty,Machine-op-inspct,Other-service,Adm-clerical,Farming-fishing,Transport-moving,Priv-house-serv,Protective-serv,Armed-Forces,Sales,Exec-managerial,Handlers-cleaners")
						.split(",")));
		QIsDomainsMap.put(6, Arrays.asList(
				("Bachelors,Some-college,Prof-school,Assoc-acdm,Assoc-voc,Masters,Doctorate,11th,HS-grad,9th,7th-8th,12th,10th,1st-4th,5th-6th,Preschool")
						.split(",")));
		QIsDomainsMap.put(7, Arrays.asList(
				("United-States,Puerto-Rico,Canada,Outlying-US(Guam-USVI-etc),Cuba,Honduras,Jamaica,Mexico,Dominican-Republic,Haiti,Guatemala,El-Salvador,Cambodia,India,Japan,China,Iran,Philippines,Vietnam,Laos,Taiwan,Thailand,Hong,England,Germany,Greece,Italy,Poland,Portugal,Ireland,France,Hungary,Scotland,Yugoslavia,Holand-Netherlands,South,Ecuador,Columbia,Nicaragua,Trinadad&Tobago,Peru")
						.split(",")));
		QIsDomainsMap.put(8, Arrays.asList(
				("17,18,19,20,21,22,23,24,25,26,27,28,29,30,31,32,33,34,35,36,37,38,39,40,41,42,43,44,45,46,47,48,49,50,51,52,53,54,55,56,57,58,59,60,61,62,63,64,65,66,67,68,69,70,71,72,73,74,75,76,77,78,79,80,81,82,83,84,85,86,88,90")
						.split(",")));
		
		return QIsDomainsMap;
	}
	private static Map<Integer, String> createDataset(String input) throws Exception {
		Map<Integer, String> dataset = new HashMap<Integer, String>();

		int row_index = 0;

		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(input), "UTF8"));

		String line = br.readLine();
		while ((line = br.readLine()) != null) {
			dataset.put(row_index++, line);
		}

		br.close();

		return dataset;
	}

	private static Map<Integer, List<Integer>> getPriorities(int experiment) throws Exception {
		Map<Integer, List<Integer>> priorities = new HashMap<Integer, List<Integer>>();

		List<Integer> pr1 = new ArrayList<Integer>();
		List<Integer> pr2 = new ArrayList<Integer>();
		List<Integer> pr3 = new ArrayList<Integer>();
		List<Integer> pr4 = new ArrayList<Integer>();
		List<Integer> pr5 = new ArrayList<Integer>();
		List<Integer> pr6 = new ArrayList<Integer>();
		List<Integer> pr7 = new ArrayList<Integer>();
		List<Integer> pr8 = new ArrayList<Integer>();
		List<Integer> pr9 = new ArrayList<Integer>();

		if (experiment <= 2) {
			pr1.add(0);
			pr1.add(1);

			pr2.add(2);

			pr3.add(3);
			pr3.add(4);

			pr4.add(5);

			pr5.add(6);

			pr6.add(7);

			pr7.add(8);
		} else {
			pr1.add(8);

			pr2.add(0);

			pr3.add(7);

			pr4.add(1);

			pr5.add(6);

			pr6.add(2);

			pr7.add(5);

			pr8.add(3);

			pr9.add(4);
		}

		if (experiment == 1) {
			priorities.put(1, pr1);
			priorities.put(2, pr2);
			priorities.put(3, pr3);
			priorities.put(4, pr4);
			priorities.put(5, pr5);
			priorities.put(6, pr6);
			priorities.put(7, pr7);
		} else if (experiment == 2) {
			priorities.put(7, pr1);
			priorities.put(6, pr2);
			priorities.put(5, pr3);
			priorities.put(4, pr4);
			priorities.put(3, pr5);
			priorities.put(2, pr6);
			priorities.put(1, pr7);
		} else if (experiment == 3) {
			priorities.put(1, pr1);
			priorities.put(2, pr2);
			priorities.put(3, pr3);
			priorities.put(4, pr4);
			priorities.put(5, pr5);
			priorities.put(6, pr6);
			priorities.put(7, pr7);
			priorities.put(8, pr8);
			priorities.put(9, pr9);
		} else {
			priorities.put(1, pr2);
			priorities.put(2, pr1);
			priorities.put(3, pr4);
			priorities.put(4, pr3);
			priorities.put(5, pr6);
			priorities.put(6, pr5);
			priorities.put(7, pr8);
			priorities.put(8, pr7);
			priorities.put(9, pr9);
		}

		return priorities;
	}

	private static void writeDataset(String output, String header, Map<Integer, String> dataset) throws Exception {
		BufferedWriter bw = new BufferedWriter(
				new OutputStreamWriter(new FileOutputStream(output), StandardCharsets.UTF_8));

		bw.write(header + "\n");

		for (Map.Entry<Integer, String> entry : dataset.entrySet()) {
			bw.write(entry.getValue() + "\n");
		}

		bw.close();
	}
}
