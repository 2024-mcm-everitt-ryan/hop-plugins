package ie.dcu.mcm.hop.pipeline.transforms.taxonomy.internals;

import edu.stanford.nlp.simple.Sentence;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.List;
import java.util.Map;

import static org.apache.commons.lang3.StringUtils.*;
import static org.apache.commons.lang3.builder.ToStringStyle.JSON_STYLE;

public final class Taxonomy implements SearchTerm {

    private final String category;
    private final String originalTerm;
    private final List<String> originalWords;
    private final String searchTerm;
    private final List<String> searchWords;
    private final long wordCount;
    private final long characterCount;
    private final boolean ignoreCase;
    private final long termStartIndex;
    private final long termEndIndex;

    public Taxonomy(Map<String, Object> tuple, boolean ignoreCase) {
        this(
                (String) tuple.get("category"),
                (String) tuple.get("term"),
                ignoreCase
        );
    }

    public Taxonomy(String category, String originalTerm, boolean ignoreCase) {
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
    }

    private Taxonomy(String category,
                     String originalTerm,
                     boolean ignoreCase,
                     String searchTerm,
                     long characterCount,
                     List<String> originalWords,
                     List<String> searchWords,
                     long wordCount,
                     long termStartIndex) {
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
    }

    public Taxonomy copy(long termStartIndex) {
        return new Taxonomy(this.category,
                this.originalTerm,
                this.ignoreCase,
                this.searchTerm,
                this.characterCount,
                this.originalWords,
                this.searchWords,
                this.wordCount,
                termStartIndex);
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

        if (!(o instanceof Taxonomy)) return false;

        Taxonomy that = (Taxonomy) o;

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
        int c = compare(this.getSearchTerm(), that.getSearchTerm());
        if (c == 0 && that instanceof Taxonomy) {
            c = compare(this.getCategory(), ((Taxonomy) that).getCategory());
        }

        return c;
    }

    public long getTermStartIndex() {
        return termStartIndex;
    }

    public long getTermEndIndex() {
        return termEndIndex;
    }

}
