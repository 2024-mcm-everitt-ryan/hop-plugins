package ie.dcu.mcm.hop.pipeline.transforms.bias.sentences;

import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.ArrayList;
import java.util.List;

import static java.lang.String.format;
import static org.apache.commons.lang3.StringUtils.join;
import static org.apache.commons.lang3.builder.ToStringStyle.SHORT_PREFIX_STYLE;

public class TaxonomyTermAnnotation {
    private String term;
    private List<String> tokens;
    private List<String> pos;
    private long startIdx;
    private long endIdx;
    private long tokenCount;


    public String getTerm() {
        return term;
    }

    public void setTerm(String term) {
        this.term = term;
    }

    public List<String> getTokens() {
        return tokens;
    }

    public void setTokens(List<String> tokens) {
        this.tokens = tokens;
    }

    public List<String> getPos() {
        return pos;
    }

    public void setPos(List<String> pos) {
        this.pos = pos;
    }

    public long getStartIdx() {
        return startIdx;
    }

    public void setStartIdx(long startIdx) {
        this.startIdx = startIdx;
    }

    public long getEndIdx() {
        return endIdx;
    }

    public void setEndIdx(long endIdx) {
        this.endIdx = endIdx;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, SHORT_PREFIX_STYLE)
                .append("term", term)
                .append("tokens", tokens)
                .append("pos", pos)
                .append("startIdx", startIdx)
                .append("endIdx", endIdx)
                .append("tokenCount", tokenCount)
                .toString();
    }

    public String toJson() {
        List<String> tokensList = new ArrayList<>(tokens == null ? 0 : tokens.size());
        if (tokens != null) {
            for (String e : tokens) {
                tokensList.add(format("\"%s\"", e));
            }
        }
        List<String> posList = new ArrayList<>(pos == null ? 0 : pos.size());
        if (pos != null) {
            for (String e : pos) {
                posList.add(format("\"%s\"", e));
            }
        }
        return "{" +
                format("\"term\":\"%s\",", term) +
                format("\"tokens\":[%s],", join(tokensList, ",")) +
                format("\"pos\":[%s],", join(posList, ",")) +
                format("\"index_start\":%s,", startIdx) +
                format("\"index_end\":%s,", endIdx) +
                format("\"token_count\":%s,", tokenCount) +
                "}";
    }

    public long getTokenCount() {
        return tokenCount;
    }

    public void setTokenCount(long tokenCount) {
        this.tokenCount = tokenCount;
    }

    public static final class Builder {
        private final TaxonomyTermAnnotation biasTerm;

        private Builder() {
            biasTerm = new TaxonomyTermAnnotation();
        }

        public static Builder biasTermBuilder() {
            return new Builder();
        }

        public Builder term(String term) {
            biasTerm.setTerm(term);
            return this;
        }

        public Builder tokens(List<String> tokens) {
            biasTerm.setTokens(tokens);
            return this;
        }

        public Builder pos(List<String> pos) {
            biasTerm.setPos(pos);
            return this;
        }

        public Builder startIdx(long startIdx) {
            biasTerm.setStartIdx(startIdx);
            return this;
        }

        public Builder endIdx(long endIdx) {
            biasTerm.setEndIdx(endIdx);
            return this;
        }

        public Builder tokenCount(long tokenCount) {
            biasTerm.setTokenCount(tokenCount);
            return this;
        }

        public TaxonomyTermAnnotation build() {
            return biasTerm;
        }
    }
}
