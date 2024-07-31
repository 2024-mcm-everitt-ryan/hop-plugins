package ie.dcu.mcm.hop.pipeline.transforms.bias.sentences;

import org.apache.commons.lang3.StringUtils;

import java.util.List;

import static java.util.Arrays.binarySearch;
import static ie.dcu.mcm.hop.pipeline.transforms.bias.sentences.SubsetIndexFinder.findSubsetIndex;

public interface SearchTerm extends Comparable<SearchTerm> {


    String getOriginalTerm();

    List<String> getOriginalWords();

    String getSearchTerm();

    List<String> getSearchWords();

    default int compareTo(SearchTerm that) {
        return StringUtils.compare(this.getSearchTerm(), that.getSearchTerm());
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
