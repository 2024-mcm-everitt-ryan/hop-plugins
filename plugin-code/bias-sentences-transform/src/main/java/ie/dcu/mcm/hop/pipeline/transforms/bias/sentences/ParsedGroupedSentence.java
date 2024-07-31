package ie.dcu.mcm.hop.pipeline.transforms.bias.sentences;

import edu.stanford.nlp.simple.Sentence;
import org.apache.commons.collections4.Bag;
import org.apache.commons.collections4.bag.HashBag;

import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.binarySearch;
import static java.util.Collections.*;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.StringUtils.*;

public final class ParsedGroupedSentence implements ParsedSentence {


    private final Object[] inputRow;
    private final boolean ignoreCase;
    private final String originalText;
    private final List<String> originalWords;
    private final String searchText;
    private final List<String> searchWords;
    private final long index;
    private final long indexBegin;
    private final long indexEnd;
    private final long characterCount;
    private final long wordCount;
    private final List<String> sentencePosTags;
    private final Bag<String> sentencePosBag = new HashBag<>();

    private final List<TaxonomyTerm> positiveMathTerms;
    private final String originalTextPosTagged;

    ParsedGroupedSentence(TaxonomyTerm[] taxonomyTerms, Object[] inputRow, Sentence sentence, boolean ignoreCase) {
        this.inputRow = inputRow;
        this.ignoreCase = ignoreCase;
        this.originalText = sentence.text();
        this.originalWords = sentence.words();
        this.searchText = ignoreCase ? lowerCase(this.originalText) : this.originalText;
        this.searchWords = ignoreCase ? this.originalWords.stream().map(String::toLowerCase).collect(toList()) : this.originalWords;

        this.index = sentence.sentenceIndex();
        this.indexBegin = sentence.sentenceTokenOffsetBegin();
        this.indexEnd = sentence.sentenceTokenOffsetEnd();
        this.characterCount = length(this.originalText);
        this.wordCount = this.originalWords.size();

        List<String> sentencePosTags = emptyList();
        List<TaxonomyTerm> positiveMatchTerms = new ArrayList<>();
        for (TaxonomyTerm taxonomyTerm : taxonomyTerms) {
            long startIndex = taxonomyTerm.indexWithin(this.searchWords);
            if (startIndex != -1L) {
                // Positive match

                sentencePosTags = sentence.posTags();
                this.sentencePosBag.addAll(sentencePosTags);

                long endIndex = startIndex + taxonomyTerm.getWordCount();
                List<String> termPosTags = sentencePosTags.subList((int) startIndex, (int) endIndex);

                TaxonomyTerm term = taxonomyTerm.copy(startIndex, termPosTags);
                positiveMatchTerms.add(term);
                // Note: no break here unlike sentence-per-term
            }
        }

        this.sentencePosTags = unmodifiableList(sentencePosTags);
        this.positiveMathTerms = unmodifiableList(positiveMatchTerms);

        List<String> originalTextPosTagged = new ArrayList<>(this.originalWords);
        for (int i = 0; i < this.sentencePosTags.size(); i++) {
            originalTextPosTagged.set(i, originalTextPosTagged.get(i) + "{" + sentencePosTags.get(i) + "}");
        }
        this.originalTextPosTagged = join(originalTextPosTagged, " ");
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
    public String getOriginalText() {
        return originalText;
    }

    @Override
    public List<String> getOriginalWords() {
        return originalWords;
    }

    @Override
    public String getSearchText() {
        return searchText;
    }

    @Override
    public List<String> getSearchWords() {
        return searchWords;
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
    public List<TaxonomyTerm> getPositiveMatchTerms() {
        return positiveMathTerms;
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
    public String getOriginalTextPosTagged() {
        return originalTextPosTagged;
    }
}
