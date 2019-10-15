package prioprivacy.algorithm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Generalisation {

    public static void generalisation(Map<Integer, Map<Integer, List<String>>> levelsQIsRulesMap, List<Integer> QIs,
            int k, Map<Integer, String> dataset, Map<Integer, String> unsafeDataset,
            List<String> geneneralizationCombinationsToExclude) throws Exception {
        for (int level = 1; level < 999; level++) {
            if (!levelsQIsRulesMap.containsKey(level)) {
                break;
            }

            Map<Integer, List<String>> QIsInLevelRulesMap = AlgorithmOtherFunctions
                    .getQIsInLevelRulesMap(levelsQIsRulesMap, level, QIs);

            if (QIsInLevelRulesMap.isEmpty()) {
                break;
            }

            List<Integer> QIsInLevel = new ArrayList<Integer>(QIsInLevelRulesMap.keySet());

            List<String> QIsGeneralizationCombinations = AlgorithmOtherFunctions.getCombinations(QIsInLevel,
                    QIsInLevel.size(), geneneralizationCombinationsToExclude);

            Map<Integer, List<String>> numberOfCombinedRulesRulesMap = AlgorithmOtherFunctions
                    .numberOfCombinedRulesRulesMap(QIsGeneralizationCombinations, QIsInLevelRulesMap);

            for (int c = 1; c <= QIsInLevel.size(); c++) {
                boolean noMore = false;
                while (!noMore) {
                    int maxK = -1;
                    Map<Integer, String> maxKGeneralisation = new HashMap<Integer, String>();
                    Map<String, Integer> maxKGeneralisationRowKAnonymityMap = new HashMap<String, Integer>();

                    List<String> CCombinationsOfRules = numberOfCombinedRulesRulesMap.get(c);

                    for (String rule : CCombinationsOfRules) {
                        if (!geneneralizationCombinationsToExclude.contains(rule)) {
                            Map<Integer, Map<String, String>> QIsRulesRulesMappingMap = AlgorithmOtherFunctions
                                    .getQIsRulesRulesMappingMap(rule);

                            Map<Integer, String> temp = generaliseDataset(unsafeDataset, QIsRulesRulesMappingMap);

                            Map<String, Integer> rowKAnonymityMap = AlgorithmOtherFunctions
                                    .createRowKAnonymityMap(temp);

                            int ruleK;
                            if (temp.size() == 0) {
                                ruleK = 0;
                            } else {
                                ruleK = Collections.max(rowKAnonymityMap.values());
                            }

                            if (ruleK > maxK) {
                                maxK = ruleK;
                                maxKGeneralisation = new HashMap<Integer, String>(temp);
                                maxKGeneralisationRowKAnonymityMap = new HashMap<String, Integer>(rowKAnonymityMap);
                            }

                            if (ruleK < k) {
                                geneneralizationCombinationsToExclude.add(rule);
                            }
                        }
                    }

                    if (maxK < k) {
                        break;
                    }

                    applyGeneralisationRuleToMaxKRows(dataset, unsafeDataset, maxK, maxKGeneralisation,
                            maxKGeneralisationRowKAnonymityMap);
                    PrioPrivacy.unsafe = unsafeDataset.size();
                }
            }
        }
    }

    private static Map<Integer, String> generaliseDataset(Map<Integer, String> dataset,
            Map<Integer, Map<String, String>> QIsRulesRulesMappingMap) throws Exception {
        Map<Integer, String> generalizedDataset = new HashMap<Integer, String>();

        for (Map.Entry<Integer, String> entry : dataset.entrySet()) {
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

    private static void applyGeneralisationRuleToMaxKRows(Map<Integer, String> dataset,
            Map<Integer, String> unsafeDataset, int maxK, Map<Integer, String> maxKGeneralisation,
            Map<String, Integer> maxKGeneralisationRowKAnonymityMap) throws Exception {
        for (Map.Entry<Integer, String> entry : maxKGeneralisation.entrySet()) {
            int rowID = entry.getKey();
            String row = entry.getValue();

            if (maxKGeneralisationRowKAnonymityMap.get(row) == maxK) {
                dataset.put(rowID, row);
                unsafeDataset.remove(rowID);
            }
        }
    }

}
