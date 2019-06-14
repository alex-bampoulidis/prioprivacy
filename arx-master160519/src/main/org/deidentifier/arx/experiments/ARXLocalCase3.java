package org.deidentifier.arx.experiments;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;

import org.deidentifier.arx.ARXAnonymizer;
import org.deidentifier.arx.ARXConfiguration;
import org.deidentifier.arx.ARXResult;
import org.deidentifier.arx.AttributeType;
import org.deidentifier.arx.Data;
import org.deidentifier.arx.DataHandle;
import org.deidentifier.arx.DataType;
import org.deidentifier.arx.aggregates.StatisticsQuality;
import org.deidentifier.arx.criteria.KAnonymity;
import org.deidentifier.arx.metric.Metric;
import org.deidentifier.arx.metric.Metric.AggregateFunction;

public class ARXLocalCase3 {

	private static String datasetPath = "C:\\Users\\abampoulidis\\Desktop\\safedeed\\conferences\\tbd-demo-short\\input\\";
	private static String hierarchiesPath = "C:\\Users\\abampoulidis\\Desktop\\safedeed\\conferences\\tbd-demo-short\\generalization_hierarchies\\arx\\";
	private static String allQIs[] = { "sex", "salary-class", "race", "workclass", "marital-status", "occupation",
			"education", "native-country", "age" };
	private static String qualityMetrics[] = { "Loss", "Non-Uniform Entropy" };
	private static String outputFolder = "C:\\Users\\abampoulidis\\Desktop\\safedeed\\conferences\\tbd-demo-short\\output\\case3\\arx\\";
	private static int numberOfIterations = 999999999;
	private static String output = outputFolder + "..\\arx.csv";

	public static void main(String args[]) throws Exception {
		BufferedWriter bw = new BufferedWriter(
				new OutputStreamWriter(new FileOutputStream(output), StandardCharsets.UTF_8));

		bw.write(
				"QIs,k,metric,gen. intensity,granularity,n.u. entropy,record-level sq. err.,att.-level sq. err.,time\n");

		for (int QIsSize = 9; QIsSize <= allQIs.length; QIsSize++) {
			String QIs[] = getQIs(QIsSize, allQIs);

			String input = datasetPath + "adult_" + QIsSize + ".csv";

			for (int k = 2; k <= 10; k++) {
				for (String qualityMetric : qualityMetrics) {
					Data data = Data.create(input, StandardCharsets.UTF_8, ';');
//					
//					
//					try setting age to String instead of Integer. does it make difference?
//					it does in the metrics record and attribute level square error
//					
//					
					setAttributeTypesAndHierarchies(QIs, data, hierarchiesPath);

					// local transformation model according to gui.worker.WorkerAnonymize line
					// 90-119 with numberOfIterations (see GUI)
					ARXAnonymizer anonymizer = new ARXAnonymizer();

					ARXConfiguration config = ARXConfiguration.create();

					configureAnonymization(config, k, numberOfIterations, qualityMetric);

					setAttributeWeights(config, QIsSize);

					long startTime = System.currentTimeMillis();

					ARXResult result = anonymizer.anonymize(data, config);

					DataHandle optimum = result.getOutput(false);

					result.optimizeIterativeFast(optimum, 1d / (double) numberOfIterations);

					long time = System.currentTimeMillis() - startTime;

					optimum.save(outputFolder + qualityMetric + "_QIs_" + QIsSize + "_k_" + k + ".csv", ';');

//					
//					
//					check later if evaluation is the same when doing it like in our case
//					it is
//					
//					
					writeEvaluationToFile(optimum, bw, QIsSize, k, qualityMetric, time);
				}
			}
		}

		bw.close();
	}

	private static String[] getQIs(int QIsSize, String allQIs[]) throws Exception {
		String QIs[] = new String[QIsSize];

		for (int i = 0; i < QIs.length; i++) {
			QIs[i] = allQIs[i];
		}

		return QIs;
	}

	private static void setAttributeTypesAndHierarchies(String QIs[], Data data, String hierarchiesPath)
			throws Exception {
		for (String QI : QIs) {
			data.getDefinition().setAttributeType(QI, AttributeType.QUASI_IDENTIFYING_ATTRIBUTE);
			data.getDefinition().setAttributeType(QI, AttributeType.Hierarchy
					.create(hierarchiesPath + "adult_hierarchy_" + QI + ".csv", StandardCharsets.UTF_8, ';'));
			if (QI.equals("age")) {
				data.getDefinition().setDataType(QI, DataType.INTEGER);
			}
		}
	}

