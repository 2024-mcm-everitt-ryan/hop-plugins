package ie.dcu.mcm.hop.pipeline.transforms.bias.sentences;

import edu.stanford.nlp.simple.Document;
import edu.stanford.nlp.simple.Sentence;
import org.apache.commons.collections4.Bag;

import java.util.ArrayList;
import java.util.List;

public interface ParsedSentence {

    Object[] getInputRow();

    boolean isIgnoreCase();

    String getOriginalText();

    List<String> getOriginalWords();

    String getSearchText();

    List<String> getSearchWords();

    List<String> getSentencePosTags();

    Bag<String> getSentencePosBag();

    List<TaxonomyTerm> getPositiveMatchTerms();

    long getIndex();

    long getIndexBegin();

    long getIndexEnd();

    long getCharacterCount();

    long getWordCount();

    String getOriginalTextPosTagged();

    final class Builder {
        private boolean ignoreCase;
        private boolean groupSentences;
        private boolean sentencesOnly;
        private TaxonomyTerm[] taxonomyTerms;
        private SubjectivityLexicon[] subjectivityLexicons;

        private Builder() {
        }

        public static Builder parsedSentenceBuilder() {
            return new Builder();
        }

        public Builder ignoreCase(boolean ignoreCase) {
            this.ignoreCase = ignoreCase;
            return this;
        }

        public Builder groupSentences(boolean groupSentences) {
            this.groupSentences = groupSentences;
            return this;
        }

        public Builder sentencesOnly(boolean sentencesOnly) {
            this.sentencesOnly = sentencesOnly;
            return this;
        }

        public Builder taxonomyTerms(TaxonomyTerm[] taxonomyTerms) {
            this.taxonomyTerms = taxonomyTerms;
            return this;
        }

        public Builder subjectivityLexicons(SubjectivityLexicon[] subjectivityLexicons) {
            this.subjectivityLexicons = subjectivityLexicons;
            return this;
        }

        public ParsedSentence buildSentence(Object[] inputRow, Sentence sentence) {
            if (this.sentencesOnly) {
                return new ParsedSentenceOnly(inputRow, sentence, ignoreCase);
            } else if (this.groupSentences) {
                return new ParsedGroupedSentence(taxonomyTerms, inputRow, sentence, ignoreCase);
            } else {
                return new ParsedSentencePerTerm(taxonomyTerms,subjectivityLexicons, inputRow, sentence, ignoreCase);
            }
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
