package org.media.processor.runtime.opencv.image;

import org.bytedeco.opencv.global.opencv_core;
import org.bytedeco.opencv.opencv_core.Mat;
import org.media.processor.Image;
import org.media.processor.ImageProcessor;
import org.media.processor.StepException;
import org.media.processor.utils.ImageUtils;

import java.io.IOException;

import static org.bytedeco.opencv.global.opencv_core.addWeighted;

@SuppressWarnings("java:S1659")
public class OpenCVImageProcessor implements ImageProcessor<Mat> {

    static {
        try {
            ClassLoader.getSystemClassLoader().loadClass(opencv_core.class.getName());
        } catch (ClassNotFoundException e) {
            throw new StepException("Fail to init class OpenCVImageProcessor", e);
        }
    }

    private final double gamma;

    private Image<Mat> source, source2;

    private ProcessType type;

    public OpenCVImageProcessor() {
        this(0.0);
    }

    public OpenCVImageProcessor(double gamma) {
        this.gamma = gamma;
    }

    @Override
    public void blending(Image<Mat> src, Image<Mat> srcOp) {
        this.source = src;
        this.source2 = srcOp;
        this.type = ProcessType.BLENDING;
    }

    @Override
    public void resize(Image<Mat> src, int width, int height) {
        this.source = new Source(src, width, height);
        this.source2 = null;
        this.type = ProcessType.RESIZED;
    }

    @Override
    public Image<Mat> process() throws StepException {
        return switch (type) {
            case BLENDING -> processBlending();
            case RESIZED -> processResize();
            case null -> throw new StepException("Unknown type to process, supported is [blending, resized]");
        };
    }

    @Override
    public void close() {
        // Do nothing
    }

    private Mat resize(int newHeight, int newWidth, Mat original) {
        return ImageUtils.resize(newHeight, newWidth, original);
    }

    private Image<Mat> processResize() {
        Mat output = resize(source.getHeight(), source.getWidth(), source.image());
        return new OpenCVImage(output, 1.0);
    }

    private Image<Mat> processBlending() {
        double alpha, beta;

        alpha = getAlpha();

        beta = 1.0 - alpha;

        if (source.getWidth() != source2.getWidth() || source.getHeight() != source2.getHeight()) {
            this.source2 = new OpenCVImage(resize(source.getWidth(), source.getHeight(), this.source2.image()), this.source2.opacity());
        }

        Mat src = this.source.image();
        Mat srcOp = this.source2.image();

        Mat output = new Mat();

        addWeighted(src, alpha, srcOp, beta, gamma, output);

        return new OpenCVImage(output, 1.0);
    }

    private double getAlpha() {
        double alpha = 0.7;

        if (this.source.opacity() < this.source2.opacity()) {
            alpha = this.source2.opacity();
        }

        if (this.source.opacity() > this.source2.opacity()) {
            alpha = this.source.opacity();
        }

        if (this.source.opacity() == this.source2.opacity()) {
            alpha = 0.5;
        }

        return alpha;
    }

    private record Source(Image<Mat> delegate, int newWidth, int newHeight) implements Image<Mat> {

        @Override
        public String getLocation() {
            return delegate.getLocation();
        }

        @Override
        public Mat image() {
            return delegate.image();
        }

        @Override
        public int getWidth() {
            return newWidth;
        }

        @Override
        public int getHeight() {
            return newHeight;
        }

        @Override
        public double opacity() {
            return delegate.opacity();
        }

        @Override
        public void close() throws IOException {
            delegate.close();
        }
    }

    private enum ProcessType {
        BLENDING,
        RESIZED
    }
}
