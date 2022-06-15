package com.deepak.spark.mongo

import org.apache.spark.sql.{Dataset, SparkSession}

object SparkMongoExample {

  def main(args: Array[String]): Unit = {

    val appName = "Spark MongoDB Integration"

    // Creating the SparkSession object
    val spark: SparkSession = SparkSession.builder().master("local").appName(appName).getOrCreate()

    val mongoDF = spark.read.format("com.mongodb.spark.sql.DefaultSource")
      .option("uri", "mongodb://node4.example.com:27017/employeeDB?authSource=admin").option("collection", "Employee").load()

    mongoDF.printSchema()

    mongoDF.show(false)

    val columns = Seq("Employeeid","EmployeeName")
    val data = Seq((104, "Deepak"), (105, "Liang"), (106, "Aditya"))

    val employeeRDD = spark.sparkContext.parallelize(data)
    val employeeDF = spark.createDataFrame(employeeRDD).toDF(columns:_*)

    employeeDF.write.format("com.mongodb.spark.sql.DefaultSource").mode("append")
      .option("uri", "mongodb://node4.example.com:27017/employeeDB?authSource=admin").option("collection", "Employee").save()

    val resultDF = spark.read.format("com.mongodb.spark.sql.DefaultSource")
      .option("uri", "mongodb://node4.example.com:27017/employeeDB?authSource=admin").option("collection", "Employee").load()

    resultDF.show(false)

  }

}
