/*
 * Copyright 2017-present Open Networking Foundation
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
package io.atomix.core.lock.impl;

import java.util.concurrent.CompletableFuture;

import io.atomix.core.lock.AsyncDistributedLock;
import io.atomix.core.lock.DistributedLock;
import io.atomix.core.lock.DistributedLockBuilder;
import io.atomix.core.lock.DistributedLockConfig;
import io.atomix.primitive.PrimitiveManagementService;
import io.atomix.primitive.protocol.ServiceProtocol;
import io.atomix.primitive.service.impl.ServiceId;
import io.atomix.primitive.session.impl.DefaultSessionClient;

/**
 * Default distributed lock builder implementation.
 */
public class DefaultDistributedLockBuilder extends DistributedLockBuilder {
  public DefaultDistributedLockBuilder(String name, DistributedLockConfig config, PrimitiveManagementService managementService) {
    super(name, config, managementService);
  }

  @Override
  @SuppressWarnings("unchecked")
  public CompletableFuture<DistributedLock> buildAsync() {
    ServiceProtocol protocol = (ServiceProtocol) protocol();
    ServiceId serviceId = ServiceId.newBuilder()
        .setName(name)
        .setType(LockService.TYPE.name())
        .build();
    return protocol.createService(name, managementService.getPartitionService())
        .thenApply(client -> new LockProxy(new DefaultSessionClient(serviceId, client.getPartition(name))))
        .thenApply(proxy -> new DefaultAsyncAtomicLock(proxy, config.getSessionTimeout(), managementService))
        .thenApply(DelegatingAsyncDistributedLock::new)
        .thenApply(AsyncDistributedLock::sync);
  }
}
