/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework;

import static org.assertj.core.api.Assertions.*;

import java.lang.annotation.Annotation;
import java.util.*;

import javax.validation.constraints.NotNull;
import org.apache.felix.bundlerepository.Repository;
import org.apache.felix.bundlerepository.Resolver;
import org.apache.felix.bundlerepository.Resource;
import org.junit.Test;
import org.osgi.framework.Bundle;
import org.osgi.framework.InvalidSyntaxException;
import dev.galasa.framework.maven.repository.spi.IMavenRepository;
import dev.galasa.framework.mocks.*;
import dev.galasa.framework.spi.*;
import dev.galasa.framework.spi.teststructure.TestStructure;

public class TestTestRunner {

    @Test 
    public void testCanCreateRunnerOK() throws Exception {
        new TestRunner();
    }

    @Test
    public void testCanRunTestRunnerOK() throws Exception {

        String TEST_STREAM_REPO_URL = "http://myhost/myRepositoryForMyRun";
        String TEST_BUNDLE_NAME = "myTestBundle";
        String TEST_CLASS_NAME = MyActualTestClass.class.getName();
        String TEST_RUN_NAME = "myTestRun";
        String TEST_STREAM = "myStreamForMyRun";
        String TEST_STREAM_OBR = "http://myhost/myObrForMyRun";
        String TEST_REQUESTOR_NAME = "daffyduck";
        boolean TEST_IS_LOCAL_RUN_TRUE = true;
        boolean IGNORE_TEST_CLASS_FALSE = false;

        Properties overrideProps = new Properties();
        MockIResultArchiveStore ras = new MockIResultArchiveStore("myRunId"); 

        MockIDynamicStatusStoreService dss = new MockIDynamicStatusStoreService() {
            @Override
            public Map<String, String> getPrefix(@NotNull String keyPrefix) throws DynamicStatusStoreException {
                return new HashMap<String,String>();
            }

            @Override
            public void put(@NotNull String key, @NotNull String value) throws DynamicStatusStoreException {
                // Do nothing.
            }
        };

        IRun run = new MockRun(
            TEST_BUNDLE_NAME, 
            TEST_CLASS_NAME , 
            TEST_RUN_NAME, 
            TEST_STREAM, 
            TEST_STREAM_OBR , 
            TEST_STREAM_REPO_URL,
            TEST_REQUESTOR_NAME,
            TEST_IS_LOCAL_RUN_TRUE
        );


        MockIFrameworkRuns frameworkRuns = new MockIFrameworkRuns( "myRunsGroup", List.of(run));

        MockShutableFramework framework = new MockShutableFramework(ras,dss,TEST_RUN_NAME, run, frameworkRuns );
        IConfigurationPropertyStoreService cps = new MockIConfigurationPropertyStoreService();



        IMavenRepository mockMavenRepo = new MockMavenRepository();

        Repository repo1 = new MockRepository(TEST_STREAM_REPO_URL);
        List<Repository> repositories = List.of(repo1);

        boolean IS_RESOLVER_GOING_TO_RESOLVE_TEST_BUNDLE = true;
        Resolver resolver = new MockResolver( IS_RESOLVER_GOING_TO_RESOLVE_TEST_BUNDLE );
        Resource mockResource = new MockResource(TEST_STREAM_OBR);

        MockRepositoryAdmin mockRepoAdmin = new MockRepositoryAdmin(repositories, resolver) {
            @Override
            public Resource[] discoverResources(String filterExpr) throws InvalidSyntaxException {
                Resource[] results = new Resource[1];
                results[0] = mockResource ;
                return results;
            }
        };

        MockBundleManager mockBundleManager = new MockBundleManager();

        Map<String,MockServiceReference<?>> servicesMap = new HashMap<>();

        Map<String,Class<?>> loadedClasses = Map.of( TEST_CLASS_NAME , MyActualTestClass.class );
        MockBundle myBundle1 = new MockBundle( loadedClasses , TEST_BUNDLE_NAME );
        List<Bundle> bundles = List.of(myBundle1);
        MockBundleContext mockBundleContext = new MockBundleContext(servicesMap, bundles);


        class MockAnnotationExtractor implements IAnnotationExtractor {

            Map<String, Annotation> annotationToReturnMap = new HashMap<>();

            public <A, B extends Annotation> void addAnnotation( Class<A> testClass, Class<B> annotationClass , B toReturn) {
                String key = testClass.getName()+"-"+annotationClass.getName();
                annotationToReturnMap.put( key, toReturn );
            }

            @SuppressWarnings("unchecked")
            @Override
            public <A, B extends Annotation> B getAnnotation(Class<A> testClass, Class<B> annotationClass) {
                String key = testClass.getName()+"-"+annotationClass.getName();
                // The following type-cast is unsafe, or would be were we not in full control of all the 
                // inputs and outputs in a unit test setting...
                return  (B) annotationToReturnMap.get(key);
            }
        }

        MockAnnotationExtractor mockAnnotationExtractor = new MockAnnotationExtractor();
        dev.galasa.Test annotationToReturn = MyActualTestClass.class.getAnnotation(dev.galasa.Test.class);
        assertThat(annotationToReturn).isNotNull();

        mockAnnotationExtractor.addAnnotation(
            MyActualTestClass.class, 
            dev.galasa.Test.class,
            annotationToReturn
        );

        TestRunner runner = new TestRunner();
        runner.mavenRepository = mockMavenRepo;
        runner.repositoryAdmin = mockRepoAdmin;

        // Inject the bundle context before we run the test.
        runner.activate(mockBundleContext);

        Result testResult = Result.passed();
        
        MockTestRunManagers mockTestRunManagers = new MockTestRunManagers(IGNORE_TEST_CLASS_FALSE, testResult );
        
        MockTestRunnerDataProvider testRunData = new MockTestRunnerDataProvider(
            cps,
            dss,
            ras,
            run,
            framework,
            overrideProps,
            mockAnnotationExtractor,
            mockBundleManager,
            mockTestRunManagers
        );

        // When...
        runner.runTest(testRunData);

        /// Then...
        List<TestStructure> rasHistory = ras.getTestStructureHistory();
        assertThat(rasHistory).hasSize(9).extracting("status").contains("finished","finished");

        assertThat(framework.isShutDown()).isTrue();

        assertThat(mockBundleManager.getLoadedBundleSymbolicNames()).hasSize(1);

        assertThat(mockTestRunManagers.calledCountEndOfTestRun).as("End of test run was not announced").isEqualTo(1);
        assertThat(dss.data).as("dss was not left in an empty state!").isEmpty();
        assertThat(mockTestRunManagers.calledCountShudown).as("Manager not shut down").isEqualTo(1);
        assertThat(mockTestRunManagers.calledCountProvisionGenerate).as("Manager not given a chance to provision").isEqualTo(1);
        assertThat(mockTestRunManagers.calledCountProvisionBuild).as("Manager not get a chance to build").isEqualTo(1);
        assertThat(mockTestRunManagers.calledCountProvisionStart).as("Manager not get a chance to start").isEqualTo(1);
        assertThat(mockTestRunManagers.calledCountProvisionDiscard).as("Managers not given an chance to discard the provisioning.").isEqualTo(1);
        assertThat(mockTestRunManagers.calledCountProvisionStop).isEqualTo(1);
        assertThat(mockTestRunManagers.calledCountStartOfTestClass).isEqualTo(1);
        assertThat(mockTestRunManagers.calledCountEndOfTestClass).isEqualTo(1);
        assertThat(mockTestRunManagers.calledCountEndOfTestRun).isEqualTo(1);
        assertThat(mockTestRunManagers.calledCountAnyReasonTestMethodShouldBeIgnored).isEqualTo(1);
    }
    
}
