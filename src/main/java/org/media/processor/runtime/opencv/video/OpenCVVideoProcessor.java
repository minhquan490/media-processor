package org.media.processor.runtime.opencv.video;

import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameConverter;
import org.bytedeco.javacv.OpenCVFrameConverter;
import org.bytedeco.opencv.opencv_core.Mat;
import org.media.processor.Image;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public abstract class OpenCVVideoProcessor extends AbstractVideoProcessor<Mat> {
    private OpenCVFrameConverter.ToMat converter;
    private MatProcessor processor;

    protected OpenCVVideoProcessor(String path, String destination) throws IOException {
        super(path, destination);
    }

    protected OpenCVVideoProcessor(File videoFile, File destination) throws IOException {
        super(videoFile, destination);
    }

    protected OpenCVVideoProcessor(InputStream videoStream, File destination) throws IOException {
        super(videoStream, destination);
    }

    @Override
    public void watermarked(Image<InputStream> watermarked) {
        this.processor = createTypeProcessor(watermarked);
    }

    @Override
    public void thumbnail(int width, int height) {
        this.processor = createTypeProcessor(width, height);
    }

    @Override
    public void close() throws IOException {
        super.close();
        converter.close();
        processor.close();
    }

    @Override
    protected Frame processFrame(Frame frame, FrameConverter<Mat> converter) {
        if (processor == null || frame == null || frame.image == null) {
            return frame;
        }

        Mat original = converter.convert(frame);

        if (original == null) {
            return frame;
        }

        Mat result = processor.process(original);

        return converter.convert(result);
    }

    @Override
    protected FrameConverter<Mat> createFrameConverter() {
        if (converter == null) {
            converter = new OpenCVFrameConverter.ToMat();
        }
        return converter;
    }

    protected abstract MatProcessor createTypeProcessor(Image<InputStream> watermarked);
    protected abstract MatProcessor createTypeProcessor(int width, int height);

    public interface MatProcessor extends Closeable {
        Mat process(Mat original);
    }
}
