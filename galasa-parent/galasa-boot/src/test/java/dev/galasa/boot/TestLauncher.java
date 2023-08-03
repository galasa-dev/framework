/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.boot;

import static org.assertj.core.api.Assertions.*;

import dev.galasa.boot.Launcher;
import dev.galasa.boot.mocks.MockEnvironment;
import org.junit.Test;
import java.util.Properties;

public class TestLauncher {

	@Test
    public void testLauncher() throws Exception{
        Launcher l = new Launcher();

        MockEnvironment mockEnv = new MockEnvironment();
        mockEnv.setProperty("GALASA_HOME","/Users/hobbit/galasa_home_dir/");

        String home = l.getGalasaHome(mockEnv);

        assertThat(home).isEqualTo("/Users/hobbit/galasa_home_dir/");
        


    }

    @Test
    public void checkEnvironmentVariablesAreHandled() {
        Launcher l  = new Launcher();
        MockEnvironment me = new MockEnvironment();
        Properties bootstrap = new Properties();

        bootstrap.setProperty("framework.config.store","/this/should/be/ignored.properties");
        me.setProperty("GALASA_CONFIG_STORE","/Users/hobbit/galasa_home/cps.properties");

        bootstrap.setProperty("framework.dynamicstatus.store","/this/should/be/ignored.properties");
        me.setProperty("GALASA_DYNAMICSTATUS_STORE","/Users/hobbit/galasa_home/dss.properties");

        bootstrap.setProperty("framework.resultarchive.store","/this/should/be/ignored/ras");
        me.setProperty("GALASA_RESULTARCHIVE_STORE","/Users/hobbit/galasa_home/ras");

        bootstrap.setProperty("framework.credentials.store","/this/should/be/ignored.properties");
        me.setProperty("GALASA_CREDENTIALS_STORE","/Users/hobbit/galasa_home/creds.properties");

        l.setStoresFromEnvironmentVariables(me,bootstrap);

        assertThat(bootstrap.getProperty("framework.config.store")).isEqualTo("/Users/hobbit/galasa_home/cps.properties");
        assertThat(bootstrap.getProperty("framework.dynamicstatus.store")).isEqualTo("/Users/hobbit/galasa_home/dss.properties");
        assertThat(bootstrap.getProperty("framework.resultarchive.store")).isEqualTo("/Users/hobbit/galasa_home/ras");
        assertThat(bootstrap.getProperty("framework.credentials.store")).isEqualTo("/Users/hobbit/galasa_home/creds.properties");
    }

    @Test
    public void checkNullBootstrap() {
        Launcher l  = new Launcher();
        MockEnvironment me = new MockEnvironment();
        Properties bootstrap = new Properties();

        me.setProperty("GALASA_CONFIG_STORE","/Users/hobbit/galasa_home/cps.properties");

        me.setProperty("GALASA_DYNAMICSTATUS_STORE","/Users/hobbit/galasa_home/dss.properties");

        me.setProperty("GALASA_RESULTARCHIVE_STORE","/Users/hobbit/galasa_home/ras");

        me.setProperty("GALASA_CREDENTIALS_STORE","/Users/hobbit/galasa_home/creds.properties");

        l.setStoresFromEnvironmentVariables(me,bootstrap);

        assertThat(bootstrap.getProperty("framework.config.store")).isEqualTo("/Users/hobbit/galasa_home/cps.properties");
        assertThat(bootstrap.getProperty("framework.dynamicstatus.store")).isEqualTo("/Users/hobbit/galasa_home/dss.properties");
        assertThat(bootstrap.getProperty("framework.resultarchive.store")).isEqualTo("/Users/hobbit/galasa_home/ras");
        assertThat(bootstrap.getProperty("framework.credentials.store")).isEqualTo("/Users/hobbit/galasa_home/creds.properties");
    }

