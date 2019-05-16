/*
 * Copyright 2018-present Open Networking Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.atomix.primitive.protocol;

import java.util.concurrent.CompletableFuture;

import io.atomix.primitive.PrimitiveClient;
import io.atomix.primitive.partition.PartitionService;

/**
 * State machine replication-based primitive protocol.
 */
public interface ServiceProtocol extends PrimitiveProtocol {

  /**
   * Returns the protocol partition group name.
   *
   * @return the protocol partition group name
   */
  String group();

  /**
   * Creates a new service via the protocol.
   *
   * @param name             the service name
   * @param partitionService the partition service
   * @return the service client
   */
  CompletableFuture<PrimitiveClient> createService(String name, PartitionService partitionService);

}
