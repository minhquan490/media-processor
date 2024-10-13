package org.media.processor.runtime.opencv;

import org.bytedeco.opencv.global.opencv_core;
import org.bytedeco.opencv.global.opencv_imgproc;
import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.Rect;
import org.bytedeco.opencv.opencv_core.Size;
import org.media.processor.Image;
import org.media.processor.ImageProcessor;
import org.media.processor.StepException;

import java.io.IOException;

@SuppressWarnings("java:S1659")
public class OpenCVImageProcessor implements ImageProcessor<Mat> {

    static {
        try {
            ClassLoader.getSystemClassLoader().loadClass(opencv_core.class.getName());
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private int x, y;
    private Image<Mat> source, source2;

    @Override
    public void blending(Image<Mat> src, Image<Mat> srcOp) {
        blending(0, 0, src, srcOp);
    }

    @Override
    public void blending(int x, int y, Image<Mat> src, Image<Mat> srcOp) {
        this.x = Math.max(x, 0);
        this.y = Math.max(y, 0);
        this.source = src;
        this.source2 = srcOp;
    }

    @Override
    public Image<Mat> process() throws StepException {
        if (source2.getHeight() > source.getHeight()) {
            int newHeight = source.getHeight();
            this.source2 = new OpenCVImage(resize(newHeight, this.source2.getWidth(), this.source2.image()), this.source2.opacity());
        }

        if (source2.getWidth() > source.getWidth()) {
            int newWidth = source.getWidth();
            this.source2 = new OpenCVImage(resize(this.source2.getHeight(), newWidth, this.source2.image()), this.source2.opacity());
        }

        if (x + source2.getWidth() <= source.getWidth() && y + source2.getHeight() <= source.getHeight()) {
            int newWidth = source.getHeight() - x;
            int newHeight = source.getWidth() - y;
            this.source2 = new OpenCVImage(resize(newHeight, newWidth, this.source2.image()), this.source2.opacity());
        }

        try (Rect roi = new Rect(this.x, this.y, this.source2.getWidth(), this.source2.getHeight())) {

            Mat src = this.source.image();
            Mat srcOp = this.source2.image();

            Mat output = new Mat(src, roi);

            srcOp.copyTo(output);

            return new OpenCVImage(output, 1.0);
        } catch (RuntimeException e) {
            throw new StepException("Fail to process image", e);
        }
    }

    private Mat resize(int newHeight, int newWidth, Mat original) {
        Mat resized = new Mat();
        opencv_imgproc.resize(original, resized, new Size(newWidth, newHeight));
        return resized;
    }

    @Override
    public void close() throws IOException {
        // Do nothing
    }
}
