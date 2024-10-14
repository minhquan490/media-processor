package org.media.processor.runtime.opencv.mat;

import org.bytedeco.opencv.opencv_core.Mat;
import org.media.processor.Image;
import org.media.processor.ImageProcessor;
import org.media.processor.StepException;
import org.media.processor.runtime.batik.Svg2ImageConverter;
import org.media.processor.runtime.opencv.image.OpenCVImage;
import org.media.processor.runtime.opencv.image.OpenCVImageProcessor;
import org.media.processor.runtime.opencv.video.OpenCVVideoProcessor;

import java.io.IOException;
import java.io.InputStream;

import static org.bytedeco.opencv.global.opencv_imgcodecs.IMREAD_COLOR;
import static org.bytedeco.opencv.global.opencv_imgcodecs.imread;

public abstract class WatermarkedMatProcessor implements OpenCVVideoProcessor.MatProcessor {
    private final ImageProcessor<Mat> imageProcessor;

    private  PreparedImage watermarkImage;
    private Mat cachedWatermark;

    protected WatermarkedMatProcessor(Image<InputStream> watermarked) {
        this.imageProcessor = new OpenCVImageProcessor();
        this.watermarkImage = new PreparedImage(watermarked);
    }

    @Override
    public void close() throws IOException {
        imageProcessor.close();
        cachedWatermark.close();
        watermarkImage.close();
    }

    @Override
    public Mat process(Mat original) {
        if (!watermarkImage.isPrepared()) {
            watermarkImage = new PreparedImage(prepareWatermark(watermarkImage, original.rows(), original.cols()));
            watermarkImage.setPrepared(true);
            cachedWatermark = imread(watermarkImage.getLocation(), IMREAD_COLOR);
        }

        Image<Mat> src = new OpenCVImage(original, getSrcOpacity());
        Image<Mat> srcOp = new OpenCVImage(cachedWatermark, getSrcOpOpacity());

        imageProcessor.blending(src, srcOp);

        Image<Mat> result = imageProcessor.process();

        return result.image();
    }

    @Deprecated(since = "1.0")
    protected abstract int calculateX(Mat src, Mat srcOp);
    @Deprecated(since = "1.0")
    protected abstract int calculateY(Mat src, Mat srcOp);
    protected abstract double getSrcOpacity();
    protected abstract double getSrcOpOpacity();

    private Image<InputStream> prepareWatermark(Image<InputStream> watermarked, int width, int height) {
        try {
            String location = watermarked.getLocation();
            if (location.endsWith("svg")) {
                try (watermarked) {
                    Svg2ImageConverter converter = new Svg2ImageConverter(location, "png");
                    return converter.convertImage(width, height, watermarked.getWidth(), watermarked.getHeight());
                }
            }
            return watermarked;
        } catch (Exception e) {
            throw new StepException("Fail to prepare watermark image", e);
        }
    }

    private static class PreparedImage implements Image<InputStream> {
        private final Image<InputStream> delegate;
        private boolean prepared = false;

        private PreparedImage(Image<InputStream> delegate) {
            this.delegate = delegate;
        }

        @Override
        public String getLocation() {
            return delegate.getLocation();
        }

        @Override
        public InputStream image() {
            return delegate.image();
        }

        @Override
        public int getWidth() {
            return delegate.getWidth();
        }

        @Override
        public int getHeight() {
            return delegate.getHeight();
        }

        @Override
        public double opacity() {
            return delegate.opacity();
        }

        @Override
        public void close() throws IOException {
            delegate.close();
        }

        public boolean isPrepared() {
            return prepared;
        }

        public void setPrepared(boolean prepared) {
            this.prepared = prepared;
        }
    }
}
