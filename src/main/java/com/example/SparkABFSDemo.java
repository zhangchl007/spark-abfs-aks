package com.example;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;
import org.apache.spark.sql.RowFactory;
import org.apache.log4j.Logger;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Demonstrates how to use Apache Spark with Azure Blob Storage.
 */
public class SparkABFSDemo {
    private static final Logger logger = Logger.getLogger(SparkABFSDemo.class);

    /**
     * Main method to run the Spark job.
     *
     * @param args Command line arguments
     */
    public static void main(String[] args) {
        // Read environment variables
        Map<String, String> env = System.getenv();
        String storageAccountName = env.get("STORAGE_ACCOUNT_NAME");
        String containerName = env.get("CONTAINER_NAME");
        String tenantId = env.get("AZURE_TENANT_ID");
        String clientId = env.get("AZURE_CLIENT_ID");
        String federatedTokenFile = env.get("AZURE_FEDERATED_TOKEN_FILE");

        if (storageAccountName == null || containerName == null) {
            return;
        }

        // Initialize Spark Configuration
        SparkConf conf = new SparkConf()
            .setAppName("SparkABFSDemo")
            .set("fs.azure.account.oauth2.client.id.dfs.core.windows.net", clientId)
            .set("fs.azure.account.oauth2.msi.tenant.dfs.core.windows.net", tenantId)
            .set("fs.azure.account.oauth2.token.file.dfs.core.windows.net", federatedTokenFile);


        try (SparkSession spark = SparkSession.builder().config(conf).getOrCreate()) {
            logger.info("Spark session created successfully.");
   
            // Run the dataset creation example
            runDatasetCreationExample(spark, storageAccountName, containerName);

            Thread.sleep(2400000);

            logger.info("Spark session stopped successfully.");
        } catch (Exception e) {
            logger.error("An error occurred during the Spark job execution.", e);
        }
    }

    /**
     * Helper method to generate a random string of specified length.
     *
     * @param length Length of the random string
     * @return Random string of specified length
     */
    private static String generateRandomString(int length) {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        Random random = new Random();
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(characters.charAt(random.nextInt(characters.length())));
        }
        return sb.toString();
    }

    private static void runDatasetCreationExample(SparkSession spark, String storageAccountName, String containerName) {
        // Define schema for the data
        StructType schema = DataTypes.createStructType(new StructField[]{
            DataTypes.createStructField("name", DataTypes.StringType, false),
            DataTypes.createStructField("age", DataTypes.IntegerType, false)
        });

        // Create data
        List<Row> data = Arrays.asList(
            RowFactory.create("Alice", 30),
            RowFactory.create("Bob", 25),
            RowFactory.create("Charlie", 35)
        );

        // Create DataFrame
        Dataset<Row> df = spark.createDataFrame(data, schema);

        // Convert DataFrame to JavaRDD
        JavaRDD<Row> rdd = df.toJavaRDD();

        // Perform some RDD operations (example: filter rows where age > 30)
        JavaRDD<Row> filteredRdd = rdd.filter(row -> row.getInt(1) > 30);

        // Collect and print the filtered rows
        List<Row> filteredData = filteredRdd.collect();
        filteredData.forEach(row -> logger.info("Filtered Row: " + row));

        // Generate a random string of 4 characters
        String randomString = generateRandomString(4);

        // Generate a unique identifier (e.g., timestamp)
        String timestamp = String.valueOf(System.currentTimeMillis());

        // Write data to Azure Blob Storage with append mode
        String outputPath = "abfss://" + containerName + "@" + storageAccountName + ".dfs.core.windows.net/test/" + randomString + timestamp + "data.csv";
        df.write().mode("append").option("header", "true").csv(outputPath);
        logger.info("Data written to " + outputPath);

        // Read data from Azure Blob Storage
        Dataset<Row> readDf = spark.read().option("header", "true").csv(outputPath);
        readDf.show();

        // Perform some basic operations
        long count = readDf.count();
        logger.info("Number of records: " + count);
    }
}