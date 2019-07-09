package prioprivacy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AlgorithmOtherFunctions {

	public static Map<Integer, String> createUnsafeDataset(Map<Integer, String> dataset, int k) throws Exception {
		Map<Integer, String> unsafeDataset = new HashMap<Integer, String>();

		Map<String, Integer> rowKanonymityMapOfDataset = createRowKAnonymityMap(dataset);

		for (Map.Entry<Integer, String> entry : dataset.entrySet()) {
			if (rowKanonymityMapOfDataset.get(entry.getValue()) < k) {
				unsafeDataset.put(entry.getKey(), entry.getValue());
			}
		}

		return unsafeDataset;
	}

	public static Map<String, Integer> createRowKAnonymityMap(Map<Integer, String> dataset) throws Exception {
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

	public static Map<Integer, List<String>> getQIsInLevelRulesMap(
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
	
	public static List<String> getCombinations(List<Integer> QIs, int N, List<String> toExclude) {
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
	
	public static void combinations(int[] sequence, int[] data, int start, int end, int index, int r,
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
	
	public static Map<Integer, List<String>> numberOfCombinedRulesRulesMap(List<String> QIsCombinations,
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
	
	public static void GeneratePermutations(List<List<String>> Lists, List<String> result, int depth, String current) {
		if (depth == Lists.size()) {
			result.add(current);
			return;
		}

		for (int i = 0; i < Lists.get(depth).size(); ++i) {
			GeneratePermutations(Lists, result, depth + 1, current + Lists.get(depth).get(i) + "\t");
		}
	}
	
	public static Map<Integer, Map<String, String>> getQIsRulesRulesMappingMap(String combination) throws Exception {
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
	
	public static Map<Integer, List<String>> getNumberOfCombinedSuppresionsSuppressionsMap(
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
}
