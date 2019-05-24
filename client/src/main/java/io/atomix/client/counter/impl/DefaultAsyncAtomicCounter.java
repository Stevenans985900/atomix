/*
 * Copyright 2016-present Open Networking Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.atomix.client.counter.impl;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;

import io.atomix.api.counter.CheckAndSetRequest;
import io.atomix.api.counter.CheckAndSetResponse;
import io.atomix.api.counter.CounterId;
import io.atomix.api.counter.CounterServiceGrpc;
import io.atomix.api.counter.CreateRequest;
import io.atomix.api.counter.CreateResponse;
import io.atomix.api.counter.DecrementRequest;
import io.atomix.api.counter.DecrementResponse;
import io.atomix.api.counter.DeleteRequest;
import io.atomix.api.counter.DeleteResponse;
import io.atomix.api.counter.GetRequest;
import io.atomix.api.counter.GetResponse;
import io.atomix.api.counter.IncrementRequest;
import io.atomix.api.counter.IncrementResponse;
import io.atomix.api.counter.SetRequest;
import io.atomix.api.counter.SetResponse;
import io.atomix.api.protocol.DistributedLogProtocol;
import io.atomix.api.protocol.MultiPrimaryProtocol;
import io.atomix.api.protocol.MultiRaftProtocol;
import io.atomix.client.PrimitiveManagementService;
import io.atomix.client.counter.AsyncAtomicCounter;
import io.atomix.client.counter.AtomicCounter;
import io.atomix.client.impl.AbstractAsyncPrimitive;
import io.atomix.client.impl.PrimitiveIdDescriptor;
import io.atomix.client.impl.PrimitivePartition;
import io.atomix.primitive.partition.Partitioner;

/**
 * Atomix counter implementation.
 */
