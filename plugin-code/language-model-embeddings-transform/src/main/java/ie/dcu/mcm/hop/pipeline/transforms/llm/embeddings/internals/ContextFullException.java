package ie.dcu.mcm.hop.pipeline.transforms.llm.embeddings.internals;

public class ContextFullException extends Exception {
    private final ParsedContext context;

    public ContextFullException(ParsedContext context) {
        this.context = context;
    }

    public ParsedContext getContext() {
        return context;
    }
}
