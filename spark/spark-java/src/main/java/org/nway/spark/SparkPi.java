package org.nway.spark;

import java.util.ArrayList;
import java.util.List;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;

public final class SparkPi {

	public static void main(String[] args) throws Exception {
		
		final SparkConf sparkConf = new SparkConf().setAppName("SparkPi").setMaster("yarn");
		
		try (final JavaSparkContext jsc = new JavaSparkContext(sparkConf)) {

			final int slices = (args.length == 1) ? Integer.parseInt(args[0]) : 2;
			final int n = 100000 * slices;
			final List<Integer> l = new ArrayList<>(n);
			for (int i = 0; i < n; i++) {
				l.add(i);
			}

			final JavaRDD<Integer> dataSet = jsc.parallelize(l, slices);

			final int count = dataSet.map(integer -> {
				double x = Math.random() * 2 - 1;
				double y = Math.random() * 2 - 1;
				return (x * x + y * y < 1) ? 1 : 0;
			}).reduce((a, b) -> a + b);

			System.out.println("Pi is roughly " + 4.0 * count / n);
		}
	}
}