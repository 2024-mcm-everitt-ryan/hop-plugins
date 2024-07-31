package ie.dcu.mcm.hop.pipeline.transforms.bias.sentences;

import edu.stanford.nlp.process.Stemmer;
import edu.stanford.nlp.simple.Sentence;
import org.apache.commons.collections4.Bag;
import org.apache.commons.collections4.bag.HashBag;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static java.util.Collections.emptyList;
import static java.util.Collections.unmodifiableList;
import static org.apache.commons.lang3.StringUtils.*;
import static org.apache.commons.lang3.builder.ToStringStyle.JSON_STYLE;

public final class TaxonomyTerm implements SearchTerm {

    private final String category;
    private final String originalTerm;
    private final List<String> originalWords;
    private final String searchTerm;
    private final List<String> searchWords;
    private final long wordCount;
    private final long characterCount;
    private final boolean ignoreCase;
    private final Bag<String> termPosBag = new HashBag<>();
    private final List<String> termPosTags;
    private final long termStartIndex;
    private final long termEndIndex;
    private final SubjectivityLexicon subjectivity;
    private final String originalTermPosTagged;
    private final List<String> stems;

    public TaxonomyTerm(Map<String, Object> tuple, SubjectivityLexicon[] subjectivityLexicons, boolean ignoreCase) {
        this(
                (String) tuple.get("category"),
                (String) tuple.get("term"),
                subjectivityLexicons,
                ignoreCase
        );
    }

    public TaxonomyTerm(String category, String originalTerm, SubjectivityLexicon[] subjectivityLexicons, boolean ignoreCase) {
        this.category = category;
        this.originalTerm = originalTerm;
        this.ignoreCase = ignoreCase;
        this.searchTerm = ignoreCase ? lowerCase(originalTerm) : originalTerm;
        this.characterCount = length(originalTerm);
        this.originalWords = new Sentence(originalTerm).words();
        this.searchWords = ignoreCase ? new Sentence(searchTerm).words() : this.originalWords;
        this.wordCount = this.originalWords.size();
        this.termStartIndex = -1L;
        this.termEndIndex = -1L;
        this.termPosTags = emptyList();
        this.subjectivity = this.findInTermArray(subjectivityLexicons);
        this.originalTermPosTagged = null;
        List<String> stems = new ArrayList<>(this.searchWords);
        Stemmer stemmer = new Stemmer(); // TODO, review because not matching subjectivity lexicon
        stems.replaceAll(stemmer::stem);
        this.stems = unmodifiableList(stems);
    }

    private TaxonomyTerm(String category,
                         String originalTerm,
                         boolean ignoreCase,
                         String searchTerm,
                         long characterCount,
                         List<String> originalWords,
                         List<String> searchWords,
                         long wordCount,
                         long termStartIndex,
                         List<String> termPosTags,
                         SubjectivityLexicon subjectivity,
                         List<String> stems) {
        this.category = category;
        this.originalTerm = originalTerm;
        this.ignoreCase = ignoreCase;
        this.searchTerm = searchTerm;
        this.characterCount = characterCount;
        this.originalWords = originalWords;//unmodifiableList(originalWords);
        this.searchWords = searchWords;//unmodifiableList(searchWords);
        this.wordCount = wordCount;
        this.termStartIndex = termStartIndex;
        this.termEndIndex = this.termStartIndex + this.wordCount;
        this.termPosTags = termPosTags;
        this.termPosBag.addAll(this.termPosTags);
        this.stems = stems;
        this.subjectivity = new SubjectivityLexicon(subjectivity, termPosTags, stems);


        List<String> originalTermPosTagged = new ArrayList<>(this.originalWords);
        for (int i = 0; i < this.termPosTags.size(); i++) {
            originalTermPosTagged.set(i, originalTermPosTagged.get(i) + "{" + termPosTags.get(i) + "}");
        }
        this.originalTermPosTagged = join(originalTermPosTagged, " ");
    }

    public TaxonomyTerm copy(long termStartIndex, List<String> termPosTags) {
        return new TaxonomyTerm(this.category,
                this.originalTerm,
                this.ignoreCase,
                this.searchTerm,
                this.characterCount,
                this.originalWords,
                this.searchWords,
                this.wordCount,
                termStartIndex,
                termPosTags,
                this.subjectivity,
                this.stems);
    }


    public String getCategory() {
        return category;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (!(o instanceof TaxonomyTerm)) return false;

        TaxonomyTerm that = (TaxonomyTerm) o;

        return new EqualsBuilder()
                .append(searchTerm, that.searchTerm)
                .append(category, that.category)
                .isEquals();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, JSON_STYLE)
                .append("category", category)
                .append("originalTerm", originalTerm)
                .append("originalWords", originalWords)
                .append("searchTerm", searchTerm)
                .append("searchWords", searchWords)
                .append("wordCount", wordCount)
                .append("characterCount", characterCount)
                .append("ignoreCase", ignoreCase)
                .toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(searchTerm)
                .append(category)
                .toHashCode();
    }

    @Override
    public int compareTo(SearchTerm that) {
        int c = StringUtils.compare(this.getSearchTerm(), that.getSearchTerm());
        if (c == 0 && that instanceof TaxonomyTerm) {
            c = StringUtils.compare(this.getCategory(), ((TaxonomyTerm) that).getCategory());
        }

        return c;
    }

    public Bag<String> getTermPosBag() {
        return termPosBag;
    }

    public List<String> getTermPosTags() {
        return termPosTags;
    }

    public long getTermStartIndex() {
        return termStartIndex;
    }

    public long getTermEndIndex() {
        return termEndIndex;
    }

    public SubjectivityLexicon getSubjectivity() {
        return subjectivity;
    }

    public String getOriginalTermPosTagged() {
        return originalTermPosTagged;
    }

    public List<String> getStems() {
        return stems;
    }
}
