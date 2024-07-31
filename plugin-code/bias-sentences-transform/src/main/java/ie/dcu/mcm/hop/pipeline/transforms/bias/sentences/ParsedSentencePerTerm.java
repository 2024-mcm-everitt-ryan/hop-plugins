package ie.dcu.mcm.hop.pipeline.transforms.bias.sentences;

import edu.stanford.nlp.process.Stemmer;
import edu.stanford.nlp.simple.Sentence;
import org.apache.commons.collections4.Bag;
import org.apache.commons.collections4.bag.HashBag;

import java.util.ArrayList;
import java.util.List;

import static java.util.Collections.emptyList;
import static java.util.Collections.unmodifiableList;
import static java.util.List.of;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.StringUtils.*;

public final class ParsedSentencePerTerm implements ParsedSentence {

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

    private final List<TaxonomyTerm> positiveMatchTerms;
    private final String originalTextPosTagged;
    private final String previousPos1;
    private final String previousPos2;
    private final String nextPos1;
    private final String nextPos2;
    private final boolean subjectivityInContext;

    ParsedSentencePerTerm(TaxonomyTerm[] taxonomyTerms, SubjectivityLexicon[] subjectivityLexicons, Object[] inputRow, Sentence sentence, boolean ignoreCase) {
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
        String previousPos1 = null;
        String previousPos2 = null;
        String nextPos1 = null;
        String nextPos2 = null;
        boolean subjectivityInContext = false;

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

                // Determine the neighbours (1 and 2)
                // TODO if neighbour is not noun/verb/adverb/adjective then keep searching out.
                int idx = (int) (startIndex - 2);
                Stemmer stemmer = new Stemmer();
                if (idx >= 0) {
                    previousPos2 = sentencePosTags.get(idx);
                    String w = searchWords.get(idx);
                    SubjectivityLexicon t = new SingleWordTerm(w).findInTermArray(subjectivityLexicons);
                    t = new SubjectivityLexicon(t, of(previousPos2), of(stemmer.stem(w)));
                    if (t.isStrong() || t.isWeak()) {
                        subjectivityInContext = true;
                    }
                }

                idx = (int) (startIndex - 1);
                if (idx >= 0) {
                    previousPos1 = sentencePosTags.get(idx);
                    String w = searchWords.get(idx);
                    SubjectivityLexicon t = new SingleWordTerm(w).findInTermArray(subjectivityLexicons);
                    t = new SubjectivityLexicon(t, of(previousPos1), of(stemmer.stem(w)));
                    if (t.isStrong() || t.isWeak()) {
                        subjectivityInContext = true;
                    }
                }

                idx = (int) (endIndex + 1);
                if (idx < sentencePosTags.size()) {
                    nextPos2 = sentencePosTags.get(idx);
                    String w = searchWords.get(idx);
                    SubjectivityLexicon t = new SingleWordTerm(w).findInTermArray(subjectivityLexicons);
                    t = new SubjectivityLexicon(t, of(nextPos2), of(stemmer.stem(w)));
                    if (t.isStrong() || t.isWeak()) {
                        subjectivityInContext = true;
                    }
                }
                idx = (int) endIndex;
                if (idx < sentencePosTags.size()) {
                    nextPos1 = sentencePosTags.get(idx);
                    String w = searchWords.get(idx);
                    SubjectivityLexicon t = new SingleWordTerm(w).findInTermArray(subjectivityLexicons);
                    t = new SubjectivityLexicon(t, of(nextPos1), of(stemmer.stem(w)));
                    if (t.isStrong() || t.isWeak()) {
                        subjectivityInContext = true;
                    }
                }
                break;
            }
        }

        this.positiveMatchTerms = unmodifiableList(positiveMatchTerms);
        this.sentencePosTags = unmodifiableList(sentencePosTags);
        List<String> originalTextPosTagged = new ArrayList<>(this.originalWords);
        for (int i = 0; i < this.sentencePosTags.size(); i++) {
            String pos = sentencePosTags.get(i);
            originalTextPosTagged.set(i, originalTextPosTagged.get(i) + "{" + pos + "}");
        }
        this.originalTextPosTagged = join(originalTextPosTagged, " ");

        this.previousPos1 = previousPos1;
        this.previousPos2 = previousPos2;
        this.nextPos1 = nextPos1;
        this.nextPos2 = nextPos2;
        this.subjectivityInContext = subjectivityInContext;
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
        return positiveMatchTerms;
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

    public String getPreviousPos1() {
        return previousPos1;
    }

    public String getPreviousPos2() {
        return previousPos2;
    }

    public String getNextPos1() {
        return nextPos1;
    }

    public String getNextPos2() {
        return nextPos2;
    }

    public boolean isSubjectivityInContext() {
        return subjectivityInContext;
    }
}
