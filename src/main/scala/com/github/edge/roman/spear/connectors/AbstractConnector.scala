/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.github.edge.roman.spear.connectors

import com.github.edge.roman.spear.commons.{ConnectorCommon, SpearCommons}
import com.github.edge.roman.spear.{Connector, SpearConnector}
import org.apache.log4j.Logger
import org.apache.spark.sql.types.StructType
import org.apache.spark.sql.{DataFrame, SaveMode}

import java.util.Properties

abstract class AbstractConnector(sourceFormat: String, destFormat: String) extends Connector {
  val logger: Logger = Logger.getLogger(this.getClass.getName)
  var df: DataFrame = _
  var verboseLogging: Boolean = false
  var numRows:Int = SpearCommons.ShowNumRows

  def setVeboseLogging(enable: Boolean): Unit = {
    this.verboseLogging = enable
  }

  def saveAs(alias: String): Connector = {
    this.df.createOrReplaceTempView(alias)
    logger.info(s"Saving data as temporary table:${alias} ${SpearCommons.SuccessStatus}")
    this
  }

  def cacheData(): Connector = {
    this.df.cache()
    logger.info(s"Cached data in dataframe: ${SpearCommons.SuccessStatus}")
    this
  }

  def repartition(n: Int): Connector = {
    this.df.repartition(n)
    logger.info(s"Repartition data in dataframe: ${SpearCommons.SuccessStatus}")
    this
  }

  def coalesce(n: Int): Connector = {
    this.df.coalesce(n)
    logger.info(s"Coalesce data in dataframe: ${SpearCommons.SuccessStatus}")
    this
  }

  def toDF: DataFrame = this.df

  def stop(): Unit = SpearConnector.spark.stop()

  override def source(sourceObject: String, params: Map[String, String], schema: StructType): Connector = {
    val paramsWithSchema = params + (SpearCommons.CustomSchema -> schema.toString())
    source(sourceObject, paramsWithSchema)
  }

  override def sourceSql(params: Map[String, String], sqlText: String): Connector = {
    logger.info(s"Connector from Source sql: ${sqlText} with Format: ${sourceFormat} started running!!")
    this.df = ConnectorCommon.sourceSQL(sqlText, sourceFormat, params)
    logger.info(s"Executing source sql query: ${sqlText} with format: ${sourceFormat} status:${SpearCommons.SuccessStatus}")
    show()
    this
  }

  override def transformSql(sqlText: String): Connector = {
    this.df = this.df.sqlContext.sql(sqlText)
    logger.info(s"Executing tranformation sql: ${sqlText} status :${SpearCommons.SuccessStatus}")
    show()
    this
  }

  override def targetSql(sqlText: String, props: Properties, saveMode: SaveMode): Unit = {
    this.df.sqlContext.sql(sqlText)
  }

  def show(): Unit = if (this.verboseLogging) this.df.show(this.numRows, false)
}