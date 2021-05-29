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

import com.github.edge.roman.spear.Connector
import com.mongodb.spark.MongoSpark
import com.mongodb.spark.config.WriteConfig
import org.apache.spark.sql.SaveMode

import java.util.Properties

abstract class AbstractTargetNoSQLConnector(sourceFormat: String, destFormat: String) extends AbstractConnector(sourceFormat: String, destFormat: String) with Connector {
  override def targetNoSQL(tableName: String, props: Properties, saveMode: SaveMode): Unit = {
    destFormat match {
      case "mongo" =>
        val writeConfig = WriteConfig(
          Map("uri" -> props.get("uri").toString.concat(s"/${props.get("database").toString}.${props.get("collection").toString}")))
        MongoSpark.save(this.df.write.format("mongo").mode(saveMode), writeConfig)
    }
  }

  override def targetFS(destinationFilePath: String, saveAsTable: String, saveMode: SaveMode): Unit = throw new NoSuchMethodException("method targetFS() not supported for given targetType nosql")

  override def targetFS(destinationFilePath: String, saveMode: SaveMode): Unit = throw new NoSuchMethodException("method targetFS() not supported for given targetType nosql")

  override def targetFS(destinationFilePath: String, params: Map[String, String]): Unit = throw new NoSuchMethodException("method targetFS() not compatible for given targetType nosql")

  override def targetJDBC(tableName: String, props: Properties, saveMode: SaveMode): Unit = throw new NoSuchMethodException("method targetJDBC() not compatible for given targetType nosql")
}
