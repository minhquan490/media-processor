package org.media.processor.utils.io;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

public class ResourceIO extends InputStream {
    private final File resource;
    private final FileInputStream[] streamPointer = new FileInputStream[1];

    public ResourceIO(@NotNull File resource) {
        this.resource = resource;
    }

    public File getResource() {
        return resource;
    }

    @Override
    public int read() throws IOException {
        FileInputStream stream = getStream();
        return stream.read();
    }

    @Override
    public int read(byte @NotNull [] b, int off, int len) throws IOException {
        FileInputStream stream = getStream();
        return stream.read(b, off, len);
    }

    @Override
    public int available() {
        return (int) resource.length();
    }

    @Override
    public void mark(int readlimit) {
        try {
            FileInputStream stream = getStream();
            stream.mark(readlimit);
        } catch (IOException e) {/* Do nothing */}
    }

    @Override
    public void reset() throws IOException {
        deAllocate();
        allocate();
    }

    @Override
    public boolean markSupported() {
        try {
            FileInputStream stream = getStream();
            return stream.markSupported();
        } catch (IOException e) {
            return false;
        }
    }

    @Override
    public void close() throws IOException {
        deAllocate();
        Files.deleteIfExists(resource.toPath());
    }

    @Override
    public String toString() {
        return resource.getAbsolutePath();
    }

    private FileInputStream openStream() throws IOException {
        return new FileInputStream(resource);
    }

    private FileInputStream getStream() throws IOException {
        FileInputStream stream = streamPointer[0];
        if (stream == null) {
            stream = allocate();
        }
        return stream;
    }

    private FileInputStream allocate() throws IOException {
        FileInputStream stream = openStream();
        streamPointer[0] = stream;
        return stream;
    }

    private void deAllocate() throws IOException {
        if (streamPointer[0] != null) {
            streamPointer[0].close();
            streamPointer[0] = null;
        }
    }
}
