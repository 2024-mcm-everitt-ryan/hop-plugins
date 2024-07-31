package ie.dcu.mcm.hop.pipeline.transforms.taxonomy.internals;

import edu.stanford.nlp.simple.Document;
import edu.stanford.nlp.simple.Sentence;
import org.apache.commons.collections4.MultiValuedMap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public interface Phrase {

    Object[] getInputRow();

    boolean isIgnoreCase();

    long getIndex();

    long getCharacterCount();

    long getWordCount();

    void setSeries(long series);

    MultiValuedMap<String, Taxonomy> getTaxonomies();

    long getMaxPhraseWordCount();

    void addSentence(ParsedSentence sentence);

    Collection<String> getTerms(String category);

    String getText();

    long getSeries();

    final class Builder {
        private boolean ignoreCase;
        private long maxPhraseWordCount;
        private Taxonomy[] taxonomies;

        private Builder() {
        }

        public static Builder phraseBuilder() {
            return new Builder();
        }

        public Builder ignoreCase(boolean ignoreCase) {
            this.ignoreCase = ignoreCase;
            return this;
        }

        public Builder maxPhraseWordCount(long maxPhraseWordCount) {
            this.maxPhraseWordCount = maxPhraseWordCount;
            return this;
        }

        public Builder taxonomies(Taxonomy[] taxonomies) {
            this.taxonomies = taxonomies;
            return this;
        }

        public ParsedSentence buildSentence(Object[] inputRow, Sentence sentence) {
            return new ParsedSentenceImpl(inputRow, sentence, taxonomies, ignoreCase);
        }

        public Phrase buildPhrase(Object[] inputRow, long phraseSeries) {
            return new PhraseImpl(inputRow, phraseSeries, maxPhraseWordCount, ignoreCase);
        }

        public List<Phrase> buildPhrases(Object[] inputRow, String corpus) {
            Document doc = new Document(corpus);
            List<Sentence> sentences = doc.sentences();
            List<Phrase> phrases = new ArrayList<>();
            Iterator<Sentence> iterator = sentences.iterator();
            long wordCount = 0L;
            long phraseIndex = -1L;
            Phrase phrase = null;
            while (iterator.hasNext()) {
                ParsedSentence s = buildSentence(inputRow, iterator.next());
                if (phrase == null || wordCount + s.getWordCount() > maxPhraseWordCount) {
                    phraseIndex++;
                    phrase = buildPhrase(inputRow, phraseIndex);
                    phrases.add(phrase);
                }
                phrase.addSentence(s);
                wordCount = phrase.getWordCount();
            }

            long series = phrases.size();
            for (Phrase p : phrases) {
                p.setSeries(series);
            }

            return phrases;
        }
    }
}
