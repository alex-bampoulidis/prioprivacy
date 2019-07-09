package experiments;

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

public class ARXExperiments {

	private static String input = "C:\\Users\\abampoulidis\\Desktop\\safedeed\\conferences\\tbd-demo-short\\repo\\input\\adult_9.csv";
	private static String generalizationsPath = "C:\\Users\\abampoulidis\\Desktop\\safedeed\\conferences\\tbd-demo-short\\repo\\generalization_hierarchies\\arx\\";
	private static String QIs[] = { "sex", "salary-class", "race", "workclass", "marital-status", "occupation",
			"education", "native-country", "age" };
	private static String methods[] = { "arx_loss", "arx_nue" };
	private static int numberOfIterations = 999999999;
	private static String outputPath = "C:\\Users\\abampoulidis\\Desktop\\safedeed\\conferences\\tbd-demo-short\\repo\\output\\";

	public static void main(String args[]) throws Exception {
		for (String method : methods) {
			for (int experiment = 1; experiment <= 4; experiment++) {
				String execTimeOutput = outputPath + "case" + experiment + "\\" + method + "_exec_time.csv";

				BufferedWriter bw = new BufferedWriter(
						new OutputStreamWriter(new FileOutputStream(execTimeOutput), StandardCharsets.UTF_8));

				bw.write("k,minutes\n");

				for (int k = 2; k <= 10; k++) {
					System.out.println(method + "\t" + experiment + "\t" + k);
					Data data = Data.create(input, StandardCharsets.UTF_8, ';');

					setAttributeTypesAndHierarchies(QIs, data, generalizationsPath);

					// local transformation model according to gui.worker.WorkerAnonymize line
					// 90-119 with numberOfIterations (see GUI)
					ARXAnonymizer anonymizer = new ARXAnonymizer();

					ARXConfiguration config = ARXConfiguration.create();

					configureAnonymization(config, k, numberOfIterations, method);

					setAttributeWeights(config, experiment);

					long startTime = System.currentTimeMillis();

					ARXResult result = anonymizer.anonymize(data, config);

					DataHandle optimum = result.getOutput(false);

					result.optimizeIterativeFast(optimum, 1d / (double) numberOfIterations);

					long time = System.currentTimeMillis() - startTime;

					bw.write(k + "," + (time / 60000.0) + "\n");

					String output = outputPath + "case" + experiment + "\\" + method + "\\k_" + k + ".csv";

					optimum.save(output, ';');

					StatisticsQuality statQ = optimum.getStatistics().getQualityStatistics();

					System.out.print("\t" + k + ",");
					System.out.print(statQ.getGeneralizationIntensity().getArithmeticMean() + ",");
					System.out.print(statQ.getGranularity().getArithmeticMean() + ",");
					System.out.print(statQ.getNonUniformEntropy().getArithmeticMean() + ",");
					System.out.print(statQ.getDiscernibility().getValue() + ",");
					System.out.print(statQ.getAverageClassSize().getValue() + ",");
					System.out.print(statQ.getRecordLevelSquaredError().getValue() + ",");
					System.out.print(statQ.getAttributeLevelSquaredError().getArithmeticMean() + ",");
					System.out.print("exp" + experiment + "," + method + "\n");
				}

				bw.close();
			}
		}
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

	private static void configureAnonymization(ARXConfiguration config, int k, int numberOfIterations, String method)
			throws Exception {
		config.addPrivacyModel(new KAnonymity(k));

		config.setSuppressionLimit(1d - (1d / (double) numberOfIterations));

		if (method.equals("arx_loss")) {
			config.setQualityModel(Metric.createLossMetric(0d, AggregateFunction.ARITHMETIC_MEAN));
		} else if (method.equals("arx_nue")) {
			config.setQualityModel(Metric.createEntropyMetric(false, 0d, AggregateFunction.ARITHMETIC_MEAN));
		}
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
