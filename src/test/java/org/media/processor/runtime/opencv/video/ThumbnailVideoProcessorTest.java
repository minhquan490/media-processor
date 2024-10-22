package org.media.processor.runtime.opencv.video;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.UUID;

class ThumbnailVideoProcessorTest {

    @Test
    void test() throws IOException {
        int widthAndHeight = 250;
        Path path = Path.of(UUID.randomUUID() + ".jpeg");
        Files.createFile(path);

        ThumbnailVideoProcessor processor = new ThumbnailVideoProcessor(
                "classpath:files/Original.mp4",
                path.toString(),
                Collections.emptyList()
        );

        processor.thumbnail(widthAndHeight, widthAndHeight);

        var result = processor.process();

        processor.close();

        InputStream data = result.getData();
        data.reset();
        BufferedImage image = ImageIO.read(data);

        Assertions.assertEquals(widthAndHeight, image.getWidth());
        Assertions.assertEquals(widthAndHeight, image.getHeight());

        Files.deleteIfExists(path);
    }
}
