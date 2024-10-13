package org.media.processor.runtime.opencv.mat;

import org.media.processor.Image;

import java.io.InputStream;

public abstract class WatermarkedPositionMatProcessor extends WatermarkedMatProcessor {
    protected WatermarkedPositionMatProcessor(Image<InputStream> watermarked) {
        super(watermarked);
    }

    @Override
    protected double getSrcOpacity() {
        return 0.7;
    }

    @Override
    protected double getSrcOpOpacity() {
        return 0.3;
    }
}
