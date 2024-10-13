package org.media.processor.runtime.opencv.mat;

import org.bytedeco.opencv.opencv_core.Mat;
import org.media.processor.Image;

import java.io.IOException;
import java.io.InputStream;

public class TopLeftWatermarkedMatProcessor extends WatermarkedPositionMatProcessor {
    public TopLeftWatermarkedMatProcessor(Image<InputStream> watermarked) throws IOException {
        super(watermarked);
    }

    @Override
    protected int calculateX(Mat src, Mat srcOp) {
        return 0;
    }

    @Override
    protected int calculateY(Mat src, Mat srcOp) {
        return 0;
    }
}
