package experiments;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.deidentifier.arx.ARXConfiguration;
import org.deidentifier.arx.AttributeType;
import org.deidentifier.arx.Data;
import org.deidentifier.arx.DataHandle;
import org.deidentifier.arx.DataType;
import org.deidentifier.arx.aggregates.StatisticsQuality;
import org.deidentifier.arx.common.WrappedBoolean;
import org.deidentifier.arx.common.WrappedInteger;
import org.deidentifier.arx.criteria.KAnonymity;

public class EvaluateAttributeLevel {

	private static String input = "C:\\Users\\abampoulidis\\Desktop\\safedeed\\conferences\\tbd-demo-short\\repo\\input\\adult_9.csv";
	private static String outputPath = "C:\\Users\\abampoulidis\\Desktop\\safedeed\\conferences\\tbd-demo-short\\repo\\output\\";
	private static String hierarchiesPath = "C:\\Users\\abampoulidis\\Desktop\\safedeed\\conferences\\tbd-demo-short\\repo\\generalization_hierarchies\\arx\\";
	private static String QIs[] = { "sex", "salary-class", "race", "workclass", "marital-status", "occupation",
			"education", "native-country", "age" };
	private static String methods[] = { "arx_nue", "prioprivacy" };
	private static String output = outputPath + "results_attribute_level.csv";

	public static void main(String args[]) throws Exception {
		BufferedWriter bw = new BufferedWriter(
				new OutputStreamWriter(new FileOutputStream(output), StandardCharsets.UTF_8));

		bw.write("k,attribute,gen. intensity, granularity,n.u. entropy,sq. err.,experiment,method\n");

		for (String method : methods) {
			for (int experiment = 1; experiment <= 4; experiment++) {
				for (int k = 2; k <= 10; k++) {
					System.out.println(method + "\t" + experiment + "\t" + k);

					Data dataOrig = Data.create(input, StandardCharsets.UTF_8, ';');
					Data dataTransformed = Data.create(
							outputPath + "case" + experiment + "\\" + method + "\\k_" + k + ".csv",
							StandardCharsets.UTF_8, ';');

					for (String QI : QIs) {
						dataOrig.getDefinition().setAttributeType(QI, AttributeType.QUASI_IDENTIFYING_ATTRIBUTE);
						dataOrig.getDefinition().setAttributeType(QI, AttributeType.Hierarchy.create(
								hierarchiesPath + "adult_hierarchy_" + QI + ".csv", StandardCharsets.UTF_8, ';'));

						dataTransformed.getDefinition().setAttributeType(QI, AttributeType.QUASI_IDENTIFYING_ATTRIBUTE);
						dataTransformed.getDefinition().setAttributeType(QI, AttributeType.Hierarchy.create(
								hierarchiesPath + "adult_hierarchy_" + QI + ".csv", StandardCharsets.UTF_8, ';'));

						if (QI.equals("age")) {
							dataOrig.getDefinition().setDataType(QI, DataType.INTEGER);
							dataTransformed.getDefinition().setDataType(QI, DataType.INTEGER);
						}
					}

					DataHandle dataOrigHandle = dataOrig.getHandle();
					DataHandle dataTransormedHandle = dataTransformed.getHandle();

					ARXConfiguration config = ARXConfiguration.create();
					config.addPrivacyModel(new KAnonymity(k));

					setAttributeWeights(config, experiment);

					Set<String> QIsAsSet = new HashSet(Arrays.asList(QIs));

					StatisticsQuality statQ = new StatisticsQuality(dataOrigHandle, dataTransormedHandle, config,
							new WrappedBoolean(), new WrappedInteger(), QIsAsSet);

					for (String QI : QIs) {
						bw.write(k + "," + QI + ",");
						bw.write(statQ.getGeneralizationIntensity().getValue(QI) + ",");
						bw.write(statQ.getGranularity().getValue(QI) + ",");
						bw.write(statQ.getNonUniformEntropy().getValue(QI) + ",");
						bw.write(statQ.getAttributeLevelSquaredError().getValue(QI) + ",");
						bw.write("exp" + experiment + "," + method + "\n");
					}
				}
			}
		}

		bw.close();
	}

	private static void setAttributeWeights(ARXConfiguration config, int experiment) throws Exception {
		if (experiment == 1) {
			config.setAttributeWeight("sex", 1d);
			config.setAttributeWeight("salary-class", 1d);
			config.setAttributeWeight("race", 6d / 7);
			config.setAttributeWeight("workclass", 5d / 7);
			config.setAttributeWeight("marital-status", 5d / 7);
			config.setAttributeWeight("occupation", 4d / 7);
			config.setAttributeWeight("education", 3d / 7);
			config.setAttributeWeight("native-country", 2d / 7);
			config.setAttributeWeight("age", 1d / 7);
		} else if (experiment == 2) {
			config.setAttributeWeight("sex", 1d / 7);
			config.setAttributeWeight("salary-class", 1d / 7);
			config.setAttributeWeight("race", 2d / 7);
			config.setAttributeWeight("workclass", 3d / 7);
			config.setAttributeWeight("marital-status", 3d / 7);
			config.setAttributeWeight("occupation", 4d / 7);
			config.setAttributeWeight("education", 5d / 7);
			config.setAttributeWeight("native-country", 6d / 7);
			config.setAttributeWeight("age", 1d);
		} else if (experiment == 3) {
			config.setAttributeWeight("age", 1d);
			config.setAttributeWeight("sex", 8d / 9);
			config.setAttributeWeight("native-country", 7d / 9);
			config.setAttributeWeight("salary-class", 6d / 9);
			config.setAttributeWeight("education", 5d / 9);
			config.setAttributeWeight("race", 4d / 9);
			config.setAttributeWeight("occupation", 3d / 9);
			config.setAttributeWeight("workclass", 2d / 9);
			config.setAttributeWeight("marital-status", 1d / 9);
		} else {
			config.setAttributeWeight("age", 8d / 9);
			config.setAttributeWeight("sex", 1d);
			config.setAttributeWeight("native-country", 6d / 9);
			config.setAttributeWeight("salary-class", 7d / 9);
			config.setAttributeWeight("education", 4d / 9);
			config.setAttributeWeight("race", 5d / 9);
			config.setAttributeWeight("occupation", 2d / 9);
			config.setAttributeWeight("workclass", 3d / 9);
			config.setAttributeWeight("marital-status", 1d / 9);
		}
	}
}
