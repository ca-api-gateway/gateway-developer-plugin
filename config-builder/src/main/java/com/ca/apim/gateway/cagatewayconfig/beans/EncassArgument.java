/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.beans;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({"type", "name"})
public class EncassArgument {

    private String name;
    private String type;
    private Boolean requireExplicit;
    private String label;

    public EncassArgument() {
    }

    public EncassArgument(String name, String type) {
        this.name = name;
        this.type = type;
    }

    public EncassArgument(String name, String type, Boolean requireExplicit, String label) {
        this(name, type);
        this.requireExplicit = requireExplicit;
        this.label = label;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Boolean getRequireExplicit() {
        return requireExplicit;
    }

    public void setRequireExplicit(Boolean requireExplicit) {
        this.requireExplicit = requireExplicit;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }
}