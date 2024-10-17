/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.k8s.controller;

import static org.assertj.core.api.Assertions.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import dev.galasa.framework.mocks.MockEnvironment;
import dev.galasa.framework.mocks.MockIDynamicStatusStoreService;
import dev.galasa.framework.mocks.MockIFrameworkRuns;
import dev.galasa.framework.spi.creds.FrameworkEncryptionService;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.models.V1ConfigMap;
import io.kubernetes.client.openapi.models.V1Container;
import io.kubernetes.client.openapi.models.V1ObjectMeta;
import io.kubernetes.client.openapi.models.V1Pod;
import io.kubernetes.client.openapi.models.V1PodSpec;
import io.kubernetes.client.openapi.models.V1Volume;
import io.kubernetes.client.openapi.models.V1VolumeMount;

public class TestPodSchedulerTest {

    class MockSettings extends Settings {

        private V1ConfigMap mockConfigMap;

        public MockSettings(V1ConfigMap configMap, K8sController controller, CoreV1Api api) throws K8sControllerException {
            super(controller, api);
            this.mockConfigMap = configMap;
        }

        @Override
        V1ConfigMap retrieveConfigMap() {
            return mockConfigMap;
        }
    }

    class MockK8sController extends K8sController {
        @Override
        public void pollUpdated() {
            // Do nothing...
        }
    }
    
    private V1ConfigMap createMockConfigMap() {
        V1ConfigMap configMap = new V1ConfigMap();

        V1ObjectMeta metadata = new V1ObjectMeta().resourceVersion("mockVersion");
        configMap.setMetadata(metadata);

        Map<String, String> data = new HashMap<>();
        data.put("bootstrap", "http://my.server/bootstrap");
        data.put("max_engines", "10");
        data.put("engine_label", "my-test-engine");
        data.put("node_arch", "arch");
        data.put("run_poll", "5");
        data.put("encryption_keys_secret_name", "service-encryption-keys-secret");
        
        configMap.setData(data);

        return configMap;
    }

    private void assertPodDetailsAreCorrect(
        V1Pod pod,
        String expectedRunName,
        String expectedPodName,
        String expectedEncryptionKeysMountPath,
        Settings settings
    ) {
        checkPodMetadata(pod, expectedRunName, expectedPodName, settings);
        checkPodContainer(pod, expectedEncryptionKeysMountPath, settings);
        checkPodVolumes(pod, settings);
    }

    @SuppressWarnings("null")
    private void checkPodMetadata(V1Pod pod, String expectedRunName, String expectedPodName, Settings settings) {
        V1ObjectMeta expectedMetadata = new V1ObjectMeta()
            .labels(Map.of("galasa-run", expectedRunName, "galasa-engine-controller", settings.getEngineLabel()))
            .name(expectedPodName);
        
        // Check the pod's metadata is as expected
        assertThat(pod).isNotNull();
        assertThat(pod.getApiVersion()).isEqualTo("v1");
        assertThat(pod.getKind()).isEqualTo("Pod");

        V1ObjectMeta actualMetadata = pod.getMetadata();
        assertThat(actualMetadata.getLabels()).containsExactlyInAnyOrderEntriesOf(expectedMetadata.getLabels());
        assertThat(actualMetadata.getName()).isEqualTo(expectedPodName);
    }

    @SuppressWarnings("null")
    private void checkPodContainer(V1Pod pod, String expectedEncryptionKeysMountPath, Settings settings) {
        // Check that test container has been added
        V1PodSpec actualPodSpec = pod.getSpec();
        List<V1Container> actualContainers = actualPodSpec.getContainers();
        assertThat(actualContainers).hasSize(1);

        V1Container testContainer = actualContainers.get(0);
        assertThat(testContainer.getCommand()).containsExactly("java");
        assertThat(testContainer.getArgs()).contains("-jar", "boot.jar", "--run", settings.getBootstrap());

        // Check that the encryption keys have been mounted to the correct location
        List<V1VolumeMount> testContainerVolumeMounts = testContainer.getVolumeMounts();
        assertThat(testContainerVolumeMounts).hasSize(1);

        V1VolumeMount encryptionKeysVolumeMount = testContainerVolumeMounts.get(0);
        assertThat(encryptionKeysVolumeMount.getName()).isEqualTo(TestPodScheduler.ENCRYPTION_KEYS_VOLUME_NAME);
        assertThat(encryptionKeysVolumeMount.getMountPath()).isEqualTo(expectedEncryptionKeysMountPath);
        assertThat(encryptionKeysVolumeMount.getReadOnly()).isTrue();
    }

    @SuppressWarnings("null")
    private void checkPodVolumes(V1Pod pod, Settings settings) {
        // Check that the encryption keys volume has been added
        V1PodSpec actualPodSpec = pod.getSpec();
        List<V1Volume> actualVolumes = actualPodSpec.getVolumes();
        assertThat(actualVolumes).hasSize(1);

        V1Volume encryptionKeysVolume = actualVolumes.get(0);
        assertThat(encryptionKeysVolume.getName()).isEqualTo(TestPodScheduler.ENCRYPTION_KEYS_VOLUME_NAME);
        assertThat(encryptionKeysVolume.getSecret().getSecretName()).isEqualTo(settings.getEncryptionKeysSecretName());
    }

    @Test
    public void testCanCreateTestPodOk() throws Exception {
        // Given...
        MockEnvironment mockEnvironment = new MockEnvironment();

        String encryptionKeysMountPath = "/encryption/encryption-keys.yaml";
        mockEnvironment.setenv(FrameworkEncryptionService.ENCRYPTION_KEYS_PATH_ENV, encryptionKeysMountPath);

        MockK8sController controller = new MockK8sController();
        MockIDynamicStatusStoreService mockDss = new MockIDynamicStatusStoreService();
        MockIFrameworkRuns mockFrameworkRuns = new MockIFrameworkRuns(new ArrayList<>());

        V1ConfigMap mockConfigMap = createMockConfigMap();
        MockSettings settings = new MockSettings(mockConfigMap, controller, null);
        settings.init();
        
        TestPodScheduler runPoll = new TestPodScheduler(mockEnvironment, mockDss, settings, null, mockFrameworkRuns);

        String runName = "run1";
        String podName = settings.getEngineLabel() + "-" + runName;
        boolean isTraceEnabled = false;

        // When...
        V1Pod pod = runPoll.createTestPod(runName, podName, isTraceEnabled);

        // Then...
        String expectedEncryptionKeysMountPath = "/encryption";
        assertPodDetailsAreCorrect(pod, runName, podName, expectedEncryptionKeysMountPath, settings);
    }
}
