package org.media.processor;

import java.io.Closeable;

public interface Image<T> extends Closeable {
    String getLocation();

    T image();

    int getWidth();

    int getHeight();

    double opacity();
}
