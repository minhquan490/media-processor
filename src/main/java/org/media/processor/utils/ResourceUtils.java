package org.media.processor.utils;

import org.jetbrains.annotations.NotNull;
import org.media.processor.utils.io.ResourceIO;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.UUID;

public final class ResourceUtils {
    public static final String CLASSPATH = "classpath:";
    public static final String DEFAULT_EXTENSION = ".raw";
    private static final ClassLoader GLOBAL_CLASS_LOADER;

    static {
        GLOBAL_CLASS_LOADER = ClassLoader.getSystemClassLoader();
    }

    private ResourceUtils() {}

    public static ResourceIO getResourceAsStream(@NotNull String resourcePath) throws IOException {
        if (resourcePath.startsWith(CLASSPATH)) {
            return getResourceInClasspath(resourcePath.substring(CLASSPATH.length()));
        }

        Path path = Path.of(resourcePath);

        return wrap(path.toFile());
    }

    public static ResourceIO wrap(@NotNull InputStream inputStream) throws IOException {
        return wrap(inputStream, DEFAULT_EXTENSION);
    }

    public static ResourceIO wrap(@NotNull InputStream other, @NotNull String ext) throws IOException {
        if (other instanceof ResourceIO resourceIO) {
            return resourceIO;
        }
        StringBuilder extension = new StringBuilder();
        if (ext.startsWith(".")) {
            extension.append(ext.substring(1));
        } else {
            extension.append(ext);
        }

        Path path = Path.of(UUID.randomUUID() + "." + extension);
        Files.createFile(path);

        copy(other, path);

        return wrap(path.toFile());
    }

    public static ResourceIO wrap(@NotNull File file) {
        return new ResourceIO(file);
    }

    public static void copy(InputStream in, Path outputPath) throws IOException {
        try (FileChannel channel = FileChannel.open(outputPath, StandardOpenOption.APPEND);
             ReadableByteChannel readableByteChannel = Channels.newChannel(in)) {

            channel.transferFrom(readableByteChannel, 0, in.available());
        }
    }

    private static ResourceIO getResourceInClasspath(@NotNull String resourcePath) throws IOException {
        InputStream stream = GLOBAL_CLASS_LOADER.getResourceAsStream(resourcePath);
        if (stream == null) {
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            if (classLoader != null) {
                stream = classLoader.getResourceAsStream(resourcePath);
            }
        }

        if (stream == null) {
            throw new IOException("Cannot load resource [" + resourcePath + "]");
        }

        String ext = resourcePath.substring(resourcePath.lastIndexOf('.') + 1);

        return wrap(stream, ext);
    }
}
