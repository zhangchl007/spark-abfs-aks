# Makefile for Spark ABFS driver on AKS

# Variables
JAR_NAME=SparkABFSDemo-1.0-SNAPSHOT.jar
DOCKER_IMAGE=zhangchl007/sparkcachedemo:v3.4.2
STORAGE_ACCOUNT ?= $(STORAGE_ACCOUNT)
CONTAINER_NAME ?= $(STORAGE_CONTAINER)
AZURE_CLIENT_ID ?= $(AZURE_CLIENT_ID)

.PHONY: all clean build-jar build-docker upload-jar deploy check-logs

all: build-jar build-docker upload-jar deploy

# Build manager binary
clean: 
	mvn clean
	rm -rf target
build-jar:
	mvn clean install

build-docker:
	cp target/$(JAR_NAME) deploy/spark-docker
	cd deploy/spark-docker && docker build -t $(DOCKER_IMAGE) .
	docker push $(DOCKER_IMAGE)

upload-jar:
	az storage blob upload --account-name $(STORAGE_ACCOUNT) --container-name $(CONTAINER_NAME) --name $(JAR_NAME) --file target/$(JAR_NAME) --auth-mode login

upload-file:
	python3 deploy/uploadfile.py

deploy:
	kubectl apply -f deploy/spark-docker/spark-driver.yaml

check-logs:
	kubectl logs -f spark-driver-<driver-id>
