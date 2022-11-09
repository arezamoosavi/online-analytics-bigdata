import org.apache.spark.sql.functions.{col, expr}
import org.apache.spark.sql.{DataFrame, SparkSession}

// To see less warnings
import org.apache.log4j._

object RunStreamer extends App{
    Logger.getLogger("org").setLevel(Level.ERROR)

    val spark = SparkSession.builder()
      .appName("Stream App")
      .config("spark.master", "local")
      .getOrCreate()

    import spark.implicits._

    var raw_df = spark.read
      .format("csv")
      .option("sep", "\t")
      .option("inferSchema", "true")
      .option("header", "true")
      .load("sparking/src/main/resources/user-ct-test-collection.txt")


    raw_df.selectExpr("CAST(AnonID AS STRING) AS key", "to_json(struct(*)) AS value")
      .write
      .format("kafka")
      .mode("append")
      .option("kafka.bootstrap.servers", "localhost:9092")
      .option("topic", "click_topic")
      .save()

    println("Spark job finished!")
    spark.stop()
}
