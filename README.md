# Spark ABFS Driver on AKS

This project provides a Makefile to manage the build, deployment, and logging of a Spark ABFS driver on Azure Kubernetes Service (AKS).

## Prerequisites

- Maven
- Docker
- Azure CLI
- Kubernetes CLI (kubectl)
- Python 3

## Setup

1. Clone the repository:
    ```sh
    git clone https://github.com/zhangchl007/spark-abfs-aks
    cd spark-abfs-aks
    ```

2. Set the required environment variables:
    ```sh
    export STORAGE_ACCOUNT=<your-storage-account>
    export STORAGE_CONTAINER=<your-container-name>
    export AZURE_CLIENT_ID=<your-azure-client-id>
    ```

## Usage

The Makefile provides several targets to manage the project. Below are the available commands:

### Build and Deploy

- **all**: Builds the JAR, Docker image, uploads the JAR to Azure Blob Storage, and deploys the application to AKS.
    ```sh
    make all
    ```

### Individual Steps

- **clean**: Cleans the Maven build and removes the `target` directory.
    ```sh
    make clean
    ```

- **build-jar**: Builds the JAR file using Maven.
    ```sh
    make build-jar
    ```

- **build-docker**: Copies the JAR file to the Docker deployment directory, builds the Docker image, and pushes it to the specified Docker registry.
    ```sh
    make build-docker
    ```

- **upload-jar**: Uploads the JAR file to Azure Blob Storage.
    ```sh
    make upload-jar
    ```

- **upload-file**: Uploads additional files using a Python script.
    ```sh
    make upload-file
    ```

- **deploy**: Deploys the application to AKS using the Kubernetes configuration file.
    ```sh
    make deploy
    ```

- **check-logs**: Checks the logs of the Spark driver pod.
    ```sh
    make check-logs
    ```

## Notes

- Ensure that you have logged in to Azure CLI and have the necessary permissions to upload to Azure Blob Storage and deploy to AKS.
- Replace `<driver-id>` in the `check-logs` target with the actual driver ID of your Spark driver pod.