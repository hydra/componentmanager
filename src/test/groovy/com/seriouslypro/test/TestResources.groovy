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

    File createTemporaryFile(TemporaryFolder folder, String fileName, byte[] content) throws IOException {

        File tmpFile = folder.newFile(fileName);
        tmpFile << content

        return tmpFile
    }

    File createTemporaryFileFromResource(TemporaryFolder folder,
                                         String classLoaderResource) throws IOException {
        URL resource = this.getClass().getResource(classLoaderResource);

        File tmpFile = folder.newFile();
        tmpFile << new File(resource.toURI()).bytes

        return tmpFile
    }

    File copyResourceToTemporaryFolder(TemporaryFolder folder, String classLoaderResource) throws IOException {
        URL resource = this.getClass().getResource(classLoaderResource);

        String fileName = new File(classLoaderResource).getName()
        createTemporaryFile(folder, fileName, new File(resource.toURI()).bytes)
    }
}
