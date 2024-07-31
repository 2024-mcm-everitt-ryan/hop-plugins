package ie.dcu.mcm.hop.pipeline.transforms.llm.chat.internals;

public enum LanguageModelChatModelName {


    // https://platform.openai.com/docs/models/continuous-model-upgrades
    OPENAI_GPT_4O("gpt-4o"),
    OPENAI_GPT_4_TURBO("gpt-4-turbo"), // with vision
    OPENAI_GPT_3_5_TURBO("gpt-3.5-turbo"),

    // https://docs.mistral.ai/getting-started/models/
    OPEN_MISTRAL_7B("open-mistral-7b"),
    OPEN_MIXTRAL_8X7B("open-mixtral-8x7b"),
    MISTRAL_SMALL_LATEST("mistral-small-latest"),
    MISTRAL_MEDIUM_LATEST("mistral-medium-latest"),
    MISTRAL_LARGE_LATEST("mistral-large-latest"),

    // https://ollama.com/library
    OLLAMA_LLAMA3_8B("llama3"),
    OLLAMA_LLAMA3_70B("llama3:70b"),
    OLLAMA_PHI3_3_8B("phi3"),
    OLLAMA_PHI3_14B("phi3:medium"),

    // https://ui.endpoints.huggingface.co/catalog
    HUGGING_FACE_LLAMA3_70B_INSTRUCT("meta-llama/Meta-Llama-3-70B-Instruct"),
    HUGGING_FACE_MISTRAL_7B_INSTRUCT("mistralai/Mistral-7B-Instruct-v0.3"),

    // https://docs.anthropic.com/en/docs/models-overview
    ANTHROPIC_CLAUDE_3_OPUS_20240229("claude-3-opus-20240229"),
    ANTHROPIC_CLAUDE_3_SONNET_20240229("claude-3-sonnet-20240229"),
    ANTHROPIC_CLAUDE_3_HAIKU_20240307("claude-3-haiku-20240307");

    private final String stringValue;

    LanguageModelChatModelName(String stringValue) {
        this.stringValue = stringValue;
    }

    @Override
    public String toString() {
        return stringValue;
    }
}
