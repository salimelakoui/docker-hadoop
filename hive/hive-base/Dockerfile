FROM salimelakoui/hadoop-base:latest-salim

# ###################################################################################################################
# Installing Hive
# ###################################################################################################################
ENV HIVE_VERSION 3.1.2
ENV HIVE_URL https://apache.mirrors.benatherton.com/hive/hive-$HIVE_VERSION/apache-hive-$HIVE_VERSION-bin.tar.gz

RUN wget $HIVE_URL  && \
    tar -xzvf apache-hive-$HIVE_VERSION-bin.tar.gz && \
    mv apache-hive-$HIVE_VERSION-bin /opt/hive-$HIVE_VERSION && \
    rm apache-hive-$HIVE_VERSION-bin.tar.gz

ENV HIVE_HOME=/opt/hive-$HIVE_VERSION

#RUN wget https://jdbc.postgresql.org/download/postgresql-9.4.1212.jar -O $HIVE_HOME/lib/postgresql-jdbc.jar

ENV HIVE_CONF_DIR=$HIVE_HOME/conf
ENV CLASSPATH=$CLASSPATH:$HIVE_HOME/lib/*:$HIVE_HOME/conf/*:.
ENV PATH=$PATH:$HIVE_HOME/bin

RUN ln -s $HIVE_HOME/conf /etc/hive
RUN ln -s $HIVE_HOME /apps/hive

RUN rm $HIVE_HOME/lib/guava-19.0.jar
RUN cp $HADOOP_HOME/share/hadoop/hdfs/lib/guava-27.0-jre.jar $HIVE_HOME/lib/

