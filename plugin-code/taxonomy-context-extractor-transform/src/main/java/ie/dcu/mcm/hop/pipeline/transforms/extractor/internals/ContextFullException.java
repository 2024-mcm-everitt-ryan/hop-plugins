package ie.dcu.mcm.hop.pipeline.transforms.extractor.internals;

public class ContextFullException extends Exception {
    private final ParsedContext context;

    public ContextFullException(ParsedContext context) {
        this.context = context;
    }

    public ParsedContext getContext() {
        return context;
    }
}
