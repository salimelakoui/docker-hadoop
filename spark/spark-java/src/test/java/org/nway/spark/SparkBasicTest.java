package org.nway.spark;

import static org.apache.spark.sql.functions.col;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.api.java.function.Function2;
import org.apache.spark.broadcast.Broadcast;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.RowFactory;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;
import org.apache.spark.util.LongAccumulator;
import org.junit.Test;
import org.nway.spark.items.AbstractSparkTest;

import scala.Tuple2;


class GetLength implements Function<String, Integer>, Serializable {
	public Integer call(String s) {
		return s.length();
	}
}

class Sum implements Function2<Integer, Integer, Integer>, Serializable  {
	public Integer call(Integer a, Integer b) {
		return a + b;
	}
}

public class SparkBasicTest extends AbstractSparkTest {



	@Test
	public void testModulo2() {
		int n = 10;
		List<Integer> l = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9);

		JavaRDD<Integer> dataSet = jsc.parallelize(l, n);

		JavaRDD<Integer> array = dataSet.filter((i) -> i % 2 == 0);

		debugRDD(array);

		int result = array.reduce((i, j) -> i + j);

		System.out.println(result);
	}

	@Test
	public void testWordCount() {
		JavaRDD<String> textFile = jsc.textFile("src/test/resources/file.txt");
		JavaPairRDD<String, Integer> counts = textFile.flatMap(s -> Arrays.asList(s.split(" ")).iterator())
				.mapToPair(word -> new Tuple2<>(word, 1)).reduceByKey((a, b) -> a + b);

		debugRDD(textFile);
		counts.collect().forEach((i) -> System.out.println("=> " + i));
	}

	@Test
	public void testWordCharactereCount() {
		

		JavaRDD<String> textFile = jsc.textFile("src/test/resources/file.txt");
		int result = textFile.map(s -> s.length()).reduce((a, b) -> a + b);
		int result2 = textFile.map(new GetLength()).reduce(new Sum());

		System.out.println(result);
		System.out.println(result2);
	}

	@Test
	public void testDataFrame() {
		// Creates a DataFrame having a single column named "line"
		JavaRDD<String> textFile = jsc.textFile("src/test/resources/file.txt");

		JavaRDD<Row> rowRDD = textFile.map(RowFactory::create);
		List<StructField> fields = Arrays.asList(DataTypes.createStructField("line", DataTypes.StringType, true));
		StructType schema = DataTypes.createStructType(fields);
		Dataset<Row> df = spark.createDataFrame(rowRDD, schema);
		df.printSchema();
		

		Dataset<Row> errors = df.filter(col("line").like("%f"));
		// Counts all the errors
		System.out.println(errors.count());
		// Fetches the MySQL errors as an array of strings
		errors.filter(col("line").like("%f%")).show();
	}
	
	@Test 
	public void testAccu()
	{
		Broadcast<int[]> broadcastVar = jsc.broadcast(new int[] {1, 2, 3});

		broadcastVar.value();
		
		LongAccumulator accum = jsc.sc().longAccumulator();

		jsc.parallelize(Arrays.asList(1, 2, 3, 4)).foreach(x -> accum.add(x));
		// ...
		// 10/09/29 18:41:08 INFO SparkContext: Tasks finished in 0.317106 s

		System.out.println(accum.value());
	}
}