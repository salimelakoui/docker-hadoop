/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.nway.spark.stock;
import static org.apache.spark.sql.functions.col;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SaveMode;
import org.apache.spark.sql.SparkSession;
import org.nway.spark.tools.DFSTools;

import scala.Function1;
import scala.Tuple2;

public final class JobStockImport {

	public static void main(String[] args) throws Exception {

		if (args.length < 3) {
			System.err.println("Usage: JavaWordCount <file> <CODE> <DEST_DIRECTORY>");
			System.exit(1);
		}
		
		SparkSession spark = SparkSession.builder().appName("StockImport").enableHiveSupport().getOrCreate();
		JavaSparkContext jsc = new JavaSparkContext(spark.sparkContext());
		
		importStock(spark, jsc, args[0], args[1], args[2]);
		DFSTools.deleteFile(args[0]);
	}
	
	public static void importStock(SparkSession spark, JavaSparkContext jsc, String file, String code, String destination) throws IOException
	{		
		Dataset<Row> stockCSV = spark.read().format("csv")
				  .option("sep", ",")
				  .option("inferSchema", "true")
				  .option("header", "true")
				  .load(file);
		
		stockCSV.show();
		stockCSV = stockCSV.select(col("Date"), col("Open"), col("High"), col("Low"), col("Close"), col("Adj Close").alias("Adj_Close"), col("Volume"));
		stockCSV.show();
		stockCSV.printSchema();
		
		stockCSV.createOrReplaceTempView("my_temp_table");
		
		
		String tablename = "stock_" + code; 
		spark.sql("drop table if exists " + tablename);
		spark.sql("create table " + tablename + " as select * from my_temp_table");
		
		stockCSV.write().mode(SaveMode.Overwrite).save(destination + "/" + code + ".parquet");
		
		Dataset<Row> usersDF = spark.read().load(destination + "/" + code + ".parquet");
		usersDF.printSchema();
	}
}
