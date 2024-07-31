package ie.dcu.mcm.hop.pipeline.transforms.stanford.nlp.simple;

import edu.stanford.nlp.simple.Sentence;
import org.apache.commons.collections4.Bag;
import org.apache.commons.collections4.bag.HashBag;

import java.util.ArrayList;
import java.util.List;

import static java.lang.Character.isLetterOrDigit;
import static java.util.Collections.emptyList;
import static org.apache.commons.lang3.StringUtils.*;

public final class ParsedSentenceImpl implements ParsedSentence {

    private final Object[] inputRow;
    private final String sentenceText;
    private final List<String> sentenceWords;
    private final long index;
    private final long indexBegin;
    private final long indexEnd;
    private final long characterCount;
    private final long wordCount;
    private final List<String> sentencePosTags;
    private final Bag<String> sentencePosBag = new HashBag<>();

    private final String sentenceTextPosTagged;

    public ParsedSentenceImpl(Object[] inputRow, Sentence sentence, boolean pos) {
        this.inputRow = inputRow;

        this.sentenceText = trim(sentence.text());
        this.sentenceWords = sentence.words();

        this.index = sentence.sentenceIndex();
        this.indexBegin = sentence.sentenceTokenOffsetBegin();
        this.indexEnd = sentence.sentenceTokenOffsetEnd();
        this.characterCount = length(this.sentenceText);

        // Words are only those that contain at least one digit or letter.  Others (e.g. punctuations are excluded.)
        //this.wordCount = this.sentenceWords.size();
        this.wordCount = this.sentenceWords.stream().filter(this::isWord).count();

        if (pos) {
            this.sentencePosTags = sentence.posTags();
            this.sentencePosBag.addAll(sentencePosTags);

            List<String> sentenceTextPosTagged = new ArrayList<>(this.sentenceWords);
            for (int i = 0; i < this.sentencePosTags.size(); i++) {
                sentenceTextPosTagged.set(i, sentenceTextPosTagged.get(i) + "{" + sentencePosTags.get(i) + "}");
            }
            this.sentenceTextPosTagged = join(sentenceTextPosTagged, " ");
        } else {
            this.sentencePosTags = emptyList();
            this.sentenceTextPosTagged = null;
        }
    }

    private boolean isWord(String word) {
        word = trim(word);
        if (isBlank(word)) {
            return false;
        }
        for (int i = 0; i < word.length(); i++) {
            char ch = word.charAt(i);
            if (isLetterOrDigit(ch)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public Object[] getInputRow() {
        return inputRow;
    }

    @Override
    public String getSentenceText() {
        return sentenceText;
    }

    @Override
    public List<String> getSentenceWords() {
        return sentenceWords;
    }

    @Override
    public List<String> getSentencePosTags() {
        return sentencePosTags;
    }

    @Override
    public Bag<String> getSentencePosBag() {
        return sentencePosBag;
    }

    @Override
    public long getIndex() {
        return index;
    }

    @Override
    public long getIndexBegin() {
        return indexBegin;
    }

    @Override
    public long getIndexEnd() {
        return indexEnd;
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
    public String getSentenceTextPosTagged() {
        return sentenceTextPosTagged;
    }
}
