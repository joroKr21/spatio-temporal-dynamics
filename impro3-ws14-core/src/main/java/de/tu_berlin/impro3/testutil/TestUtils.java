package de.tu_berlin.impro3.testutil;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class TestUtils {

    /**
     * Grab an input URI from a resource path.
     * <p>
     * If the resource URI has a "file" scheme, just return the URI.
     * <p>
     * If the resource URI has a "jar" scheme, extract the jar file referenced by the resource into
     * a tmp folder and return the URI of the extracted file.
     * <p>
     * Otherwise, throw an UnsupportedOperationException exception.
     * 
     * @throws UnsupportedOperationException If the URI scheme is not supported.
     * @throws java.net.URISyntaxException If the URI has bad syntax.
     * @throws java.io.IOException If the URI has a "jar" scheme but the extract to tmp logic fails.
     * 
     * @return The URI to be used for the test input.
     */
    public static URI getInputURIFromResourcePath(String path) throws URISyntaxException, IOException {
        URI uri = TestUtils.class.getResource(path).toURI();

        if ("jar".equals(uri.getScheme())) {
            // resource has a jar:${path} uri

            // we expect ${path} to have the format ${jarLocation}!${jarFileLocation}
            int bangPosition = uri.getSchemeSpecificPart().lastIndexOf('!');
            if (bangPosition == -1) {
                throw new RuntimeException("Expected jar URI '" + uri + "' with a !${jarFileLocation} suffix, but could not find one");
            }

            // strip the jarFileLocation and construct a tmpPath with it
            String jarFileLocation = uri.getSchemeSpecificPart().substring(bangPosition + 1);
            URI tmpUri = URI.create(String.format("file:%s/impro-3-tmp-input/%s", System.getProperty("java.io.tmpdir"), jarFileLocation));
            Path tmpPath = Paths.get(tmpUri);

            // (re-)create the file at the computed tmpPath
            Files.deleteIfExists(tmpPath);
            Files.createDirectories(tmpPath.getParent());

            // copy the contents of the jar file in to the tmpPath
            Files.copy(TestUtils.class.getResourceAsStream(path), tmpPath);

            // return the URI of the created tmp file
            return tmpUri;

        } else if ("file".equals(uri.getScheme())) {
            // resource is a normal file, just return the uri
            return uri;
        } else {
            // resource scheme is neither file nor jar, throw an exception
            throw new UnsupportedOperationException("Unsupported URI scheme for input path " + uri);
        }
    }
}
