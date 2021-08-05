# hadoop-plateform

## Here are the commands to buid the images : 
```
make build
```

## Now we run it :
```
docker-compose up -d
```

## We can test it with :

```
make mapreduce
make spark-submit-cluster
make spark-submit-client
```




#docker build -t salimelakoui/zeppelin:$(current_branch) ./others/zeppelin
#docker build -t salimelakoui/hive:$(current_branch) ./others/hive




