package ie.dcu.mcm.hop.pipeline.transforms.llm.chat;

import org.apache.hop.core.row.IRowMeta;
import org.apache.hop.pipeline.transform.BaseTransformData;
import org.apache.hop.pipeline.transform.ITransformData;

public class LanguageModelChatData extends BaseTransformData implements ITransformData {
    public int indexOfInputField;
    public int indexOfIdentifier;
    public int indexOfOutputFieldNamePrefix;
    public int indexOfModelType;
    public int indexOfModelName;
    public int indexOfFinishReason;
    public int indexOfInputTokenCount;
    public int indexOfOutputTokenCount;
    public int indexOfTotalTokenCount;
    public int indexOfInferenceTime;
    public int indexOfOutput;

    public IRowMeta previousRowMeta;
    public IRowMeta outputRowMeta;
    public int NrPrevFields;

    public LanguageModelChatData() {
        super();
        indexOfInputField = -1;
        indexOfIdentifier = -1;
        indexOfOutputFieldNamePrefix = -1;
        indexOfModelType = -1;
        indexOfModelName = -1;
        indexOfFinishReason = -1;
        indexOfInputTokenCount = -1;
        indexOfOutputTokenCount = -1;
        indexOfTotalTokenCount = -1;
        indexOfInferenceTime = -1;
        indexOfOutput = -1;
    }
}
