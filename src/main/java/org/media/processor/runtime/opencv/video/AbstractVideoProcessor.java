package org.media.processor.runtime.opencv.video;

import org.bytedeco.ffmpeg.global.avcodec;
import org.bytedeco.ffmpeg.global.avutil;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.FFmpegFrameRecorder;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameConverter;
import org.bytedeco.javacv.FrameGrabber;
import org.bytedeco.javacv.FrameRecorder;
import org.media.processor.Image;
import org.media.processor.IncorrectTypeException;
import org.media.processor.StepException;
import org.media.processor.Video;
import org.media.processor.VideoProcessor;
import org.media.processor.VideoRecorderCustomizer;
import org.media.processor.utils.ResourceUtils;
import org.media.processor.utils.io.ResourceIO;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.List;

public abstract class AbstractVideoProcessor<T> implements VideoProcessor {
    protected final ResourceIO videoStream;
    protected final File destination;

    private FrameGrabber frameGrabber;

    protected AbstractVideoProcessor(String path, String destination) throws IOException {
        this(ResourceUtils.getResourceAsStream(path), Path.of(destination).toFile());
    }

    protected AbstractVideoProcessor(File videoFile, File destination) throws IOException {
        this(ResourceUtils.wrap(videoFile), destination);
    }

    protected AbstractVideoProcessor(InputStream videoStream, File destination) throws IOException {
        if (videoStream instanceof ResourceIO resourceIO) {
            this.videoStream = resourceIO;
        } else {
            this.videoStream = ResourceUtils.wrap(videoStream, getExtension(destination));
        }
        this.destination = destination;
    }

    private String getExtension(File file) {
        String fileName = file.getName();
        if (!fileName.contains(".")) {
            throw new IncorrectTypeException("Not a file");
        }
        return fileName.substring(fileName.lastIndexOf(".") + 1);
    }

    protected FrameGrabber startFrameGrabber(InputStream source) throws FrameGrabber.Exception {
        if (frameGrabber == null) {
            if (source instanceof ResourceIO resourceIO) {
                frameGrabber = new FFmpegFrameGrabber(resourceIO.getResource());
            } else {
                frameGrabber = new FFmpegFrameGrabber(source);
            }
            frameGrabber.start(); // grabber call source.toString to find file name?
        }
        return frameGrabber;
    }

    protected abstract List<VideoRecorderCustomizer<FrameRecorder>> getVideoRecorderCustomizers();
    protected abstract Frame processFrame(Frame frame, FrameConverter<T> converter) throws FrameGrabber.Exception;
    protected abstract FrameConverter<T> createFrameConverter();

    @Override
    public synchronized Video<InputStream> process() throws StepException {
        try (FrameGrabber grabber = startFrameGrabber(videoStream);
             FrameRecorder recorder = createRecorder(grabber, destination)) {
            recorder.start();

            try (FrameConverter<T> converter = createFrameConverter()) {
                Frame frame;
                while ((frame = grabber.grab()) != null) {
                    try (Frame result = processFrame(frame, converter)) {
                        recorder.record(result);
                        frame.close();
                    }
                }
            }

            try {
                return new VideoStream(
                        ResourceUtils.wrap(destination),
                        grabber.getImageHeight(),
                        grabber.getImageWidth(),
                        grabber.getFrameRate(),
                        grabber.getLengthInTime() * 1000
                );
            } finally {
                releaseResource(recorder, grabber);
            }
        } catch (Exception e) {
            throw new StepException("Can not process input video", e);
        }
    }

    @Override
    public void close() throws IOException {
        videoStream.close();
    }

    @Override
    public void watermarked(String watermarkPath, int width, int height) {
        try {
            ResourceIO stream = ResourceUtils.getResourceAsStream(watermarkPath);
            watermarked(new Image<>() {
                @Override
                public void close() throws IOException {
                    stream.close();
                }

                @Override
                public String getLocation() {
                    return stream.toString();
                }

                @Override
                public InputStream image() {
                    return stream;
                }

                @Override
                public int getWidth() {
                    return width;
                }

                @Override
                public int getHeight() {
                    return height;
                }

                @Override
                public double opacity() {
                    return 0.3;
                }
            });
        } catch (IOException e) {
            throw new StepException("Fail to prepare data for watermarked", e);
        }
    }

    @Override
    public void watermarked(File watermark, int width, int height) {
        Image<InputStream> image = new WatermarkImage(watermark, height, width);
        watermarked(image);
    }

    protected void releaseGrabber(FrameGrabber grabber) throws FrameGrabber.Exception {
        grabber.release();
        grabber.close();
    }

    private void releaseResource(FrameRecorder recorder, FrameGrabber grabber) throws FrameRecorder.Exception, FrameGrabber.Exception {
        recorder.stop();
        recorder.release();
        releaseGrabber(grabber);
    }

    private FrameRecorder createRecorder(FrameGrabber frameGrabber, File des) {
        FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(des, frameGrabber.getImageWidth(), frameGrabber.getImageHeight(), frameGrabber.getAudioChannels());

        recorder.setFrameRate(frameGrabber.getFrameRate());
        recorder.setVideoCodec(avcodec.AV_CODEC_ID_H264);
        recorder.setFormat(frameGrabber.getFormat());
        recorder.setSampleRate(frameGrabber.getSampleRate());
        recorder.setAudioCodec(avcodec.AV_CODEC_ID_AAC);
        recorder.setPixelFormat(avutil.AV_PIX_FMT_YUV420P);

        getVideoRecorderCustomizers().forEach(customizer -> customizer.customize(recorder, frameGrabber));

        return recorder;
    }

    private record VideoStream(ResourceIO resourceIO,
                               int height,
                               int width,
                               double frameRate,
                               long duration) implements Video<InputStream> {

        @Override
        public InputStream getData() {
            return resourceIO;
        }

        @Override
        public InputStream stream() throws IOException {
            InputStream stream = getData();
            stream.reset();
            return stream;
        }

        @Override
        public long size() {
            return resourceIO.available();
        }
    }

    private static class WatermarkImage implements Image<InputStream> {
        private final ResourceIO inputStream;
        private final int height;
        private final int width;

        private WatermarkImage(File file, int height, int width) {
            this.inputStream = ResourceUtils.wrap(file);
            this.height = height;
            this.width = width;
        }

        @Override
        public String getLocation() {
            return inputStream.toString();
        }

        @Override
        public InputStream image() {
            return inputStream;
        }

        @Override
        public int getWidth() {
            return width;
        }

        @Override
        public int getHeight() {
            return height;
        }

        @Override
        public double opacity() {
            return 1.0;
        }

        @Override
        public void close() throws IOException {
            inputStream.close();
        }
    }
}
