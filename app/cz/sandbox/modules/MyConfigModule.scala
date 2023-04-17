
package cz.sandbox.modules

import com.google.inject.{AbstractModule, Provides, Singleton}
import org.slf4j.LoggerFactory
import play.api.Configuration
import cz.sandbox.MyConfig


class MyConfigModule extends AbstractModule {
  override def configure(): Unit = ()
    private val logger = LoggerFactory.getLogger(this.getClass)

  @Provides
  @Singleton
  def initConfig(config: Configuration): MyConfig = {
    logger.info("Starting Module AppConfigModule")
    new MyConfig
  }
}
