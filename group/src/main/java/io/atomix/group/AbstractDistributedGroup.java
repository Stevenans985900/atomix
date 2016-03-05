/*
 * Copyright 2016 the original author or authors.
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
 * limitations under the License
 */
package io.atomix.group;

import io.atomix.catalyst.serializer.Serializer;
import io.atomix.catalyst.util.Assert;
import io.atomix.catalyst.util.Listener;
import io.atomix.catalyst.util.concurrent.ThreadContext;
import io.atomix.catalyst.util.hash.Hasher;
import io.atomix.catalyst.util.hash.Murmur2Hasher;
import io.atomix.resource.ReadConsistency;
import io.atomix.resource.ResourceType;
import io.atomix.resource.WriteConsistency;

import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.function.Consumer;

/**
 * Abstract distributed group.
 *
 * @author <a href="http://github.com/kuujo>Jordan Halterman</a>
 */
public abstract class AbstractDistributedGroup implements DistributedGroup {
  protected final MembershipGroup group;
  protected final Set<AbstractDistributedGroup> children = new CopyOnWriteArraySet<>();

  protected AbstractDistributedGroup(MembershipGroup group) {
    this.group = Assert.notNull(group, "group");
  }

  @Override
  public ResourceType type() {
    return group.type();
  }

  @Override
  public Serializer serializer() {
    return group.serializer();
  }

  @Override
  public ThreadContext context() {
    return group.context();
  }

  @Override
  public Config config() {
    return group.config();
  }

  @Override
  public Options options() {
    return group.options();
  }

  @Override
  public State state() {
    return group.state();
  }

  @Override
  public Listener<State> onStateChange(Consumer<State> callback) {
    return group.onStateChange(callback);
  }

  @Override
  public WriteConsistency writeConsistency() {
    return group.writeConsistency();
  }

  @Override
  public DistributedGroup with(WriteConsistency consistency) {
    group.with(consistency);
    return this;
  }

  @Override
  public ReadConsistency readConsistency() {
    return group.readConsistency();
  }

  @Override
  public DistributedGroup with(ReadConsistency consistency) {
    group.with(consistency);
    return this;
  }

  @Override
  public GroupProperties properties() {
    return group.properties();
  }

  @Override
  public GroupElection election() {
    return group.election();
  }

  @Override
  public GroupTaskQueue tasks() {
    return group.tasks();
  }

  @Override
  public ConsistentHashGroup hash() {
    return hash(new Murmur2Hasher(), 100);
  }

  @Override
  public ConsistentHashGroup hash(Hasher hasher) {
    return hash(hasher, 100);
  }

  @Override
  public ConsistentHashGroup hash(int virtualNodes) {
    return hash(new Murmur2Hasher(), virtualNodes);
  }

  @Override
  public ConsistentHashGroup hash(Hasher hasher, int virtualNodes) {
    ConsistentHashGroup group = new ConsistentHashGroup(this.group, members(), hasher, virtualNodes);
    children.add(group);
    return group;
  }

  @Override
  public PartitionGroup partition(int partitions) {
    return partition(partitions, 1, new HashPartitioner());
  }

  @Override
  public PartitionGroup partition(int partitions, int replicationFactor) {
    return partition(partitions, replicationFactor, new HashPartitioner());
  }

  @Override
  public PartitionGroup partition(int partitions, GroupPartitioner partitioner) {
    return partition(partitions, 1, partitioner);
  }

  @Override
  public PartitionGroup partition(int partitions, int replicationFactor, GroupPartitioner partitioner) {
    PartitionGroup group = new PartitionGroup(this.group, members(), partitions, replicationFactor, partitioner);
    children.add(group);
    return group;
  }

  @Override
  public CompletableFuture<LocalGroupMember> join() {
    return group.join();
  }

  @Override
  public CompletableFuture<LocalGroupMember> join(String memberId) {
    return group.join(memberId);
  }

  @Override
  public CompletableFuture<DistributedGroup> open() {
    return CompletableFuture.completedFuture(this);
  }

  @Override
  public CompletableFuture<Void> close() {
    return CompletableFuture.completedFuture(null);
  }

  @Override
  public CompletableFuture<Void> delete() {
    return CompletableFuture.completedFuture(null);
  }

  @Override
  public boolean isOpen() {
    return group.isOpen();
  }

  @Override
  public boolean isClosed() {
    return group.isClosed();
  }

  protected abstract void onJoin(GroupMember member);

  protected abstract void onLeave(GroupMember member);

  @Override
  public String toString() {
    return String.format("%s[members=%s]", getClass().getSimpleName(), members());
  }

}
