
# Spark MongoDB Integration

Access document from MongoDB collection (Read + Write)


## Tech Stack

* Spark
* MongoDB

## Installation

MongoDB is a document-based NoSQL database application. Unlike MySQL, it allows data to be stored differently in different documents.

Install mongodb-4.2 on CentOS 7

Step 1: First We need to add the MongoDB Software Repository.

By default, MongoDB is not available in the official CentOS repositories. To add the MongoDB repositories, open a terminal window, and create a MongoDB repository configuration file:

```bash
sudo vi /etc/yum.repos.d/mongodb-org-4.2.repo
```
In the newly created repo configuration file, enter the following:

```bash
[mongodb-org-4.2]

name=MongoDB Repository

baseurl=https://repo.mongodb.org/yum/redhat/$releasever/mongodb-org/4.2/x86_64/

gpgcheck=1

enabled=1

gpgkey=https://www.mongodb.org/static/pgp/server-4.2.asc
```

Step 2: Install MongoDB Software

```bash
sudo yum install –y mongodb-org
```
![App Screenshot](https://github.com/deepakpanda93/Spark_MongoDB_Example/blob/master/src/main/resources/assets/install_mongo.png?raw=true)

Step 3: Then open the /etc/mongod.conf file ,modify the below section (Enter your mongo server address).

```bash
# network interfaces
net:
  port: 27017
  bindIp: 127.0.0.1  # Enter 0.0.0.0,:: to bind to all IPv4 and IPv6 addresses or, alternatively, use the net.bindIpAll setting.
```

Step 4: Start and enable mongodb service on boot

```bash
sudo systemctl start mongod
sudo systemctl enable mongod
```

Confirm that the service is running.

```bash
sudo systemctl status mongod
```
## Usage/Examples

1. Create MongoDB Admin User and switch to the admin user account.

```bash
$ mongo
> use admin
```

2. Create an administrator user account for the Mongo database.

```bash
db.createUser(
 {
 user: "dpanda",
 pwd: "dpanda",
 roles: [ { role: "userAdminAnyDatabase", db: "admin" } ]
 }
 )
```
![App Screenshot](https://github.com/deepakpanda93/Spark_MongoDB_Example/blob/master/src/main/resources/assets/setAdminUser.png?raw=true)

3. Display the list of users.

![App Screenshot](https://github.com/deepakpanda93/Spark_MongoDB_Example/blob/master/src/main/resources/assets/show_users.png?raw=true)

4. Creating a database in MongoDB is as simple as issuing the "use" command. If the database does not exist a new one will be created.

```bash
> use employeeDB
```

![App Screenshot](https://github.com/deepakpanda93/Spark_MongoDB_Example/blob/master/src/main/resources/assets/useDB.png?raw=true)

5. The easiest way to create a collection is to insert a record (which is nothing but a document consisting of Field names and Values) into a collection. If the collection does not exist a new one will be created. 

```bash
> db.Employee.insert([{"Employeeid" : NumberInt(101), "EmployeeName" : "John"}, {"Employeeid" : NumberInt(102), "EmployeeName" : "Clark"}, {"Employeeid" : NumberInt(103), "EmployeeName" : "Martin"}])
```
Here Employee is the Collection name.

![App Screenshot](https://github.com/deepakpanda93/Spark_MongoDB_Example/blob/master/src/main/resources/assets/InsertIntoCollection.png?raw=true)

6. Listing all the databases & collections available on the server.

```bash
> show dbs
> show collections
```

![App Screenshot](https://github.com/deepakpanda93/Spark_MongoDB_Example/blob/master/src/main/resources/assets/DBs_Collection.png?raw=true)

7. Display all the documents of a collection.

```bash
> db.Employee.find().pretty();
```

![App Screenshot](https://github.com/deepakpanda93/Spark_MongoDB_Example/blob/master/src/main/resources/assets/ShowCollectionDocs.png?raw=true)

8. Create a Spark Code file to read/write data from/to MongoDB Collection.

```javascript
$ cat SparkMongoExample.scala

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

```

4) Prepare a pom.xml file for your project

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>org.example</groupId>
    <artifactId>SparkMongoIntegration</artifactId>
    <version>1.0-SNAPSHOT</version>

    <properties>
        <scala.version>2.11.8</scala.version>
        <spark.version>2.4.7</spark.version>
        <jackson.version>2.11.0</jackson.version>
        <spark.scope>provided</spark.scope>
    </properties>

    <!-- Developers -->
    <developers>
        <developer>
            <id>deepakpanda93</id>
            <name>Deepak Panda</name>
            <email>deepakpanda93@gmail.com</email>
            <url>https://github.com/deepakpanda93</url>
        </developer>
    </developers>

    <dependencies>
    <dependency>
        <groupId>org.scala-lang</groupId>
        <artifactId>scala-library</artifactId>
        <version>${scala.version}</version>
    </dependency>
    <dependency>
        <groupId>org.apache.spark</groupId>
        <artifactId>spark-core_2.11</artifactId>
        <version>${spark.version}</version>
    </dependency>
    <dependency>
        <groupId>org.apache.spark</groupId>
        <artifactId>spark-sql_2.11</artifactId>
        <version>${spark.version}</version>
    </dependency>
    <dependency>
        <groupId>org.apache.spark</groupId>
        <artifactId>spark-sql-kafka-0-10_2.11</artifactId>
        <version>${spark.version}</version>
    </dependency>
    <!-- mongo-spark-connector -->
    <dependency>
        <groupId>org.mongodb.spark</groupId>
        <artifactId>mongo-spark-connector_2.11</artifactId>
        <version>2.4.4</version>
    </dependency>
    </dependencies>
</project>
```



## Run Locally

Start MongoDB

```bash
sudo systemctl start mongod
```

Run the Spark job from IDE

```bash
I used InteliJ to run the project. But one can build the project, deploy the JAR on the cluster and execute using spark-submit
```




## Screenshots

### Collection Schema

![App Screenshot](https://github.com/deepakpanda93/Spark_MongoDB_Example/blob/master/src/main/resources/assets/mongocollectionSchema.png?raw=true)

### Initial records of MongoDB (Fetched using Spark)

![App Screenshot](https://github.com/deepakpanda93/Spark_MongoDB_Example/blob/master/src/main/resources/assets/initialMongodata.png?raw=true)

### Spark Ingested Data (final result after inserting more records)

![App Screenshot](https://github.com/deepakpanda93/Spark_MongoDB_Example/blob/master/src/main/resources/assets/finalmongoResult.png?raw=true)

### Final output in MongoDB(Employee) Collection

![App Screenshot](https://github.com/deepakpanda93/Spark_MongoDB_Example/blob/master/src/main/resources/assets/showAllRecord_new.png?raw=true)

### Remove all data from a collection

![App Screenshot](https://github.com/deepakpanda93/Spark_MongoDB_Example/blob/master/src/main/resources/assets/deleteAllRecord.png?raw=true)

## 🚀 About Me

## Hi, I'm Deepak! 👋

I'm a Big Data Engineer...




## 🔗 Links
[![portfolio](https://img.shields.io/badge/my_portfolio-000?style=for-the-badge&logo=ko-fi&logoColor=white)](https://github.com/deepakpanda93)
[![linkedin](https://img.shields.io/badge/linkedin-0A66C2?style=for-the-badge&logo=linkedin&logoColor=white)](https://www.linkedin.com/in/deepakpanda93)

