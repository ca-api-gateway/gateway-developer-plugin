/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.config.loader;

import com.ca.apim.gateway.cagatewayconfig.beans.Bundle;
import com.ca.apim.gateway.cagatewayconfig.beans.StoredPassword;
import com.ca.apim.gateway.cagatewayconfig.util.file.FileUtils;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Map;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;

@SuppressWarnings("squid:S2068") // sonarcloud believes 'password' field names may have hardcoded passwords
@Singleton
public class StoredPasswordsLoader extends PropertiesLoaderBase {

    private static final String STORED_PASSWORDS_PROPERTIES = "config/stored-passwords.properties";

    @Inject
    StoredPasswordsLoader(FileUtils fileUtils) {
        super(fileUtils);
    }

    @Override
    protected String getFilePath() {
        return STORED_PASSWORDS_PROPERTIES;
    }

    @Override
    protected void putToBundle(Bundle bundle, Map<String, String> properties) {
        bundle.putAllStoredPasswords(properties.entrySet().stream().map(e -> buildStoredPassword(e.getKey(), e.getValue())).collect(toMap(StoredPassword::getName, identity())));
    }

    public static StoredPassword buildStoredPassword(String name, String password) {
        StoredPassword storedPassword = new StoredPassword();
        storedPassword.setName(name);
        storedPassword.setPassword(password);
        storedPassword.addDefaultProperties();
        return storedPassword;
    }

    @Override
    public String getEntityType() {
        return "PASSWORD";
    }
}