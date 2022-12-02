/*
 * Copyright contributors to the Galasa project
 */
package dev.galasa.framework.spi;

import org.junit.Test;
import static org.assertj.core.api.Assertions.*;

public class TestFrameworkException {

    @Test
    public void testRaisingWithNoCodeResultsInUnknownCodeStored() {
        FrameworkException ex = new FrameworkException();
        assertThat(ex.getErrorCode()).isEqualTo(FrameworkErrorCode.UNKNOWN);
    }

    @Test
    public void testRaisingWithErrorCodeCausesCodeToBeStored() {
        FrameworkException ex = new FrameworkException(FrameworkErrorCode.INVALID_NAMESPACE);
        assertThat(ex.getErrorCode()).isEqualTo(FrameworkErrorCode.INVALID_NAMESPACE);
    }

    @Test
    public void testRaisingWithRandomExceptionCausesUnknownCodeToBeStored() {
        Exception cause = new Exception("random exception for unit tests");
        FrameworkException ex = new FrameworkException(cause);
        assertThat(ex.getErrorCode()).isEqualTo(FrameworkErrorCode.UNKNOWN);
    }

    @Test
    public void testRaisingWithFrameworkExceptionCausesCodeToBeTransferred() {
        Exception cause = new FrameworkException(FrameworkErrorCode.INVALID_NAMESPACE,
                "random exception for unit tests");
        FrameworkException ex = new FrameworkException(cause);
        assertThat(ex.getErrorCode()).isEqualTo(FrameworkErrorCode.INVALID_NAMESPACE);
    }

    @Test
    public void testRaisingWithMessageAndRandomExceptionCausesUnknownToBeTransferred() {
        Exception cause = new Exception("random exception for unit tests");
        FrameworkException ex = new FrameworkException("exception for unit testing",cause);
        assertThat(ex.getErrorCode()).isEqualTo(FrameworkErrorCode.UNKNOWN);
    }

    @Test
    public void testRaisingWithMessageAndFrameworkExceptionCausesCodeToBeTransferred() {
        Exception cause = new FrameworkException(FrameworkErrorCode.INVALID_NAMESPACE,
                "random exception for unit tests");
        FrameworkException ex = new FrameworkException("exception for unit testing",cause);
        assertThat(ex.getErrorCode()).isEqualTo(FrameworkErrorCode.INVALID_NAMESPACE);
    }

    @Test
    public void testRaisingWithLoadsOfParametersAndRandomExceptionCausesUnknownToBeStored() {
        Exception cause = new Exception("random exception for unit tests");
        FrameworkException ex = new FrameworkException("exception for unit testing",cause,true,true);
        assertThat(ex.getErrorCode()).isEqualTo(FrameworkErrorCode.UNKNOWN);
    }

    @Test
    public void testRaisingWithLoadsOfParametersAndFrameworkExceptionCausesCodeToBeTransferred() {
        Exception cause = new FrameworkException(FrameworkErrorCode.INVALID_NAMESPACE,
                "random exception for unit tests");
        FrameworkException ex = new FrameworkException("exception for unit testing",cause,true,true);
        assertThat(ex.getErrorCode()).isEqualTo(FrameworkErrorCode.INVALID_NAMESPACE);
    }
}
