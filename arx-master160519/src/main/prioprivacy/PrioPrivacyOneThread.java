package prioprivacy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PrioPrivacyOneThread {

	public static Map<Integer, String> Algorithm(Map<Integer, List<Integer>> priorities,
			Map<Integer, Map<Integer, List<String>>> levelsQIsRulesMap, Map<Integer, String> dataset, int k,
			Map<Integer, List<String>> QIsDomainsMap) throws Exception {
		Map<Integer, String> anonymizedDataset = new HashMap<Integer, String>(dataset);

		Map<Integer, String> unsafeDataset = AlgorithmOtherFunctions.createUnsafeDataset(anonymizedDataset, k);

		List<Integer> QIs = new ArrayList<Integer>();

		List<String> generalisationRulesToExclude = new ArrayList<String>();

		List<String> suppRulesToExclude = new ArrayList<String>();

		for (int p = priorities.size(); p >= 1; p--) {
			if (unsafeDataset.isEmpty()) {
				break;
			}

			for (int QI : priorities.get(p)) {
				QIs.add(QI);

				Generalisation.generalisation(levelsQIsRulesMap, QIs, k, anonymizedDataset, unsafeDataset,
						generalisationRulesToExclude);

				SuppressionOneThread.suppression(QIs, dataset, anonymizedDataset, unsafeDataset, k, suppRulesToExclude,
						QIsDomainsMap);
			}
		}

		return anonymizedDataset;
	}
}
