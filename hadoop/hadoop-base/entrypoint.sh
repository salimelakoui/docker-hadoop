#!/bin/bash
# MAINTAINER Salim EL AKOUI <salim.elakoui@nway.org>

# Set some sensible defaults
export CORE_CONF_fs_defaultFS=${CORE_CONF_fs_defaultFS:-hdfs://`hostname -f`:8020}

function log() {
  if [ ! -z "$DEBUG_DOCKER" ] && [ "$DEBUG_DOCKER" = "1"  ] ; then
    echo $1
  fi
}

function addProperty() {
  local path=$1
  local name=$2
  local value=$3

  local entry="<property><name>$name</name><value>${value}</value></property>"
  local escapedEntry=$(echo $entry | sed 's/\//\\\//g')
  sed -i "/<\/configuration>/ s/.*/${escapedEntry}\n&/" $path
}

function configure() {
    local path=$1
    local module=$2
    local envPrefix=$3

    local var
    local value
    
    log "Configuring $module"
    if [ ! -f "$path" ]; then
      log "File does not exists, creating it"
      log "<configuration>" > $path
      log "</configuration>" >> $path
    fi

    for c in `printenv | perl -sne 'print "$1 " if m/^${envPrefix}_(.+?)=.*/' -- -envPrefix=$envPrefix`; do 
        name=`echo ${c} | perl -pe 's/___/-/g; s/__/@/g; s/_/./g; s/@/_/g;'`
        var="${envPrefix}_${c}"
        value=${!var}
        log " - Setting $name=$value"
        addProperty $path $name "$value"
    done
}

mkdir -p /etc/hadoop
mkdir -p /etc/hive
configure /etc/hadoop/core-site.xml core CORE_CONF
configure /etc/hadoop/hdfs-site.xml hdfs HDFS_CONF
configure /etc/hadoop/yarn-site.xml yarn YARN_CONF
configure /etc/hive/hive-site.xml hive HIVE_CONF
configure /etc/hadoop/httpfs-site.xml httpfs HTTPFS_CONF
configure /etc/hadoop/kms-site.xml kms KMS_CONF
configure /etc/hadoop/ssl-server.xml ssl SSL_CONF
configure /etc/hadoop/mapred-site.xml mapred MAPRED_CONF

echo "export HADOOP_OPTS=\"$HADOOP_OPTS -Djavax.net.debug=ssl -Dsun.security.krb5.debug=true\"" >> /etc/hadoop/hadoop-env.sh

if [ "$MULTIHOMED_NETWORK" = "1" ]; then
    log "Configuring for multihomed network"

    # HDFS
    addProperty /etc/hadoop/hdfs-site.xml dfs.namenode.rpc-bind-host 0.0.0.0
    addProperty /etc/hadoop/hdfs-site.xml dfs.namenode.servicerpc-bind-host 0.0.0.0
    addProperty /etc/hadoop/hdfs-site.xml dfs.namenode.http-bind-host 0.0.0.0
    addProperty /etc/hadoop/hdfs-site.xml dfs.namenode.https-bind-host 0.0.0.0
    addProperty /etc/hadoop/hdfs-site.xml dfs.client.use.datanode.hostname true
    addProperty /etc/hadoop/hdfs-site.xml dfs.datanode.use.datanode.hostname true

    # YARN
    addProperty /etc/hadoop/yarn-site.xml yarn.resourcemanager.bind-host 0.0.0.0
    addProperty /etc/hadoop/yarn-site.xml yarn.nodemanager.bind-host 0.0.0.0
    addProperty /etc/hadoop/yarn-site.xml yarn.nodemanager.bind-host 0.0.0.0
    addProperty /etc/hadoop/yarn-site.xml yarn.timeline-service.bind-host 0.0.0.0

    # MAPRED
    addProperty /etc/hadoop/mapred-site.xml yarn.nodemanager.bind-host 0.0.0.0
fi

function wait_for_it()
{
    local serviceport=$1
    local service=${serviceport%%:*}
    local port=${serviceport#*:}
    local retry_seconds=5
    local max_try=100
    let i=1

    nc -z $service $port
    result=$?

    until [ $result -eq 0 ]; do
      echo "[$i/$max_try] check for ${service}:${port}..."
      echo "[$i/$max_try] ${service}:${port} is not available yet"
      if (( $i == $max_try )); then
        echo "[$i/$max_try] ${service}:${port} is still not available; giving up after ${max_try} tries. :/"
        exit 1
      fi
      
      echo "[$i/$max_try] try in ${retry_seconds}s once again ..."
      let "i++"
      sleep $retry_seconds

      nc -z $service $port
      result=$?
    done
    echo "[$i/$max_try] $service:${port} is available."
}

for i in ${SERVICE_PRECONDITION[@]}
do
    wait_for_it ${i}
done

if [ ! -z "$PRE_CMD_1" ]; then
  $PRE_CMD_1 || true
fi

if [ ! -z "$PRE_CMD_2" ]; then
  $PRE_CMD_2 || true
fi

if [ ! -z "$PRE_CMD_3" ]; then
  $PRE_CMD_3 || true
fi

apt-get clean
rm -rf /var/lib/apt/lists/*

exec $@
