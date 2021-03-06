package com.ca.apim.gateway.cagatewayexport.tasks.explode.writer;

import com.ca.apim.gateway.cagatewayconfig.beans.*;
import com.ca.apim.gateway.cagatewayconfig.util.file.DocumentFileUtils;
import io.github.glytching.junit.extension.folder.TemporaryFolder;
import io.github.glytching.junit.extension.folder.TemporaryFolderExtension;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.File;

import static com.ca.apim.gateway.cagatewayconfig.beans.Folder.ROOT_FOLDER;
import static com.ca.apim.gateway.cagatewayconfig.config.loader.FolderLoaderUtils.SOAP_RESOURCES_FOLDER;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(TemporaryFolderExtension.class)
class SoapResourceWriterTest {

    private static final String NAME_SERVICE = "service";
    private static final String POLICY_XML = "<wsp:Policy xmlns:wsp=\"http://schemas.xmlsoap.org/ws/2002/12/policy\" xmlns:L7p=\"http://www.layer7tech.com/ws/policy\">\n" +
            "    <wsp:All wsp:Usage=\"Required\">\n" +
            "        <L7p:HttpRoutingAssertion>\n" +
            "            <L7p:ProtectedServiceUrl stringValue=\"http://hugh:8080/axis/services/urn:EchoAttachmentsService\"/>\n" +
            "            <L7p:RequestHeaderRules httpPassthroughRuleSet=\"included\">\n" +
            "                <L7p:Rules httpPassthroughRules=\"included\">\n" +
            "                    <L7p:item httpPassthroughRule=\"included\">\n" +
            "                        <L7p:Name stringValue=\"Cookie\"/>\n" +
            "                    </L7p:item>\n" +
            "                    <L7p:item httpPassthroughRule=\"included\">\n" +
            "                        <L7p:Name stringValue=\"SOAPAction\"/>\n" +
            "                    </L7p:item>\n" +
            "                </L7p:Rules>\n" +
            "            </L7p:RequestHeaderRules>\n" +
            "            <L7p:RequestParamRules httpPassthroughRuleSet=\"included\">\n" +
            "                <L7p:ForwardAll booleanValue=\"true\"/>\n" +
            "                <L7p:Rules httpPassthroughRules=\"included\"/>\n" +
            "            </L7p:RequestParamRules>\n" +
            "            <L7p:ResponseHeaderRules httpPassthroughRuleSet=\"included\">\n" +
            "                <L7p:Rules httpPassthroughRules=\"included\">\n" +
            "                    <L7p:item httpPassthroughRule=\"included\">\n" +
            "                        <L7p:Name stringValue=\"Set-Cookie\"/>\n" +
            "                    </L7p:item>\n" +
            "                </L7p:Rules>\n" +
            "            </L7p:ResponseHeaderRules>\n" +
            "        </L7p:HttpRoutingAssertion>\n" +
            "    </wsp:All>\n" +
            "</wsp:Policy>";
    private static final String WSDL_XML = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<wsdl:definitions targetNamespace=\"urn:EchoAttachmentsService\" xmlns:mime=\"http://schemas.xmlsoap.org/wsdl/mime/\" xmlns=\"http://schemas.xmlsoap.org/wsdl/\" xmlns:apachesoap=\"http://xml.apache.org/xml-soap\" xmlns:impl=\"urn:EchoAttachmentsService\" xmlns:intf=\"urn:EchoAttachmentsService\" xmlns:soapenc=\"http://schemas.xmlsoap.org/soap/encoding/\" xmlns:tns1=\"http://activation.javax\" xmlns:wsdl=\"http://schemas.xmlsoap.org/wsdl/\" xmlns:wsdlsoap=\"http://schemas.xmlsoap.org/wsdl/soap/\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\">\n" +
            " <wsdl:types>\n" +
            "  <schema targetNamespace=\"urn:EchoAttachmentsService\" xmlns=\"http://www.w3.org/2001/XMLSchema\">\n" +
            "   <import namespace=\"http://schemas.xmlsoap.org/soap/encoding/\"/>\n" +
            "   <complexType name=\"ArrayOf_tns1_DataHandler\">\n" +
            "    <complexContent>\n" +
            "     <restriction base=\"soapenc:Array\">\n" +
            "      <attribute ref=\"soapenc:arrayType\" wsdl:arrayType=\"null[]\"/>\n" +
            "     </restriction>\n" +
            "    </complexContent>\n" +
            "   </complexType>\n" +
            "   <element name=\"ArrayOf_tns1_DataHandler\" nillable=\"true\" type=\"impl:ArrayOf_tns1_DataHandler\"/>\n" +
            "  </schema>\n" +
            " </wsdl:types>\n" +
            "  <wsdl:message name=\"echoOneRequest\">\n" +
            "      <wsdl:part name=\"source\" type=\"xsd:anyType\"/>\n" +
            "   </wsdl:message>\n" +
            "   <wsdl:message name=\"echoOneResponse\">\n" +
            "      <wsdl:part name=\"returnqname\" type=\"xsd:anyType\"/>\n" +
            "   </wsdl:message>\n" +
            "   <wsdl:message name=\"echoTwoRequest\">\n" +
            "      <wsdl:part name=\"source1\" type=\"xsd:anyType\"/>\n" +
            "      <wsdl:part name=\"source2\" type=\"xsd:anyType\"/>\n" +
            "   </wsdl:message>\n" +
            "   <wsdl:message name=\"echoTwoResponse\">\n" +
            "      <wsdl:part name=\"returnqname\" type=\"xsd:anyType\"/>\n" +
            "   </wsdl:message>\n" +
            "   <wsdl:message name=\"echoDirRequest\">\n" +
            "      <wsdl:part name=\"item\" type=\"impl:ArrayOf_tns1_DataHandler\"/>\n" +
            "   </wsdl:message>\n" +
            "   <wsdl:message name=\"echoDirResponse\">\n" +
            "      <wsdl:part name=\"item\" type=\"impl:ArrayOf_tns1_DataHandler\"/>\n" +
            "   </wsdl:message>\n" +
            "  <wsdl:message name=\"sayHelloRequest\"/>\n" +
            "   <wsdl:message name=\"sayHelloResponse\">\n" +
            "      <wsdl:part name=\"sayHelloReturn\" type=\"xsd:anyType\"/>\n" +
            "   </wsdl:message>\n" +
            "\n" +
            "\n" +
            "   <wsdl:portType name=\"EchoAttachmentsService\">\n" +
            "      <wsdl:operation name=\"echoOne\" parameterOrder=\"dh\">\n" +
            "         <wsdl:input message=\"impl:echoOneRequest\" name=\"echoOneRequest\"/>\n" +
            "         <wsdl:output message=\"impl:echoOneResponse\" name=\"echoOneResponse\"/>\n" +
            "      </wsdl:operation>\n" +
            "      <wsdl:operation name=\"echoTwo\" parameterOrder=\"dh\">\n" +
            "         <wsdl:input message=\"impl:echoTwoRequest\" name=\"echoTwoRequest\"/>\n" +
            "         <wsdl:output message=\"impl:echoTwoResponse\" name=\"echoTwoResponse\"/>\n" +
            "      </wsdl:operation>\n" +
            "     <wsdl:operation name=\"echoDir\" parameterOrder=\"attachments\">\n" +
            "         <wsdl:input message=\"impl:echoDirRequest\" name=\"echoDirRequest\"/>\n" +
            "         <wsdl:output message=\"impl:echoDirResponse\" name=\"echoDirResponse\"/>\n" +
            "      </wsdl:operation>\n" +
            "      <wsdl:operation name=\"sayHello\">\n" +
            "         <wsdl:input message=\"impl:sayHelloRequest\" name=\"sayHelloRequest\"/>\n" +
            "         <wsdl:output message=\"impl:sayHelloResponse\" name=\"sayHelloResponse\"/>\n" +
            "      </wsdl:operation>\n" +
            "   </wsdl:portType>\n" +
            "\n" +
            "   <wsdl:binding name=\"EchoAttachmentsServiceSoapBinding1\" type=\"impl:EchoAttachmentsService\">\n" +
            "      <wsdlsoap:binding style=\"rpc\" transport=\"http://schemas.xmlsoap.org/soap/http\"/>\n" +
            "      <wsdl:operation name=\"echoOne\">\n" +
            "         <wsdlsoap:operation soapAction=\"\"/>\n" +
            "         <wsdl:input name=\"echoOneRequest\">\n" +
            "            <mime:multipartRelated>\n" +
            "\n" +
            "\n" +
            "                 <mime:part>\n" +
            "\n" +
            "\n" +
            "                      <wsdlsoap:body encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\" namespace=\"urn:EchoAttachmentsService\" use=\"encoded\"/>\n" +
            "\n" +
            "\n" +
            "                 </mime:part>\n" +
            "\n" +
            "\n" +
            "                 <mime:part>\n" +
            "\n" +
            " \n" +
            "                       \t<mime:content part=\"source\" type=\"*/*\"/>\n" +
            "\n" +
            "                     \n" +
            " \t\t </mime:part>\n" +
            "\n" +
            "                   \n" +
            "\t     </mime:multipartRelated>\n" +
            "\n" +
            "\n" +
            "         </wsdl:input>\n" +
            "         <wsdl:output name=\"echoOneResponse\">\n" +
            "            <mime:multipartRelated>\n" +
            "\n" +
            "\n" +
            "                 <mime:part>\n" +
            "\n" +
            "\n" +
            "                      <wsdlsoap:body encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\" namespace=\"urn:EchoAttachmentsService\" use=\"encoded\"/>\n" +
            "\n" +
            "\n" +
            "                 </mime:part>\n" +
            "\n" +
            "\n" +
            "                 <mime:part>\n" +
            "\n" +
            " \n" +
            "                       <mime:content part=\"returnqname\" type=\"*/*\"/>\n" +
            "\n" +
            "                     \n" +
            "  \t\t </mime:part>\n" +
            "\n" +
            "                   \n" +
            "\t     </mime:multipartRelated>\n" +
            "\n" +
            "\n" +
            "         </wsdl:output>\n" +
            "      </wsdl:operation>\n" +
            "      <wsdl:operation name=\"sayHello\">\n" +
            "         <wsdlsoap:operation soapAction=\"\"/>\n" +
            "         <wsdl:input name=\"sayHelloRequest\"/>\n" +
            "         <wsdl:output name=\"sayHelloResponse\"/>\n" +
            "      </wsdl:operation>\n" +
            "   </wsdl:binding>\n" +
            "\n" +
            "   <wsdl:binding name=\"EchoAttachmentsServiceSoapBinding2\" type=\"impl:EchoAttachmentsService\">\n" +
            "      <wsdlsoap:binding style=\"rpc\" transport=\"http://schemas.xmlsoap.org/soap/http\"/>\n" +
            "      <wsdl:operation name=\"echoTwo\">\n" +
            "         <wsdlsoap:operation soapAction=\"\"/>\n" +
            "         <wsdl:input name=\"echoTwoRequest\">\n" +
            "            <mime:multipartRelated>\n" +
            "\n" +
            "\n" +
            "                 <mime:part>\n" +
            "\n" +
            "\n" +
            "                      <wsdlsoap:body encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\" namespace=\"urn:EchoAttachmentsService\" use=\"encoded\"/>\n" +
            "\n" +
            "\n" +
            "                 </mime:part>\n" +
            "\n" +
            "\n" +
            "                 <mime:part>\n" +
            "\n" +
            " \n" +
            "                       \t<mime:content part=\"source1\" type=\"*/*\"/>\n" +
            "\n" +
            "                     \n" +
            " \t\t </mime:part>\n" +
            "\n" +
            "                   \n" +
            "                 <mime:part>\n" +
            "\n" +
            " \n" +
            "                       \t<mime:content part=\"source2\" type=\"*/*\"/>\n" +
            "\n" +
            "                     \n" +
            " \t\t </mime:part>\n" +
            "\n" +
            "                   \n" +
            "\t     </mime:multipartRelated>\n" +
            "\n" +
            "\n" +
            "         </wsdl:input>\n" +
            "         <wsdl:output name=\"echoTwoResponse\">\n" +
            "            <mime:multipartRelated>\n" +
            "\n" +
            "\n" +
            "                 <mime:part>\n" +
            "\n" +
            "\n" +
            "                      <wsdlsoap:body encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\" namespace=\"urn:EchoAttachmentsService\" use=\"encoded\"/>\n" +
            "\n" +
            "\n" +
            "                 </mime:part>\n" +
            "\n" +
            "\n" +
            "                 <mime:part>\n" +
            "\n" +
            " \n" +
            "                       <mime:content part=\"returnqname\" type=\"*/*\"/>\n" +
            "\n" +
            "                     \n" +
            "  \t\t </mime:part>\n" +
            "\n" +
            "                   \n" +
            "\t     </mime:multipartRelated>\n" +
            "\n" +
            "\n" +
            "         </wsdl:output>\n" +
            "      </wsdl:operation>\n" +
            "      <wsdl:operation name=\"echoDir\">\n" +
            "         <wsdlsoap:operation soapAction=\"\"/>\n" +
            "         <wsdl:input name=\"echoDirRequest\">\n" +
            "            <mime:multipartRelated>\n" +
            "\n" +
            "\n" +
            "                 <mime:part>\n" +
            "\n" +
            "\n" +
            "                      <wsdlsoap:body encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\" namespace=\"urn:EchoAttachmentsService\" use=\"encoded\"/>\n" +
            "\n" +
            "\n" +
            "                 </mime:part>\n" +
            "\n" +
            "\n" +
            "                 <mime:part>\n" +
            "\n" +
            " \n" +
            "                       <mime:content part=\"source\" type=\"*/*\"/>\n" +
            "\n" +
            "                     \n" +
            "\t\t </mime:part>\n" +
            "\n" +
            "                   \n" +
            "\t     </mime:multipartRelated>\n" +
            "\n" +
            "\n" +
            "         </wsdl:input>\n" +
            "         <wsdl:output name=\"echoDirResponse\">\n" +
            "            <mime:multipartRelated>\n" +
            "\n" +
            "\n" +
            "                 <mime:part>\n" +
            "\n" +
            "\n" +
            "                      <wsdlsoap:body encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\" namespace=\"urn:EchoAttachmentsService\" use=\"encoded\"/>\n" +
            "\n" +
            "\n" +
            "                 </mime:part>\n" +
            "\n" +
            "\n" +
            "                 <mime:part>\n" +
            "\n" +
            " \n" +
            "                       <mime:content part=\"echoDirReturn\" type=\"*/*\"/>\n" +
            "\n" +
            "                     \n" +
            "  \t\t </mime:part>\n" +
            "\n" +
            "                   \n" +
            "\t     </mime:multipartRelated>\n" +
            "\n" +
            "\n" +
            "         </wsdl:output>\n" +
            "      </wsdl:operation>\n" +
            "   </wsdl:binding>\n" +
            "\n" +
            "   <wsdl:service name=\"EchoAttachmentsServiceService\">\n" +
            "      <wsdl:port binding=\"impl:EchoAttachmentsServiceSoapBinding2\" name=\"EchoAttachmentsService\">\n" +
            "         <wsdlsoap:address location=\"http://hugh:8080/axis/services/urn:EchoAttachmentsService\"/>\n" +
            "      </wsdl:port>\n" +
            "      <wsdl:port binding=\"impl:EchoAttachmentsServiceSoapBinding1\" name=\"EchoAttachmentsService\">\n" +
            "         <wsdlsoap:address location=\"http://hugh:8080/axis/services/urn:EchoAttachmentsService\"/>\n" +
            "      </wsdl:port>\n" +
            "\n" +
            "   </wsdl:service>\n" +
            "</wsdl:definitions>\n";
    public static final String WSDL_FILE_NAME = "EchoAttachmentsServiceAxisAll.wsdl";
    private static final String XML_SCHEMA = "<xs:schema\n" +
            "    targetNamespace=\"http://schemas.xmlsoap.org/ws/2004/09/transfer\"\n" +
            "    xmlns:tns=\"http://schemas.xmlsoap.org/ws/2004/09/transfer\"\n" +
            "    xmlns:wsa=\"http://schemas.xmlsoap.org/ws/2004/08/addressing\"\n" +
            "    xmlns:xs=\"http://www.w3.org/2001/XMLSchema\"\n" +
            "    elementFormDefault=\"qualified\"\n" +
            "    blockDefault=\"#all\" >\n" +
            "\n" +
            "  <xs:import\n" +
            "    namespace=\"http://schemas.xmlsoap.org/ws/2004/08/addressing\"\n" +
            "    schemaLocation=\"http://schemas.xmlsoap.org/ws/2004/08/addressing/addressing.xsd\"\n" +
            "    />\n" +
            "\n" +
            "  <xs:complexType name=\"AnyXmlType\">\n" +
            "    <xs:sequence>\n" +
            "      <xs:any namespace=\"##other\" processContents=\"lax\" />\n" +
            "    </xs:sequence>\n" +
            "  </xs:complexType>\n" +
            "\n" +
            "  <xs:complexType name=\"AnyXmlOptionalType\">\n" +
            "    <xs:sequence>\n" +
            "      <xs:any namespace=\"##other\" processContents=\"lax\"\n" +
            "              minOccurs=\"0\"/>\n" +
            "    </xs:sequence>\n" +
            "  </xs:complexType>\n" +
            "\n" +
            "  <xs:complexType name=\"CreateResponseType\">\n" +
            "    <xs:sequence>\n" +
            "      <xs:element ref=\"tns:ResourceCreated\" />\n" +
            "      <xs:any namespace=\"##other\" processContents=\"lax\"\n" +
            "              minOccurs=\"0\"/>\n" +
            "    </xs:sequence>\n" +
            "  </xs:complexType>\n" +
            "\n" +
            "  <xs:element name=\"ResourceCreated\" type=\"wsa:EndpointReferenceType\" />\n" +
            "\n" +
            "</xs:schema>";
    private static final String XML_SCHEMA_FILE_NAME = "schema.xsd";

