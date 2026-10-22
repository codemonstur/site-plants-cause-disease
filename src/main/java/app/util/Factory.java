package app.util;

import com.googlecode.htmlcompressor.compressor.HtmlCompressor;

public enum Factory {;

    public static HtmlCompressor newHtmlCompressor() {
        final HtmlCompressor compressor = new HtmlCompressor();
        compressor.setRemoveComments(true);
        compressor.setRemoveIntertagSpaces(true);
        return compressor;
    }

}
