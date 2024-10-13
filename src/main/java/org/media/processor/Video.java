package org.media.processor;

import java.io.IOException;
import java.io.InputStream;

public interface Video<T> {
    T getData() throws IOException;

    InputStream getStream() throws IOException;

    long duration();

    long getSize();

    double frameRate();

    int width();

    int height();
}
