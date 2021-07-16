package com.seriouslypro.test

import org.junit.After
import org.junit.Before
import org.junit.rules.TemporaryFolder

trait TestResources {

    TemporaryFolder temporaryFolder

    @Before
    void setupTestResources() {
        temporaryFolder = new TemporaryFolder();
        temporaryFolder.create()
    }

    @After
    void teardownTestResources() {
        temporaryFolder.delete()
    }

    String testResource(String relativePart) {
        "/" + this.getClass().getName().toLowerCase() + relativePart
    }

    CharSequence stripFilenameExtension(String inputFileName) {
        inputFileName.take(inputFileName.lastIndexOf('.'))
    }

    File createTemporaryFileFromResource(TemporaryFolder folder,
                                         String classLoaderResource) throws IOException {
        URL resource = this.getClass().getResource(classLoaderResource);

        File tmpFile = folder.newFile();
        tmpFile << new File(resource.toURI()).bytes

        return tmpFile;
    }

    File copyResourceToTemporaryFolder(TemporaryFolder folder, String classLoaderResource) throws IOException {
        URL resource = this.getClass().getResource(classLoaderResource);

        String fileName = new File(classLoaderResource).getName()
        File tmpFile = folder.newFile(fileName);
        tmpFile << new File(resource.toURI()).bytes

        return tmpFile;
    }
}
