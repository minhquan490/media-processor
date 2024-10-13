package org.media.processor;

import java.io.Closeable;

public interface Processor<T> extends Closeable {
    T process() throws StepException;

    enum Type {
        WATERMARKED,
        THUMBNAIL,
    }
}
