/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package test.fpf;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.io.monitor.FileAlterationObserver;
import org.junit.Assert;
import org.junit.Test;

import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.DynamicStatusStoreException;
import dev.galasa.framework.spi.FrameworkPropertyFile;
import dev.galasa.framework.spi.FrameworkPropertyFileException;
import dev.galasa.framework.spi.IFrameworkPropertyFileWatcher;
import dev.galasa.framework.spi.ResultArchiveStoreException;

/**
 * <p>
 * This test class checks that exceptions are behavoing as expected.
 * </p>
 * 
 * @author James Davies
 */
public class ExceptionsTest {

    /**
     * <p>
     * This test class checks the exception class has no problems using any of the
     * exception methods.
     * </p>
     */
    @Test
    public void testDynamicStatusStoreException() {
        Throwable throwable = new DynamicStatusStoreException();
        new DynamicStatusStoreException("Message");
        new DynamicStatusStoreException("Message", throwable);
        new DynamicStatusStoreException(throwable);
        new DynamicStatusStoreException("Message", throwable, false, false);
        Assert.assertTrue("dummy", true);
    }

    /**
     * <p>
     * This test class checks the exception class has no problems using any of the
     * exception methods.
     * </p>
     */
    @Test
    public void testResultArchiveStoreException() {
        Throwable throwable = new ResultArchiveStoreException();
        new ResultArchiveStoreException("Message");
        new ResultArchiveStoreException("Message", throwable);
        new ResultArchiveStoreException(throwable);
        new ResultArchiveStoreException("Message", throwable, false, false);
        Assert.assertTrue("dummy", true);
    }

    /**
     * <p>
     * This test class checks the exception class has no problems using any of the
     * exception methods.
     * </p>
     */
    @Test
    public void testFrameworkPropertyFileException() {
        Throwable throwable = new FrameworkPropertyFileException();
        new FrameworkPropertyFileException("Message");
        new FrameworkPropertyFileException("Message", throwable);
        new FrameworkPropertyFileException(throwable);
        new FrameworkPropertyFileException("Message", throwable, false, false);
        Assert.assertTrue("dummy", true);
    }

    /**
     * <p>
     * This test class checks the exception class has no problems using any of the
     * exception methods.
     * </p>
     */
    @Test
    public void testConfigurationPropertyStoreException() {
        Throwable throwable = new ConfigurationPropertyStoreException();
        new ConfigurationPropertyStoreException("Message");
        new ConfigurationPropertyStoreException("Message", throwable);
        new ConfigurationPropertyStoreException(throwable);
        new ConfigurationPropertyStoreException("Message", throwable, false, false);
        Assert.assertTrue("dummy", true);
    }

    /**
     * <p>
     * This method tests that an exception is thrown when no prooperties file
     * exissts
     * </p>
     */
    @Test
    public void testExceptionOfNOfileExists() {
        boolean caught = false;
        File nOfile = new File("/tmp/nope");
        try {
            FrameworkPropertyFile fpf = new FrameworkPropertyFile(nOfile.toURI());
        } catch (FrameworkPropertyFileException e) {
            caught = true;
        }
        assertTrue("Did not throw expected exception", caught);
    }

    /**
     * <p>
     * This method tests the exception when a set to a file fails.
     * </p>
     */
    @Test
    public void testExceptionOfFailedSet() throws IOException {
        boolean caught = false;
        File file = File.createTempFile("toBeDeleted", ".properties");
        try {
            FrameworkPropertyFile fpf = new FrameworkPropertyFile(file.toURI());
            file.delete();
            fpf.set("No", "BiggerNo");
        } catch (FrameworkPropertyFileException e) {
            caught = true;
        }
        assertTrue("Did not throw expected exception", caught);
    }

    /**
     * <p>
     * This method tests the exception is thrown when setting a map of values fails.
     * </p>
     */
    @Test
    public void testExceptionOfFailedSetMap() throws IOException {
        boolean caught = false;
        File file = File.createTempFile("toBeDeleted", ".properties");

        Map<String, String> map = new HashMap<String, String>();
        map.put("key", "value");
        map.put("key2", "value");

        try {
            FrameworkPropertyFile fpf = new FrameworkPropertyFile(file.toURI());
            file.delete();
            fpf.set(map);
        } catch (FrameworkPropertyFileException e) {
            caught = true;
        }
        assertTrue("Did not throw expected exception", caught);
    }

