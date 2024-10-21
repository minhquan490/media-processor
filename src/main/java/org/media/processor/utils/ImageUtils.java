package org.media.processor.utils;

import org.bytedeco.opencv.global.opencv_imgproc;
import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.Size;

public final class ImageUtils {
    private ImageUtils() {}

    public static Mat resize(int height, int width, Mat original) {
        Mat resized = new Mat();
        opencv_imgproc.resize(original, resized, new Size(width, height), 0, 0, opencv_imgproc.INTER_CUBIC);
        return resized;
    }
}
