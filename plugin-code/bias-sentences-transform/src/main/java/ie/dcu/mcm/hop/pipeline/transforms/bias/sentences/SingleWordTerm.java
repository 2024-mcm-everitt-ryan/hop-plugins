package ie.dcu.mcm.hop.pipeline.transforms.bias.sentences;

import java.util.List;

public class SingleWordTerm implements SearchTerm {

    private final String term;
    private final List<String> word;

    public SingleWordTerm(String term) {
        this.term = term;
        this.word = List.of(term);
    }

    @Override
    public String getOriginalTerm() {
        return term;
    }

    @Override
    public List<String> getOriginalWords() {
        return word;
    }

    @Override
    public String getSearchTerm() {
        return term;
    }

    @Override
    public List<String> getSearchWords() {
        return word;
    }
}
