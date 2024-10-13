package org.media.processor.runtime.video;

import org.bytedeco.javacv.FrameRecorder;
import org.media.processor.Image;
import org.media.processor.VideoRecorderCustomizer;
import org.media.processor.WatermarkPosition;
import org.media.processor.runtime.opencv.OpenCVVideoProcessor;
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

public class WatermarkedVideoProcessor extends OpenCVVideoProcessor {
    private final WatermarkPosition position;

    protected WatermarkedVideoProcessor(String path, String destination, WatermarkPosition position) throws IOException {
        super(path, destination);
        this.position = position;
    }

    protected WatermarkedVideoProcessor(File videoFile, File destination, WatermarkPosition position) throws IOException {
        super(videoFile, destination);
        this.position = position;
    }

    protected WatermarkedVideoProcessor(InputStream videoStream, File destination, WatermarkPosition position) throws IOException {
        super(videoStream, destination);
        this.position = position;
    }

    @Override
    protected Map<String, String> getAdditionRecorderOptions() {
        return Map.of();
    }

    @Override
    protected List<VideoRecorderCustomizer<FrameRecorder>> getVideoRecorderCustomizers() {
        return List.of();
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
            throw new RuntimeException(e);
        }
    }

    @Override
    protected MatProcessor createTypeProcessor(int width, int height) {
        throw new UnsupportedOperationException("Thumbnail creation not supported");
    }
}
