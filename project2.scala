import org.apache.spark.sql.{SQLContext, SparkSession}
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.log4j.Logger
import org.apache.log4j.Level
import org.apache.spark.sql.expressions.Window
import org.apache.spark.sql.functions.{col, count, dense_rank, hour, month}

object project2 {
  def main(args: Array[String]): Unit = {

    Logger.getLogger("org").setLevel(Level.OFF)
    Logger.getLogger("akka").setLevel(Level.OFF)
    val spark = SparkSession
      .builder
      .appName("Project2")
      .config("spark.master", "local[4]")
      .enableHiveSupport()
      .getOrCreate()

    println("Welcome to the login page to access ecommerce sales analysis!\n" +
      "Please enter\n" +
      "1 to login\n" +
      "2 to exit\n" +
      "Please choose your entry: ")
    var choice = scala.io.StdIn.readLine()
    choice match {
      case "1" =>
        login(spark)
      case "2" =>
        sys.exit()
      case deafult =>
        println("Invalid input, please try again.")
        loginmenu()
    }

    def loginmenu(): Unit = {
      println("Welcome to the login page to access ecommerce sales analysis!\n" +
        "Please enter\n" +
        "1 to login\n" +
        "2 to exit")
      var choice = scala.io.StdIn.readLine()
      choice match {
        case "1" =>
          login(spark)
        case "2" =>
          sys.exit()
        case deafult =>
          println("Invalid input, please try again.")
          loginmenu()
      }
    }
    def login(spark: SparkSession): Unit = {
      //import sqlContext.implicits
      println("Enter username: ")
      var user_in = scala.io.StdIn.readLine()
      val logincolumns = Seq("username", "password")
      val loginDF = spark.read.csv("Inputs/logintable").toDF(logincolumns: _*)
      loginDF.createOrReplaceTempView("logindata")
      var first1 = spark.sql("SELECT username FROM logindata WHERE username = '" + user_in + "'")
      if (first1.count() == 1) {
        println("Enter password: ")
        var password_in = scala.io.StdIn.readLine()
        val logincolumns = Seq("username", "password")
        val loginDF = spark.read.csv("Inputs/logintable").toDF(logincolumns: _*)
        loginDF.createOrReplaceTempView("logindata")
        var first2 = spark.sql("SELECT password FROM logindata WHERE password = '" + password_in + "'")
        if (first2.count() == 1) {
          println("Login successful\n")
          mainmenu()
        }
        else {
          println("Incorrect password, please try again")
          var password_in2 = scala.io.StdIn.readLine()
          val logincolumns = Seq("username", "password")
          val loginDF = spark.read.csv("Inputs//logintable").toDF(logincolumns: _*)
          loginDF.createOrReplaceTempView("logindata")
          var first3 = spark.sql("SELECT password FROM logindata WHERE password = '" + password_in2 + "'")
          if (first3.count() > 1) {
            println("Login successful")
          }
        }
      }
      else {
        println("Incorrect username, please try again")
        loginmenu()
      }
    }

    def mainmenu(): Unit = {
      println("Welcome to the ecommerce sales analysis dashboard!")
      println("Please select 1 - 4  to select a query or 5 to exit out.")
      println(" 1 to see best selling category per country\n" +
        " 2 to see popularity of product change throughout year per country\n" +
        " 3 to see location with highest traffic of sales\n" +
        " 4 to see time with highest traffic of sales per country")
      print("Enter your choice: ")
      var choice = scala.io.StdIn.readLine()
      choice match {
        case "1" =>
          Q1(spark)
        case "2" =>
          Q2(spark)
        case "3" =>
          Q3(spark)
        case "4" =>
          Q4(spark)
        case "5" =>
          println("Exiting!\n")
          sys.exit()
        case default =>
          println("Invalid entry, please try again!\n")
          mainmenu()
      }
    }

    def Q1(spark: SparkSession): Unit = {
      import spark.implicits._
      val ecomercefile = spark.read.option("header", true).csv("Inputs//finaldata.csv")
      val ecommerceDF = ecomercefile.toDF()
      println("\n" + "Please enter:\n" +
        "1 to display the top selling category of items per country\n" +
        "2 to display the top selling category for country of choice\n" +
        "3 to display the top 5 total sales per category\n" +
        "4 to display total sales for category of choice\n" +
        "5 to go back to main menu")
      print("Please choose you entry: ")
      val choice = scala.io.StdIn.readLine()
      choice match {
        case "1" =>
          val first1 = ecommerceDF.groupBy("Country", "Product_Category").count().as("Total_Sales")
          val first2 = first1.withColumn("rank", dense_rank().over(Window.partitionBy($"Country").orderBy($"count".desc)))
          val BSCOIPC = first2.where(first2("rank") === "1")
          BSCOIPC.show(10)
          Q1(spark)
        case "2" =>
          println("Please enter country of choice: ")
          val country_input = scala.io.StdIn.readLine()
          val first1 = ecommerceDF.groupBy("Product_Category", "Country").count()
          val first2 = first1.withColumn("rank", dense_rank().over(Window.partitionBy($"Country").orderBy($"count".desc)))
          val BSCOIPC = first2.where(first2("rank") === "1")
          BSCOIPC.where(BSCOIPC("Country") === country_input).show()
          Q1(spark)
        case "3" =>
          val first3 = ecommerceDF.groupBy("Product_Category").count()
          val first4 = first3.orderBy($"Count".desc)
          first4.show(5)
          Q1(spark)
        case "4" =>
          print("Please enter product category of choice: ")
          val category_input = scala.io.StdIn.readLine()
          val first3 = ecommerceDF.groupBy("Product_Category").count()
          val first4 = first3.orderBy($"Count".desc)
          first4.where(first4("Product_Category") === category_input).show()
          Q1(spark)
        case "5" =>
          mainmenu()
        case default =>
          println("Invalid entry, please try again!\n")
          Q1(spark)
      }
    }

    def Q2(spark: SparkSession): Unit = {
      val ecomercefile = spark.read.option("header", true).csv("Inputs//finaldata.csv")
      val ecommerceDF = ecomercefile.toDF()
      println("\n" + "Please enter:\n" +
        "1 to display how total sales changes throughout year\n" +
        "2 to display the total sales changes for country of choice\n" +
        "3 to go back to main menu")
      print("Please choose you entry: ")
      val choice = scala.io.StdIn.readLine()
      choice match {
        case "1" =>
          val step1 = ecommerceDF.select("Datetime", "Product_Category", "Country").withColumn("Month", month(col("DateTime")))
          val PCPY = step1.groupBy("Month").count().orderBy("Month")
          PCPY.show()
          Q2(spark)
        case "2" =>
          print("Please enter country of choice: ")
          val coun_in = scala.io.StdIn.readLine()
          val step1 = ecommerceDF.select("Datetime", "Product_Category", "Country").withColumn("Month", month(col("DateTime")))
          val step2 = step1.groupBy("Country", "Month").count().as("Sales_per_month")
          val PCPYPC = step2.where(step2("Country") === coun_in).orderBy("Month").show()
          Q2(spark)
        case "3" =>
          mainmenu()
        case default =>
          println("Invalid entry, please try again!\n")
          Q2(spark)
      }
    }

    def Q3(spark: SparkSession): Unit = {
      import spark.implicits._
      val ecomercefile = spark.read.option("header", true).csv("Inputs//finaldata.csv")
      val ecommerceDF = ecomercefile.toDF()
      println("\n" + "Please enter:\n" +
        "1 to display the location with highest sales\n" +
        "2 to display sales for location of choice\n" +
        "3 to go back to main menu")
      print("Please choose you entry: ")
      val choice = scala.io.StdIn.readLine()
      choice match {
        case "1" =>
          val first1 = ecommerceDF.groupBy("City", "Country").count()
          val first2 = first1.orderBy($"Count".desc)
          val LWHS = first2.show(10)
          Q3(spark)
        case "2" =>
          println("Please enter city of choice: ")
          val city_in = scala.io.StdIn.readLine()
          val first1 = ecommerceDF.groupBy("City", "Country").count()
          val first2 = first1.orderBy($"Count".desc)
          val HSPC = first2.where(first2("City") === city_in).show()
          Q3(spark)
        case "3" =>
          mainmenu()
        case default =>
          println("Invalid entry, please try again!\n")
          Q3(spark)
      }
    }

    def Q4(spark: SparkSession): Unit = {
      import spark.implicits._
      val ecomercefile = spark.read.option("header", true).csv("Inputs//finaldata.csv")
      val ecommerceDF = ecomercefile.toDF()
      println("\n" + "Please enter:\n" +
        "1 to display the time with highest sales\n" +
        "2 to display total sales for time of choice\n" +
        "3 to display total sales per hour for country of choice\n" +
        "4 to go back to main menu")
      print("Please choose you entry: ")
      val choice = scala.io.StdIn.readLine()
      choice match {
        case "1" =>
          val step1 = ecommerceDF.select("DateTime", "Product_Name", "Country").withColumn("Hour", hour(col("DateTime")))
          val step2 = step1.groupBy("Hour").count()
          val BSPH = step2.orderBy($"count".desc)
          BSPH.show(5)
          Q4(spark)
        case "2" =>
          val step1 = ecommerceDF.select("DateTime", "Product_Name", "Country").withColumn("Hour", hour(col("DateTime")))
          val step2 = step1.groupBy("Hour").count()
          print("Please enter hour of choice: ")
          val hour_in = scala.io.StdIn.readLine()
          val BSPCH = step2.where(step2("Hour") === hour_in).show()
          Q4(spark)
        case "3" =>
          val step1 = ecommerceDF.select("DateTime", "Product_Name", "Country").withColumn("Hour", hour(col("DateTime")))
          val step3 = step1.groupBy("Country", "Hour").count()
          print("Please enter country of choice: ")
          val coun_in = scala.io.StdIn.readLine()
          val BSPHPC = step3.where(step3("Country") === coun_in).orderBy($"count".desc).show()
          Q4(spark)
        case "4" =>
          mainmenu()
        case default =>
          println("Invalid entry, please try again!\n")
          Q4(spark)
      }
    }
  }
}
