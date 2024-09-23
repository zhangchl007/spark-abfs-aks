#!/usr/bin/env python3
# -*- coding: utf-8 -*-

import os
import platform
from azure.identity import ManagedIdentityCredential
from azure.storage.blob import BlobServiceClient, BlobClient, ContainerClient

def upload_file_to_blob(container_name: str, blob_name: str, file_path: str, storage_account_url: str, credential):
    try:
        # Create the BlobServiceClient object
        blob_service_client = BlobServiceClient(account_url=storage_account_url, credential=credential)

        # Get the container client
        container_client = blob_service_client.get_container_client(container_name)

        # Upload the file
        with open(file_path, "rb") as data:
            blob_client = container_client.get_blob_client(blob_name)
            blob_client.upload_blob(data, overwrite=True)

        print(f"File {file_path} uploaded to {container_name}/{blob_name} successfully.")
    except Exception as e:
        print(f"An error occurred: {e}")
    return

def upload_directory_to_blob(container_name: str, directory_path: str, storage_account_url: str, credential):
    """Upload all files in a directory to Azure Blob Storage while preserving the directory structure based on OS."""
    try:
        # Determine the operating system
        os_type = platform.system()  # 'Windows' or 'Linux'

        # Iterate through the files in the directory, preserving the structure
        for root, dirs, files in os.walk(directory_path):
            for file in files:
                # Create the relative path for the blob name to maintain directory structure
                blob_name = file_path = os.path.join(root, file)

                # Handle Windows and Linux path separators differently
                if os_type == "Windows":
                    # Replace backslashes with forward slashes for Windows
                    blob_name = blob_name.replace("\\", "/")
                elif os_type == "Linux":
                    # On Linux, no replacement is needed since it uses forward slashes
                    pass

                # Upload the file with the correct blob name
                upload_file_to_blob(container_name, blob_name, file_path, storage_account_url, credential)

    except Exception as e:
        print(f"An error occurred: {e}")
    return


def read_file_from_blob(container_name: str, blob_name: str, storage_account_url: str, credential):
    """Read a file from Azure Blob Storage."""
    try:
        blob_service_client = BlobServiceClient(account_url=storage_account_url, credential=credential)
        container_client = blob_service_client.get_container_client(container_name)
        blob_client = container_client.get_blob_client(blob_name)

        blob_data = blob_client.download_blob().readall()
        try:
            text_data = blob_data.decode('utf-8')
            print(f"Content of the blob {blob_name}:\n{text_data}")
        except UnicodeDecodeError:
            print(f"Content of the blob {blob_name} is binary data.")
    except Exception as e:
        print(f"Error reading blob: {e}")
    return

def read_directory_from_blob(container_name: str, directory_path: str, storage_account_url: str, credential):
    """Read all blobs whose names contain a specified directory path."""
    try:
        blob_service_client = BlobServiceClient(account_url=storage_account_url, credential=credential)
        container_client = blob_service_client.get_container_client(container_name)

        blob_list = container_client.list_blobs()
        filtered_blobs = [blob for blob in blob_list if directory_path in blob.name]

        for blob in filtered_blobs:
            blob_client = container_client.get_blob_client(blob.name)
            blob_properties = blob_client.get_blob_properties()
            creation_time = blob_properties.creation_time
            print(f"Reading blob: {blob.name}, Creation time: {creation_time}")
            read_file_from_blob(container_name, blob.name, storage_account_url, credential)
    except Exception as e:
        print(f"Error reading directory: {e}")
    return

def check_env_variables(required_vars):
    """Check if required environment variables are set."""
    missing_vars = [var for var in required_vars if not os.getenv(var)]
    if missing_vars:
        print(f"Missing environment variables: {', '.join(missing_vars)}")
        return False
    return True

def main():
    required_vars = ["AZURE_CLIENT_ID", "STORAGE_ACCOUNT", "CONTAINER_NAME"]

    if not check_env_variables(required_vars):
        return

    client_id = os.getenv("AZURE_CLIENT_ID")
    storage_account_name = os.getenv("STORAGE_ACCOUNT")
    container_name = os.getenv("CONTAINER_NAME")

    directory_path = "java"
    storage_account_url = f"https://{storage_account_name}.blob.core.windows.net"

    credential = ManagedIdentityCredential(client_id=client_id)

    # Upload the directory to Azure Blob Storage
    upload_directory_to_blob(container_name, directory_path, storage_account_url, credential)

    # Read files from Azure Blob Storage based on the directory path
    read_directory_from_blob(container_name, directory_path, storage_account_url, credential)

if __name__ == "__main__":
    main()