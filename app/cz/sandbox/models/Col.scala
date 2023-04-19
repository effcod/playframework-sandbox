package cz.sandbox.models

case class Col(appName: String,
               tabName: String,
               colName: String,
               isPk: Boolean,
               isRequired: Boolean,
               colType: String,
               colFormat: Option[String]
              )