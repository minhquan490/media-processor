package org.media.processor;

public interface ImageProcessor<T> extends Processor<Image<T>> {
    void blending(Image<T> src, Image<T> srcOp);

    void resize(Image<T> src, int width, int height);
}
