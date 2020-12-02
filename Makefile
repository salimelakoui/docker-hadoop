DOCKER_NETWORK = dp-network
ENV_FILE = hadoop.env
current_branch := latest-salim

up-setup:
	docker network create -d bridge dp-network

up-clean:
	

up-core: up-clean
	docker-compose -f docker-compose-core.yml up 

up-hive: up-clean
	docker-compose -f docker-compose-hive.yml up

up-tools: up-clean
	docker-compose -f docker-compose-tools.yml up

up-process: up-clean
	docker-compose -f docker-compose-process.yml up

up: up-hdfs up-yarn up-hive

# make ARGS="asdf" run
build:
	docker build -t salimelakoui/hadoop-base:$(current_branch) ./hadoop/hadoop-base
	docker build -t salimelakoui/hadoop-namenode:$(current_branch) ./hadoop/namenode
	docker build -t salimelakoui/hadoop-datanode:$(current_branch) ./hadoop/datanode
	docker build -t salimelakoui/hive-base:$(current_branch) ./hive/hive-base
	docker build -t salimelakoui/hadoop-resourcemanager:$(current_branch) ./hadoop/resourcemanager
	docker build -t salimelakoui/hadoop-nodemanager:$(current_branch) ./hadoop/nodemanager
	docker build -t salimelakoui/hadoop-historyserver:$(current_branch) ./hadoop/historyserver
	docker build -t salimelakoui/spark-base:$(current_branch) ./spark/spark-base
	docker build -t salimelakoui/hive-server:$(current_branch) ./hive/hive-server

build-extra:
	docker build -t salimelakoui/airflow:$(current_branch) ./others/airflow

mr-test:
	docker run --network ${DOCKER_NETWORK} --env-file ${ENV_FILE} salimelakoui/hadoop-base:$(current_branch) yarn jar /opt/hadoop-3.2.1/share/hadoop/mapreduce/hadoop-mapreduce-examples-3.2.1.jar pi 16 1000 

spark-tty:
	docker run --interactive --tty --network ${DOCKER_NETWORK} --env-file ${ENV_FILE} --volume '$(CURDIR)/spark/spark-java/:/opt/spark-java' \
	 			salimelakoui/spark-base:$(current_branch) \
				/bin/bash

spark-test:
	docker run --network ${DOCKER_NETWORK} --env-file ${ENV_FILE} salimelakoui/spark-base:$(current_branch) spark-submit \
				--master yarn --deploy-mode cluster \
				--class org.apache.spark.examples.SparkPi \
				/opt/spark-3.0.1/examples/jars/spark-examples_2.12-3.0.1.jar 100

spark-prepare:
	cd spark/spark-java && mvn clean package -DskipTests
	h.bat hdfs dfs -mkdir -p /input/
	h.bat hdfs dfs -mkdir -p /output/
	h.bat hdfs dfs -ls /
	h.bat hdfs dfs -ls /output
	h.bat hdfs dfs -ls /input

spark-java-test: spark-prepare
	docker run --network ${DOCKER_NETWORK} --env-file ${ENV_FILE} --volume '$(CURDIR)/spark/spark-java/target/spark-java-jar-with-dependencies.jar:/opt/spark-3.0.1/spark-java.jar' \
	 			salimelakoui/spark-base:$(current_branch) \
				spark-submit \
				--master yarn --deploy-mode client \
               	--class org.nway.spark.SparkPi \
				/opt/spark-3.0.1/spark-java.jar 100

spark-java-hdfs-test: spark-prepare
	h.bat hdfs dfs -rm -r -f /input/README.txt
	h.bat hdfs dfs -copyFromLocal /opt/hadoop-3.2.1/README.txt /input/
	docker run --network ${DOCKER_NETWORK} --env-file ${ENV_FILE} --volume '$(CURDIR)/spark/spark-java/target/spark-java-jar-with-dependencies.jar:/opt/spark-3.0.1/spark-java.jar' \
	 			salimelakoui/spark-base:$(current_branch) \
				spark-submit \
				--master yarn --deploy-mode client \
               	--class org.nway.spark.JavaWordCount \
				/opt/spark-3.0.1/spark-java.jar hdfs://namenode:9000/input/README.txt
	h.bat hdfs dfs -ls /
	h.bat hdfs dfs -ls /output
	h.bat hdfs dfs -ls /input

spark-stock-import:	 spark-prepare
	docker run --network ${DOCKER_NETWORK} --env-file ${ENV_FILE} --volume '$(CURDIR)/spark/spark-java/target/spark-java-jar-with-dependencies.jar:/opt/spark-3.0.1/spark-java.jar' \
	 			salimelakoui/spark-base:$(current_branch) \
				spark-submit \
				--master yarn --deploy-mode client \
               	--class org.nway.spark.stock.JobDeleteIfExist \
				/opt/spark-3.0.1/spark-java.jar hdfs://namenode:9000/input/AAPL.csv
	h.bat hdfs dfs -copyFromLocal /project/spark/spark-java/src/main/resources/stock/AAPL.csv /input/
	docker run --network ${DOCKER_NETWORK} --env-file ${ENV_FILE} --volume '$(CURDIR)/spark/spark-java/target/spark-java-jar-with-dependencies.jar:/opt/spark-3.0.1/spark-java.jar' \
	 			salimelakoui/spark-base:$(current_branch) \
				spark-submit \
				--master yarn --deploy-mode client \
               	--class org.nway.spark.stock.JobStockImport \
				/opt/spark-3.0.1/spark-java.jar hdfs://namenode:9000/input/AAPL.csv AAPL hdfs://namenode:9000/output/
	h.bat hdfs dfs -ls /
	h.bat hdfs dfs -ls /output
	h.bat hdfs dfs -ls /input


hive-tty:
	docker run --interactive --tty --network ${DOCKER_NETWORK} --env-file ${ENV_FILE} \
	 			salimelakoui/hive-server:$(current_branch) \
				/bin/bash

hive-cmd:
	docker run --interactive --tty --network ${DOCKER_NETWORK} --env-file ${ENV_FILE} \
	 			salimelakoui/hive-server:$(current_branch) \
				hive


rm:
	docker-compose rm -a

stop:
	cd scripts && stopall.bat