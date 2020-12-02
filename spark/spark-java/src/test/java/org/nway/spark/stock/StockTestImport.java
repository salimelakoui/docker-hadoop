package org.nway.spark.stock;

import java.io.IOException;

import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.SparkSession;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.nway.spark.items.AbstractSparkTest;


public class StockTestImport extends AbstractSparkTest {
	private transient SparkSession spark;
	private transient JavaSparkContext jsc;

	@Before
	public void setUp() throws IOException {
		System.setProperty("hadoop.home.dir", new java.io.File( "." ).getCanonicalPath() + "/winutils/");
		spark = SparkSession.builder().appName(this.getClass().getName()).master("local[*]").getOrCreate();
		jsc = new JavaSparkContext(spark.sparkContext());
	}

	@After
	public void tearDown() {
		spark.stop();
		jsc.close();
	}
	
	@Test
	public void testImportAAPL() throws IOException
	{
		JobStockImport.importStock(spark, jsc, "src/main/resources/stock/AAPL.csv", "AAPL", "target/");
	}
}
