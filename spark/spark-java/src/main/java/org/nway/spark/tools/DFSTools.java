package org.nway.spark.tools;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

public class DFSTools {

	public static void deleteFile(String filename) throws IOException {
		Configuration conf = new Configuration();
		conf.addResource(new Path("/etc/hadoop/core-site.xml"));
		FileSystem fs = FileSystem.get(conf);
		Path file = new Path(filename);
		if (fs.exists(file))
			fs.delete(file, true);	
	}
}