	private static void configureAnonymization(ARXConfiguration config, int k, int numberOfIterations,
			String qualityMetric) throws Exception {
		config.addPrivacyModel(new KAnonymity(k));

		config.setSuppressionLimit(1d - (1d / (double) numberOfIterations));

		if (qualityMetric.equals("Loss")) {
			config.setQualityModel(Metric.createLossMetric(0d, AggregateFunction.ARITHMETIC_MEAN));
		} else if (qualityMetric.equals("Non-Uniform Entropy")) {
			config.setQualityModel(Metric.createEntropyMetric(false, 0d, AggregateFunction.ARITHMETIC_MEAN));
		}
	}

	private static void setAttributeWeights(ARXConfiguration config, int QIsSize) throws Exception {
//		if (QIsSize == 5) {
//			config.setAttributeWeight("sex", 1d);
//			config.setAttributeWeight("salary-class", 1d);
//			config.setAttributeWeight("race", 2d / 3);
//			config.setAttributeWeight("workclass", 1d / 3);
//			config.setAttributeWeight("marital-status", 1d / 3);
//		} else if (QIsSize == 6) {
//			config.setAttributeWeight("sex", 1d);
//			config.setAttributeWeight("salary-class", 1d);
//			config.setAttributeWeight("race", 3d / 4);
//			config.setAttributeWeight("workclass", 2d / 4);
//			config.setAttributeWeight("marital-status", 2d / 4);
//			config.setAttributeWeight("occupation", 1d / 4);
//		} else if (QIsSize == 7) {
//			config.setAttributeWeight("sex", 1d);
//			config.setAttributeWeight("salary-class", 1d);
//			config.setAttributeWeight("race", 3d / 4);
//			config.setAttributeWeight("workclass", 2d / 4);
//			config.setAttributeWeight("marital-status", 2d / 4);
//			config.setAttributeWeight("occupation", 1d / 4);
//			config.setAttributeWeight("education", 1d / 4);
//		} else if (QIsSize == 8) {
//			config.setAttributeWeight("sex", 1d);
//			config.setAttributeWeight("salary-class", 1d);
//			config.setAttributeWeight("race", 4d / 5);
//			config.setAttributeWeight("workclass", 3d / 5);
//			config.setAttributeWeight("marital-status", 3d / 5);
//			config.setAttributeWeight("occupation", 2d / 5);
//			config.setAttributeWeight("education", 2d / 5);
//			config.setAttributeWeight("native-country", 1d / 5);
//		} else
		if (QIsSize == 9) {
			config.setAttributeWeight("age", 1d);
			config.setAttributeWeight("sex", 8d / 9);
			config.setAttributeWeight("native-country", 7d / 9);
			config.setAttributeWeight("salary-class", 6d / 9);
			config.setAttributeWeight("education", 5d / 9);
			config.setAttributeWeight("race", 4d / 9);
			config.setAttributeWeight("occupation", 3d / 9);
			config.setAttributeWeight("workclass", 2d / 9);
			config.setAttributeWeight("marital-status", 1d / 9);
		}
	}

	private static void writeEvaluationToFile(DataHandle optimum, BufferedWriter bw, int QIsSize, int k,
			String qualityMetric, long time) throws Exception {
		StatisticsQuality statQ = optimum.getStatistics().getQualityStatistics();

		bw.write(QIsSize + "," + k + "," + qualityMetric + ",");
		bw.write(statQ.getGeneralizationIntensity().getArithmeticMean() + ",");
		bw.write(statQ.getGranularity().getArithmeticMean() + ",");
		bw.write(statQ.getNonUniformEntropy().getArithmeticMean() + ",");
		bw.write(statQ.getRecordLevelSquaredError().getValue() + ",");
		bw.write(statQ.getAttributeLevelSquaredError().getArithmeticMean() + ",");
		bw.write(time + "\n");

		System.out.print(QIsSize + "," + k + "," + qualityMetric + ",");
		System.out.print(statQ.getGeneralizationIntensity().getArithmeticMean() + ",");
		System.out.print(statQ.getGranularity().getArithmeticMean() + ",");
		System.out.print(statQ.getNonUniformEntropy().getArithmeticMean() + ",");
		System.out.print(statQ.getRecordLevelSquaredError().getValue() + ",");
		System.out.print(statQ.getAttributeLevelSquaredError().getArithmeticMean() + ",");
		System.out.print(time + "\n");
	}
}
