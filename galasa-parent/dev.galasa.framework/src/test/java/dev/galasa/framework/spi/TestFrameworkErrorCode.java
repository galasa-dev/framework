/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.spi;

import org.junit.Test;
import static org.assertj.core.api.Assertions.*;

public class TestFrameworkErrorCode {

    @Test
    public void testCanGetEnumerationFromIntValue() {
        assertThat(FrameworkErrorCode.findByErrorId(1)).isEqualTo(FrameworkErrorCode.INVALID_NAMESPACE);
    }

    @Test
    public void testUnknownIntValueTranslatesToUnknown() {
        assertThat(FrameworkErrorCode.findByErrorId(999)).isEqualTo(FrameworkErrorCode.UNKNOWN);
    }
}
