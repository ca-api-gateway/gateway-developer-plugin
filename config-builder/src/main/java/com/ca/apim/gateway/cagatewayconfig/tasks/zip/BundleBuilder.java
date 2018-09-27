/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.tasks.zip;

import com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans.Bundle;
import com.ca.apim.gateway.cagatewayconfig.tasks.zip.builder.BundleEntityBuilder;
import com.ca.apim.gateway.cagatewayconfig.tasks.zip.builder.EnvironmentBundleBuilder;
import com.ca.apim.gateway.cagatewayconfig.tasks.zip.bundle.DependencyBundleLoader;
import com.ca.apim.gateway.cagatewayconfig.tasks.zip.loader.EntityLoader;
import com.ca.apim.gateway.cagatewayconfig.tasks.zip.loader.EntityLoaderRegistry;
import com.ca.apim.gateway.cagatewayconfig.util.IdGenerator;
import com.ca.apim.gateway.cagatewayconfig.util.file.DocumentFileUtils;
import com.ca.apim.gateway.cagatewayconfig.util.file.FileUtils;
import com.ca.apim.gateway.cagatewayconfig.util.json.JsonTools;
import com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentTools;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import java.io.File;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

class BundleBuilder {

    private final DocumentFileUtils documentFileUtils;
    private final EntityLoaderRegistry entityLoaderRegistry;
    private final BundleEntityBuilder bundleEntityBuilder;
    private final EnvironmentBundleBuilder environmentBundleBuilder;
    private final DocumentTools documentTools;

    BundleBuilder(final DocumentTools documentTools, final DocumentFileUtils documentFileUtils, final FileUtils fileUtils, final JsonTools jsonTools) {
        IdGenerator idGenerator = new IdGenerator();
        final DocumentBuilder documentBuilder = documentTools.getDocumentBuilder();
        final Document document = documentBuilder.newDocument();

        this.documentFileUtils = documentFileUtils;
        this.documentTools = documentTools;
        this.entityLoaderRegistry = new EntityLoaderRegistry(fileUtils, jsonTools, idGenerator);
        this.bundleEntityBuilder = new BundleEntityBuilder(documentFileUtils, documentTools, document, idGenerator);
        this.environmentBundleBuilder = new EnvironmentBundleBuilder(document, idGenerator);
    }

    void buildBundle(File rootDir, File outputDir, Set<File> dependencies, String name) {

        final Collection<EntityLoader> entityLoaders = entityLoaderRegistry.getEntityLoaders();
        final Bundle bundle = new Bundle();

        //Load
        entityLoaders.parallelStream().forEach(e -> e.load(bundle, rootDir));

        //Load Dependencies
        // Improvements can be made here by doing this loading in a separate task and caching the intermediate results.
        // That way the dependent bundles are not re-processed on every new build
        final DependencyBundleLoader dependencyBundleLoader = new DependencyBundleLoader(documentTools);
        final Set<Bundle> dependencyBundles = dependencies.stream().map(dependencyBundleLoader::load).collect(Collectors.toSet());
        bundle.setDependencies(dependencyBundles);

        //Zip
        Element bundleElement = bundleEntityBuilder.build(bundle);
        documentFileUtils.createFile(bundleElement, new File(outputDir, name + ".req.bundle").toPath());

        Element environmentElement = environmentBundleBuilder.build(bundle);
        documentFileUtils.createFile(environmentElement, new File(outputDir, "_" + name + "-env.req.bundle").toPath());

    }


}