package ie.dcu.mcm.hop.pipeline.transforms.taxonomy.internals;

import org.apache.commons.collections4.MultiValuedMap;

import java.util.Collection;
import java.util.TreeSet;

import static org.apache.commons.collections4.MultiMapUtils.newSetValuedHashMap;
import static org.apache.commons.lang3.math.NumberUtils.max;

public final class PhraseImpl implements Phrase {

    private final Object[] inputRow;
    private final long index;
    private final long maxPhraseWordCount;
    private final boolean ignoreCase;

    private final StringBuilder text;
    private final MultiValuedMap<String, Taxonomy> taxonomies;
    private long wordCount = 0L;
    private long characterCount = 0L;
    private long series = 0L;

    public PhraseImpl(Object[] inputRow, long index, long maxPhraseWordCount, boolean ignoreCase) {
        this.inputRow = inputRow;
        this.ignoreCase = ignoreCase;
        this.index = index;
        this.maxPhraseWordCount = maxPhraseWordCount;
        this.text = new StringBuilder();
        this.taxonomies = newSetValuedHashMap();
    }

    @Override
    public Object[] getInputRow() {
        return inputRow;
    }

    @Override
    public boolean isIgnoreCase() {
        return ignoreCase;
    }

    @Override
    public long getIndex() {
        return index;
    }

    @Override
    public long getCharacterCount() {
        return characterCount;
    }

    @Override
    public long getWordCount() {
        return wordCount;
    }

    @Override
    public long getMaxPhraseWordCount() {
        return maxPhraseWordCount;
    }

    @Override
    public void addSentence(ParsedSentence sentence) {
        text.append(" ").append(sentence.getOriginalText());
        wordCount += sentence.getWordCount();
        characterCount += sentence.getCharacterCount();
        for (Taxonomy taxonomy : sentence.getTaxonomies()) {
            taxonomies.put(taxonomy.getCategory(), taxonomy);
        }
    }

    @Override
    public void setSeries(long series) {
        this.series = max(0, series);
    }

    @Override
    public MultiValuedMap<String, Taxonomy> getTaxonomies() {
        return taxonomies;
    }

    @Override
    public Collection<String> getTerms(String category) {
        Collection<Taxonomy> t = taxonomies.get(category);
        Collection<String> terms = new TreeSet<>();
        for (Taxonomy taxonomy : t) {
            // Escape any quotes within the term, then wrap the term in quotes.
            String term = "\"" + taxonomy.getOriginalTerm().replace("\"", "\\\"") + "\"";
            terms.add(term);
        }
        return terms;
    }

    @Override
    public String getText() {
        return text.toString();
    }

    @Override
    public long getSeries() {
        return series;
    }
}
