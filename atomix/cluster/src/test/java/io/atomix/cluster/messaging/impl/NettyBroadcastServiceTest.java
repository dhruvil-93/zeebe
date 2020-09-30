/*
 * Copyright 2018-present Open Networking Foundation
 * Copyright © 2020 camunda services GmbH (info@camunda.com)
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
package io.atomix.cluster.messaging.impl;

import static org.slf4j.LoggerFactory.getLogger;

import io.atomix.cluster.messaging.ManagedBroadcastService;
import io.atomix.utils.net.Address;
import io.zeebe.test.util.socket.SocketUtil;
import net.jodah.concurrentunit.ConcurrentTestCase;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;

/** Netty broadcast service test. */
public class NettyBroadcastServiceTest extends ConcurrentTestCase {

  private static final Logger LOGGER = getLogger(NettyBroadcastServiceTest.class);
  private static final String BROADCAST_INTERFACE_HOST = "127.0.0.1";

  ManagedBroadcastService netty1;
  ManagedBroadcastService netty2;

  Address groupAddress;

  @Test
  public void testBroadcast() throws Exception {
    netty1.addListener(
        "test",
        bytes -> {
          threadAssertEquals(0, bytes.length);
          resume();
        });

    netty2.broadcast("test", new byte[0]);
    await(5000);
  }

  @Before
  public void setUp() throws Exception {
    groupAddress = Address.from("230.0.0.1", SocketUtil.getNextAddress().getPort());

    netty1 =
        (ManagedBroadcastService)
            NettyBroadcastService.builder()
                .withHost(BROADCAST_INTERFACE_HOST)
                .withGroupAddress(groupAddress)
                .build()
                .start()
                .join();

    netty2 =
        (ManagedBroadcastService)
            NettyBroadcastService.builder()
                .withHost(BROADCAST_INTERFACE_HOST)
                .withGroupAddress(groupAddress)
                .build()
                .start()
                .join();
  }

  @After
  public void tearDown() throws Exception {
    if (netty1 != null) {
      try {
        netty1.stop().join();
      } catch (final Exception e) {
        LOGGER.warn("Failed stopping netty1", e);
      }
    }

    if (netty2 != null) {
      try {
        netty2.stop().join();
      } catch (final Exception e) {
        LOGGER.warn("Failed stopping netty2", e);
      }
    }
  }
}
