package ie.dcu.mcm.hop.pipeline.transforms.stanford.nlp.simple;

import edu.stanford.nlp.simple.Document;
import edu.stanford.nlp.simple.Sentence;
import org.apache.commons.collections4.Bag;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

public interface ParsedSentence {

    Object[] getInputRow();

    String getSentenceText();

    List<String> getSentenceWords();

    List<String> getSentencePosTags();

    Bag<String> getSentencePosBag();

    long getIndex();

    long getIndexBegin();

    long getIndexEnd();

    long getCharacterCount();

    long getWordCount();

    String getSentenceTextPosTagged();

    final class Builder {
        private boolean includePartOfSpeech;

        private Builder() {
        }

        public static Builder parsedSentenceBuilder() {
            return new Builder();
        }

        public Builder includePartOfSpeech(boolean includePartOfSpeech) {
            this.includePartOfSpeech = includePartOfSpeech;
            return this;
        }

        public ParsedSentence buildSentence(Object[] inputRow, Sentence sentence) {
            return new ParsedSentenceImpl(inputRow, sentence, includePartOfSpeech);
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
