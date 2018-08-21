/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayexport.tasks.explode.writer.beans;

import java.util.List;
import java.util.Map;

public class ListenPort {

    private String protocol;
    private int port;
    private List<String> enabledFeatures;
    private ListenPortTlsSettings tlsSettings;
    private Map<String, Object> properties;

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public List<String> getEnabledFeatures() {
        return enabledFeatures;
    }

    public void setEnabledFeatures(List<String> enabledFeatures) {
        this.enabledFeatures = enabledFeatures;
    }

    public Map<String, Object> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, Object> properties) {
        this.properties = properties;
    }

    public ListenPortTlsSettings getTlsSettings() {
        return tlsSettings;
    }

    public void setTlsSettings(ListenPortTlsSettings tlsSettings) {
        this.tlsSettings = tlsSettings;
    }

    public static class ListenPortTlsSettings {

        private ClientAuthentication clientAuthentication;
        private List<String> enabledVersions;
        private List<String> enabledCipherSuites;
        private Map<String, Object> properties;

        public ClientAuthentication getClientAuthentication() {
            return clientAuthentication;
        }

        public void setClientAuthentication(ClientAuthentication clientAuthentication) {
            this.clientAuthentication = clientAuthentication;
        }

        public List<String> getEnabledVersions() {
            return enabledVersions;
        }

        public void setEnabledVersions(List<String> enabledVersions) {
            this.enabledVersions = enabledVersions;
        }

        public List<String> getEnabledCipherSuites() {
            return enabledCipherSuites;
        }

        public void setEnabledCipherSuites(List<String> enabledCipherSuites) {
            this.enabledCipherSuites = enabledCipherSuites;
        }

        public Map<String, Object> getProperties() {
            return properties;
        }

        public void setProperties(Map<String, Object> properties) {
            this.properties = properties;
        }
    }

    public enum ClientAuthentication {

        NONE("None"), OPTIONAL("Optional"), REQUIRED("Required");

        private String type;

        private ClientAuthentication(String type) {
            this.type = type;
        }

        public String getType() {
            return type;
        }
    }

}
