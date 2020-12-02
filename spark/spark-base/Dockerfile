FROM salimelakoui/hive-base:latest-salim

# ###################################################################################################################
# Installing Spark
# ###################################################################################################################

ENV SPARK_HADOOP_VERSION 3.2
ENV SPARK_VERSION 3.0.1
ENV SPARK_MASTER_LOG /spark/logs
ENV SPARK_PUBLIC_DNS localhost

# Downloading Spark
ENV SPARK_URL https://archive.apache.org/dist/spark/spark-${SPARK_VERSION}/spark-${SPARK_VERSION}-bin-hadoop${SPARK_HADOOP_VERSION}.tgz
RUN wget $SPARK_URL && \
    tar -xzvf spark-${SPARK_VERSION}-bin-hadoop${SPARK_HADOOP_VERSION}.tgz && \
    mv spark-${SPARK_VERSION}-bin-hadoop${SPARK_HADOOP_VERSION} /opt/spark-$SPARK_VERSION && \
    rm spark-${SPARK_VERSION}-bin-hadoop${SPARK_HADOOP_VERSION}.tgz

ENV SPARK_HOME=/opt/spark-$SPARK_VERSION
ENV PATH=$PATH:$SPARK_HOME/bin
ENV LD_LIBRARY_PATH=$HADOOP_HOME/lib/native:$LD_LIBRARY_PATH

RUN ln -s $SPARK_HOME/conf /etc/spark
RUN ln -s $SPARK_HOME /apps/spark

COPY config/spark-env.sh /etc/spark/spark-env.sh
COPY config/spark-defaults.conf /etc/spark/spark-defaults.conf

CMD ["/run.sh"]
