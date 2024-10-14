package org.media.processor;

import org.bytedeco.javacv.FrameGrabber;

public interface VideoRecorderCustomizer<T> {
    void customize(T videoRecorder, FrameGrabber grabber);
}
