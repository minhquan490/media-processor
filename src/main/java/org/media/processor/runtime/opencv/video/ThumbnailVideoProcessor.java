package org.media.processor.runtime.opencv.video;

import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameConverter;
import org.bytedeco.javacv.FrameGrabber;
import org.bytedeco.javacv.FrameRecorder;
import org.bytedeco.javacv.Java2DFrameConverter;
import org.bytedeco.opencv.opencv_core.Mat;
import org.media.processor.Image;
import org.media.processor.ImageProcessor;
import org.media.processor.StepException;
import org.media.processor.Video;
import org.media.processor.VideoRecorderCustomizer;
import org.media.processor.runtime.opencv.image.OpenCVImage;
import org.media.processor.runtime.opencv.image.OpenCVImageProcessor;
import org.media.processor.utils.ResourceUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class ThumbnailVideoProcessor extends OpenCVVideoProcessor {
    private static final String EXT = "jpeg";

    private final List<VideoRecorderCustomizer<FrameRecorder>> customizers;
    private int width = 0;
    private int height = 0;

    public ThumbnailVideoProcessor(String path, String destination, List<VideoRecorderCustomizer<FrameRecorder>> customizers) throws IOException {
        super(path, destination);
        this.customizers = customizers;
    }

    public ThumbnailVideoProcessor(File videoFile, File destination, List<VideoRecorderCustomizer<FrameRecorder>> customizers) throws IOException {
        super(videoFile, destination);
        this.customizers = customizers;
    }

    public ThumbnailVideoProcessor(InputStream videoStream, File destination, List<VideoRecorderCustomizer<FrameRecorder>> customizers) throws IOException {
        super(videoStream, destination);
        this.customizers = customizers;
    }

    @Override
    public synchronized Video<InputStream> process() throws StepException {
        try (FrameGrabber frameGrabber = startFrameGrabber(videoStream)) {

            try (FrameConverter<Mat> converter = createFrameConverter()) {
                Frame frame;
                Frame processedFrame = null;
                while ((frame = frameGrabber.grab()) != null) {
                    if (processedFrame != null) {
                        break;
                    }
                    if (frame.image != null) {
                        processedFrame = processFrame(frame, converter);
                    }
                    frame.close();
                }
                if (processedFrame != null) {
                    InputStream inputStream = writeImage(processedFrame);
                    ResourceUtils.copy(inputStream, destination.toPath());
                    return new ThumbnailVideoInputStream(inputStream, inputStream.available(), height, width);
                }
            }

            return new ThumbnailVideoInputStream(InputStream.nullInputStream(), 0, 0, 0);
        } catch (Exception e) {
            throw new StepException("Can not process input video", e);
        }
    }

    private InputStream writeImage(Frame processedFrame) throws IOException {
        try (processedFrame; Java2DFrameConverter java2DFrameConverter = new Java2DFrameConverter()) {
            BufferedImage bufferedImage = java2DFrameConverter.getBufferedImage(processedFrame);
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            ImageIO.write(bufferedImage, EXT, byteArrayOutputStream);
            byte[] imageBytes = byteArrayOutputStream.toByteArray();
            return new ByteArrayInputStream(imageBytes);
        }
    }

    @Override
    protected MatProcessor createTypeProcessor(Image<InputStream> watermarked) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected MatProcessor createTypeProcessor(int width, int height) {
        this.width = Math.max(300, width);
        this.height = Math.max(300, height);
        return new ThumbnailMatProcessor(width, height);
    }

    @Override
    protected List<VideoRecorderCustomizer<FrameRecorder>> getVideoRecorderCustomizers() {
        return customizers;
    }

    private record ThumbnailVideoInputStream(InputStream stream,
                                             long size,
                                             int height,
                                             int width) implements Video<InputStream> {

        @Override
        public InputStream getData() {
            return stream;
        }

        @Override
        public InputStream stream() {
            return getData();
        }

        @Override
        public long duration() {
            return 0;
        }

        @Override
        public double frameRate() {
            return 0;
        }
    }

    private static class ThumbnailMatProcessor implements MatProcessor {
        private final ImageProcessor<Mat> resizeProcessor = new OpenCVImageProcessor();
        private final int width;
        private final int height;

        private ThumbnailMatProcessor(int width, int height) {
            this.width = width;
            this.height = height;
        }

        @Override
        public Mat process(Mat original) {
            OpenCVImage openCVImage = new OpenCVImage(original, 1.0f);
            resizeProcessor.resize(openCVImage, width, height);

            Image<Mat> result = resizeProcessor.process();

            return result.image();
        }

        @Override
        public void close() throws IOException {
            resizeProcessor.close();
        }
    }
}
