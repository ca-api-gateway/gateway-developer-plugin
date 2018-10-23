package com.ca.apim.gateway.cagatewayexport.tasks.explode.filter.entityfilters;

import com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.Bundle;
import com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.entity.Folder;
import com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.entity.FolderTree;
import com.ca.apim.gateway.cagatewayexport.tasks.explode.filter.FilterConfiguration;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FolderFilterTest {
    @Test
    void filterEmptyBundle() {
        FolderFilter folderFilter = new FolderFilter();

        Bundle filteredBundle = new Bundle();
        List<Folder> childFolders = folderFilter.filter("/my/folder/path", new FilterConfiguration(), FilterTestUtils.getBundle(), filteredBundle);

        assertTrue(childFolders.isEmpty());
    }

    @Test
    void filterBundle() {
        Bundle bundle = FilterTestUtils.getBundle();
        bundle.addEntity(new Folder("my", "1", FilterTestUtils.ROOT_FOLDER_ID));
        bundle.addEntity(new Folder("folder", "2", "1"));
        bundle.addEntity(new Folder("fold", "5", "1"));
        bundle.addEntity(new Folder("path", "3", "2"));
        bundle.addEntity(new Folder("sub-folder", "4", "3"));
        bundle.addEntity(new Folder("another-path", "6", "2"));
        bundle.setFolderTree(new FolderTree(bundle.getEntities(Folder.class).values()));

        FolderFilter folderFilter = new FolderFilter();

        Bundle filteredBundle = new Bundle();
        List<Folder> childFolders = folderFilter.filter("/my/folder", new FilterConfiguration(), bundle, filteredBundle);

        assertEquals(4, childFolders.size());
        assertTrue(childFolders.stream().anyMatch(f -> "folder".equals(f.getName())));
        assertTrue(childFolders.stream().anyMatch(f -> "path".equals(f.getName())));
        assertTrue(childFolders.stream().anyMatch(f -> "another-path".equals(f.getName())));
        assertTrue(childFolders.stream().anyMatch(f -> "sub-folder".equals(f.getName())));
    }

    @Test
    void filterParentFolderNoParents() {
        List<Folder> parentFolders = FolderFilter.parentFolders("my/folder/path", FilterTestUtils.getBundle());
        assertEquals(1, parentFolders.size());
        assertTrue(parentFolders.stream().anyMatch(f -> "Root Node".equals(f.getName())));
    }

    @Test
    void filterParentFolder() {
        Bundle bundle = FilterTestUtils.getBundle();
        bundle.addEntity(new Folder("my", "1", FilterTestUtils.ROOT_FOLDER_ID));
        bundle.addEntity(new Folder("folder", "2", "1"));
        bundle.addEntity(new Folder("path", "3", "2"));
        bundle.addEntity(new Folder("sub-folder", "4", "3"));
        bundle.setFolderTree(new FolderTree(bundle.getEntities(Folder.class).values()));

        List<Folder> parentFolders = FolderFilter.parentFolders("/my/folder/path", bundle);
        assertEquals(4, parentFolders.size());
        assertTrue(parentFolders.stream().anyMatch(f -> "Root Node".equals(f.getName())));
        assertTrue(parentFolders.stream().anyMatch(f -> "my".equals(f.getName())));
        assertTrue(parentFolders.stream().anyMatch(f -> "folder".equals(f.getName())));
        assertTrue(parentFolders.stream().anyMatch(f -> "path".equals(f.getName())));
    }

    @Test
    void filterParentFolderFoldersWithPartialNames() {
        Bundle bundle = FilterTestUtils.getBundle();
        bundle.addEntity(new Folder("my", "1", FilterTestUtils.ROOT_FOLDER_ID));
        bundle.addEntity(new Folder("folder", "2", "1"));
        bundle.addEntity(new Folder("fold", "5", "1"));
        bundle.addEntity(new Folder("path", "3", "2"));
        bundle.addEntity(new Folder("pa", "6", "2"));
        bundle.addEntity(new Folder("sub-folder", "4", "3"));
        bundle.setFolderTree(new FolderTree(bundle.getEntities(Folder.class).values()));

        List<Folder> parentFolders = FolderFilter.parentFolders("/my/folder/path", bundle);
        assertEquals(4, parentFolders.size());
        assertTrue(parentFolders.stream().anyMatch(f -> "Root Node".equals(f.getName())));
        assertTrue(parentFolders.stream().anyMatch(f -> "my".equals(f.getName())));
        assertTrue(parentFolders.stream().anyMatch(f -> "folder".equals(f.getName())));
        assertTrue(parentFolders.stream().anyMatch(f -> "path".equals(f.getName())));
    }
}