    @Test
    public void checkBootstrapIsDefault() {
        Launcher l  = new Launcher();
        MockEnvironment me = new MockEnvironment();
        Properties bootstrap = new Properties();

        bootstrap.setProperty("framework.config.store","/this/should/be/ignored.properties");

        bootstrap.setProperty("framework.dynamicstatus.store","/this/should/be/ignored.properties");

        bootstrap.setProperty("framework.resultarchive.store","/this/should/be/ignored/ras");

        bootstrap.setProperty("framework.credentials.store","/this/should/be/ignored.properties");

        l.setStoresFromEnvironmentVariables(me,bootstrap);

        assertThat(bootstrap.getProperty("framework.config.store")).isEqualTo("/this/should/be/ignored.properties");
        assertThat(bootstrap.getProperty("framework.dynamicstatus.store")).isEqualTo("/this/should/be/ignored.properties");
        assertThat(bootstrap.getProperty("framework.resultarchive.store")).isEqualTo("/this/should/be/ignored/ras");
        assertThat(bootstrap.getProperty("framework.credentials.store")).isEqualTo("/this/should/be/ignored.properties");
    }

    @Test
    public void checkEnvironmentVariablesAreTrimmed() {
        Launcher l  = new Launcher();
        MockEnvironment me = new MockEnvironment();
        Properties bootstrap = new Properties();

        bootstrap.setProperty("framework.config.store","/this/should/be/ignored.properties");
        me.setProperty("GALASA_CONFIG_STORE","/Users/hobbit/galasa_home/cps.properties  ");

        bootstrap.setProperty("framework.dynamicstatus.store","/this/should/be/ignored.properties");
        me.setProperty("GALASA_DYNAMICSTATUS_STORE","  /Users/hobbit/galasa_home/dss.properties");

        bootstrap.setProperty("framework.resultarchive.store","/this/should/be/ignored/ras");
        me.setProperty("GALASA_RESULTARCHIVE_STORE"," /Users/hobbit/galasa_home/ras  ");

        bootstrap.setProperty("framework.credentials.store","/this/should/be/ignored.properties");
        me.setProperty("GALASA_CREDENTIALS_STORE","/Users/hobbit/galasa_home/creds.properties");

        l.setStoresFromEnvironmentVariables(me,bootstrap);

        assertThat(bootstrap.getProperty("framework.config.store")).isEqualTo("/Users/hobbit/galasa_home/cps.properties");
        assertThat(bootstrap.getProperty("framework.dynamicstatus.store")).isEqualTo("/Users/hobbit/galasa_home/dss.properties");
        assertThat(bootstrap.getProperty("framework.resultarchive.store")).isEqualTo("/Users/hobbit/galasa_home/ras");
        assertThat(bootstrap.getProperty("framework.credentials.store")).isEqualTo("/Users/hobbit/galasa_home/creds.properties");
    }

    @Test
    public void testJava8Fails() {
        Launcher l  = new Launcher();
        MockEnvironment me = new MockEnvironment();

        me.setProperty("java.version","8");

        try{
            l.validateJavaLevel(me);
        }catch(LauncherException le){
            return;
        }
        fail("LauncherException should have been thrown");
    }

    @Test
    public void testJava11Passes() {
        Launcher l  = new Launcher();
        MockEnvironment me = new MockEnvironment();

        me.setProperty("java.version","11");

        try{
            l.validateJavaLevel(me);
        }catch(LauncherException le){
            fail("LauncherException thrown");
        }
    }

    @Test
    public void testJava16Fails() {
        Launcher l  = new Launcher();
        MockEnvironment me = new MockEnvironment();

        me.setProperty("java.version","16");

        try{
            l.validateJavaLevel(me);
        }catch(LauncherException le){
            return;
        }
        fail("LauncherException should have been thrown");
    }

    @Test
    public void testCurrentJavaWorks() {
        Launcher l  = new Launcher();
        MockEnvironment me = new MockEnvironment();

        me.setProperty("java.version",System.getProperty("java.version"));

        try{
            l.validateJavaLevel(me);
        }catch(LauncherException le){
            fail("LauncherException thrown");
        }
    }

    @Test
    public void testWhenNoJavaFails() {
        Launcher l  = new Launcher();
        MockEnvironment me = new MockEnvironment();
        try{
            l.validateJavaLevel(me);
        }catch(LauncherException le){
            return;
        }
        fail("LauncherException should have been thrown");
    }
}
