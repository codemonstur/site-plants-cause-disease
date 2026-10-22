# Plants Cause Disease

A static site generator that produces a website listing diseases caused or worsened
by plant consumption. Each disease is a JSON data file; the generator renders an index
page plus one page per disease.

## Build

```bash
mvn clean package
```

## Run

```bash
# Generate the site into docs/
java -cp target/classes app.Generate

# Validate the data and generated HTML
java -cp target/classes app.Validate
```

## Project layout

- `src/main/resources/disease/*.json` — one JSON file per disease (validated against
  `src/main/resources/schema/disease.json`).
- `src/main/resources/index.pebble` / `disease.pebble` — Pebble templates.
- `src/main/resources/articles.txt` — harvested article URLs (input to the harvesters).
- `src/main/resources/further-research.txt` — harvested research links (input to the harvesters).
- `src/main/resources/known-words.txt` — known words used when discovering new diseases.
- `docs/` — generated site output (`index.html` + `disease/*.html`).
- `.url_cache/` — cached HTTP responses used by the harvesters (large, gitignored).

## Harvesting

`Harvest` and `HarvestCarnivoreDiet` scrape anecdote URLs from carnivore.diet and
print new JSON entries to stdout, which are then added to the disease files by hand.

## License

MIT
