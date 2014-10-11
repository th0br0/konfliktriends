package org.hiddenconflict.utils

import twitter4j.Status

/**
 * @author Andreas C. Osowski
 */
object StatusHelpers {
  implicit class RichStatus(status: Status) {
    def containsMentions = status.getUserMentionEntities.length > 0

    def hasTweetLocation = status.getGeoLocation != null

    def hasAuthorLocation = !status.getUser.getLocation.isEmpty

    def hasLocation = hasTweetLocation || hasAuthorLocation
  }

}
