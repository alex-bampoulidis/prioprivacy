package prioprivacy;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class KAnonymityCheckForRowsWithSuppression {

	public static boolean getKAnonymityOfSuppressedRow(String combination, String suppRow,
			Map<Integer, String> originalDataset, int unsafeRowID, int k, Map<Integer, String> anonymizedDataset,
			Map<Integer, List<String>> QIsDomainsMap) throws Exception {
		List<Integer> QIsSuppressed = getQIsSuppressed(combination);

		int domainSize = 1;
		for (int QI : QIsSuppressed) {
			domainSize *= QIsDomainsMap.get(QI).size();
		}

		if (domainSize <= k) {
			return false;
		}

		int similarRows = 0;
		List<String> uniqueSimilarRows = new ArrayList<String>();

		for (Map.Entry<Integer, String> entry : originalDataset.entrySet()) {
			int rowID = entry.getKey();
			String row = entry.getValue();

			if (rowID != unsafeRowID) {
				if (Suppression.applySuppression(combination, row).equals(suppRow)) {
					similarRows++;

					if (!uniqueSimilarRows.contains(row) && !row.equals(originalDataset.get(unsafeRowID))) {
						uniqueSimilarRows.add(row);
					}
				}
			}

			if ((domainSize - uniqueSimilarRows.size()) < k) {
				return false;
			}

			if ((similarRows >= (k - 1))
					&& ((domainSize - uniqueSimilarRows.size() - k) >= (originalDataset.size() - (rowID + 1)))) {
				return true;
			}
		}

		return false;

//		if ((domainSize <= k)) {
//			return -1;
//		}
//
//		List<String> similarRows = getSimilarRows(suppRow, originalDataset, unsafeRowID);
////		Map<Integer, String> similarRowsIDsRows = getSimilarRowsIDsRows(row, originalDataset, unsafeRowID);
//
//		System.out.println(originalDataset.get(unsafeRowID) + "\n\t" + suppRow);
//
////		for (Map.Entry<Integer, String> entry : similarRowsIDsRows.entrySet()) {
////			System.out.println("\t\t" + entry.getValue() + "\t" + anonymizedDataset.get(entry.getKey()));
////		}
//
//		for (String s : similarRows) {
//			System.out.println("\t\t" + s);
//		}
//
//		if (similarRows.size() < (k - 1)) {
//			return -1;
//		}
//
//		List<String> uniqueSimilarRows = getUniqueSimilarRows(similarRows);
//
//		for (String u : uniqueSimilarRows) {
//			System.out.println("\t\t\t" + u);
//		}
//
//		if (uniqueSimilarRows.contains(originalDataset.get(unsafeRowID))) {
//			uniqueSimilarRows.remove(originalDataset.get(unsafeRowID));
//		}
//
//		if ((domain.size() - uniqueSimilarRows.size()) < k) {
//			return -1;
//		}
//
//		System.out.println(domain.size() - uniqueSimilarRows.size());
//
//		return domain.size() - uniqueSimilarRows.size();
	}

	public static List<Integer> getQIsSuppressed(String combination) throws Exception {
		List<Integer> QIsSuppressed = new ArrayList<Integer>();

		for (String QI : combination.split(" ")) {
			QIsSuppressed.add(Integer.parseInt(QI));
		}

		Collections.sort(QIsSuppressed);

		return QIsSuppressed;
	}

	private static List<String> getSimilarRows(String suppRow, Map<Integer, String> dataset, int unsafeRowID)
			throws Exception {
		List<String> similarRows = new ArrayList<String>();

		for (Map.Entry<Integer, String> entry : dataset.entrySet()) {
			int rowID = entry.getKey();
			String row = entry.getValue();

			if (rowID != unsafeRowID) {
				if (row.matches(suppRow.replaceAll("\\*", ".*"))) {
					similarRows.add(row);
				}
			}
		}

		return similarRows;
	}

	private static Map<Integer, String> getSimilarRowsIDsRows(String suppRow, Map<Integer, String> dataset,
			int unsafeRowID) throws Exception {
		Map<Integer, String> similarRows = new HashMap<Integer, String>();

		for (Map.Entry<Integer, String> entry : dataset.entrySet()) {
			int rowID = entry.getKey();
			String row = entry.getValue();

			if (rowID != unsafeRowID) {
				if (row.matches(suppRow.replaceAll("\\*", ".*"))) {
					similarRows.put(rowID, row);
				}
			}
		}

		return similarRows;
	}

	private static List<String> getUniqueSimilarRows(List<String> similarRows) throws Exception {
		List<String> uniqueSimilarRows = new ArrayList<String>();

		for (String similarRow : similarRows) {
			if (!uniqueSimilarRows.contains(similarRow)) {
				uniqueSimilarRows.add(similarRow);
			}
		}

		return uniqueSimilarRows;
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
}
