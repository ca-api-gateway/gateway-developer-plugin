/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayexport.tasks.explode.writer;

import com.ca.apim.gateway.cagatewayconfig.beans.*;
import com.ca.apim.gateway.cagatewayconfig.config.loader.policy.AssertionJSPolicyConverter;
import com.ca.apim.gateway.cagatewayconfig.config.loader.policy.PolicyConverterRegistry;
import com.ca.apim.gateway.cagatewayconfig.config.loader.policy.XMLPolicyConverter;
import com.ca.apim.gateway.cagatewayconfig.util.entity.EntityTypes;
import com.ca.apim.gateway.cagatewayconfig.util.file.DocumentFileUtils;
import com.ca.apim.gateway.cagatewayconfig.util.file.JsonFileUtils;
import com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentParseException;
import com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentTools;
import com.ca.apim.gateway.cagatewayexport.tasks.explode.linker.EntityLinkerRegistry;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.MapType;
import com.google.common.collect.ImmutableSet;
import io.github.glytching.junit.extension.folder.TemporaryFolder;
import io.github.glytching.junit.extension.folder.TemporaryFolderExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import com.ca.apim.gateway.cagatewayconfig.util.json.JsonTools;

import java.io.File;
import java.io.IOException;
import java.util.*;

import org.w3c.dom.Document;

import static com.ca.apim.gateway.cagatewayconfig.beans.Folder.ROOT_FOLDER;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(TemporaryFolderExtension.class)
class PolicyWriterTest {
    private PolicyConverterRegistry policyConverterRegistry = new PolicyConverterRegistry(ImmutableSet.of(new AssertionJSPolicyConverter(), new XMLPolicyConverter(DocumentTools.INSTANCE)));

    @Test
    void testNoPolicies(final TemporaryFolder temporaryFolder) {
        PolicyWriter writer = new PolicyWriter(policyConverterRegistry, DocumentFileUtils.INSTANCE, JsonFileUtils.INSTANCE, new EntityLinkerRegistry(new HashSet<>()));

        Bundle bundle = new Bundle();
        bundle.addEntity(ROOT_FOLDER);
        bundle.setFolderTree(new FolderTree(bundle.getEntities(Folder.class).values()));

        writer.write(bundle, temporaryFolder.getRoot(), bundle);

        File policyFolder = new File(temporaryFolder.getRoot(), "policy");
        assertTrue(policyFolder.exists());

        assertEquals(0, policyFolder.listFiles().length);
    }

    @Test
    void testWriteAssertionJS(final TemporaryFolder temporaryFolder) throws DocumentParseException {
        PolicyWriter writer = new PolicyWriter(policyConverterRegistry, DocumentFileUtils.INSTANCE, JsonFileUtils.INSTANCE, new EntityLinkerRegistry(new HashSet<>()));

        Bundle bundle = new Bundle();
        bundle.addEntity(ROOT_FOLDER);
        bundle.setFolderTree(new FolderTree(bundle.getEntities(Folder.class).values()));
        Policy policy = new Policy();
        policy.setGuid("123");
        policy.setPath("assertionPolicy");
        policy.setParentFolder(ROOT_FOLDER);
        policy.setName("assertionPolicy");
        policy.setId("asd");
        policy.setPolicyXML("<wsp:Policy xmlns:wsp=\"http://schemas.xmlsoap.org/ws/2002/12/policy\" xmlns:L7p=\"http://www.layer7tech.com/ws/policy\">\n" +
                "    <wsp:All wsp:Usage=\"Required\"><L7p:JavaScript>\n" +
                "            <L7p:ExecutionTimeout stringValue=\"\"/>\n" +
                "            <L7p:Name stringValue=\"assertionPolicy\"/>\n" +
                "            <L7p:Script stringValueReference=\"inline\"><![CDATA[var js = {};]]></L7p:Script>\n" +
                "        </L7p:JavaScript></wsp:All>\n" +
                "</wsp:Policy>");
        policy.setPolicyDocument(DocumentTools.INSTANCE.parse(policy.getPolicyXML()).getDocumentElement());
        bundle.getPolicies().put("assertionPolicy", policy);

        writer.write(bundle, temporaryFolder.getRoot(), bundle);

        File policyFolder = new File(temporaryFolder.getRoot(), "policy");
        assertTrue(policyFolder.exists());

        File policyFile = new File(policyFolder, "assertionPolicy.assertion.js");
        assertTrue(policyFile.exists());

        File configFolder = new File(temporaryFolder.getRoot(), "config");
        assertTrue(configFolder.exists());
        File policyMetadataFile = new File(configFolder, "policies.yml");
        assertTrue(policyMetadataFile.exists());

        final Map<String, PolicyMetadata> policyMetadataMap =
                JsonFileUtils.INSTANCE.readPoliciesConfigFile(temporaryFolder.getRoot(), PolicyMetadata.class);
        assertEquals(1, policyMetadataMap.size());
        assertFalse(policyMetadataMap.get("assertionPolicy").isHasRouting());
    }

