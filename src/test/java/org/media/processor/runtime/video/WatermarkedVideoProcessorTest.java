package org.media.processor.runtime.video;

import org.bytedeco.ffmpeg.global.avutil;
import org.bytedeco.javacv.FFmpegLogCallback;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.media.processor.WatermarkPosition;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
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

        // Original - width: 1080, height: 1920
        WatermarkedVideoProcessor videoProcessor = new WatermarkedVideoProcessor("classpath:files/Original.mp4", path.toString(), WatermarkPosition.TOP_LEFT);

        videoProcessor.watermarked("classpath:files/fog-4436636_1920.jpg", 1279, 1920);

        var result = videoProcessor.process();

        videoProcessor.close();

        Assertions.assertTrue(result.getData().available() > 0);
    }
}
