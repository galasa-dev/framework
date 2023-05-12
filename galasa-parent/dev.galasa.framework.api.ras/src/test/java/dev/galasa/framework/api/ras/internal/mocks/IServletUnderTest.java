/*
 * Copyright contributors to the Galasa project 
 */
package dev.galasa.framework.api.ras.internal.mocks;

import dev.galasa.framework.IFileSystem;
import dev.galasa.framework.spi.IFramework;

public interface IServletUnderTest {
    void setFramework(IFramework framework);

    void setFileSystem(IFileSystem fileSystem);
}