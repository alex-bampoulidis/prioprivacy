package prioprivacy.algorithm;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SuppressionOneThread {

    public static void suppression(List<Integer> QIs, Map<Integer, String> originalDataset,
            Map<Integer, String> dataset, Map<Integer, String> unsafeDataset, int k, List<String> suppRulesToExclude,
            Map<Integer, List<String>> QIsDomainsMap) throws Exception {
        List<String> suppressionCombinations = AlgorithmOtherFunctions.getCombinations(QIs, QIs.size(),
                suppRulesToExclude);

        Map<Integer, List<String>> numberOfCombinedSuppresionsSuppressionsMap = AlgorithmOtherFunctions
                .getNumberOfCombinedSuppresionsSuppressionsMap(suppressionCombinations);

        for (int c = 1; c <= QIs.size(); c++) {
            List<String> combinations = numberOfCombinedSuppresionsSuppressionsMap.get(c);

            Map<String, Map<String, Integer>> suppCombSuppRowKMap = new HashMap<String, Map<String, Integer>>();
            for (String combination : combinations) {
                Map<String, Integer> suppRowKMap = new HashMap<String, Integer>();

                for (Map.Entry<Integer, String> entry : originalDataset.entrySet()) {
                    String row = entry.getValue();

                    String suppressedRow = applySuppression(combination, row);

                    if (suppRowKMap.containsKey(suppressedRow)) {
                        suppRowKMap.put(suppressedRow, suppRowKMap.get(suppressedRow) + 1);
                    } else {
                        suppRowKMap.put(suppressedRow, 1);
                    }
                }

                suppCombSuppRowKMap.put(combination, suppRowKMap);
            }

            List<Integer> unsafeRowsIDs = new ArrayList<Integer>(unsafeDataset.keySet());

            for (int i = 0; i < unsafeRowsIDs.size(); i++) {
                suppression(i, unsafeRowsIDs, unsafeDataset, combinations, originalDataset, k,
                        dataset, suppCombSuppRowKMap, QIsDomainsMap);
            }

            suppRulesToExclude.addAll(suppressionCombinations);
        }
    }

    private static void suppression(int unsafeRowIDIndex, List<Integer> unsafeRowIDs,
            Map<Integer, String> unsafeDataset, List<String> combinations, Map<Integer, String> originalDataset, int k,
            Map<Integer, String> dataset, Map<String, Map<String, Integer>> suppCombSuppRowKMap,
            Map<Integer, List<String>> QIsDomainsMap// , List<String> suppRowsSatisfyingK,
    // List<String> suppRowsNotSatisfyingK
    ) throws Exception {
        String row = unsafeDataset.get(unsafeRowIDs.get(unsafeRowIDIndex));

        Map<String, Integer> suppRowKMap = new HashMap<String, Integer>();

        for (String supp : combinations) {
            String suppressedRow = applySuppression(supp, row);

            suppRowKMap.put(supp, suppCombSuppRowKMap.get(supp).get(suppressedRow));
        }

//		Map<String, Integer> sortedSuppRowKMap = suppRowKMap.entrySet().stream().sorted(comparingByValue())
//				.collect(toMap(e -> e.getKey(), e -> e.getValue(), (e1, e2) -> e2, LinkedHashMap::new));
        for (Map.Entry<String, Integer> entry : suppRowKMap.entrySet()) {
//		for (Map.Entry<String, Integer> entry : sortedSuppRowKMap.entrySet()) {
            String supp = entry.getKey();
            int similarRows = entry.getValue() - 1;

            if (similarRows >= (k - 1)) {
                List<Integer> QIsSuppressed = KAnonymityCheckForRowsWithSuppression.getQIsSuppressed(supp);

                int domainSize = 1;
                for (int QI : QIsSuppressed) {
                    domainSize *= QIsDomainsMap.get(QI).size();
                }

                if (domainSize > k) {
                    if (similarRows < domainSize - k) {
                        dataset.put(unsafeRowIDs.get(unsafeRowIDIndex), applySuppression(supp, row));
                        unsafeDataset.remove(unsafeRowIDs.get(unsafeRowIDIndex));

                        break;
                    }

                    boolean isKAnonymous = KAnonymityCheckForRowsWithSuppression.getKAnonymityOfSuppressedRow(supp,
                            applySuppression(supp, row), originalDataset, unsafeRowIDs.get(unsafeRowIDIndex), k,
                            dataset, QIsDomainsMap);

                    if (isKAnonymous) {
                        dataset.put(unsafeRowIDs.get(unsafeRowIDIndex), applySuppression(supp, row));
                        unsafeDataset.remove(unsafeRowIDs.get(unsafeRowIDIndex));
                        PrioPrivacyOneThread.unsafe = unsafeDataset.size();

                        break;
                    }
                }
            }

        }

//		String row = unsafeDataset.get(unsafeRowIDs.get(unsafeRowIDIndex));
//
//		for (String supp : combinations) {
//			String suppressedRow = applySuppression(supp, row);
//
//			boolean isKAnonymous;
//			if (suppRowsSatisfyingK.contains(suppressedRow)) {
//				isKAnonymous = true;
//			} else {
//				if (suppRowsNotSatisfyingK.contains(suppressedRow)) {
//					isKAnonymous = false;
//				} else {
//					isKAnonymous = KAnonymityCheckForRowsWithSuppression.getKAnonymityOfSuppressedRow(supp,
//							suppressedRow, originalDataset, unsafeRowIDs.get(unsafeRowIDIndex), k, dataset, QIsDomainsMap);
//
//					if (isKAnonymous) {
//						suppRowsSatisfyingK.add(suppressedRow);
//					} else {
//						suppRowsNotSatisfyingK.add(suppressedRow);
//					}
//				}
//			}
//
////			int isKanonymous = KAnonymityCheckForRowsWithSuppression.getKAnonymityOfSuppressedRow(supp, suppressedRow,
////					originalDataset, unsafeRowIDs.get(unsafeRowIDIndex), k, dataset);
//
//			if (isKAnonymous) {
////			if (isKanonymous >= k) {
//				dataset.put(unsafeRowIDs.get(unsafeRowIDIndex), suppressedRow);
//				unsafeDataset.remove(unsafeRowIDs.get(unsafeRowIDIndex));
//
//				break;
//			}
//		}
    }

    public static String applySuppression(String combination, String row) throws Exception {
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

}
