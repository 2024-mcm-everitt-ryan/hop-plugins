package ie.dcu.mcm.hop.pipeline.transforms.llm.embeddings.internals;

import ie.dcu.mcm.hop.pipeline.transforms.llm.embeddings.LanguageModelEmbeddingsMeta.ContextMeasurementType;

import java.util.Collection;

public interface ParsedContext {
    void append(Iterable<ParsedSentence> sentences) throws ContextFullException;

    void append(ParsedSentence... sentences) throws ContextFullException;

    ContextMeasurementType getMeasurementType();

    long getLeftContextSize();

    long getLeftContextMax();

    long getRightContextSize();

    long getRightContextMax();

    String getContext();

    Collection<Taxonomy> getTaxonomies();

    long getLeftCurrentSize();

    long getRightCurrentSize();

    long getContextSize();

    void buildContext() throws ContextFullException;
}
