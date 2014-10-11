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

  val twitterConfig = new ConfigurationBuilder().setJSONStoreEnabled(true).build()
}

trait TwitterStreamClient extends TwitterConfig {
  lazy val twitterStream = {
    val stream = new TwitterStreamFactory(twitterConfig).getInstance()
    stream.setOAuthAccessToken(new AccessToken(accessToken, accessTokenSecret))
    stream.setOAuthConsumer(consumerKey, consumerSecret)
    stream
  }
}

trait TwitterClient extends TwitterConfig {
  lazy val twitter = {
    val cli = new TwitterFactory(twitterConfig).getInstance()
    cli.setOAuthAccessToken(new AccessToken(accessToken, accessTokenSecret))
    cli.setOAuthConsumer(consumerKey, consumerSecret)
    cli
  }
}