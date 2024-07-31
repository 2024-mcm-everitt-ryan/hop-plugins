package ie.dcu.mcm.hop.pipeline.transforms.extractor.internals;

import edu.stanford.nlp.simple.Document;
import edu.stanford.nlp.simple.Sentence;

import java.util.ArrayList;
import java.util.List;

public interface ParsedSentence {

    Object[] getInputRow();

    boolean isIgnoreCase();

    String getOriginalText();

    List<String> getOriginalWords();

    String getSearchText();

    List<String> getSearchWords();

    long getIndex();

    long getIndexBegin();

    long getIndexEnd();

    long getCharacterCount();

    long getWordCount();

    Taxonomy[] getTaxonomies();

    final class Builder {
        private boolean ignoreCase;
        private Taxonomy[] taxonomies;

        private Builder() {
        }

        public static Builder parsedSentenceBuilder() {
            return new Builder();
        }

        public Builder ignoreCase(boolean ignoreCase) {
            this.ignoreCase = ignoreCase;
            return this;
        }


        public Builder taxonomies(Taxonomy[] taxonomies) {
            this.taxonomies = taxonomies;
            return this;
        }

        public ParsedSentence buildSentence(Object[] inputRow, Sentence sentence) {
            return new ParsedSentenceImpl(inputRow, sentence, taxonomies, ignoreCase);
        }

        public List<ParsedSentence> buildSentences(Object[] inputRow, String corpus) {
            Document doc = new Document(corpus);
            List<Sentence> sentences = doc.sentences();
            List<ParsedSentence> parsedSentences = new ArrayList<>(sentences.size());
            for (Sentence sentence : sentences) {
                parsedSentences.add(buildSentence(inputRow, sentence));
            }

            return parsedSentences;
        }
    }
}