    @Test
    void testWriteRoutingAssertion(final TemporaryFolder temporaryFolder) throws DocumentParseException {
        PolicyWriter writer = new PolicyWriter(policyConverterRegistry, DocumentFileUtils.INSTANCE, JsonFileUtils.INSTANCE, new EntityLinkerRegistry(new HashSet<>()));

        Bundle bundle = new Bundle();
        bundle.addEntity(ROOT_FOLDER);
        bundle.setFolderTree(new FolderTree(bundle.getEntities(Folder.class).values()));
        Policy policy = new Policy();
        policy.setGuid("123");
        policy.setPath("assertionPolicy");
        policy.setParentFolder(ROOT_FOLDER);
        policy.setName("assertionPolicy");
        policy.setId("asd");
        policy.setHasRouting(true);
        policy.setPolicyXML("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<wsp:Policy xmlns:L7p=\"http://www.layer7tech.com/ws/policy\" \n" +
                "    xmlns:wsp=\"http://schemas.xmlsoap.org/ws/2002/12/policy\">\n" +
                "    <wsp:All wsp:Usage=\"Required\">\n" +
                "        <L7p:Http2Routing>\n" +
                "            <L7p:Http2ClientConfigName stringValue=\"&lt;Default Config>\"/>\n" +
                "            <L7p:ProtectedServiceUrl stringValue=\"http://apim-hugh-new.lvn.broadcom.net:90\"/>\n" +
                "        </L7p:Http2Routing>\n" +
                "    </wsp:All>\n" +
                "</wsp:Policy>");
        policy.setPolicyDocument(DocumentTools.INSTANCE.parse(policy.getPolicyXML()).getDocumentElement());
        bundle.getPolicies().put("assertionPolicy", policy);

        writer.write(bundle, temporaryFolder.getRoot(), bundle);

        File policyFolder = new File(temporaryFolder.getRoot(), "policy");
        assertTrue(policyFolder.exists());

        File configFolder = new File(temporaryFolder.getRoot(), "config");
        assertTrue(configFolder.exists());

        File policyMetadataFile = new File(configFolder, "policies.yml");
        assertTrue(policyMetadataFile.exists());

        final Map<String, PolicyMetadata> policyMetadataMap =
                JsonFileUtils.INSTANCE.readPoliciesConfigFile(temporaryFolder.getRoot(), PolicyMetadata.class);
        assertEquals(1, policyMetadataMap.size());
        assertTrue(policyMetadataMap.get("assertionPolicy").isHasRouting());
    }

