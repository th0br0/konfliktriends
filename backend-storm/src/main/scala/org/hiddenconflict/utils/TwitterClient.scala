package org.hiddenconflict.utils

import twitter4j.auth.AccessToken
import twitter4j.{ TwitterFactory, TwitterStreamFactory }
import twitter4j.conf.ConfigurationBuilder

/**
 * @author Andreas C. Osowski
 */

sealed trait TwitterConfig {
  val consumerKey = "OypeY1uGpzqZ8gQNzhT5pv9QF"
  val consumerSecret = "TVfHVBFW8HJXhYRNh7tkHTxggLmRAfMbCu2Y5jgBRneHIF0bSu"
  val accessToken = "17422249-FTPgN7WOoyY6gwJfHlh2ieH4z3SwpJCImLGIUYcqw"
  val accessTokenSecret = "A5ZjRgxwMc7cLACkCCRtwOaBjGbDdkBEs3vEoozK0IOb1"

  val twitterConfig = new ConfigurationBuilder().setJSONStoreEnabled(true)
    .setOAuthConsumerKey(consumerKey)
    .setOAuthConsumerSecret(consumerSecret)
    .setOAuthAccessToken(accessToken)
    .setOAuthAccessTokenSecret(accessTokenSecret)
    .build()
}

trait TwitterStreamClient extends TwitterConfig {
  lazy val twitterStreamFactory = new TwitterStreamFactory(twitterConfig)
  lazy val twitterStream = {
    val stream = twitterStreamFactory.getInstance()
    stream.setOAuthAccessToken(new AccessToken(accessToken, accessTokenSecret))
    stream.setOAuthConsumer(consumerKey, consumerSecret)
    stream
  }
}

trait TwitterClient extends TwitterConfig {
  lazy val twitterFactory = new TwitterFactory(twitterConfig)
  lazy val twitter = {
    val cli = twitterFactory.getInstance()
    cli.setOAuthAccessToken(new AccessToken(accessToken, accessTokenSecret))
    cli.setOAuthConsumer(consumerKey, consumerSecret)
    cli
  }
}