package org.media.processor.runtime.batik;

import org.apache.batik.anim.dom.SAXSVGDocumentFactory;
import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.ImageTranscoder;
import org.apache.batik.transcoder.image.JPEGTranscoder;
import org.apache.batik.transcoder.image.PNGTranscoder;
import org.apache.batik.util.XMLResourceDescriptor;
import org.media.processor.Image;
import org.media.processor.IncorrectTypeException;
import org.media.processor.utils.ResourceUtils;
import org.media.processor.utils.io.ResourceIO;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.svg.SVGDocument;
import org.w3c.dom.svg.SVGSVGElement;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class Svg2ImageConverter {
    private final ResourceIO resourceIO;
    private final String resultExt;

    public Svg2ImageConverter(String svgPath, String resultExt) throws IOException {
        this.resourceIO = ResourceUtils.getResourceAsStream(ensureIsSvg(svgPath));
        this.resultExt = resultExt;
    }

    public Image<InputStream> convertImage(int outputWidth, int outputHeight, int originalWidth, int originalHeight) throws IOException, TranscoderException {
        String parser = XMLResourceDescriptor.getXMLParserClassName();
        SAXSVGDocumentFactory factory = new SAXSVGDocumentFactory(parser);

        SVGDocument svgDocument = factory.createSVGDocument(resourceIO.toString());
        rewriteViewBoxAndWidthHeight(svgDocument, originalWidth, originalHeight, outputWidth, outputHeight);

        ImageTranscoder imageTranscoder = createTranscoder();

        ByteArrayOutputStream outputStream = transcode(imageTranscoder, svgDocument);

        return new BatikImage(outputStream, outputWidth, outputHeight, resultExt);
    }

    private ByteArrayOutputStream transcode(ImageTranscoder imageTranscoder, SVGDocument svgDocument) throws TranscoderException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        TranscoderOutput output = new TranscoderOutput(outputStream);
        TranscoderInput input = new TranscoderInput(svgDocument);
        imageTranscoder.transcode(input, output);
        return outputStream;
    }

    private void rewriteViewBoxAndWidthHeight(SVGDocument svgDocument, int originalWidth, int originalHeight, int outputWidth, int outputHeight) {
        SVGSVGElement root = svgDocument.getRootElement();
        root.setAttribute("width", String.valueOf(originalWidth));
        root.setAttribute("height", String.valueOf(originalHeight));

        float scaleVal = calculateScale(originalWidth, outputWidth, originalHeight, outputHeight);

        float minX = ((float) outputWidth / 2) - (125f * scaleVal);
        float minY = (float) ((double) outputHeight / 2) - (100f * scaleVal);

        root.setAttribute("viewBox", -minY + " " + -minX + " " + (float) outputHeight + " " + (float) outputWidth);

        NodeList gTagList = root.getElementsByTagName("g");

        for (int i = 0; i < gTagList.getLength(); i++) {
            Element gTag = (Element) gTagList.item(i);
            gTag.setAttribute("transform", "scale(" + scaleVal + ")");
        }
    }

    private float calculateScale(int originalWidth, int outputWidth, int originalHeight, int outputHeight) {
        return (float) (1.0 + (((float) originalWidth / (float) outputWidth) + ((float) originalHeight / (float) outputHeight)) / 2f);
    }

    private ImageTranscoder createTranscoder() {
        if ("png".equals(resultExt)) {
            return new PNGTranscoder();
        }
        if ("jpeg".equals(resultExt) || "jpg".equals(resultExt)) {
            return new JPEGTranscoder();
        }
        throw new IncorrectTypeException("Unsupported for extension [" + resultExt + "]");
    }

    private String ensureIsSvg(String path) {
        if (path.endsWith("svg")) {
            return path;
        }
        throw new IncorrectTypeException("Input file must be a svg");
    }
}
