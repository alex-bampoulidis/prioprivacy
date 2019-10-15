package prioprivacy.algorithm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PrioPrivacy {

    public static int unsafe = -1;

    public static Map<Integer, String> Algorithm(Map<Integer, List<Integer>> priorities,
            Map<Integer, Map<Integer, List<String>>> levelsQIsRulesMap, Map<Integer, String> dataset, int k,
            Map<Integer, List<String>> QIsDomainsMap) throws Exception {
        Map<Integer, String> anonymisedDataset = new HashMap<Integer, String>(dataset);

        Map<Integer, String> unsafeDataset = AlgorithmOtherFunctions.createUnsafeDataset(anonymisedDataset, k);
        unsafe = unsafeDataset.size();

        List<Integer> QIs = new ArrayList<Integer>();

        List<String> generalisationRulesToExclude = new ArrayList<String>();

        List<String> suppRulesToExclude = new ArrayList<String>();

        for (int p = priorities.size(); p >= 1; p--) {
            if (unsafeDataset.isEmpty()) {
                break;
            }

            for (int QI : priorities.get(p)) {
                QIs.add(QI);

                Generalisation.generalisation(levelsQIsRulesMap, QIs, k, anonymisedDataset, unsafeDataset,
                        generalisationRulesToExclude);
                unsafe = unsafeDataset.size();

                Suppression.suppression(QIs, dataset, anonymisedDataset, unsafeDataset, k, suppRulesToExclude,
                        QIsDomainsMap);
                unsafe = unsafeDataset.size();
            }
        }
        
        for (int id : unsafeDataset.keySet()) {
            String row = "";
            for (int QI : QIs) {
                row += "*;";
            }
            row = row.substring(0, row.length() - 1);
            
            anonymisedDataset.put(id, row);
        }
        
        unsafeDataset.clear();
        unsafe = 0;

        return anonymisedDataset;
    }
}
