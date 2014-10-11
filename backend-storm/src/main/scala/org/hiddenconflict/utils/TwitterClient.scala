package org.hiddenconflict.utils

import twitter4j.auth.AccessToken
import twitter4j.{ TwitterFactory, TwitterStreamFactory }
import twitter4j.conf.ConfigurationBuilder

/**
 * @author Andreas C. Osowski
 */

sealed trait TwitterConfig {
  private[this] val consumerKey = "OypeY1uGpzqZ8gQNzhT5pv9QF"
  private[this] val consumerSecret = "TVfHVBFW8HJXhYRNh7tkHTxggLmRAfMbCu2Y5jgBRneHIF0bSu"
  private[this] val accessToken = "17422249-FTPgN7WOoyY6gwJfHlh2ieH4z3SwpJCImLGIUYcqw"
  private[this] val accessTokenSecret = "A5ZjRgxwMc7cLACkCCRtwOaBjGbDdkBEs3vEoozK0IOb1"

  val twitterConfig = new ConfigurationBuilder().setJSONStoreEnabled(true)
    .setOAuthConsumerKey(consumerKey)
    .setOAuthConsumerSecret(consumerSecret)
    .setOAuthAccessToken(accessToken)
    .setOAuthAccessTokenSecret(accessTokenSecret)
    // .setHttpProxyHost("localhost")
    // .setHttpProxyPort(9999)
    .build()
}

trait TwitterStreamClient extends TwitterConfig {
  lazy val twitterStreamFactory = new TwitterStreamFactory(twitterConfig)
  lazy val twitterStream = twitterStreamFactory.getInstance()
}

trait TwitterClient extends TwitterConfig {
  lazy val twitterFactory = new TwitterFactory(twitterConfig)
  lazy val twitter = twitterFactory.getInstance()
}