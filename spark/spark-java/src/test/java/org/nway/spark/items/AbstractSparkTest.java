package org.nway.spark.items;

import java.io.File;
import java.io.IOException;

import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.SparkSession;
import org.junit.After;
import org.junit.Before;

public abstract class AbstractSparkTest {
	protected transient SparkSession spark;
	protected transient JavaSparkContext jsc;

	@Before
	public void setUp() throws IOException {

		System.setProperty("hadoop.home.dir", new java.io.File(".").getCanonicalPath() + "/winutils/");
		String warehouseLocation = new File("target/spark-warehouse").getAbsolutePath();

		spark = SparkSession.builder().appName(this.getClass().getName())
				.config("spark.sql.warehouse.dir", warehouseLocation)
				.enableHiveSupport().master("local[*]").getOrCreate();
		jsc = new JavaSparkContext(spark.sparkContext());
	}

	@After
	public void tearDown() {
		spark.stop();
		jsc.close();
	}

	protected void log(String s) {
		System.out.println(s);
	}

	protected void debugRDD(JavaRDD<?> rdd) {
		rdd.collect().forEach((i) -> System.out.println("=> " + i));
	}

}
