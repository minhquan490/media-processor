package org.media.processor.runtime.opencv.video;

import org.bytedeco.ffmpeg.global.avutil;
import org.bytedeco.javacv.FFmpegLogCallback;
import org.bytedeco.javacv.FrameRecorder;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.media.processor.VideoRecorderCustomizer;
import org.media.processor.WatermarkPosition;
import org.media.processor.runtime.opencv.video.WatermarkedVideoProcessor;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

import static org.bytedeco.ffmpeg.global.avutil.AV_ERROR_MAX_STRING_SIZE;

class WatermarkedVideoProcessorTest {

    @Test
    void test() throws IOException {
        FFmpegLogCallback.set();

        byte[] bytes = avutil.av_make_error_string(new byte[AV_ERROR_MAX_STRING_SIZE], AV_ERROR_MAX_STRING_SIZE, -1094995529);

        System.out.println(new String(bytes, StandardCharsets.UTF_8));

        Path path = Path.of(UUID.randomUUID() + ".mp4");
        Files.createFile(path);

        WatermarkedVideoProcessor videoProcessor = getWatermarkedVideoProcessor(path);

        var result = videoProcessor.process();

        videoProcessor.close();

        Assertions.assertTrue(result.getData().available() > 0);
    }

    private @NotNull WatermarkedVideoProcessor getWatermarkedVideoProcessor(Path path) throws IOException {
        VideoRecorderCustomizer<FrameRecorder> customizer = (videoRecorder, grabber) -> {
            videoRecorder.setAudioBitrate(grabber.getAudioBitrate());
            videoRecorder.setVideoBitrate(grabber.getVideoBitrate());
        };

        // Original - width: 1080, height: 1920
        WatermarkedVideoProcessor videoProcessor = new WatermarkedVideoProcessor("classpath:files/Original.mp4", path.toString(), WatermarkPosition.CENTER, List.of(customizer));

        videoProcessor.watermarked("classpath:files/WatermarkLogo.svg", 1080, 1920);

        return videoProcessor;
    }
}
