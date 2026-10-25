package app.core;

import app.model.Disease;
import com.googlecode.htmlcompressor.compressor.HtmlCompressor;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static app.util.Factory.newHtmlCompressor;
import static app.util.IO.compilePebble;
import static java.nio.charset.StandardCharsets.UTF_8;

public enum Pages {;

    private static final HtmlCompressor COMPRESSOR = newHtmlCompressor();

    public static String newIndexFile(final Disease[] diseases) throws IOException {
        final var list = new ArrayList<Map<String, Object>>();
        for (final var disease : diseases) {
            if (!disease.visible()) continue;
            final var entry = new HashMap<String, Object>();
            entry.put("uri", disease.uri());
            entry.put("name", disease.name());
            list.add(entry);
        }
        final var model = new HashMap<String, Object>();
        model.put("diseases", list);
        return COMPRESSOR.compress(compilePebble("/index.pebble", model));
    }

    public static String newDiseaseFile(final Disease disease) throws IOException, URISyntaxException {
        return COMPRESSOR.compress(compilePebble("/disease.pebble", disease.toModel()));
    }

}
