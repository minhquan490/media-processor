package org.media.processor;

import java.io.IOException;
import java.io.InputStream;

public interface Video<T> {
    T getData() throws IOException;

    InputStream stream() throws IOException;

    long duration();

    long size();

    double frameRate();

    int width();

    int height();
}
