package ie.dcu.mcm.hop.pipeline.transforms.llm.embeddings.internals;

import java.util.List;

import static ie.dcu.mcm.hop.pipeline.transforms.llm.embeddings.internals.SubsetIndexFinder.findSubsetIndex;
import static java.util.Arrays.binarySearch;
import static org.apache.commons.lang3.StringUtils.compare;

public interface SearchTerm extends Comparable<SearchTerm> {


    String getOriginalTerm();

    List<String> getOriginalWords();

    String getSearchTerm();

    List<String> getSearchWords();

    default int compareTo(SearchTerm that) {
        return compare(this.getSearchTerm(), that.getSearchTerm());
    }

    default <T extends SearchTerm> T findInTermArray(T[] terms) {
        int idx = binarySearch(terms, this);
        return idx < 0 ? null : terms[idx];
    }

    default int indexWithin(List<String> corpus) {
        // return indexOfSubList(corpus, this.getSearchWords());
        return findSubsetIndex(corpus, this.getSearchWords());
    }
}
