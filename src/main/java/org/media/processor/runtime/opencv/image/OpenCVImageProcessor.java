package org.media.processor.runtime.opencv.image;

import org.bytedeco.opencv.global.opencv_core;
import org.bytedeco.opencv.global.opencv_imgproc;
import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.Size;
import org.media.processor.Image;
import org.media.processor.ImageProcessor;
import org.media.processor.StepException;

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
    }

    @Override
    public Image<Mat> process() throws StepException {
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

    private Mat resize(int newHeight, int newWidth, Mat original) {
        Mat resized = new Mat();
        opencv_imgproc.resize(original, resized, new Size(newWidth, newHeight));
        return resized;
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

    @Override
    public void close() {
        // Do nothing
    }
}