    /**
     * <p>
     * This method tests the exception is thrown when the delete of a k-v pair fails
     * </p>
     */
    @Test
    public void testExceptionOffaileddelete() throws IOException {
        boolean caught = false;
        File file = File.createTempFile("toBeDeleted", ".properties");
        try {
            FrameworkPropertyFile fpf = new FrameworkPropertyFile(file.toURI());
            file.delete();
            fpf.destroy();
            fpf.delete("Test1");
        } catch (FrameworkPropertyFileException e) {
            caught = true;
        }
        assertTrue("Did not throw expected exception", caught);
    }

    /**
     * <p>
     * This method tests the exception is thrown when deleting a set of values
     * fails.
     * </p>
     */
    @Test
    public void testExceptionOfFailedDeleteSet() throws IOException {
        boolean caught = false;
        File file = File.createTempFile("toBeDeleted", ".properties");

        Set<String> set = new HashSet<String>();
        set.add("One");
        set.add("two");
        ;
        try {
            FrameworkPropertyFile fpf = new FrameworkPropertyFile(file.toURI());
            file.delete();
            fpf.destroy();
            fpf.delete(set);
        } catch (FrameworkPropertyFileException e) {
            caught = true;
        }
        assertTrue("Did not throw expected exception", caught);
    }

    /**
     * <p>
     * This method tests the exception is thrown when unwatching a value fails
     * </p>
     */
    @Test
    public void testExceptionOfFailedUnwatch() throws IOException {
        boolean caught = false;
        File file = File.createTempFile("toBeDeleted", ".properties");
        try {
            FrameworkPropertyFile fpf = new FrameworkPropertyFile(file.toURI());
            UUID uuid = fpf.watch(new Watcher(), "Test1");
            file.delete();
            fpf.destroy();
            fpf.unwatch(uuid);
        } catch (FrameworkPropertyFileException e) {
            caught = true;
        }
        assertTrue("Did not throw expected exception", caught);
    }

    /**
     * <p>
     * This method tests the exception is thrown when an atomic set of a value
     * failes
     * </p>
     */
    @Test
    public void testExceptionOfFailedAtomicSet() throws IOException {
        boolean caught = false;
        File file = File.createTempFile("toBeDeleted", ".properties");
        try {
            FrameworkPropertyFile fpf = new FrameworkPropertyFile(file.toURI());
            file.delete();
            fpf.destroy();
            fpf.setAtomic("Test1", "ItDoesntMatter", "Nothing else matters");
        } catch (FrameworkPropertyFileException e) {
            caught = true;
        }
        assertTrue("Did not throw expected exception", caught);
    }

    /**
     * <p>
     * This method tests the exception is thrown when an atomic set of a value and
     * map of other values fails.
     * </p>
     */
    @Test
    public void testExceptionOfFailedAtomicSetMap() throws IOException {
        boolean caught = false;
        File file = File.createTempFile("toBeDeleted", ".properties");

        Map<String, String> map = new HashMap<String, String>();
        map.put("key", "value");
        map.put("key2", "value");

        try {
            FrameworkPropertyFile fpf = new FrameworkPropertyFile(file.toURI());
            file.delete();
            fpf.destroy();
            fpf.setAtomic("Test1", "ItDoesntMatter", "Nothing else matters", map);

        } catch (FrameworkPropertyFileException e) {
            caught = true;
        }
        assertTrue("Did not throw expected exception", caught);
    }

    /**
     * <p>
     * This method tests the exception is not thrown when other implemented methods
     * are used.
     * </p>
     */
    @Test
    public void testNonUsedImpletmentedMethods() throws FrameworkPropertyFileException, IOException {
        File file = File.createTempFile("random", ".properties");
        FileAlterationObserver observer = null;
        FrameworkPropertyFile fpf = new FrameworkPropertyFile(file.toURI());
        file.delete();
        // Would throw excpetions if using default methods.
        try {
            fpf.onFileCreate(file);
            fpf.onFileDelete(file);
            fpf.onDirectoryChange(file);
            fpf.onDirectoryCreate(file);
            fpf.onDirectoryDelete(file);
            fpf.onStart(observer);
            fpf.onStop(observer);
        } catch (Exception e) {
            assertTrue("Non used methods have been implemented", false);
        }
        assertTrue("Unused methods responded as epxpected", true);
    }

    /**
     * <p>
     * This class is to test the impletmentation of propertyModifed method called by
     * the FPF object.
     * </p>
     */
    private static class Watcher implements IFrameworkPropertyFileWatcher {

        private Event  event;
        private String key;
        private String newValue;
        private String oldValue;

        /**
         * <p>
         * This method is used in the testing of watches in the FPF object.
         * </p>
         */
        @Override
        public void propertyModified(String key, Event event, String oldValue, String newValue) {
            this.event = event;
            this.key = key;
            this.newValue = newValue;
            this.oldValue = oldValue;
        }
    }
}
