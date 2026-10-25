package app.model;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Map.entry;
import static java.util.Map.ofEntries;

public record Disease(boolean visible,
                      String name, String uri,
                      See see, Definition definition,
                      List<String> carnivoreDietSearchQueries,
                      List<String> filteredAnecdotes,
                      List<Anecdote> anecdotes,
                      String text,
                      List<String> papers,
                      List<String> articles,
                      List<String> videos) {

    public Map<String, Object> toModel() throws URISyntaxException {
        final var model = new HashMap<String, Object>();
        model.put("title", name);
        model.put("definition", definition != null ? definition.text() : null);
        model.put("source_uri", definition != null ? definition.source() : null);
        model.put("source_name", definition != null ? toSourceName(definition().source()) : null);
        model.put("see_uri", see != null ? see.uri() : null);
        model.put("see_name", see != null ? see.name() : null);
        final var anecdoteMaps = new ArrayList<Map<String, Object>>();
        if (anecdotes != null) for (final var a : anecdotes) {
            final var m = new HashMap<String, Object>();
            m.put("visible", a.visible());
            m.put("uri", a.uri());
            m.put("title", a.title());
            m.put("date", a.date());
            anecdoteMaps.add(m);
        }
        model.put("anecdotes", anecdoteMaps);
        model.put("filteredAnecdotes", filteredAnecdotes);
        model.put("papers", papers);
        model.put("articles", articles);
        model.put("videos", videos);
        model.put("text", text);
        return model;
    }

    private static final Map<String, String> SOURCE_HOSTNAME_TO_NAME = ofEntries(
        entry("pmc.ncbi.nlm.nih.gov", "National Library of Medicine"),
        entry("www.medicalnewstoday.com", "MedicalNewsToday"),
        entry("www.medicinenet.com", "MedicineNet"),
        entry("my.clevelandclinic.org", "Cleveland Clinic"),
        entry("www.mayoclinic.org", "Mayo Clinic"),
        entry("fascrs.org", "American Society of Colon & Rectal Surgeons"),
        entry("www.mayoclinichealthsystem.org", "Mayo Clinic"),
        entry("www.hopkinsmedicine.org", "Johns Hopkins"),
        entry("www.webmd.com", "WebMD"),
        entry("en.wikipedia.org", "Wikipedia"),
        entry("www.ncbi.nlm.nih.gov", "National Library of Medicine"),
        entry("www.healthline.com", "Healthline"),
        entry("pubmed.ncbi.nlm.nih.gov", "PubMed"),
        entry("www.unicef.org", "Unicef"),
        entry("www.cdc.gov", "Centers for Disease Control")
    );
    public static String toSourceName(final String source) throws URISyntaxException {
        if (source == null || source.isBlank()) return null;
        final var host = new URI(source).getHost();
        return host == null ? null : SOURCE_HOSTNAME_TO_NAME.get(host);
    }

}
