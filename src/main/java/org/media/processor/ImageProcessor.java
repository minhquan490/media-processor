package org.media.processor;

public interface ImageProcessor<T> extends Processor<Image<T>> {
    void blending(Image<T> src, Image<T> srcOp);

    void blending(int x, int y, Image<T> src, Image<T> srcOp);
}
