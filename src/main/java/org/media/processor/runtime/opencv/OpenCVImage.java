package org.media.processor.runtime.opencv;

import org.apache.commons.lang3.StringUtils;
import org.bytedeco.opencv.opencv_core.Mat;
import org.media.processor.Image;

public record OpenCVImage(Mat image, double opacity) implements Image<Mat> {

    @Override
    public String getLocation() {
        return StringUtils.EMPTY;
    }

    @Override
    public int getWidth() {
        return image().rows();
    }

    @Override
    public int getHeight() {
        return image().cols();
    }

    @Override
    public void close() {
        if (image != null) {
            image.close();
        }
    }
}
