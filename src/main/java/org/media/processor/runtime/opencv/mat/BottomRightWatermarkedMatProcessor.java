package org.media.processor.runtime.opencv.mat;

import org.bytedeco.opencv.opencv_core.Mat;
import org.media.processor.Image;

import java.io.IOException;
import java.io.InputStream;

public class BottomRightWatermarkedMatProcessor extends WatermarkedPositionMatProcessor {
    public BottomRightWatermarkedMatProcessor(Image<InputStream> watermarked) throws IOException {
        super(watermarked);
    }

    @Override
    protected int calculateMinX(Mat src, float scale) {
        return (src.rows() / 2) + 500;
    }

    @Override
    protected int calculateMinY(Mat src, float scale) {
        return (src.cols() / 2) + 100;
    }
}
