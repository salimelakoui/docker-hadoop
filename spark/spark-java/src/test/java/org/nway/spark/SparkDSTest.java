package org.nway.spark;

import static org.apache.spark.sql.functions.col;

import java.util.Arrays;
import java.util.Collections;

import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.function.MapFunction;
import org.apache.spark.sql.AnalysisException;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Encoder;
import org.apache.spark.sql.Encoders;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SaveMode;
import org.junit.Ignore;
import org.junit.Test;
import org.nway.spark.items.AbstractSparkTest;
import org.nway.spark.items.Person;

import scala.math.BigInt;

public class SparkDSTest extends AbstractSparkTest {

	@Test
	public void testLoad() throws AnalysisException {

		Dataset<Row> df = spark.read().json("src/test/resources/people.json");

		df.show();
		df.printSchema();
		df.select("name").show();
		df.select(col("name"), col("age").plus(1)).show();
		log("Select people older than 21");
		df.filter(col("age").gt(21)).show();
		df.groupBy("age").count().show();

		log("Selecting Temp View");
		df.createOrReplaceTempView("people");
		Dataset<Row> sqlDF = spark.sql("SELECT * FROM people");
		sqlDF.show();

		df.createGlobalTempView("people");
		spark.sql("SELECT * FROM global_temp.people").show();
		spark.newSession().sql("SELECT * FROM global_temp.people").show();
	}

	@Test
	public void testCreatingDS() throws AnalysisException {
		// Create an instance of a Bean class
		Person person = new Person();
		person.setName("Andy");
		person.setAge(BigInt.apply(12));

		Encoder<Person> personEncoder = Encoders.bean(Person.class);
		Dataset<Person> javaBeanDS = spark.createDataset(Collections.singletonList(person), personEncoder);
		javaBeanDS.show();

		// Encoders for most common types are provided in class Encoders
		Encoder<Integer> integerEncoder = Encoders.INT();
		Dataset<Integer> primitiveDS = spark.createDataset(Arrays.asList(1, 2, 3), integerEncoder);
		Dataset<Integer> transformedDS = primitiveDS.map((MapFunction<Integer, Integer>) value -> value + 1,
				integerEncoder);
		transformedDS.show();

		// DataFrames can be converted to a Dataset by providing a class. Mapping based
		// on name
		String path = "src/test/resources/people.json";
		Dataset<Person> peopleDS = spark.read().json(path).as(personEncoder);
		peopleDS.show();
		// +----+-------+
		// | age| name|
		// +----+-------+
		// |null|Michael|
		// | 30| Andy|
		// | 19| Justin|
		// +----+-------+
	}

	@Test
	public void testDSTranscodingAndSelectingAndCollecting() {

		// Create an RDD of Person objects from a text file
		JavaRDD<Person> peopleRDD = spark.read().textFile("src/test/resources/people.txt").javaRDD().map(line -> {
			System.out.println(line);
			String[] parts = line.split(",");
			Person person = new Person();
			person.setName(parts[0]);
			person.setAge(BigInt.apply(Integer.parseInt(parts[1].trim())));
			return person;
		});
		
		peopleRDD.collect().forEach((i) -> System.out.println("=> " + i));

		// Apply a schema to an RDD of JavaBeans to get a DataFrame
		Dataset<Row> peopleDF = spark.createDataFrame(peopleRDD, Person.class);
		// Register the DataFrame as a temporary view
		peopleDF.createOrReplaceTempView("people");

		// SQL statements can be run by using the sql methods provided by spark
		Dataset<Row> teenagersDF = spark.sql("SELECT name FROM people");

		// The columns of a row in the result can be accessed by field index
		Encoder<String> stringEncoder = Encoders.STRING();
		Dataset<String> teenagerNamesByIndexDF = teenagersDF
				.map((MapFunction<Row, String>) row -> "Name: " + row.getString(0), stringEncoder);
		teenagerNamesByIndexDF.show();
		// +------------+
		// | value|
		// +------------+
		// |Name: Justin|
		// +------------+

		// or by field name
		Dataset<String> teenagerNamesByFieldDF = teenagersDF
				.map((MapFunction<Row, String>) row -> "Name: " + row.<String>getAs("name"), stringEncoder);
		teenagerNamesByFieldDF.show();
		// +------------+
		// | value|
		// +------------+
		// |Name: Justin|
		// +------------+
	}
	
	@Test
	@Ignore
	public void testParquet()
	{
		// Reading & Saving Parket & Hive format
		Dataset<Row> usersDF = spark.read().load("src/test/resources/users.parquet");
		usersDF.printSchema();
		usersDF.select("name", "favorite_color").show();
		
		usersDF.write().mode(SaveMode.Overwrite).save("target/namesAndFavColors.parquet");
		usersDF.write().mode(SaveMode.Overwrite).option("path", "target/hive/").saveAsTable("t");
		
		// Parquet files can also be used to create a temporary view and then used in SQL statements
		usersDF.createOrReplaceTempView("parquetFile");
		Dataset<Row> namesDF = spark.sql("SELECT name FROM parquetFile");
		Dataset<String> namesDS = namesDF.map(
		    (MapFunction<Row, String>) row -> "Name: " + row.getString(0),
		    Encoders.STRING());
		namesDS.show();
		
		// Reading all Parket
		Dataset<Row> testGlobFilterDF = spark.read().format("parquet")
		        .option("pathGlobFilter", "*.parquet") // json file should be filtered out
		        .option("recursiveFileLookup", "true")
		        .load("src/test/resources");
		testGlobFilterDF.show();
	}
	
	
	@Test
	public void testCsv()
	{
		Dataset<Row> peopleDFCsv = spark.read().format("csv")
				  .option("sep", ";")
				  .option("inferSchema", "true")
				  .option("header", "true")
				  .load("src/test/resources/people.csv");
		
		peopleDFCsv.show();
		
	}
	
}