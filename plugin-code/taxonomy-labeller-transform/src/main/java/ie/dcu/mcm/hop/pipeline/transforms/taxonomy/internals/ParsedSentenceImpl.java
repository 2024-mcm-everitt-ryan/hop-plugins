package ie.dcu.mcm.hop.pipeline.transforms.taxonomy.internals;

import edu.stanford.nlp.simple.Sentence;

import java.util.ArrayList;
import java.util.List;

import static java.lang.Character.isLetterOrDigit;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.StringUtils.*;

public final class ParsedSentenceImpl implements ParsedSentence {

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
    private final Taxonomy[] taxonomies;

    public ParsedSentenceImpl(Object[] inputRow, Sentence sentence, Taxonomy[] taxonomies, boolean ignoreCase) {
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

        // Words are only those that contain at least one digit or letter.  Others (e.g. punctuations are excluded.)
        //this.wordCount = this.originalWords.size();
        this.wordCount = this.originalWords.stream().filter(this::isWord).count();


        List<Taxonomy> positiveMatchTerms = new ArrayList<>();
        for (Taxonomy taxonomy : taxonomies) {
            long startIndex = taxonomy.indexWithin(this.searchWords);
            if (startIndex != -1L) {
                // Positive match
                //long endIndex = startIndex + taxonomy.getWordCount();

                Taxonomy term = taxonomy.copy(startIndex);
                positiveMatchTerms.add(term);
            }
        }

        this.taxonomies = positiveMatchTerms.toArray(new Taxonomy[0]);

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
    public Taxonomy[] getTaxonomies() {
        return taxonomies;
    }
}
