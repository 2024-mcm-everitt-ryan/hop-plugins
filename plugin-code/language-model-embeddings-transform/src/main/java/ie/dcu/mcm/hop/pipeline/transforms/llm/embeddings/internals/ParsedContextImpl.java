package ie.dcu.mcm.hop.pipeline.transforms.llm.embeddings.internals;

import ie.dcu.mcm.hop.pipeline.transforms.llm.embeddings.LanguageModelEmbeddingsMeta.ContextMeasurementType;
import org.apache.commons.collections4.queue.CircularFifoQueue;

import java.util.*;

import static java.util.Arrays.asList;

public class ParsedContextImpl implements ParsedContext {

    private final ContextMeasurementType measurementType;
    private final long leftContextSize;
    private final long leftContextMax;
    private final long rightContextSize;
    private final long rightContextMax;
    private final Collection<Taxonomy> taxonomies = new TreeSet<>();
    private final Queue<ParsedSentence> leftContextSentences;
    private final Queue<ParsedSentence> rightContextSentences;
    private StringBuilder context;
    private ParsedSentence middleContextSentence;
    private long leftCurrentSize = 0;
    private long rightCurrentSize = 0;
    private long middleCurrentSize = 0;

    public ParsedContextImpl(ContextMeasurementType measurementType, long leftContextSize, long leftContextMax, long rightContextSize, long rightContextMax) {
        this.measurementType = measurementType;
        this.leftContextSize = leftContextSize;
        this.leftContextMax = leftContextMax;
        this.rightContextSize = rightContextSize;
        this.rightContextMax = rightContextMax;

        leftContextSentences = new CircularFifoQueue<>((int) this.leftContextMax);
        rightContextSentences = new CircularFifoQueue<>((int) this.rightContextMax);
    }

    @Override
    public void append(Iterable<ParsedSentence> sentences) throws ContextFullException {
        for (ParsedSentence sentence : sentences) {
            append(sentence);
        }
    }

    @Override
    public void append(ParsedSentence... sentences) throws ContextFullException {
        for (ParsedSentence sentence : sentences) {
            append(sentence);
        }
    }

    private void append(ParsedSentence sentence) throws ContextFullException {
        if (middleContextSentence == null && sentence.getTaxonomies().length > 0) {
            // Set the positive match as the middle
            middleContextSentence = sentence;
            taxonomies.addAll(asList(sentence.getTaxonomies()));
        } else if (middleContextSentence == null) {
            // append left
            leftContextSentences.add(sentence);
        } else {
            // append right
            rightContextSentences.add(sentence);
        }

        buildContext();
    }

    @Override
    public ContextMeasurementType getMeasurementType() {
        return measurementType;
    }

    @Override
    public long getLeftContextSize() {
        return leftContextSize;
    }

    @Override
    public long getLeftContextMax() {
        return leftContextMax;
    }

    @Override
    public long getRightContextSize() {
        return rightContextSize;
    }

    @Override
    public long getRightContextMax() {
        return rightContextMax;
    }

    @Override
    public Collection<Taxonomy> getTaxonomies() {
        return taxonomies;
    }

    @Override
    public String getContext() {
        if (context == null) {
            try {
                buildContext();
            } catch (ContextFullException e) {
                // Ignore if it's full, we are fetching the final value
            }
        }

        return context == null ? null : context.toString();
    }

    @Override
    public long getLeftCurrentSize() {
        if (context == null) {
            try {
                buildContext();
            } catch (ContextFullException e) {
                // Ignore if it's full, we are fetching the final value
            }
        }

        return leftCurrentSize;
    }

    @Override
    public long getRightCurrentSize() {
        if (context == null) {
            try {
                buildContext();
            } catch (ContextFullException e) {
                // Ignore if it's full, we are fetching the final value
            }
        }

        return rightCurrentSize;
    }

    @Override
    public long getContextSize() {
        if (context == null) {
            try {
                buildContext();
            } catch (ContextFullException e) {
                // Ignore if it's full, we are fetching the final value
            }
        }

        return leftCurrentSize + middleCurrentSize + rightCurrentSize;
    }

    @Override
    public void buildContext() throws ContextFullException {
        // Don't build the context until a positive match has been found
        if (middleContextSentence != null) {

            middleCurrentSize = size(middleContextSentence);

            // Sentence with term is a special sentence, it always gets included whole.
            if (middleCurrentSize > leftContextSize + rightContextSize) {
                // We've reached our limit, don't append.
                throw new ContextFullException(this);
            }

            if (leftCurrentSize + rightCurrentSize >= leftContextSize + rightContextSize) {
                // We've reached our context limit, don't append
                throw new ContextFullException(this);
            }

            // Ensure left,right sizes start from 0
            leftCurrentSize = 0;
            rightCurrentSize = 0;

            // Construct the context
            Iterator<ParsedSentence> left = new LinkedList<>(this.leftContextSentences).descendingIterator();
            Iterator<ParsedSentence> right = new LinkedList<>(this.rightContextSentences).iterator();
            context = new StringBuilder(middleContextSentence.getOriginalText());

            // Fill up the left context to the target
            while (leftCurrentSize < leftContextSize && left.hasNext()) {
                ParsedSentence s = left.next();
                long c = size(s);
                if (c + leftCurrentSize <= leftContextSize) {
                    // Still within the target limits, prepend
                    context.insert(0, s.getOriginalText());
                    context.append(" ");
                    taxonomies.addAll(asList(s.getTaxonomies()));
                }
                leftCurrentSize += c;
            }

            // Fill up the right context to the target
            while (rightCurrentSize < rightContextSize && right.hasNext()) {
                ParsedSentence s = right.next();
                long c = size(s);
                if (c + rightCurrentSize <= rightContextSize) {
                    // Still within the target limits, append
                    context.append(" ");
                    context.append(s.getOriginalText());
                    taxonomies.addAll(asList(s.getTaxonomies()));
                }
                rightCurrentSize += c;
            }

            // Fill up the left context to full context, if the right context is under target.
            long maxContext = leftContextSize + rightContextSize;
            if (rightCurrentSize < rightContextSize) {
                while (leftCurrentSize + rightCurrentSize < maxContext && left.hasNext()) {
                    ParsedSentence s = left.next();
                    long c = size(s);
                    if (c + leftCurrentSize + rightCurrentSize <= maxContext) {
                        // Still within the target limits, prepend
                        context.insert(0, s.getOriginalText());
                        taxonomies.addAll(asList(s.getTaxonomies()));
                    }
                    leftCurrentSize += c;
                }
            }

            if (leftCurrentSize < leftContextSize) {
                while (leftCurrentSize + rightCurrentSize < maxContext && right.hasNext()) {
                    ParsedSentence s = right.next();
                    long c = size(s);
                    if (c + leftCurrentSize + rightCurrentSize <= maxContext) {
                        // Still within the target limits, append
                        context.append(s.getOriginalText());
                        taxonomies.addAll(asList(s.getTaxonomies()));
                    }
                    rightCurrentSize += c;
                }
            }
        }
    }

    private long size(ParsedSentence sentence) {
        switch (this.measurementType) {
            case WORD:
                return sentence.getWordCount();
            case CHARACTER:
                return sentence.getCharacterCount();
            case SENTENCE:
                return 1;
        }

        return 0;
    }
}
