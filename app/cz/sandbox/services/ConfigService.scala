package cz.sandbox.services

import cz.sandbox.models._

trait ConfigService {
  def getApps: List[App]
  def getTabs(appName: String): List[Tab]
}
