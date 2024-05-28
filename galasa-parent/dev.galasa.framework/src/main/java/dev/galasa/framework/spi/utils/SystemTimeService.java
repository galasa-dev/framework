/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.spi.utils;

import java.time.Instant;

public class SystemTimeService implements ITimeService {

    @Override
    public Instant now() {
        return Instant.now();
    }
}
