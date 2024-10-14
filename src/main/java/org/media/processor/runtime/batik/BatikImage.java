package org.media.processor.runtime.batik;

import org.media.processor.Image;
import org.media.processor.utils.ResourceUtils;
import org.media.processor.utils.io.ResourceIO;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class BatikImage implements Image<InputStream> {
    private final ResourceIO resourceIO;
    private final int width;
    private final int height;

    private double opacity = 0.3d;

    public BatikImage(ByteArrayOutputStream outputStream, int width, int height, String ext) throws IOException {
        this(new ByteArrayInputStream(outputStream.toByteArray()), width, height, ext);
    }

    public BatikImage(InputStream inputStream, int width, int height, String ext) throws IOException {
        if (inputStream instanceof ResourceIO io) {
            this.resourceIO = io;
        } else {
            this.resourceIO = ResourceUtils.wrap(inputStream, ext);
        }
        this.width = width;
        this.height = height;
    }

    @Override
    public String getLocation() {
        return resourceIO.getResource().getAbsolutePath();
    }

    @Override
    public InputStream image() {
        return resourceIO;
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
        return opacity;
    }

    @Override
    public void close() throws IOException {
        resourceIO.close();
    }

    public void setOpacity(double opacity) {
        this.opacity = opacity;
    }
}
