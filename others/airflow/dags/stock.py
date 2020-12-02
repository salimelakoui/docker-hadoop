
from airflow import DAG
from airflow.operators.bash_operator import BashOperator
from airflow.contrib.operators.spark_submit_operator import SparkSubmitOperator
from datetime import datetime, timedelta


default_args = {
    "owner": "airflow",
    "depends_on_past": False,
    "start_date": datetime(2020, 11, 1),
    "retries": 1,
    "retry_delay": timedelta(minutes=15),
}

dag = DAG("stock", default_args=default_args, schedule_interval=timedelta(1))


#---- Spark resource size
NUM_EXECUTOR = 1
EXECUTOR_CORES = 1
EXECUTOR_MEMORY = '2g'

JAR_FILE = '/usr/local/airflow/target/spark-java-jar-with-dependencies.jar'
EXECUTE_CLASS = 'org.nway.spark.stock.JobDeleteIfExist'
AGG_PERIOD = 1

default_conf = {
    'spark.network.timeout': '800s',
    'spark.executor.heartbeatInterval': '60s'
}

t1 = SparkSubmitOperator(
    task_id='delete_data',
    application=JAR_FILE,
    conf=default_conf,
    java_class=EXECUTE_CLASS,
    executor_cores=EXECUTOR_CORES,
    executor_memory=EXECUTOR_MEMORY,
    num_executors=NUM_EXECUTOR,
    application_args=["hdfs://namenode:9000/input/AAPL.csv"],
    proxy_user='root',
    verbose=True,
    dag=dag)


