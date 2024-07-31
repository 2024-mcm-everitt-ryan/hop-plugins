package ie.dcu.mcm.hop.pipeline.transforms.bias.sentences;

import edu.stanford.nlp.simple.Sentence;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.List;
import java.util.Map;

import static java.util.Collections.emptyList;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;
import static org.apache.commons.lang3.BooleanUtils.toBoolean;
import static org.apache.commons.lang3.StringUtils.*;
import static org.apache.commons.lang3.builder.ToStringStyle.SHORT_PREFIX_STYLE;
import static ie.dcu.mcm.hop.pipeline.transforms.bias.sentences.PennTreebankPartOfSpeech.*;

// https://github.com/IonescuCostin/MPQA_subjectivity_lexicon/tree/main
public final class SubjectivityLexicon implements SearchTerm {
    //private final String word; // token or stem of the clue
    private final String type; // either strongsubj or weaksubj
    private final int length; //length of the clue in words
    private final String pos; // part of speech of the clue, may be anypos (any part of speech)
    private final boolean stemmed; //  y (yes) or n (no) Is the clue word1 stemmed?
    // If stemmed1=y, this means that the clue should match all unstemmed variants of the word with the
    // corresponding part of speech. For example, "abuse", above, will match "abuses" (verb), "abused" (verb),
    // "abusing" (verb), but not "abuse" (noun) or "abuses" (noun).
    private final String priorPolarity; // positive, negative, both, neutral The prior polarity of the clue.
    // Out of context, does the clue seem to evoke something positive or something negative.


    private final String originalTerm; // word; token or stem of the clue
    private final List<String> originalWords;
    private final String searchTerm; // word, but used for searching and comparing
    private final List<String> searchWords;
    private final long wordCount;
    private final long characterCount;
    private final boolean ignoreCase;

    private final boolean strong;
    private final boolean weak;

    public SubjectivityLexicon() {
        this(null, null, 0, null, false, null, false);
    }

    public SubjectivityLexicon(Map<String, Object> tuple) {
        this(tuple, true);
    }

    public SubjectivityLexicon(Map<String, Object> tuple, boolean ignoreCase) {
        this(
                (String) tuple.get("word"),
                (String) tuple.get("type"),
                ((Number) tuple.get("length")).intValue(),
                (String) tuple.get("pos"),
                toBoolean((String) tuple.get("stemmed")),
                (String) tuple.get("priorPolarity"),
                ignoreCase
        );
    }

    public SubjectivityLexicon(String word, String type, int length, String pos, boolean stemmed, String priorPolarity, boolean ignoreCase) {
        this.type = type;
        this.length = length;
        this.pos = pos;
        this.stemmed = stemmed;
        this.priorPolarity = priorPolarity;

        this.strong = containsIgnoreCase(type, "strong");
        this.weak = containsIgnoreCase(type, "weak");

        this.originalTerm = word;
        this.ignoreCase = ignoreCase;
        this.searchTerm = ignoreCase ? lowerCase(originalTerm) : originalTerm;
        this.characterCount = length(originalTerm);
        this.originalWords = originalTerm == null ? emptyList() : new Sentence(originalTerm).words();
        if (ignoreCase) {
            this.searchWords = searchTerm == null ? emptyList() : new Sentence(searchTerm).words();
        } else {
            this.searchWords = this.originalWords;
        }
        this.wordCount = this.originalWords.size();

    }

    public SubjectivityLexicon(SubjectivityLexicon subjectivity, List<String> termPosTags, List<String> stems) {

        boolean valid = false;
        if (subjectivity != null && isNotEmpty(termPosTags) && isNotEmpty(stems)) {
            if ("anypos".equals(subjectivity.getPos())) {
                valid = true;
            } else {
                PennTreebankPartOfSpeech pos = PennTreebankPartOfSpeech.valueOf(termPosTags.get(0));
                //TODO check all termPosTags, not only the first one.
                if ("noun".equals(subjectivity.getPos()) && NOUNS.contains(pos)) {
                    valid = true;
                } else if ("adj".equals(subjectivity.getPos()) && ADJECTIVES.contains(pos)) {
                    valid = true;
                } else if ("adverb".equals(subjectivity.getPos()) && ADVERBS.contains(pos)) {
                    valid = true;
                } else if ("verb".equals(subjectivity.getPos()) && VERBS.contains(pos)) {
                    valid = true;
                }
            }
        }

        // TODO Review stemming because lexicon list is not always matching Porter Stemming Algorithm
        /*
        if(valid && subjectivity.isStemmed()) {
            String stem = stems.get(0);
            if (subjectivity.ignoreCase) {
                valid = equalsIgnoreCase(subjectivity.getSearchTerm(), stem);
            } else {
                valid = StringUtils.equals(subjectivity.getSearchTerm(),stem);
            }
        }
         */

        if (valid) {
            this.originalTerm = subjectivity.getOriginalTerm();
            this.originalWords = subjectivity.getOriginalWords();
            this.searchTerm = subjectivity.getSearchTerm();
            this.searchWords = subjectivity.getSearchWords();
            this.ignoreCase = subjectivity.isIgnoreCase();
            this.characterCount = subjectivity.getCharacterCount();
            this.wordCount = subjectivity.getWordCount();
            this.type = subjectivity.getType();
            this.length = subjectivity.getLength();
            this.pos = subjectivity.getPos();
            this.stemmed = subjectivity.isStemmed();
            this.priorPolarity = subjectivity.getPriorPolarity();
            this.strong = subjectivity.isStrong();
            this.weak = subjectivity.isWeak();
        } else {
            this.originalTerm = null;
            this.originalWords = null;
            this.searchTerm = null;
            this.searchWords = null;
            this.ignoreCase = false;
            this.characterCount = 0;
            this.wordCount = 0;
            this.type = null;
            this.length = 0;
            this.pos = null;
            this.stemmed = false;
            this.priorPolarity = null;
            this.strong = false;
            this.weak = false;
        }
    }

    public String getType() {
        return type;
    }

    public int getLength() {
        return length;
    }

    public String getPos() {
        return pos;
    }

    public boolean isStemmed() {
        return stemmed;
    }

    public String getPriorPolarity() {
        return priorPolarity;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (!(o instanceof SubjectivityLexicon)) return false;

        SubjectivityLexicon that = (SubjectivityLexicon) o;

        return new EqualsBuilder()
                .append(searchTerm, that.searchTerm)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(stemmed)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, SHORT_PREFIX_STYLE)
                .append("originalTerm", originalTerm)
                .append("searchTerm", searchTerm)
                .append("type", type)
                .append("length", length)
                .append("pos", pos)
                .append("stemmed", stemmed)
                .append("priorPolarity", priorPolarity)
                .toString();
    }

    @Override
    public String getOriginalTerm() {
        return originalTerm;
    }


    @Override
    public List<String> getOriginalWords() {
        return originalWords;
    }

    @Override
    public String getSearchTerm() {
        return searchTerm;
    }

    @Override
    public List<String> getSearchWords() {
        return searchWords;
    }

    public long getWordCount() {
        return wordCount;
    }

    public long getCharacterCount() {
        return characterCount;
    }

    public boolean isIgnoreCase() {
        return ignoreCase;
    }

    public boolean isStrong() {
        return strong;
    }

    public boolean isWeak() {
        return weak;
    }

    @Override
    public int compareTo(SearchTerm that) {
        if (this.ignoreCase) {
            return compareIgnoreCase(this.getSearchTerm(), that.getSearchTerm());
        } else {
            return compare(this.getSearchTerm(), that.getSearchTerm());
        }
    }
}
