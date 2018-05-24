/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.tasks.zip.bundle.loader;

public class DependencyBundleLoadException extends RuntimeException {
    public DependencyBundleLoadException(String message) {
        super(message);
    }

    public DependencyBundleLoadException(String message, Throwable cause) {
        super(message, cause);
    }
}