    @Test
    void testWriteSoapResources(final TemporaryFolder temporaryFolder) {
        SoapResourceWriter writer = new SoapResourceWriter(DocumentFileUtils.INSTANCE);

        Bundle bundle = new Bundle();
        bundle.addEntity(ROOT_FOLDER);
        bundle.setFolderTree(new FolderTree(bundle.getEntities(Folder.class).values()));
        Service service = new Service();
        service.setName(NAME_SERVICE);
        service.setParentFolder(ROOT_FOLDER);
        service.addSoapResource(getSoapResource(WSDL_FILE_NAME, WSDL_XML, SoapResourceType.WSDL));
        service.addSoapResource(getSoapResource(XML_SCHEMA_FILE_NAME, XML_SCHEMA, SoapResourceType.XMLSCHEMA));
        service.setPolicy(POLICY_XML);
        service.setSoapVersion("1.1");
        service.setWssProcessingEnabled(true);
        bundle.getServices().put("service", service);

        writer.write(bundle, temporaryFolder.getRoot(), bundle);

        File wsdlFolder = new File(temporaryFolder.getRoot(), SOAP_RESOURCES_FOLDER);
        assertTrue(wsdlFolder.exists());

        File serviceSoapResourcesFolder = new File(wsdlFolder, NAME_SERVICE);
        assertTrue(serviceSoapResourcesFolder.exists());

        File wsdlFile = new File(serviceSoapResourcesFolder, WSDL_FILE_NAME);
        assertTrue(wsdlFile.exists());

        File xsdFile = new File(serviceSoapResourcesFolder, XML_SCHEMA_FILE_NAME);
        assertTrue(xsdFile.exists());
    }

