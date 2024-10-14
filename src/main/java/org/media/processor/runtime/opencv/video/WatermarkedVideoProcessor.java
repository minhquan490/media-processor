package org.media.processor.runtime.opencv.video;

import org.bytedeco.javacv.FrameRecorder;
import org.media.processor.Image;
import org.media.processor.StepException;
import org.media.processor.VideoRecorderCustomizer;
import org.media.processor.WatermarkPosition;
import org.media.processor.runtime.opencv.mat.BottomCenterWatermarkedMatProcessor;
import org.media.processor.runtime.opencv.mat.BottomLeftWatermarkedMatProcessor;
import org.media.processor.runtime.opencv.mat.BottomRightWatermarkedMatProcessor;
import org.media.processor.runtime.opencv.mat.CenterWatermarkedMatProcessor;
import org.media.processor.runtime.opencv.mat.TopCenterWatermarkedMatProcessor;
import org.media.processor.runtime.opencv.mat.TopLeftWatermarkedMatProcessor;
import org.media.processor.runtime.opencv.mat.TopRightWatermarkedMatProcessor;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

// TODO since watermark position Deprecated, modify this class to support it
public class WatermarkedVideoProcessor extends OpenCVVideoProcessor {
    private final WatermarkPosition position;
    private final List<VideoRecorderCustomizer<FrameRecorder>> customizers;

    public WatermarkedVideoProcessor(String path, String destination, WatermarkPosition position) throws IOException {
        this(path, destination, position, List.of());
    }

    public WatermarkedVideoProcessor(String path, String destination, WatermarkPosition position, List<VideoRecorderCustomizer<FrameRecorder>> customizers) throws IOException {
        super(path, destination);
        this.position = position;
        this.customizers = customizers;
    }

    public WatermarkedVideoProcessor(File videoFile, File destination, WatermarkPosition position, List<VideoRecorderCustomizer<FrameRecorder>> customizers) throws IOException {
        super(videoFile, destination);
        this.position = position;
        this.customizers = customizers;
    }

    public WatermarkedVideoProcessor(InputStream videoStream, File destination, WatermarkPosition position, List<VideoRecorderCustomizer<FrameRecorder>> customizers) throws IOException {
        super(videoStream, destination);
        this.position = position;
        this.customizers = customizers;
    }

    @Override
    protected Map<String, String> getAdditionRecorderOptions() {
        return Map.of();
    }

    @Override
    protected List<VideoRecorderCustomizer<FrameRecorder>> getVideoRecorderCustomizers() {
        return customizers;
    }

    @Override
    protected MatProcessor createTypeProcessor(Image<InputStream> watermarked) {
        try {
            return switch (position) {
                case TOP_LEFT -> new TopLeftWatermarkedMatProcessor(watermarked);
                case TOP_CENTER -> new TopCenterWatermarkedMatProcessor(watermarked);
                case TOP_RIGHT -> new TopRightWatermarkedMatProcessor(watermarked);
                case BOTTOM_LEFT -> new BottomLeftWatermarkedMatProcessor(watermarked);
                case BOTTOM_CENTER -> new BottomCenterWatermarkedMatProcessor(watermarked);
                case BOTTOM_RIGHT -> new BottomRightWatermarkedMatProcessor(watermarked);
                case CENTER -> new CenterWatermarkedMatProcessor(watermarked);
            };
        } catch (IOException e) {
            throw new StepException("Fail to create type processor", e);
        }
    }

    @Override
    protected MatProcessor createTypeProcessor(int width, int height) {
        throw new UnsupportedOperationException("Thumbnail creation not supported");
    }
}
