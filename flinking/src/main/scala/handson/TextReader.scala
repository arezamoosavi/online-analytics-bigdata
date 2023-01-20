package handson

import org.apache.flink.api.scala.ExecutionEnvironment
import org.apache.flink.streaming.api.scala.StreamExecutionEnvironment

object TextReader {
  def main(args: Array[String]) {

    val env = ExecutionEnvironment.createLocalEnvironment(1)
    // access flink configuration after table environment instantiation

    val dataset = env.readTextFile("flinking/src/main/resources/user-ct-test-collection.txt")

    dataset.first(10).print()

  }
}