    @Test
    void testWriteEmptySoapResource(final TemporaryFolder temporaryFolder) {
        SoapResourceWriter writer = new SoapResourceWriter(DocumentFileUtils.INSTANCE);

        Bundle bundle = new Bundle();
        bundle.addEntity(ROOT_FOLDER);
        bundle.setFolderTree(new FolderTree(bundle.getEntities(Folder.class).values()));
        Service service = new Service();
        service.setName(NAME_SERVICE);
        service.setParentFolder(ROOT_FOLDER);
        service.addSoapResource(getSoapResource(WSDL_FILE_NAME, EMPTY, SoapResourceType.WSDL));
        service.setPolicy(POLICY_XML);
        service.setSoapVersion("1.1");
        service.setWssProcessingEnabled(true);
        bundle.getServices().put("service", service);

        writer.write(bundle, temporaryFolder.getRoot(), bundle);

        File wsdlFolder = new File(temporaryFolder.getRoot(), SOAP_RESOURCES_FOLDER);
        assertTrue(wsdlFolder.exists());

        File serviceSoapResourcesFolder = new File(wsdlFolder, NAME_SERVICE);
        assertTrue(serviceSoapResourcesFolder.exists());

        File wsdlFile = new File(serviceSoapResourcesFolder, WSDL_FILE_NAME);
        assertTrue(wsdlFile.exists());

    }

