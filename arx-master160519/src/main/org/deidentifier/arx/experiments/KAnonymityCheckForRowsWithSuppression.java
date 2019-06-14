package org.deidentifier.arx.experiments;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.mahout.cf.taste.hadoop.item.ToVectorAndPrefReducer;

public class KAnonymityCheckForRowsWithSuppression {

	public static boolean checkIfCovered(String combination, String row, Map<Integer, String> dataset, int unsafeRowID)
			throws Exception {
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

		List<Integer> QIsSuppressed = getQIsSuppressed(combination);
		
		int totalDomain = 1;
		for (int QI : QIsSuppressed) {
			totalDomain *= QIsDomainsMap.get(QI).size();
		}
		if (totalDomain > 30162) {
			return true;
		}

		List<String> domain = createDomain(QIsSuppressed, QIsDomainsMap);		
		
		List<String> covered = new ArrayList<String>();

		List<String> similarRows = getSimilarRows(row, dataset, unsafeRowID);

		if ((domain.size() - 1) > similarRows.size()) {
			return true;
		}

		for (String similarRow : similarRows) {
			String toCover = createToCoverString(QIsSuppressed, similarRow);
			
			if (domain.contains(toCover) && !covered.contains(toCover)) {
				covered.add(toCover);
			}
			
			if (covered.size() == domain.size()) {
				break;
			}
		}

		if (covered.size() != (domain.size() - 1)) {
			return true;
		}
		
//		System.out.println(row);
//		for (String similarRow : similarRows) {
//			System.out.println("\t" + similarRow);
//		}
		return false;
	}

	private static List<Integer> getQIsSuppressed(String combination) throws Exception {
		List<Integer> QIsSuppressed = new ArrayList<Integer>();

		for (String QI : combination.split(" ")) {
			QIsSuppressed.add(Integer.parseInt(QI));
		}

		Collections.sort(QIsSuppressed);

		return QIsSuppressed;
	}

	private static List<String> createDomain(List<Integer> QIsSuppressed, Map<Integer, List<String>> QIsDomainsMap)
			throws Exception {
		List<String> temp = new ArrayList<String>();

		List<List<String>> lists = new ArrayList<List<String>>();
		for (int QI : QIsSuppressed) {
			lists.add(QIsDomainsMap.get(QI));
		}

		GeneratePermutations(lists, temp, 0, "");

		List<String> domain = new ArrayList<String>();
		for (String t : temp) {
			t = t.replaceAll("\t", ";");
			t = t.substring(0, t.length() - 1);

			domain.add(t);
		}

		return domain;
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

	private static void GeneratePermutations(List<List<String>> Lists, List<String> result, int depth, String current) {
		if (depth == Lists.size()) {
			result.add(current);
			return;
		}

		for (int i = 0; i < Lists.get(depth).size(); ++i) {
			GeneratePermutations(Lists, result, depth + 1, current + Lists.get(depth).get(i) + "\t");
		}
	}

	private static String createToCoverString(List<Integer> QIsSuppressed, String row) throws Exception {
		String toCover = "";

		String fields[] = row.split(";");
		for (int i = 0; i < fields.length; i++) {
			if (QIsSuppressed.contains(i)) {
				toCover += fields[i] + ";";
			}
		}
		toCover = toCover.substring(0, toCover.length() - 1);

		return toCover;
	}
}
