package org.deidentifier.arx.experiments;

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
import java.util.Set;

public class OurAlgorithmCase4 {

	// 2 + with maxChanges

	private static String generalizationsPath = "C:\\Users\\abampoulidis\\Desktop\\safedeed\\conferences\\tbd-demo-short\\generalization_hierarchies\\our\\";
	private static String QIsNames[] = { "sex", "salary-class", "race", "workclass", "marital-status", "occupation",
			"education", "native-country", "age" };
	private static String inputPath = "C:\\Users\\abampoulidis\\Desktop\\safedeed\\conferences\\tbd-demo-short\\input\\";
	private static String outputPath = "C:\\Users\\abampoulidis\\Desktop\\safedeed\\conferences\\tbd-demo-short\\output\\case4\\our\\";
	private static String execTimeOutput = outputPath + "..\\our_exec_time.csv";

	public static void main(String args[]) throws Exception {
		Map<String, Map<Integer, List<Integer>>> allPriorities = setPriorities();

		Map<Integer, Map<Integer, List<String>>> levelsQIsRulesMap = createLevelsQIsRulesMap(generalizationsPath);
		
		BufferedWriter bwExecTime = new BufferedWriter(
				new OutputStreamWriter(new FileOutputStream(execTimeOutput), StandardCharsets.UTF_8));
		
		bwExecTime.write("QIsSize,k,maxChanges,time\n");

		for (int QIsSize = 9; QIsSize <= 9; QIsSize++) {
			Map<Integer, List<Integer>> priorities = allPriorities.get("adult_" + QIsSize);

			String input = inputPath + "adult_" + QIsSize + ".csv";

			for (int k = 2; k <= 10; k++) {
				for (int maxChanges = 9; maxChanges <= QIsSize; maxChanges++) {
					System.out.println(QIsSize + "\t" + k + "\t" + maxChanges);

					Map<Integer, String> dataset = createDataset(input);

					long startTime = System.currentTimeMillis();

					Map<Integer, String> unsafeDataset = createUnsafeDataset(dataset, k);

					List<Integer> QIs = new ArrayList<Integer>();

					List<String> geneneralizationCombinationsToExclude = new ArrayList<String>();
					List<String> suppressionCombinationsToExclude = new ArrayList<String>();

					System.out.println("before gen.\t" + unsafeDataset.size());

					for (int p = priorities.size(); p >= 1; p--) {
						if (unsafeDataset.isEmpty()) {
							break;
						}

						for (int QI : priorities.get(p)) {
							QIs.add(QI);

							// generalization (no suppression)
							for (int level = 1; level < 999; level++) {
								if (!levelsQIsRulesMap.containsKey(level)) {
									break;
								}

								Map<Integer, List<String>> QIsInLevelRulesMap = getQIsInLevelRulesMap(levelsQIsRulesMap,
										level, QIs);

								if (QIsInLevelRulesMap.isEmpty()) {
									break;
								}

								List<Integer> QIsInLevel = getQIsInLevel(levelsQIsRulesMap.get(level), QIs);

								List<String> QIsGeneralizationCombinations = getCombinations(QIsInLevel,
										QIsInLevel.size(), geneneralizationCombinationsToExclude);

								Map<Integer, List<String>> numberOfCombinedRulesRulesMap = numberOfCombinedRulesRulesMap(
										QIsGeneralizationCombinations, QIsInLevelRulesMap);

								for (int i = 1; i <= QIsInLevel.size(); i++) {
									if (i > maxChanges) {
										break;
									}
									
									boolean noMore = false;
									while (!noMore) {
										Map<String, Map<Integer, String>> combinationsGeneralizedDatasetsMap = getCombinationsGeneralizedDatasetsMap(
												numberOfCombinedRulesRulesMap, i, unsafeDataset, QIs,
												geneneralizationCombinationsToExclude, k);

										Map<String, Map<String, Integer>> rowKanonymityMapOfGeneralizedDatasets = getRowKanonymityMapOfGeneralizedDatasets(
												combinationsGeneralizedDatasetsMap);

										String maxKGeneralizationRule = getMaxKGeneralizationRule(
												rowKanonymityMapOfGeneralizedDatasets, k,
												geneneralizationCombinationsToExclude);

										if (maxKGeneralizationRule.equals("")) {
											noMore = true;
										} else {
											int maxK = getMaxOfGenDataset(
													rowKanonymityMapOfGeneralizedDatasets.get(maxKGeneralizationRule),
													k);

											Map<Integer, String> generalizedDatasetWithMaxK = combinationsGeneralizedDatasetsMap
													.get(maxKGeneralizationRule);

											Map<String, Integer> generalizedDatasetRowKanonymityMap = rowKanonymityMapOfGeneralizedDatasets
													.get(maxKGeneralizationRule);

											generalize(generalizedDatasetWithMaxK, generalizedDatasetRowKanonymityMap,
													maxK, dataset, unsafeDataset);
										}
									}
								}
							}
							System.out.println("after gen.\t" + QIs.size() + "\t" + unsafeDataset.size());

							// suppression
							List<String> suppressionCombinations = getCombinations(QIs, QIs.size(),
									suppressionCombinationsToExclude);

							Map<Integer, List<String>> numberOfCombinedSuppresionsSuppressionsMap = getNumberOfCombinedSuppresionsSuppressionsMap(
									suppressionCombinations);

							for (int i = 1; i <= QIs.size(); i++) {
								if (i > maxChanges) {
									break;
								}
								
								List<String> combinations = numberOfCombinedSuppresionsSuppressionsMap.get(i);

								Map<String, Map<Integer, String>> combinationsDatasetWithoutQIsMap = getCombinationsDatasetWithoutQIsMap(
										combinations, dataset);

								Map<String, Map<String, Integer>> combinationsDatasetsWithoutQIsRowKanonymityMap = getCombinationsDatasetsWithoutQIsRowKanonymityMap(
										combinationsDatasetWithoutQIsMap);

								List<Integer> unsafeRowIDs = new ArrayList<Integer>();
								unsafeRowIDs.addAll(unsafeDataset.keySet());

								for (int unsafeRowID : unsafeRowIDs) {
									String row = unsafeDataset.get(unsafeRowID);

									Map<String, String> combinationsSuppressedRowMap = getCombinationsSuppressedRowMap(
											row, combinations);

									String maxKanonymityCombination = getMaxKanonymityCombination(
											combinationsSuppressedRowMap,
											combinationsDatasetsWithoutQIsRowKanonymityMap, k,
											suppressionCombinationsToExclude, dataset, unsafeRowID);

									if (!maxKanonymityCombination.equals("")) {
										dataset.put(unsafeRowID,
												combinationsSuppressedRowMap.get(maxKanonymityCombination));
										unsafeDataset.remove(unsafeRowID);
									}
								}
							}
							System.out.println("after sup.\t" + QIs.size() + "\t" + unsafeDataset.size());
						}
					}

					suppressRemainingRows(unsafeDataset, QIsSize, dataset);

					unsafeDataset.clear();

					long stopTime = System.currentTimeMillis();
					
					bwExecTime.write(QIsSize + "," + k + "," + maxChanges + "," + (stopTime - startTime) + "\n");

					System.out.println("time\t" + (stopTime - startTime));
										
					String output = outputPath + "QIs_" + QIsSize + "_k_" + k + ".csv";

					BufferedWriter bw = new BufferedWriter(
							new OutputStreamWriter(new FileOutputStream(output), StandardCharsets.UTF_8));

					bw.write(header(QIsSize, QIsNames) + "\n");

					writeDataset(dataset, bw);

					bw.close();
				}
			}
		}
		
		bwExecTime.close();
	}

