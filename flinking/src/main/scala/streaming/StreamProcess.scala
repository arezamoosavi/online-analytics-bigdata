package streaming

import java.io.IOException
import org.apache.flink.api.common.eventtime.WatermarkStrategy
import org.apache.flink.api.common.serialization.SimpleStringSchema
import org.apache.flink.api.common.typeinfo.TypeInformation
import org.apache.flink.connector.kafka.source.KafkaSource
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer
import org.apache.flink.streaming.api.scala._
import org.apache.kafka.clients.consumer.OffsetResetStrategy
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.json.JsonMapper
import org.apache.flink.api.common.serialization.AbstractDeserializationSchema
import org.apache.flink.api.common.serialization.DeserializationSchema.InitializationContext
import org.apache.flink.connector.jdbc.{JdbcConnectionOptions, JdbcSink, JdbcStatementBuilder}
import org.apache.flink.streaming.api.datastream.DataStreamSource

import java.sql.PreparedStatement
import java.time.Duration

object StreamProcess {

  val env = StreamExecutionEnvironment.getExecutionEnvironment

  // read simple data (strings) from a Kafka topic
  def readStrings(): Unit = {
    val kafkaSource = KafkaSource.builder[String]()
      .setBootstrapServers("localhost:9092")
      .setTopics("click_topic")
      .setGroupId("click_topic-group")
      .setStartingOffsets(OffsetsInitializer.committedOffsets(OffsetResetStrategy.EARLIEST))
      .setValueOnlyDeserializer(new SimpleStringSchema())
      .build()

    val kafkaStrings: DataStream[String] = env.fromSource(kafkaSource,
      WatermarkStrategy.noWatermarks(), "Kafka Source")

    // use the DS
    kafkaStrings.print()
    env.execute()
  }

  // read custom data
  case class ClickEvent(AnonID: String, Query: String, QueryTime: String,
                        ItemRank: String, ClickURL: String)

  class ClickEventDeserializer extends AbstractDeserializationSchema[ClickEvent] {
    private var objectMapper: ObjectMapper = null

    override def open(context: InitializationContext): Unit = {
      objectMapper = JsonMapper.builder().build()
    }

    @throws[IOException]
    override def deserialize(message: Array[Byte]): ClickEvent = {
      val string = new String(message)

      val items = string.split(",")
      val anonId = items(0)
      val query = items(1)
      val queryTime = items(2)
      val itemRank = items(3)
      val clickURL = items(4)
      ClickEvent(AnonID = anonId, Query = query, QueryTime = queryTime,
        ItemRank = itemRank, ClickURL = clickURL)
    }

    override def isEndOfStream(nextElement: ClickEvent): Boolean = false

    override def getProducedType: TypeInformation[ClickEvent] = implicitly[TypeInformation[ClickEvent]]
  }


  def writeStreamToPostgres(): Unit = {
    val kafkaSource = KafkaSource.builder[ClickEvent]()
      .setBootstrapServers("localhost:9092")
      .setTopics("click_topic")
      .setGroupId("click_topic-group")
      .setStartingOffsets(OffsetsInitializer.committedOffsets(OffsetResetStrategy.EARLIEST))
      .setValueOnlyDeserializer(new ClickEventDeserializer)
      .build()

      val kafkaClickEvents: DataStream[ClickEvent] =
      env.fromSource(kafkaSource, WatermarkStrategy.noWatermarks()
        , "Kafka Source")


    val statementBuilder: JdbcStatementBuilder[(String, String, String, String, String)] =
      (ps: PreparedStatement, t: (String, String, String, String, String)) => {
        ps.setString(1, t._1);
        ps.setString(2, t._2);
        ps.setString(3, t._3);
        ps.setString(4, t._4);
        ps.setString(5, t._5);

      };

    val connection = new JdbcConnectionOptions.JdbcConnectionOptionsBuilder()
      .withDriverName("org.postgresql.Driver")
      .withUrl("jdbc:postgresql://postgres:5432/postgres")
      .withUsername("admin")
      .withPassword("admin")
      .build();

    val jdbcSink = JdbcSink.sink(
      "INSERT INTO postgres.public.click_events (anon_id, query, query_time, item_rank, click_url) VALUES (?, ?, ?, ?, ?)",
      statementBuilder,
      connection);

    kafkaClickEvents.addSink(jdbcSink)

    env.execute("Aggregator")

    kafkaClickEvents.print()
    env.execute()
  }


  def main(args: Array[String]): Unit = {
    println("Hey I am steaming. . . ")
    //    readStrings()
    writeStreamToPostgres()
  }
}