    @Test
    void testWritePolicyWithSubfolder(final TemporaryFolder temporaryFolder) throws DocumentParseException {
        PolicyWriter writer = new PolicyWriter(policyConverterRegistry, DocumentFileUtils.INSTANCE, JsonFileUtils.INSTANCE, new EntityLinkerRegistry(new HashSet<>()));

        Bundle bundle = new Bundle();
        bundle.addEntity(ROOT_FOLDER);
        Folder folder = new Folder("0000000000000000ffffffffffff54", "Test");
        folder.setParentFolder(ROOT_FOLDER);
        bundle.addEntity(folder);
        bundle.setFolderTree(new FolderTree(bundle.getEntities(Folder.class).values()));
        Policy policy = new Policy();
        policy.setGuid("123");
        policy.setPath("assertionPolicy");
        policy.setParentFolder(folder);
        policy.setName("assertionPolicy");
        policy.setId("asd");
        policy.setPolicyXML("<wsp:Policy xmlns:wsp=\"http://schemas.xmlsoap.org/ws/2002/12/policy\" xmlns:L7p=\"http://www.layer7tech.com/ws/policy\">\n" +
                "    <wsp:All wsp:Usage=\"Required\"><L7p:JavaScript>\n" +
                "            <L7p:ExecutionTimeout stringValue=\"\"/>\n" +
                "            <L7p:Name stringValue=\"assertionPolicy\"/>\n" +
                "            <L7p:Script stringValueReference=\"inline\"><![CDATA[var js = {};]]></L7p:Script>\n" +
                "        </L7p:JavaScript></wsp:All>\n" +
                "</wsp:Policy>");
        policy.setPolicyDocument(DocumentTools.INSTANCE.parse(policy.getPolicyXML()).getDocumentElement());
        bundle.getPolicies().put("assertionPolicy", policy);

        writer.write(bundle, temporaryFolder.getRoot(), bundle);

        File policyFolder = new File(temporaryFolder.getRoot(), "policy");
        assertTrue(policyFolder.exists());

        File testFolder = new File(policyFolder, "Test");
        assertTrue(testFolder.exists());

        File policyFile = new File(testFolder, "assertionPolicy.assertion.js");
        assertTrue(policyFile.exists());

        File configFolder = new File(temporaryFolder.getRoot(), "config");
        assertTrue(configFolder.exists());

        File policyMetadataFile = new File(configFolder, "policies.yml");
        assertTrue(policyMetadataFile.exists());
    }

    @Test
    void testWriteServicePolicy(final TemporaryFolder temporaryFolder) throws DocumentParseException {
        PolicyWriter writer = new PolicyWriter(policyConverterRegistry, DocumentFileUtils.INSTANCE, JsonFileUtils.INSTANCE, new EntityLinkerRegistry(new HashSet<>()));

        Bundle bundle = new Bundle();
        bundle.addEntity(ROOT_FOLDER);
        bundle.setFolderTree(new FolderTree(bundle.getEntities(Folder.class).values()));
        Service service = new Service();
        service.setPath("assertionPolicy");
        service.setParentFolder(ROOT_FOLDER);
        service.setName("assertionPolicy");
        service.setId("asd");
        DocumentTools documentTools = DocumentTools.INSTANCE;
        Document document = documentTools.parse("<wsp:Policy xmlns:wsp=\"http://schemas.xmlsoap.org/ws/2002/12/policy\" xmlns:L7p=\"http://www.layer7tech.com/ws/policy\">\n" +
                "    <wsp:All wsp:Usage=\"Required\"><L7p:JavaScript>\n" +
                "            <L7p:ExecutionTimeout stringValue=\"\"/>\n" +
                "            <L7p:Name stringValue=\"assertionPolicy\"/>\n" +
                "            <L7p:Script stringValueReference=\"inline\"><![CDATA[var js = {};]]></L7p:Script>\n" +
                "        </L7p:JavaScript></wsp:All>\n" +
                "</wsp:Policy>");
        service.setPolicyXML(document.getDocumentElement());
        bundle.getServices().put("assertionPolicy", service);

        writer.write(bundle, temporaryFolder.getRoot(), bundle);

        File policyFolder = new File(temporaryFolder.getRoot(), "policy");
        assertTrue(policyFolder.exists());

        File policyFile = new File(policyFolder, "assertionPolicy.assertion.js");
        assertTrue(policyFile.exists());

        File configFolder = new File(temporaryFolder.getRoot(), "config");
        assertTrue(configFolder.exists());

        File policyMetadataFile = new File(configFolder, "policies.yml");
        assertTrue(policyMetadataFile.exists());
    }