	private static Map<String, Map<Integer, List<Integer>>> setPriorities() throws Exception {
		Map<String, Map<Integer, List<Integer>>> priorities = new HashMap<String, Map<Integer, List<Integer>>>();

		List<Integer> pr1 = new ArrayList<Integer>();
		pr1.add(8);

		List<Integer> pr2 = new ArrayList<Integer>();
		pr2.add(0);

		List<Integer> pr3 = new ArrayList<Integer>();
		pr3.add(7);		

		List<Integer> pr4 = new ArrayList<Integer>();
		pr4.add(1);
		
		List<Integer> pr5 = new ArrayList<Integer>();
		pr5.add(6);
		
		List<Integer> pr6 = new ArrayList<Integer>();
		pr6.add(2);
		
		List<Integer> pr7 = new ArrayList<Integer>();
		pr7.add(5);
		
		List<Integer> pr8 = new ArrayList<Integer>();
		pr8.add(3);
		
		List<Integer> pr9 = new ArrayList<Integer>();
		pr9.add(4);
			
//		Map<Integer, List<Integer>> p5 = new HashMap<Integer, List<Integer>>();
//		p5.put(1, pr1);
//		p5.put(2, pr2);
//		p5.put(3, pr3);
//		priorities.put("adult_5", p5);
//
//		Map<Integer, List<Integer>> p6 = new HashMap<Integer, List<Integer>>();
//		p6.put(1, pr1);
//		p6.put(2, pr2);
//		p6.put(3, pr3);
//		p6.put(4, pr4_1);
//		priorities.put("adult_6", p6);
//
//		Map<Integer, List<Integer>> p7 = new HashMap<Integer, List<Integer>>();
//		p7.put(1, pr1);
//		p7.put(2, pr2);
//		p7.put(3, pr3);
//		p7.put(4, pr4_2);
//		priorities.put("adult_7", p7);
//
//		Map<Integer, List<Integer>> p8 = new HashMap<Integer, List<Integer>>();
//		p8.put(1, pr1);
//		p8.put(2, pr2);
//		p8.put(3, pr3);
//		p8.put(4, pr4_2);
//		p8.put(5, pr5_1);
//		priorities.put("adult_8", p8);

		Map<Integer, List<Integer>> p9 = new HashMap<Integer, List<Integer>>();
		p9.put(1, pr2);
		p9.put(2, pr1);
		p9.put(3, pr4);
		p9.put(4, pr3);
		p9.put(5, pr6);
		p9.put(6, pr5);
		p9.put(7, pr8);
		p9.put(8, pr7);
		p9.put(9, pr9);
		priorities.put("adult_9", p9);

		return priorities;
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

	private static Map<Integer, String> createUnsafeDataset(Map<Integer, String> dataset, int k) throws Exception {
		Map<Integer, String> unsafeDataset = new HashMap<Integer, String>();

		Map<String, Integer> rowKanonymityMapOfDataset = createRowKAnonymityMap(dataset);

		for (Map.Entry<Integer, String> entry : dataset.entrySet()) {
			if (rowKanonymityMapOfDataset.get(entry.getValue()) < k) {
				unsafeDataset.put(entry.getKey(), entry.getValue());
			}
		}

		return unsafeDataset;
	}

	private static Map<String, Integer> createRowKAnonymityMap(Map<Integer, String> dataset) throws Exception {
		Map<String, Integer> rowKanonymityMap = new HashMap<String, Integer>();

		for (Map.Entry<Integer, String> entry : dataset.entrySet()) {
			String row = entry.getValue();

			if (rowKanonymityMap.containsKey(row)) {
				rowKanonymityMap.put(row, rowKanonymityMap.get(row) + 1);
			} else {
				rowKanonymityMap.put(row, 1);
			}
		}

		return rowKanonymityMap;
	}

	private static Map<Integer, List<String>> getQIsInLevelRulesMap(
			Map<Integer, Map<Integer, List<String>>> levelsQIsRulesMap, int level, List<Integer> QIs) throws Exception {
		Map<Integer, List<String>> QIsInLevelRulesMap = new HashMap<Integer, List<String>>();

		for (Map.Entry<Integer, List<String>> QIsRulesMap : levelsQIsRulesMap.get(level).entrySet()) {
			int QI = QIsRulesMap.getKey();
			List<String> rules = QIsRulesMap.getValue();

			if (QIs.contains(QI)) {
				QIsInLevelRulesMap.put(QI, rules);
			}
		}

		return QIsInLevelRulesMap;
	}

	private static List<Integer> getQIsInLevel(Map<Integer, List<String>> QIsRulesMap, List<Integer> QIs) {
		List<Integer> QIsInLevel = new ArrayList<Integer>();

		Set<Integer> allQIsInLevel = QIsRulesMap.keySet();

		for (int QI : QIs) {
			if (allQIsInLevel.contains(QI)) {
				QIsInLevel.add(QI);
			}
		}

		return QIsInLevel;
	}

	private static List<String> getCombinations(List<Integer> QIs, int N, List<String> toExclude) {
		int sequence[] = new int[QIs.size()];
		for (int i = 0; i < QIs.size(); i++) {
			sequence[i] = QIs.get(i);
		}

		List<String> combinations = new ArrayList<String>();

		int[] data = new int[N];

		for (int r = 0; r < sequence.length; r++) {
			combinations(sequence, data, 0, N - 1, 0, r, combinations, toExclude);
		}

		String all = "";
		for (int QI : QIs) {
			all += QI + " ";
		}
		all = all.substring(0, all.length() - 1);

		combinations.add(all);

		return combinations;
	}

	private static void combinations(int[] sequence, int[] data, int start, int end, int index, int r,
			List<String> combinations, List<String> toExclude) {

		if (index == r) {
			String combination = "";

			for (int j = 0; j < r; j++) {
				combination += data[j] + " ";
			}

			if (!combination.equals("")) {
				combination = combination.substring(0, combination.length() - 1);

				if (!toExclude.contains(combination)) {
					combinations.add(combination);
				}
			}
		}

		for (int i = start; i <= end && ((end - i + 1) >= (r - index)); i++) {
			data[index] = sequence[i];

			combinations(sequence, data, i + 1, end, index + 1, r, combinations, toExclude);
		}
	}

	private static Map<Integer, List<String>> numberOfCombinedRulesRulesMap(List<String> QIsCombinations,
			Map<Integer, List<String>> QIsInLevelRulesMap) throws Exception {
		Map<Integer, List<String>> numberOfCombinedRulesRulesMap = new HashMap<Integer, List<String>>();

		for (String QIsCombination : QIsCombinations) {
			String QIsInCombinations[] = QIsCombination.split(" ");
			int numberOfCombinedQIs = QIsInCombinations.length;

			List<List<String>> RulesOfQIs = new ArrayList<List<String>>();

			for (String QIInCombination : QIsInCombinations) {
				RulesOfQIs.add(QIsInLevelRulesMap.get(Integer.parseInt(QIInCombination)));
			}

			List<String> rulesCombinations = new ArrayList<String>();
			GeneratePermutations(RulesOfQIs, rulesCombinations, 0, "");

			for (String ruleCombination : rulesCombinations) {
				ruleCombination = ruleCombination.substring(0, ruleCombination.length() - 1);

				if (numberOfCombinedRulesRulesMap.containsKey(numberOfCombinedQIs)) {
					List<String> rules = numberOfCombinedRulesRulesMap.get(numberOfCombinedQIs);
					if (!rules.contains(ruleCombination)) {
						rules.add(ruleCombination);
						numberOfCombinedRulesRulesMap.put(numberOfCombinedQIs, rules);
					}
				} else {
					List<String> rules = new ArrayList<String>();
					rules.add(ruleCombination);
					numberOfCombinedRulesRulesMap.put(numberOfCombinedQIs, rules);
				}
			}
		}

		return numberOfCombinedRulesRulesMap;
	}

	private static void GeneratePermutations(List<List<String>> Lists, List<String> result, int depth, String current) {
		if (depth == Lists.size()) {
			result.add(current);
			return;
		}

		for (int i = 0; i < Lists.get(depth).size(); ++i) {
			GeneratePermutations(Lists, result, depth + 1, current + Lists.get(depth).get(i) + "\t");
		}
	}

	private static Map<String, Map<Integer, String>> getCombinationsGeneralizedDatasetsMap(
			Map<Integer, List<String>> numberOfCombinedRulesRulesMap, int numberOfCombinations,
			Map<Integer, String> unsafeDataset, List<Integer> QIs, List<String> toExclude, int k) throws Exception {
		Map<String, Map<Integer, String>> combinationsGeneralizedDatasetsMap = new HashMap<String, Map<Integer, String>>();

		for (String combination : numberOfCombinedRulesRulesMap.get(numberOfCombinations)) {
			if (!toExclude.contains(combination)) {
				Map<Integer, Map<String, String>> QIsRulesRulesMappingMap = getQIsRulesRulesMappingMap(combination);

				Map<Integer, String> generalizedDataset = generalizeDataset(QIsRulesRulesMappingMap, unsafeDataset,
						QIs);

				if (getMaxOfGenDataset(createRowKAnonymityMap(generalizedDataset), k) >= k) {
					combinationsGeneralizedDatasetsMap.put(combination,
							generalizeDataset(QIsRulesRulesMappingMap, unsafeDataset, QIs));
				} else {
					toExclude.add(combination);
				}
			}
		}

		return combinationsGeneralizedDatasetsMap;
	}

	private static Map<Integer, Map<String, String>> getQIsRulesRulesMappingMap(String combination) throws Exception {
		Map<Integer, Map<String, String>> QIsRulesRulesMappingMap = new HashMap<Integer, Map<String, String>>();

		String rules[] = combination.split("\t");

		for (String rule : rules) {
			String mapping[] = rule.split("=>");

			int QI = Integer.parseInt(mapping[0].split(":")[0]);
			String from = mapping[0].split(":")[1].trim();
			String to = mapping[1].trim();

			Map<String, String> rulesRulesMappingMap = new HashMap<String, String>();
			for (String value : from.split(",")) {
				rulesRulesMappingMap.put(value, to);
			}

			QIsRulesRulesMappingMap.put(QI, rulesRulesMappingMap);
		}

		return QIsRulesRulesMappingMap;
	}

	private static Map<Integer, String> generalizeDataset(Map<Integer, Map<String, String>> QIsRulesRulesMappingMap,
			Map<Integer, String> unsafeDataset, List<Integer> QIs) throws Exception {
		Map<Integer, String> generalizedDataset = new HashMap<Integer, String>();

		for (Map.Entry<Integer, String> entry : unsafeDataset.entrySet()) {
			int rowID = entry.getKey();
			String row = entry.getValue();

			String newRow = "";

			String fields[] = row.split(";");
			for (int i = 0; i < fields.length; i++) {
				if (QIsRulesRulesMappingMap.containsKey(i)) {
					if (QIsRulesRulesMappingMap.get(i).containsKey(fields[i])) {
						fields[i] = QIsRulesRulesMappingMap.get(i).get(fields[i]);
					}
				}
				newRow += fields[i] + ";";
			}

			newRow = newRow.substring(0, newRow.length() - 1);

			generalizedDataset.put(rowID, newRow);
		}

		return generalizedDataset;
	}

	private static int getMaxOfGenDataset(Map<String, Integer> generalizedDatasetRowKanonymityMap, int k)
			throws Exception {
		int maxKOfDataset = -1;

		for (Map.Entry<String, Integer> entry : generalizedDatasetRowKanonymityMap.entrySet()) {
			int kOfRow = entry.getValue();

			if (kOfRow >= k && kOfRow > maxKOfDataset) {
				maxKOfDataset = kOfRow;
			}
		}

		return maxKOfDataset;
	}

	private static Map<String, Map<String, Integer>> getRowKanonymityMapOfGeneralizedDatasets(
			Map<String, Map<Integer, String>> combinationsGeneralizedDatasetsMap) throws Exception {
		Map<String, Map<String, Integer>> rowKanonymityMapOfGeneralizedDatasets = new HashMap<String, Map<String, Integer>>();

		for (Map.Entry<String, Map<Integer, String>> entry : combinationsGeneralizedDatasetsMap.entrySet()) {
			String combination = entry.getKey();
			Map<Integer, String> generalizedDataset = entry.getValue();

			rowKanonymityMapOfGeneralizedDatasets.put(combination, createRowKAnonymityMap(generalizedDataset));
		}

		return rowKanonymityMapOfGeneralizedDatasets;
	}

	private static String getMaxKGeneralizationRule(
			Map<String, Map<String, Integer>> rowKanonymityMapOfGeneralizedDatasets, int k, List<String> toExclude)
			throws Exception {
		int maxK = -1;
		String maxGen = "";

		for (Map.Entry<String, Map<String, Integer>> entry : rowKanonymityMapOfGeneralizedDatasets.entrySet()) {
			String combination = entry.getKey();
			Map<String, Integer> generalizedDatasetRowKanonymityMap = entry.getValue();

			int maxOfGenDataset = getMaxOfGenDataset(generalizedDatasetRowKanonymityMap, k);

			if (maxOfGenDataset > maxK) {
				maxK = maxOfGenDataset;
				maxGen = combination;
			}

			if (maxOfGenDataset < k) {
				toExclude.add(combination);
			}
		}

		return maxGen;
	}

	private static void generalize(Map<Integer, String> generalizedDatasetWithMaxK,
			Map<String, Integer> generalizedDatasetRowKanonymityMap, int maxK, Map<Integer, String> dataset,
			Map<Integer, String> unsafeDataset) throws Exception {
		for (Map.Entry<Integer, String> entry : generalizedDatasetWithMaxK.entrySet()) {
			int rowID = entry.getKey();
			String row = entry.getValue();

			if (generalizedDatasetRowKanonymityMap.get(row) == maxK) {
				dataset.put(rowID, row);
				unsafeDataset.remove(rowID);
			}
		}
	}

	private static Map<Integer, List<String>> getNumberOfCombinedSuppresionsSuppressionsMap(
			List<String> suppressionCombinations) throws Exception {
		Map<Integer, List<String>> numberOfCombinedSuppresionsSuppressionsMap = new HashMap<Integer, List<String>>();
		for (String suppressionCombination : suppressionCombinations) {
			int numberOfCombinedSuppresions = suppressionCombination.split(" ").length;

			List<String> combinations;
			if (numberOfCombinedSuppresionsSuppressionsMap.containsKey(numberOfCombinedSuppresions)) {
				combinations = numberOfCombinedSuppresionsSuppressionsMap.get(numberOfCombinedSuppresions);
			} else {
				combinations = new ArrayList<String>();
			}

			combinations.add(suppressionCombination);

			numberOfCombinedSuppresionsSuppressionsMap.put(numberOfCombinedSuppresions, combinations);
		}

		return numberOfCombinedSuppresionsSuppressionsMap;
	}

	private static Map<String, Map<Integer, String>> getCombinationsDatasetWithoutQIsMap(List<String> combinations,
			Map<Integer, String> dataset) throws Exception {
		Map<String, Map<Integer, String>> combinationsDatasetWithoutQIsMap = new HashMap<String, Map<Integer, String>>();

		for (String combination : combinations) {
			Map<Integer, String> datasetWithoutQIs = new HashMap<Integer, String>();

			for (Map.Entry<Integer, String> entry : dataset.entrySet()) {
				int rowID = entry.getKey();
				String row = entry.getValue();

				datasetWithoutQIs.put(rowID, suppressRow(combination, row));
			}

			combinationsDatasetWithoutQIsMap.put(combination, datasetWithoutQIs);
		}

		return combinationsDatasetWithoutQIsMap;
	}

	private static String suppressRow(String combination, String row) throws Exception {
		String suppressedRow = "";

		List<String> QIs = new ArrayList<String>(Arrays.asList(combination.split(" ")));

		String fields[] = row.split(";");
		for (int f = 0; f < fields.length; f++) {
			for (String QI : QIs) {
				if (f == Integer.parseInt(QI)) {
					fields[f] = "*";
					break;
				}
			}
			suppressedRow += fields[f] + ";";
		}
		suppressedRow = suppressedRow.substring(0, suppressedRow.length() - 1);

		return suppressedRow;
	}

	private static Map<String, Map<String, Integer>> getCombinationsDatasetsWithoutQIsRowKanonymityMap(
			Map<String, Map<Integer, String>> combinationsDatasetWithoutQIsMap) throws Exception {
		Map<String, Map<String, Integer>> combinationsDatasetsWithoutQIsRowKanonymityMap = new HashMap<String, Map<String, Integer>>();

		for (Map.Entry<String, Map<Integer, String>> entry : combinationsDatasetWithoutQIsMap.entrySet()) {
			combinationsDatasetsWithoutQIsRowKanonymityMap.put(entry.getKey(),
					createRowKAnonymityMap(entry.getValue()));
		}

		return combinationsDatasetsWithoutQIsRowKanonymityMap;
	}

	private static Map<String, String> getCombinationsSuppressedRowMap(String row, List<String> combinations)
			throws Exception {
		Map<String, String> combinationsSuppressedRowMap = new HashMap<String, String>();

		for (String combination : combinations) {
			combinationsSuppressedRowMap.put(combination, suppressRow(combination, row));
		}

		return combinationsSuppressedRowMap;
	}

	private static String getMaxKanonymityCombination(Map<String, String> combinationsSuppressedRowMap,
			Map<String, Map<String, Integer>> combinationsDatasetsWithoutQIsRowKanonymityMap, int k,
			List<String> toExclude, Map<Integer, String> dataset, int unsafeRowID) throws Exception {
		String maxKanonymityCombination = "";
		int maxK = -1;

		for (Map.Entry<String, String> entry : combinationsSuppressedRowMap.entrySet()) {
			String combination = entry.getKey();
			String row = entry.getValue();

			if (combinationsDatasetsWithoutQIsRowKanonymityMap.get(combination).containsKey(row)) {
				int Kanonymity = combinationsDatasetsWithoutQIsRowKanonymityMap.get(combination).get(row);

				if (Kanonymity > maxK && Kanonymity >= k && KAnonymityCheckForRowsWithSuppression.checkIfCovered(combination, row, dataset, unsafeRowID)) {
					maxK = Kanonymity;
					maxKanonymityCombination = combination;
				}
			}
		}

		return maxKanonymityCombination;
	}

	private static void suppressRemainingRows(Map<Integer, String> unsafeDataset, int QIsSize,
			Map<Integer, String> dataset) throws Exception {
		for (Map.Entry<Integer, String> entry : unsafeDataset.entrySet()) {
			int rowID = entry.getKey();

			String suppressedRow = "";
			for (int i = 0; i < QIsSize; i++) {
				suppressedRow += "*;";
			}
			suppressedRow = suppressedRow.substring(0, suppressedRow.length() - 1);

			dataset.put(rowID, suppressedRow);
		}
	}

	private static String header(int QIsSize, String QIsNames[]) throws Exception {
		String header = "";

		for (int i = 0; i < QIsSize; i++) {
			header += QIsNames[i] + ";";
		}

		header = header.substring(0, header.length() - 1);

		return header;
	}

	private static void writeDataset(Map<Integer, String> dataset, BufferedWriter bw) throws Exception {
		for (Map.Entry<Integer, String> entry : dataset.entrySet()) {
			bw.write(entry.getValue() + "\n");
		}
	}

}
