package com.example;

import static org.mockito.Mockito.*;

import org.apache.log4j.Logger;
import org.apache.spark.SparkConf;
import org.apache.spark.sql.SparkSession;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

public class SparkABFSDemoTest {

    private Logger logger;
    private MockedStatic<Logger> mockedLoggerStatic;

    @Before
    public void setUp() {
        // Mock the static method Logger.getLogger
        mockedLoggerStatic = mockStatic(Logger.class);
        logger = mock(Logger.class);
        mockedLoggerStatic.when(() -> Logger.getLogger(SparkABFSDemo.class)).thenReturn(logger);
    }

    @After
    public void tearDown() {
        // Close the static mock after each test
        if (mockedLoggerStatic != null) {
            mockedLoggerStatic.close();
        }
    }

    @Test
    public void testMain_MissingStorageAccountName() {
        try (MockedStatic<System> mockedSystem = Mockito.mockStatic(System.class)) {
            System.out.println("Mocking System.getenv for STORAGE_ACCOUNT_NAME");
            mockedSystem.when(() -> System.getenv("STORAGE_ACCOUNT_NAME")).thenReturn(null);
            System.out.println("Mocking System.getenv for CONTAINER_NAME");
            mockedSystem.when(() -> System.getenv("CONTAINER_NAME")).thenReturn(null);


            System.out.println("Calling SparkABFSDemo.main");
            SparkABFSDemo.main(new String[]{});

            System.out.println("Verifying logger.error call");
            verify(logger).error("Environment variable STORAGE_ACCOUNT_NAME is not set.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Other test methods...
    public void testMain_SuccessfulExecution() {
        try (MockedStatic<System> mockedSystem = Mockito.mockStatic(System.class);
             MockedStatic<SparkSession> mockedSparkSession = Mockito.mockStatic(SparkSession.class)) {

            mockedSystem.when(() -> System.getenv("STORAGE_ACCOUNT_NAME")).thenReturn("storageAccount");
            mockedSystem.when(() -> System.getenv("CONTAINER_NAME")).thenReturn("container");
            mockedSystem.when(() -> System.getenv("AZURE_CLIENT_ID")).thenReturn("clientId");
            mockedSystem.when(() -> System.getenv("AZURE_TENANT_ID")).thenReturn("tenantId");

            SparkSession sparkSession = mock(SparkSession.class);
            mockedSparkSession.when(() -> SparkSession.builder().config(any(SparkConf.class)).getOrCreate()).thenReturn(sparkSession);

            SparkABFSDemo.main(new String[]{});

            verify(logger).info("Spark session created successfully.");
            verify(sparkSession).stop();
            verify(logger).info("Spark session stopped successfully.");
        }
    }

}
