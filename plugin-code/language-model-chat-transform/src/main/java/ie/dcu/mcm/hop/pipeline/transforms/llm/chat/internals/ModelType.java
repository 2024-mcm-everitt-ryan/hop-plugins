package ie.dcu.mcm.hop.pipeline.transforms.llm.chat.internals;

import static ie.dcu.mcm.hop.pipeline.transforms.llm.chat.internals.ui.i18nUtil.i18n;
import static org.apache.commons.lang3.StringUtils.equalsIgnoreCase;

public enum ModelType {
    OPEN_AI("OPEN_AI", i18n("LanguageModelChatDialog.ModelType.OPEN_AI")),
    ANTHROPIC("ANTHROPIC", i18n("LanguageModelChatDialog.ModelType.ANTHROPIC")),
    OLLAMA("OLLAMA", i18n("LanguageModelChatDialog.ModelType.OLLAMA")),
    MISTRAL("MISTRAL", i18n("LanguageModelChatDialog.ModelType.MISTRAL")),
    HUGGING_FACE("HUGGING_FACE", i18n("LanguageModelChatDialog.ModelType.HUGGING_FACE"));

    private String code;
    private String description;

    ModelType(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public static ModelType typeFromDescription(String description) {
        for (ModelType type : values()) {
            if (equalsIgnoreCase(type.description, description)) {
                return type;
            }
        }
        return OPEN_AI;
    }

    public static String[] modelTypeDescriptions() {
        ModelType[] types = ModelType.values();
        String[] descriptions = new String[types.length];
        for (int i = 0; i < types.length; i++) {
            descriptions[i] = types[i].description;
        }
        return descriptions;
    }

    public String code() {
        return code;
    }

    public String description() {
        return description;
    }
}
