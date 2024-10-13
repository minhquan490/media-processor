package org.media.processor;

import java.io.File;
import java.io.InputStream;

public interface VideoProcessor extends Processor<Video<InputStream>> {
    void watermarked(Image<InputStream> watermarked);

    void watermarked(String watermarkPath, int width, int height);

    void watermarked(File watermark, int width, int height);

    void thumbnail(int width, int height);
}
