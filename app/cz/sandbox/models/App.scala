package cz.sandbox.models

case class App(
                appName: String,
                appDesc: String,
                isAllowedRestDownload: Option[Boolean],
                isAllowedRestUpload: Option[Boolean]
              )


case class Tab(
                appName: String,
                tabName: String,
                schema: String
              )

case class Col(appName: String,
                  tabName: String,
                  colName: String,
                  isPk: Boolean,
                  isRequired: Boolean,
                  colType: String,
                  colFormat: Option[String]
                 )