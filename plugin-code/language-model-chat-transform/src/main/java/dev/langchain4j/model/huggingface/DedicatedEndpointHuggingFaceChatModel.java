package dev.langchain4j.model.huggingface;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.huggingface.client.*;
import dev.langchain4j.model.huggingface.spi.HuggingFaceClientFactory;
import dev.langchain4j.model.output.Response;

import java.time.Duration;
import java.util.List;

import static dev.langchain4j.internal.Utils.isNullOrBlank;
import static dev.langchain4j.model.huggingface.HuggingFaceModelName.TII_UAE_FALCON_7B_INSTRUCT;
import static java.util.stream.Collectors.joining;

public class DedicatedEndpointHuggingFaceChatModel implements ChatLanguageModel {

    private final HuggingFaceClient client;
    private final Double temperature;
    private final Integer maxNewTokens;
    private final Boolean returnFullText;
    private final Boolean waitForModel;

    public DedicatedEndpointHuggingFaceChatModel(String accessToken,
                                                 String endpointUrl,
                                                 Duration timeout,
                                                 Double temperature,
                                                 Integer maxNewTokens,
                                                 Boolean returnFullText,
                                                 Boolean waitForModel) {
        this(DedicatedEndpointHuggingFaceChatModel.builder()
                .accessToken(accessToken)
                .endpointUrl(endpointUrl)
                .timeout(timeout)
                .temperature(temperature)
                .maxNewTokens(maxNewTokens)
                .returnFullText(returnFullText)
                .waitForModel(waitForModel));
    }

    public DedicatedEndpointHuggingFaceChatModel(Builder builder) {
        this.client = DedicatedEndpointFactoryCreator.FACTORY.create(new HuggingFaceClientFactory.Input() {
            @Override
            public String apiKey() {
                return builder.accessToken;
            }

            @Override
            public String modelId() {
                return builder.endpointUrl;
            }

            @Override
            public Duration timeout() {
                return builder.timeout;
            }
        });
        this.temperature = builder.temperature;
        this.maxNewTokens = builder.maxNewTokens;
        this.returnFullText = builder.returnFullText;
        this.waitForModel = builder.waitForModel;
    }

    @Override
    public Response<AiMessage> generate(List<ChatMessage> messages) {

        TextGenerationRequest request = TextGenerationRequest.builder()
                .inputs(messages.stream()
                        .map(ChatMessage::text)
                        .collect(joining("\n")))
                .parameters(Parameters.builder()
                        .temperature(temperature)
                        .maxNewTokens(maxNewTokens)
                        .returnFullText(returnFullText)
                        .build())
                .options(Options.builder()
                        .waitForModel(waitForModel)
                        .build())
                .build();

        TextGenerationResponse textGenerationResponse = client.chat(request);

        return Response.from(AiMessage.from(textGenerationResponse.generatedText()));
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {

        private String accessToken;
        private String endpointUrl = TII_UAE_FALCON_7B_INSTRUCT;
        private Duration timeout = Duration.ofSeconds(15);
        private Double temperature;
        private Integer maxNewTokens;
        private Boolean returnFullText = false;
        private Boolean waitForModel = true;

        public Builder accessToken(String accessToken) {
            this.accessToken = accessToken;
            return this;
        }

        public Builder endpointUrl(String endpointUrl) {
            if (endpointUrl != null) {
                this.endpointUrl = endpointUrl;
            }
            return this;
        }

        public Builder timeout(Duration timeout) {
            if (timeout != null) {
                this.timeout = timeout;
            }
            return this;
        }

        public Builder temperature(Double temperature) {
            this.temperature = temperature;
            return this;
        }

        public Builder maxNewTokens(Integer maxNewTokens) {
            this.maxNewTokens = maxNewTokens;
            return this;
        }

        public Builder returnFullText(Boolean returnFullText) {
            if (returnFullText != null) {
                this.returnFullText = returnFullText;
            }
            return this;
        }

        public Builder waitForModel(Boolean waitForModel) {
            if (waitForModel != null) {
                this.waitForModel = waitForModel;
            }
            return this;
        }

        public DedicatedEndpointHuggingFaceChatModel build() {
            if (isNullOrBlank(accessToken)) {
                throw new IllegalArgumentException("HuggingFace access token must be defined. It can be generated here: https://huggingface.co/settings/tokens");
            }
            return new DedicatedEndpointHuggingFaceChatModel(this);
        }
    }

    public static DedicatedEndpointHuggingFaceChatModel withAccessToken(String accessToken) {
        return builder().accessToken(accessToken).build();
    }
}
