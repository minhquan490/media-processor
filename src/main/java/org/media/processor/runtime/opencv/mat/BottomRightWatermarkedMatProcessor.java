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
    protected int calculateX(Mat src, Mat srcOp) {
        return src.rows() - srcOp.rows();
    }

    @Override
    protected int calculateY(Mat src, Mat srcOp) {
        return src.cols() - srcOp.cols();
    }
}
