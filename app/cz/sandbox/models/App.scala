package cz.sandbox.models

case class App(
                appName: String,
                appDesc: String,
                isAllowedRestDownload: Option[Boolean],
                isAllowedRestUpload: Option[Boolean]
              )