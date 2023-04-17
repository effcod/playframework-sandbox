package cz.sandbox.models

case class App(
                appName: String,
                appDesc: String,
                isRestAllowed: Option[Boolean]
              )