    @Test
    void testWriteNonSoapService_NofolderCreated(final TemporaryFolder temporaryFolder) {
        SoapResourceWriter writer = new SoapResourceWriter(DocumentFileUtils.INSTANCE);

        Bundle bundle = new Bundle();
        bundle.addEntity(ROOT_FOLDER);
        bundle.setFolderTree(new FolderTree(bundle.getEntities(Folder.class).values()));
        Service service = new Service();
        service.setName(NAME_SERVICE);
        service.setParentFolder(ROOT_FOLDER);
        service.setPolicy(POLICY_XML);
        bundle.getServices().put("service", service);

        writer.write(bundle, temporaryFolder.getRoot(), bundle);

        File wsdlFolder = new File(temporaryFolder.getRoot(), SOAP_RESOURCES_FOLDER);
        assertFalse(wsdlFolder.exists());

        File serviceSoapResourcesFolder = new File(wsdlFolder, NAME_SERVICE);
        assertFalse(serviceSoapResourcesFolder.exists());

        File wsdlFile = new File(serviceSoapResourcesFolder, WSDL_FILE_NAME);
        assertFalse(wsdlFile.exists());

    }

    @NotNull
    private SoapResource getSoapResource(String fileName, String content, SoapResourceType type) {
        SoapResource wsdl = new SoapResource();
        wsdl.setRootUrl("file:/" + fileName);
        wsdl.setContent(content);
        wsdl.setType(type.getType());
        return wsdl;
    }
}
