package org.media.processor.runtime.opencv.mat;

import org.bytedeco.opencv.opencv_core.Mat;
import org.media.processor.Image;
import org.media.processor.ImageProcessor;
import org.media.processor.runtime.opencv.OpenCVImage;
import org.media.processor.runtime.opencv.OpenCVImageProcessor;
import org.media.processor.runtime.opencv.OpenCVVideoProcessor;

import java.io.IOException;
import java.io.InputStream;

import static org.bytedeco.opencv.global.opencv_imgcodecs.IMREAD_COLOR;
import static org.bytedeco.opencv.global.opencv_imgcodecs.imread;

public abstract class WatermarkedMatProcessor implements OpenCVVideoProcessor.MatProcessor {
    private final ImageProcessor<Mat> imageProcessor;
    private final Mat watermark;
    private final Image<InputStream> watermarkImage;

    protected WatermarkedMatProcessor(Image<InputStream> watermarked) {
        this.imageProcessor = new OpenCVImageProcessor();
        this.watermark = imread(watermarked.getLocation(), IMREAD_COLOR);
        this.watermarkImage = watermarked;
    }

    @Override
    public void close() throws IOException {
        imageProcessor.close();
        watermark.close();
        watermarkImage.close();
    }

    @Override
    public Mat process(Mat original) {
        Image<Mat> src = new OpenCVImage(original, getSrcOpacity());
        Image<Mat> srcOp = new OpenCVImage(watermark, getSrcOpOpacity());

        int x = calculateX(src.image(), srcOp.image());
        int y = calculateY(src.image(), srcOp.image());

        imageProcessor.blending(x, y, src, srcOp);

        Image<Mat> result = imageProcessor.process();

        return result.image();
    }

    protected abstract int calculateX(Mat src, Mat srcOp);
    protected abstract int calculateY(Mat src, Mat srcOp);
    protected abstract double getSrcOpacity();
    protected abstract double getSrcOpOpacity();
}