public class DefaultAsyncAtomicCounter
    extends AbstractAsyncPrimitive<CounterId, AsyncAtomicCounter>
    implements AsyncAtomicCounter {
  private final CounterServiceGrpc.CounterServiceStub counter;

  public DefaultAsyncAtomicCounter(
      CounterId id,
      PrimitiveManagementService managementService) {
    super(id, COUNTER_ID_DESCRIPTOR, managementService, Partitioner.MURMUR3, Duration.ZERO);
    this.counter = CounterServiceGrpc.newStub(managementService.getChannelFactory().getChannel());
  }

  @Override
  public CompletableFuture<Long> get() {
    PrimitivePartition partition = getPartition();
    return this.<GetResponse>execute(observer -> counter.get(GetRequest.newBuilder()
        .setId(id())
        .setHeader(partition.getRequestHeader())
        .build(), observer))
        .thenCompose(response -> partition.complete(response.getValue(), response.getHeader()));
  }

  @Override
  public CompletableFuture<Void> set(long value) {
    PrimitivePartition partition = getPartition();
    return this.<SetResponse>execute(observer -> counter.set(SetRequest.newBuilder()
        .setId(id())
        .setHeader(partition.getRequestHeader())
        .setValue(value)
        .build(), observer))
        .thenCompose(response -> partition.complete(null, response.getHeader()));
  }

  @Override
  public CompletableFuture<Boolean> compareAndSet(long expectedValue, long updateValue) {
    PrimitivePartition partition = getPartition();
    return this.<CheckAndSetResponse>execute(observer -> counter.checkAndSet(CheckAndSetRequest.newBuilder()
        .setId(id())
        .setHeader(partition.getRequestHeader())
        .setExpect(expectedValue)
        .setUpdate(updateValue)
        .build(), observer))
        .thenCompose(response -> partition.complete(response.getSucceeded(), response.getHeader()));
  }

  @Override
  public CompletableFuture<Long> addAndGet(long delta) {
    PrimitivePartition partition = getPartition();
    return this.<IncrementResponse>execute(observer -> counter.increment(IncrementRequest.newBuilder()
        .setId(id())
        .setHeader(partition.getRequestHeader())
        .setDelta(delta)
        .build(), observer))
        .thenCompose(response -> partition.complete(response.getNextValue(), response.getHeader()));
  }

  @Override
  public CompletableFuture<Long> getAndAdd(long delta) {
    PrimitivePartition partition = getPartition();
    return this.<IncrementResponse>execute(observer -> counter.increment(IncrementRequest.newBuilder()
        .setId(id())
        .setHeader(partition.getRequestHeader())
        .setDelta(delta)
        .build(), observer))
        .thenCompose(response -> partition.complete(response.getPreviousValue(), response.getHeader()));
  }

  @Override
  public CompletableFuture<Long> incrementAndGet() {
    PrimitivePartition partition = getPartition();
    return this.<IncrementResponse>execute(observer -> counter.increment(IncrementRequest.newBuilder()
        .setId(id())
        .setHeader(partition.getRequestHeader())
        .build(), observer))
        .thenCompose(response -> partition.complete(response.getNextValue(), response.getHeader()));
  }

  @Override
  public CompletableFuture<Long> getAndIncrement() {
    PrimitivePartition partition = getPartition();
    return this.<IncrementResponse>execute(observer -> counter.increment(IncrementRequest.newBuilder()
        .setId(id())
        .setHeader(partition.getRequestHeader())
        .build(), observer))
        .thenCompose(response -> partition.complete(response.getPreviousValue(), response.getHeader()));
  }

  @Override
  public CompletableFuture<Long> decrementAndGet() {
    PrimitivePartition partition = getPartition();
    return this.<DecrementResponse>execute(observer -> counter.decrement(DecrementRequest.newBuilder()
        .setId(id())
        .setHeader(partition.getRequestHeader())
        .build(), observer))
        .thenCompose(response -> partition.complete(response.getNextValue(), response.getHeader()));
  }

  @Override
  public CompletableFuture<Long> getAndDecrement() {
    PrimitivePartition partition = getPartition();
    return this.<DecrementResponse>execute(observer -> counter.decrement(DecrementRequest.newBuilder()
        .setId(id())
        .setHeader(partition.getRequestHeader())
        .build(), observer))
        .thenCompose(response -> partition.complete(response.getPreviousValue(), response.getHeader()));
  }

  @Override
  public CompletableFuture<AsyncAtomicCounter> connect() {
    return this.<CreateResponse>execute(observer -> counter.create(CreateRequest.newBuilder()
        .setId(id())
        .build(), observer))
        .thenApply(response -> {
          getOrCreatePartition(response.getHeader().getPartitionId()).init(response.getHeader());
          return this;
        });
  }

  @Override
  protected CompletableFuture<Void> keepAlive() {
    return CompletableFuture.completedFuture(null);
  }

  @Override
  public CompletableFuture<Void> close() {
    return CompletableFuture.completedFuture(null);
  }

  @Override
  public CompletableFuture<Void> delete() {
    PrimitivePartition partition = getPartition();
    return this.<DeleteResponse>execute(observer -> counter.delete(DeleteRequest.newBuilder()
        .setId(id())
        .setHeader(partition.getRequestHeader())
        .build(), observer))
        .thenApply(v -> null);
  }

  @Override
  public AtomicCounter sync(Duration operationTimeout) {
    return new BlockingAtomicCounter(this, operationTimeout.toMillis());
  }

  private static final PrimitiveIdDescriptor<CounterId> COUNTER_ID_DESCRIPTOR = new PrimitiveIdDescriptor<CounterId>() {
    @Override
    public String getName(CounterId id) {
      return id.getName();
    }

    @Override
    public boolean hasMultiRaftProtocol(CounterId id) {
      return id.hasRaft();
    }

    @Override
    public MultiRaftProtocol getMultiRaftProtocol(CounterId id) {
      return id.getRaft();
    }

    @Override
    public boolean hasMultiPrimaryProtocol(CounterId id) {
      return id.hasMultiPrimary();
    }

    @Override
    public MultiPrimaryProtocol getMultiPrimaryProtocol(CounterId id) {
      return id.getMultiPrimary();
    }

    @Override
    public boolean hasDistributedLogProtocol(CounterId id) {
      return id.hasLog();
    }

    @Override
    public DistributedLogProtocol getDistributedLogProtocol(CounterId id) {
      return id.getLog();
    }
  };
}