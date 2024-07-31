package ie.dcu.mcm.hop.pipeline.transforms.taxonomy.internals;

import java.util.List;

public interface ParsedSentence {

    Object[] getInputRow();

    boolean isIgnoreCase();

    String getOriginalText();

    List<String> getOriginalWords();

    String getSearchText();

    List<String> getSearchWords();

    long getIndex();

    long getIndexBegin();

    long getIndexEnd();

    long getCharacterCount();

    long getWordCount();

    Taxonomy[] getTaxonomies();
}
