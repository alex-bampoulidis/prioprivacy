package org.deidentifier.arx.experiments;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.deidentifier.arx.ARXAnonymizer;
import org.deidentifier.arx.ARXConfiguration;
import org.deidentifier.arx.ARXResult;
import org.deidentifier.arx.AttributeType;
import org.deidentifier.arx.Data;
import org.deidentifier.arx.DataHandle;
import org.deidentifier.arx.DataType;
import org.deidentifier.arx.aggregates.StatisticsQuality;
import org.deidentifier.arx.common.WrappedBoolean;
import org.deidentifier.arx.common.WrappedInteger;
import org.deidentifier.arx.criteria.KAnonymity;
import org.deidentifier.arx.metric.Metric;
import org.deidentifier.arx.metric.Metric.AggregateFunction;

public class MeasureARXDatasetLevel {

	private static String datasetPath = "C:\\Users\\abampoulidis\\Desktop\\safedeed\\conferences\\tbd-demo-short\\input\\";
	private static String generatedDataPath = "C:\\Users\\abampoulidis\\Desktop\\safedeed\\conferences\\tbd-demo-short\\output\\case1\\arx\\";
	private static String hierarchiesPath = "C:\\Users\\abampoulidis\\Desktop\\safedeed\\conferences\\tbd-demo-short\\generalization_hierarchies\\arx\\";
	private static String QIss[] = { "sex", "salary-class", "race", "workclass", "marital-status", "occupation",
			"education", "native-country", "age" };
	private static String qualityMetrics[] = { "Loss", "Non-Uniform Entropy" };

	public static void main(String args[]) throws Exception {
		for (String metric : qualityMetrics) {

			String output = generatedDataPath + "..\\arx_dataset_level_" + metric + ".csv";

			BufferedWriter bw = new BufferedWriter(
					new OutputStreamWriter(new FileOutputStream(output), StandardCharsets.UTF_8));

			bw.write("QIs,k,metric,gen. intensity,granularity,n.u. entropy,record-level sq. err.,att.-level sq. err.\n");

			for (int QISize = 9; QISize <= 9; QISize++) {
				String QIs[] = new String[QISize];
				for (int q = 0; q < QIs.length; q++) {
					QIs[q] = QIss[q];
				}

				for (int k = 2; k <= 10; k++) {

					Data dataOrig = Data.create(datasetPath + "adult_" + QISize + ".csv", StandardCharsets.UTF_8, ';');
					Data dataTransformed = Data.create(
							generatedDataPath + metric + "_QIs_" + QISize + "_k_" + k + ".csv", StandardCharsets.UTF_8,
							';');

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
					
					// setting weights does not change the result
//					setAttributeWeights(config, QISize);

					Set<String> QIsAsSet = new HashSet(Arrays.asList(QIs));

					StatisticsQuality statQ = new StatisticsQuality(dataOrigHandle, dataTransormedHandle, config,
							new WrappedBoolean(), new WrappedInteger(), QIsAsSet);

					System.out.println(QISize + "," + k + ",");
					bw.write(QISize + "," + k + "," + metric + ",");
					bw.write(statQ.getGeneralizationIntensity().getArithmeticMean() + ",");
					bw.write(statQ.getGranularity().getArithmeticMean() + ",");
					bw.write(statQ.getNonUniformEntropy().getArithmeticMean() + ",");
					bw.write(statQ.getRecordLevelSquaredError().getValue() + ",");
					bw.write(statQ.getAttributeLevelSquaredError().getArithmeticMean() + "\n");
				}
			}
			bw.close();
		}
	}

	private static void setAttributeWeights(ARXConfiguration config, int QIsSize) throws Exception {
		if (QIsSize == 5) {
			config.setAttributeWeight("sex", 1d);
			config.setAttributeWeight("salary-class", 1d);
			config.setAttributeWeight("race", 2d / 3);
			config.setAttributeWeight("workclass", 1d / 3);
			config.setAttributeWeight("marital-status", 1d / 3);
		} else if (QIsSize == 6) {
			config.setAttributeWeight("sex", 1d);
			config.setAttributeWeight("salary-class", 1d);
			config.setAttributeWeight("race", 3d / 4);
			config.setAttributeWeight("workclass", 2d / 4);
			config.setAttributeWeight("marital-status", 2d / 4);
			config.setAttributeWeight("occupation", 1d / 4);
		} else if (QIsSize == 7) {
			config.setAttributeWeight("sex", 1d);
			config.setAttributeWeight("salary-class", 1d);
			config.setAttributeWeight("race", 3d / 4);
			config.setAttributeWeight("workclass", 2d / 4);
			config.setAttributeWeight("marital-status", 2d / 4);
			config.setAttributeWeight("occupation", 1d / 4);
			config.setAttributeWeight("education", 1d / 4);
		} else if (QIsSize == 8) {
			config.setAttributeWeight("sex", 1d);
			config.setAttributeWeight("salary-class", 1d);
			config.setAttributeWeight("race", 4d / 5);
			config.setAttributeWeight("workclass", 3d / 5);
			config.setAttributeWeight("marital-status", 3d / 5);
			config.setAttributeWeight("occupation", 2d / 5);
			config.setAttributeWeight("education", 2d / 5);
			config.setAttributeWeight("native-country", 1d / 5);
		} else if (QIsSize == 9) {
			config.setAttributeWeight("sex", 1d);
			config.setAttributeWeight("salary-class", 1d);
			config.setAttributeWeight("race", 4d / 5);
			config.setAttributeWeight("workclass", 3d / 5);
			config.setAttributeWeight("marital-status", 3d / 5);
			config.setAttributeWeight("occupation", 2d / 5);
			config.setAttributeWeight("education", 2d / 5);
			config.setAttributeWeight("native-country", 1d / 5);
			config.setAttributeWeight("age", 1d / 5);
		}
	}
}
