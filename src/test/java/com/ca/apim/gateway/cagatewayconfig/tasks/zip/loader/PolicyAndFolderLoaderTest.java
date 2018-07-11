package com.ca.apim.gateway.cagatewayconfig.tasks.zip.loader;

import com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans.Bundle;
import com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans.Folder;
import com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans.Policy;
import com.ca.apim.gateway.cagatewayconfig.util.IdGenerator;
import com.ca.apim.gateway.cagatewayconfig.util.file.FileUtils;
import io.github.glytching.junit.extension.folder.TemporaryFolder;
import io.github.glytching.junit.extension.folder.TemporaryFolderExtension;
import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.testcontainers.shaded.com.google.common.io.Files;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class PolicyAndFolderLoaderTest {

    @Mock
    private FileUtils fileUtils;

    @Test
    void getPolicyAsStringTest() {

        File root = new File("a");
        File a = new File(root, "a");
        File b = new File(a, "b");
        File c = new File(b, "c");
        File policy = new File(c, "policy.xml");

        PolicyAndFolderLoader policyAndFolderLoader = new PolicyAndFolderLoader(FileUtils.INSTANCE, new IdGenerator());

        String path = policyAndFolderLoader.getPath(policy, root);

        Assert.assertEquals("a/b/c/policy.xml", path);
    }

    @Test
    void getPolicyNameTest() {
        PolicyAndFolderLoader policyAndFolderLoader = new PolicyAndFolderLoader(FileUtils.INSTANCE, new IdGenerator());

        File policy = new File("policy.xml");
        Assert.assertEquals("policy", policyAndFolderLoader.getPolicyName(policy));

        policy = new File("policy.xml");
        Assert.assertEquals("policy", policyAndFolderLoader.getPolicyName(policy));

        policy = new File("my.policy.xml");
        Assert.assertEquals("my.policy", policyAndFolderLoader.getPolicyName(policy));

        policy = new File("something");
        Assert.assertEquals("something", policyAndFolderLoader.getPolicyName(policy));

    }

    @Test
    @ExtendWith(TemporaryFolderExtension.class)
    void testLoad(TemporaryFolder temporaryFolder) throws IOException {
        PolicyAndFolderLoader policyAndFolderLoader = new PolicyAndFolderLoader(fileUtils, new IdGenerator());
        Mockito.when(fileUtils.getFileAsString(Mockito.any(File.class))).thenReturn("policy-content");

        File policyFolder = temporaryFolder.createDirectory("policy");
        File a = new File(policyFolder, "a");
        Assert.assertTrue(a.mkdir());
        File b = new File(a, "b");
        Assert.assertTrue(b.mkdir());
        File c = new File(b, "c");
        Assert.assertTrue(c.mkdir());
        File policy = new File(c, "policy.xml");
        Files.touch(policy);

        Bundle bundle = new Bundle();
        policyAndFolderLoader.load(bundle, temporaryFolder.getRoot());

        Assert.assertNotNull(bundle.getFolders().get(""));
        Assert.assertNotNull(bundle.getFolders().get("a/"));
        Assert.assertNotNull(bundle.getFolders().get("a/b/"));
        Folder parentFolder = bundle.getFolders().get("a/b/c/");
        Assert.assertNotNull(parentFolder);

        Policy loadedPolicy = bundle.getPolicies().get("a/b/c/policy.xml");
        Assert.assertNotNull(loadedPolicy);
        Assert.assertEquals(parentFolder, loadedPolicy.getParentFolder());
    }


    @Test
    @ExtendWith(TemporaryFolderExtension.class)
    void testLoadNoPolicyFolder(TemporaryFolder temporaryFolder) {
        PolicyAndFolderLoader policyAndFolderLoader = new PolicyAndFolderLoader(FileUtils.INSTANCE, new IdGenerator());

        Bundle bundle = new Bundle();
        policyAndFolderLoader.load(bundle, temporaryFolder.getRoot());

        Assert.assertTrue(bundle.getFolders().isEmpty());
    }

    @Test
    @ExtendWith(TemporaryFolderExtension.class)
    void testLoadPolicyFolderIsFile(TemporaryFolder temporaryFolder) throws IOException {
        PolicyAndFolderLoader policyAndFolderLoader = new PolicyAndFolderLoader(FileUtils.INSTANCE, new IdGenerator());

        temporaryFolder.createFile("policy");

        Bundle bundle = new Bundle();

        assertThrows(BundleLoadException.class, () -> policyAndFolderLoader.load(bundle, temporaryFolder.getRoot()));
    }
}