    @Test
    void testWritePolicyWithDependencies(final TemporaryFolder temporaryFolder) throws DocumentParseException {
        PolicyWriter writer = new PolicyWriter(policyConverterRegistry, DocumentFileUtils.INSTANCE, JsonFileUtils.INSTANCE, new EntityLinkerRegistry(new HashSet<>()));

        Bundle bundle = new Bundle();
        bundle.addEntity(ROOT_FOLDER);
        bundle.setFolderTree(new FolderTree(bundle.getEntities(Folder.class).values()));
        // create and add root policy to bundle
        Policy policy = new Policy();
        policy.setGuid("123");
        policy.setPath("assertionPolicy");
        policy.setParentFolder(ROOT_FOLDER);
        policy.setName("assertionPolicy");
        policy.setId("asd");
        policy.setPolicyXML("<wsp:Policy xmlns:wsp=\"http://schemas.xmlsoap.org/ws/2002/12/policy\" xmlns:L7p=\"http://www.layer7tech.com/ws/policy\">" +
                "    <wsp:All wsp:Usage=\"Required\"><L7p:JavaScript>" +
                "            <L7p:ExecutionTimeout stringValue=\"\"/>" +
                "            <L7p:Name stringValue=\"assertionPolicy\"/>" +
                "            <L7p:Script stringValueReference=\"inline\"><![CDATA[var js = {};]]></L7p:Script>" +
                "        </L7p:JavaScript>" +
                "        <L7p:Encapsulated encassName=\"encassDep\"/>" +
                "</wsp:All>" +
                "</wsp:Policy>");
        policy.setPolicyDocument(DocumentTools.INSTANCE.parse(policy.getPolicyXML()).getDocumentElement());
        bundle.getPolicies().put("assertionPolicy", policy);

        // create and add dependency policy to bundle
        Policy depPolicy = new Policy();
        depPolicy.setGuid("456");
        depPolicy.setPath("assertionDepPolicy");
        depPolicy.setParentFolder(Folder.ROOT_FOLDER);
        depPolicy.setName("assertionDepPolicy");
        depPolicy.setId("qwe");
        depPolicy.setPolicyXML("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<wsp:Policy xmlns:L7p=\"http://www.layer7tech.com/ws/policy\" xmlns:wsp=\"http://schemas.xmlsoap.org/ws/2002/12/policy\">\n" +
                "    <wsp:All wsp:Usage=\"Required\">\n" +
                "        <L7p:CommentAssertion>\n" +
                "            <L7p:Comment stringValue=\"Policy Fragment: includedPolicy\"/>\n" +
                "        </L7p:CommentAssertion>\n" +
                "    </wsp:All>\n" +
                "</wsp:Policy>");
        depPolicy.setPolicyDocument(DocumentTools.INSTANCE.parse(depPolicy.getPolicyXML()).getDocumentElement());
        bundle.getPolicies().put("assertionDepPolicy", depPolicy);

        Encass encass = new Encass();
        encass.setName("encassOne");
        encass.setPolicy("assertionPolicy");
        bundle.getEncasses().put("encassOne", encass);

        encass = new Encass();
        encass.setName("encassDep");
        encass.setPolicy("anotherPolicy");
        bundle.getEncasses().put("encassDep", encass);

        encass = new Encass();
        encass.setName("encassTwo");
        encass.setPolicy("assertionPolicy");
        bundle.getEncasses().put("encassTwo", encass);

        // create encass for dependency policy
        encass = new Encass();
        encass.setName("encassThree");
        encass.setPolicy("assertionDepPolicy");
        bundle.getEncasses().put("encassThree", encass);

        JdbcConnection.Builder builder = new JdbcConnection.Builder();
        builder.id("jdbcid");
        builder.name("testjdbc");
        builder.driverClass("testDriver");
        builder.jdbcUrl("jdbc:localhost:3306");
        JdbcConnection jdbcConnection = builder.build();
        bundle.addEntity(jdbcConnection);
        Map<Dependency, List<Dependency>> dependencyListMap = new HashMap<>();
        List<Dependency> dependencies = new ArrayList<>();
        // create dependency for jdbc connection
        Dependency jdbcCon = new Dependency("jdbcid", JdbcConnection.class, "testjdbc", EntityTypes.JDBC_CONNECTION);
        // add jdbc connection as direct dependency to root policy
        dependencies.add(jdbcCon);
        Dependency encassDependency = new Dependency(null, null, "encassDep", EntityTypes.ENCAPSULATED_ASSERTION_TYPE);
        dependencies.add(encassDependency);
        Dependency encassOne = new Dependency(null, null, "encassOne", EntityTypes.ENCAPSULATED_ASSERTION_TYPE);
        dependencies.add(encassOne);
        Dependency encassTwo = new Dependency(null, null, "encassTwo", EntityTypes.ENCAPSULATED_ASSERTION_TYPE);
        dependencies.add(encassTwo);
        // create dependency for dependency policy
        Dependency policyDep = new Dependency("qwe", Policy.class, "assertionDepPolicy", EntityTypes.POLICY_TYPE);
        // add dependency policy as direct dependency to root policy
        dependencies.add(policyDep);
        dependencyListMap.put(new Dependency("asd", Policy.class, "assertionPolicy", EntityTypes.POLICY_TYPE), dependencies);
        // create dependency list for jdbc connection
        List<Dependency> jdbcDependencies = new ArrayList<>();
        // create and add stored password as direct dependency to jdbc connection
        Dependency jdncConPass = new Dependency(null, null, "jdbcpass", EntityTypes.STORED_PASSWORD_TYPE);
        jdbcDependencies.add(jdncConPass);
        dependencyListMap.put(jdbcCon, jdbcDependencies);
        // create dependency list for dependency policy
        List<Dependency> depPolicyDependencies = new ArrayList<>();
        // create and add encassThree as direct dependency to dependency policy
        Dependency encassThree = new Dependency(null, null, "encassThree", EntityTypes.ENCAPSULATED_ASSERTION_TYPE);
        depPolicyDependencies.add(encassThree);
        dependencyListMap.put(policyDep, depPolicyDependencies);
        bundle.setDependencyMap(dependencyListMap);
        writer.write(bundle, temporaryFolder.getRoot(), bundle);

        File policyFolder = new File(temporaryFolder.getRoot(), "policy");
        assertTrue(policyFolder.exists());

        File configFolder = new File(temporaryFolder.getRoot(), "config");
        assertTrue(configFolder.exists());

        File policyMetadataFile = new File(configFolder, "policies.yml");
        assertTrue(policyMetadataFile.exists());
        Map<String, PolicyMetadata> policyMetadataMap = getPolicyMetadata(policyMetadataFile);
        // get policy metadata of root policy
        PolicyMetadata policyMetadata = policyMetadataMap.get("assertionPolicy");
        Set<Dependency> usedEntities = policyMetadata.getUsedEntities();
        assertTrue(usedEntities.contains(encassDependency));
        assertFalse(usedEntities.contains(encassOne));
        assertFalse(usedEntities.contains(encassTwo));
        // policy metadata should contain direct dependency jdbc connection and also transitive dependency stored password
        assertTrue(usedEntities.contains(new Dependency(null, null, "testjdbc", EntityTypes.JDBC_CONNECTION)));
        assertTrue(usedEntities.contains(jdncConPass));
        // policy metadata should contain only direct dependency - only dependency policy as it is non environmental entity
        assertTrue(usedEntities.contains(new Dependency(null, null, "assertionDepPolicy", EntityTypes.POLICY_TYPE)));
        assertFalse(usedEntities.contains(encassThree));
    }

    private Map<String, PolicyMetadata> getPolicyMetadata(File policyMetadataFile) {
        Map<String, PolicyMetadata> policyMetadataMap = null;
        JsonTools jsonTools = JsonTools.INSTANCE;
        final ObjectMapper objectMapper = jsonTools.getObjectMapper();
        final MapType type = objectMapper.getTypeFactory().constructMapType(HashMap.class, String.class, PolicyMetadata.class);
        try {
            policyMetadataMap = objectMapper.readValue(policyMetadataFile, type);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return policyMetadataMap;
    }
}