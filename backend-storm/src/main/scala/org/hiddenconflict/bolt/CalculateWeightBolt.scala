package org.hiddenconflict.bolt

import java.util
import java.util.Properties

import backtype.storm.task.{ OutputCollector, TopologyContext }
import backtype.storm.tuple.Tuple
import edu.stanford.nlp.ling.CoreAnnotations
import edu.stanford.nlp.neural.rnn.RNNCoreAnnotations
import edu.stanford.nlp.pipeline.StanfordCoreNLP
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations
import edu.stanford.nlp.util.CoreMap
import org.apache.log4j.Logger
import storm.scala.dsl.StormBolt

import scala.collection.JavaConverters._

/**
 * @author Andreas C. Osowski
 */
class CalculateWeightBolt extends StormBolt(List("status")) {
  var logger: Logger = null
  var pipeline: StanfordCoreNLP = null

  override def prepare(conf: util.Map[_, _], context: TopologyContext, collector: OutputCollector): Unit = {
    super.prepare(conf, context, collector)
    logger = Logger.getLogger(getClass)

    initNLP()
  }

  def initNLP() = {
    val props = new Properties()
    props.setProperty("annotators", "tokenize, ssplit, parse, sentiment")
    pipeline = new StanfordCoreNLP(props)
  }

  def calculateSentiment(message: String): Double = {
    val annotation = pipeline.process(message)
    val sentiments = annotation.get(classOf[CoreAnnotations.SentencesAnnotation]).asScala map { sentence: CoreMap =>
      val tree = sentence.get(classOf[SentimentCoreAnnotations.AnnotatedTree])
      val sentiment = RNNCoreAnnotations.getPredictedClass(tree)
      val part = sentence.toString
      (part.length, sentiment)
    } sortBy (_._1) toSeq

    (sentiments.reverse.head._2 - 1) / 2.0
  }

  override def execute(input: Tuple): Unit = {
    val status = input.getValueByField("status").asInstanceOf[StatusContent]

    val weight = calculateSentiment(status.text)
    logger.info(s"""caluclated sentiment of $weight for "${status.text}"""")

    using anchor input emit status.copy(
      weight = calculateSentiment(status.text)
    )
  }
}
