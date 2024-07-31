package ie.dcu.mcm.hop.pipeline.transforms.llm.chat.internals;

import ie.dcu.mcm.hop.pipeline.transforms.llm.chat.LanguageModelChatMeta;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class LanguageModel {

    private final ModelType type;
    private final String name;

    public LanguageModel(LanguageModelChatMeta meta) {
        this.type = ModelType.valueOf(meta.getModelType());
        switch (this.type) {
            case OPEN_AI:
                name = meta.getOpenAiModelName();
                break;
            case ANTHROPIC:
                name = meta.getAnthropicModelName();
                break;
            case OLLAMA:
                name = meta.getOllamaModelName();
                break;
            case MISTRAL:
                name = meta.getMistralModelName();
                break;
            case HUGGING_FACE:
                name = meta.getHuggingFaceModelId();
                break;
            default:
                throw new IllegalArgumentException("Invalid model type");
        }
    }

    public ModelType getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (!(o instanceof LanguageModel)) return false;

        LanguageModel model = (LanguageModel) o;

        return new EqualsBuilder()
                .append(type, model.type)
                .append(name, model.name)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(type)
                .append(name)
                .toHashCode();
    }
}
