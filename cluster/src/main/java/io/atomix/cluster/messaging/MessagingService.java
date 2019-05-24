/*
 * Copyright 2015-present Open Networking Foundation
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
package io.atomix.cluster.messaging;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import com.google.common.util.concurrent.MoreExecutors;
import io.atomix.utils.net.Address;
import io.atomix.utils.stream.StreamFunction;
import io.atomix.utils.stream.StreamHandler;

/**
 * Interface for low level messaging primitives.
 */
public interface MessagingService {

  /**
   * Returns the local messaging service address.
   *
   * @return the local address
   */
  Address address();

  /**
   * Sends a message asynchronously to the specified communication address.
   * The message is specified using the type and payload.
   *
   * @param address   address to send the message to.
   * @param type      type of message.
   * @param payload   message payload bytes.
   * @return future that is completed when the message is sent
   */
  CompletableFuture<Void> sendAsync(Address address, String type, byte[] payload);

  /**
   * Sends a message asynchronously to the specified communication address.
   * The message is specified using the type and payload.
   *
   * @param address address to send the message to.
   * @param type    type of message.
   * @return future that is completed when the message is sent
   */
  CompletableFuture<StreamHandler<byte[]>> sendStreamAsync(Address address, String type);

  /**
   * Sends a message asynchronously and expects a response.
   *
   * @param address   address to send the message to.
   * @param type      type of message.
   * @param payload   message payload.
   * @return a response future
   */
  default CompletableFuture<byte[]> sendAndReceive(Address address, String type, byte[] payload) {
    return sendAndReceive(address, type, payload, Duration.ZERO, MoreExecutors.directExecutor());
  }

  /**
   * Sends a message synchronously and expects a response.
   *
   * @param address   address to send the message to.
   * @param type      type of message.
   * @param payload   message payload.
   * @param executor  executor over which any follow up actions after completion will be executed.
   * @return a response future
   */
  default CompletableFuture<byte[]> sendAndReceive(Address address, String type, byte[] payload, Executor executor) {
    return sendAndReceive(address, type, payload, Duration.ZERO, executor);
  }

  /**
   * Sends a message asynchronously and expects a response.
   *
   * @param address address to send the message to.
   * @param type    type of message.
   * @param payload message payload.
   * @param timeout response timeout
   * @return a response future
   */
  default CompletableFuture<byte[]> sendAndReceive(Address address, String type, byte[] payload, Duration timeout) {
    return sendAndReceive(address, type, payload, timeout, MoreExecutors.directExecutor());
  }

  /**
   * Sends a message synchronously and expects a response.
   *
   * @param address   address to send the message to.
   * @param type      type of message.
   * @param payload   message payload.
   * @param timeout   response timeout
   * @param executor  executor over which any follow up actions after completion will be executed.
   * @return a response future
   */
  CompletableFuture<byte[]> sendAndReceive(Address address, String type, byte[] payload, Duration timeout, Executor executor);

  default CompletableFuture<StreamFunction<byte[], CompletableFuture<byte[]>>> sendStreamAndReceive(Address address, String type) {
    return sendStreamAndReceive(address, type, null, MoreExecutors.directExecutor());
  }

  default CompletableFuture<StreamFunction<byte[], CompletableFuture<byte[]>>> sendStreamAndReceive(Address address, String type, Duration timeout) {
    return sendStreamAndReceive(address, type, timeout, MoreExecutors.directExecutor());
  }

  default CompletableFuture<StreamFunction<byte[], CompletableFuture<byte[]>>> sendStreamAndReceive(Address address, String type, Executor executor) {
    return sendStreamAndReceive(address, type, null, executor);
  }

  CompletableFuture<StreamFunction<byte[], CompletableFuture<byte[]>>> sendStreamAndReceive(Address address, String type, Duration timeout, Executor executor);

  default CompletableFuture<Void> sendAndReceiveStream(Address address, String type, byte[] payload, StreamHandler<byte[]> handler) {
    return sendAndReceiveStream(address, type, payload, handler, null, MoreExecutors.directExecutor());
  }

  default CompletableFuture<Void> sendAndReceiveStream(Address address, String type, byte[] payload, StreamHandler<byte[]> handler, Duration timeout) {
    return sendAndReceiveStream(address, type, payload, handler, timeout, MoreExecutors.directExecutor());
  }

  default CompletableFuture<Void> sendAndReceiveStream(Address address, String type, byte[] payload, StreamHandler<byte[]> handler, Executor executor) {
    return sendAndReceiveStream(address, type, payload, handler, null, executor);
  }

  CompletableFuture<Void> sendAndReceiveStream(Address address, String type, byte[] payload, StreamHandler<byte[]> handler, Duration timeout, Executor executor);

  default CompletableFuture<StreamHandler<byte[]>> sendStreamAndReceiveStream(Address address, String type, StreamHandler<byte[]> handler) {
    return sendStreamAndReceiveStream(address, type, handler, null, MoreExecutors.directExecutor());
  }

  default CompletableFuture<StreamHandler<byte[]>> sendStreamAndReceiveStream(Address address, String type, StreamHandler<byte[]> handler, Duration timeout) {
    return sendStreamAndReceiveStream(address, type, handler, timeout, MoreExecutors.directExecutor());
  }

  default CompletableFuture<StreamHandler<byte[]>> sendStreamAndReceiveStream(Address address, String type, StreamHandler<byte[]> handler, Executor executor) {
    return sendStreamAndReceiveStream(address, type, handler, null, executor);
  }

  CompletableFuture<StreamHandler<byte[]>> sendStreamAndReceiveStream(Address address, String type, StreamHandler<byte[]> handler, Duration timeout, Executor executor);

  /**
   * Registers a new message handler for message type.
   *
   * @param type     message type.
   * @param handler  message handler
   * @param executor executor to use for running message handler logic.
   */
  void registerHandler(String type, Consumer<byte[]> handler, Executor executor);

  /**
   * Registers a new message handler for message type.
   *
   * @param type     message type.
   * @param handler  message handler
   * @param executor executor to use for running message handler logic.
   */
  void registerHandler(String type, Function<byte[], byte[]> handler, Executor executor);

  /**
   * Registers a new message handler for message type.
   *
   * @param type    message type.
   * @param handler message handler
   */
  void registerHandler(String type, Function<byte[], CompletableFuture<byte[]>> handler);

  void registerStreamHandler(String type, Supplier<StreamFunction<byte[], CompletableFuture<byte[]>>> handler);

  void registerStreamingHandler(String type, BiConsumer<byte[], StreamHandler<byte[]>> handler);

  void registerStreamingStreamHandler(String type, Function<StreamHandler<byte[]>, StreamHandler<byte[]>> handler);

  /**
   * Unregister current handler, if one exists for message type.
   *
   * @param type message type
   */
  void unregisterHandler(String type);

  /**
   * Messaging service builder.
   */
  abstract class Builder implements io.atomix.utils.Builder<MessagingService> {
  }
}