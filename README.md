# Spark ABFS driver on AKS

This is Java application that reads data from Azure Blob Storage (ABFS) using Spark on Azure Kubernetes Service (AKS).


## build jar

```
mvn clean install

```
## build docker image

```
cp target/SparkABFSDemo-1.0-SNAPSHOT.jar deploy/spark-docker
cd deploy/spark-docker
docker build -t zhangchl007/sparkcachedemo:v3.4.2 .
docker push zhangchl007/sparkcachedemo:v3.4.2

```

## upload jar to Azure Blob Storage

```
az storage blob upload --account-name <storage-account-name> --container-name <container-name> --name <jar-name> --file target/SparkABFSDemo-1.0-SNAPSHOT.jar --auth-mode login

# or upload the jar to the container using uploadfile.py with managed identity

export AZURE_STORAGE_ACCOUNT=<storage-account-name>
export AZURE_STORAGE_CONTAINER=<container-name>
export AZURE_CLIENT_ID=<client-id>

```
## Deploy Java Spark application on AKS

```

kubectl apply -f deploy/spark-docker/spark-driver.yaml

```

## Check the logs

```
kubectl logs -f spark-driver-<driver-id>

```
