package ie.dcu.mcm.hop.pipeline.transforms.extractor;

import org.apache.hop.core.CheckResult;
import org.apache.hop.core.ICheckResult;
import org.apache.hop.core.annotations.Transform;
import org.apache.hop.core.exception.HopXmlException;
import org.apache.hop.core.row.IRowMeta;
import org.apache.hop.core.row.IValueMeta;
import org.apache.hop.core.row.value.ValueMetaBoolean;
import org.apache.hop.core.row.value.ValueMetaInteger;
import org.apache.hop.core.row.value.ValueMetaString;
import org.apache.hop.core.variables.IVariables;
import org.apache.hop.metadata.api.IHopMetadataProvider;
import org.apache.hop.pipeline.PipelineMeta;
import org.apache.hop.pipeline.transform.BaseTransformMeta;
import org.apache.hop.pipeline.transform.TransformMeta;
import org.w3c.dom.Node;

import java.util.List;

import static ie.dcu.mcm.hop.pipeline.transforms.extractor.TaxonomyContextExtractorMeta.ContextMeasurementType.WORD;
import static org.apache.commons.lang3.StringUtils.equalsIgnoreCase;
import static org.apache.commons.lang3.math.NumberUtils.toInt;
import static org.apache.commons.lang3.math.NumberUtils.toLong;
import static org.apache.hop.core.ICheckResult.TYPE_RESULT_ERROR;
import static org.apache.hop.core.ICheckResult.TYPE_RESULT_OK;
import static org.apache.hop.core.util.Utils.isEmpty;
import static org.apache.hop.core.xml.XmlHandler.addTagValue;
import static org.apache.hop.core.xml.XmlHandler.getTagValue;
import static org.apache.hop.i18n.BaseMessages.getString;

@Transform(
        id = "TaxonomyContextExtractor",
        image = "taxonomy-context-extractor.svg",
        name = "i18n::BaseTransform.TypeLongDesc.TaxonomyContextExtractor",
        description = "i18n::BaseTransform.TypeTooltipDesc.TaxonomyContextExtractor",
        categoryDescription =
                "i18n:org.apache.hop.pipeline.transform:BaseTransform.Category.Transform",
        documentationUrl = "/pipeline/transforms/taxonomy-context-extractor.html")
public class TaxonomyContextExtractorMeta extends BaseTransformMeta<TaxonomyContextExtractor, TaxonomyContextExtractorData> {
    private static final Class<?> PKG = TaxonomyContextExtractorMeta.class; // For Translator


    private String corpusField;
    private String taxonomyField;
    private String documentPartitionField;
    private String contextMeasurement = WORD.getCode();
    private long leftContextMaxSize = 500L;
    private long leftContextTargetSize = 250L;
    private long rightContextMaxSize = 500L;
    private long rightContextTargetSize = 250L;
    private boolean ignoreCase = false;

    public TaxonomyContextExtractorMeta() {
        super();
    }


    @Override
    public void loadXml(Node transformNode, IHopMetadataProvider metadataProvider)
            throws HopXmlException {
        try {
            corpusField = getTagValue(transformNode, "corpusField");
            taxonomyField = getTagValue(transformNode, "taxonomyField");
            documentPartitionField = getTagValue(transformNode, "documentPartitionField");
            contextMeasurement = getTagValue(transformNode, "contextMeasurement");
            leftContextMaxSize = toLong(getTagValue(transformNode, "leftContextMaxSize"));
            leftContextTargetSize = toLong(getTagValue(transformNode, "leftContextTargetSize"));
            rightContextMaxSize = toLong(getTagValue(transformNode, "rightContextMaxSize"));
            rightContextTargetSize = toLong(getTagValue(transformNode, "rightContextTargetSize"));
            ignoreCase = equalsIgnoreCase("Y", getTagValue(transformNode, "ignoreCase"));

        } catch (Exception e) {
            throw new HopXmlException(
                    getString(PKG, "TaxonomyContextExtractorMeta.Exception.UnableToReadTransformMeta"), e);
        }
    }

    @Override
    public void setDefault() {
        ignoreCase = false;
        leftContextTargetSize = 250;
        leftContextMaxSize = leftContextTargetSize * 2;
        rightContextTargetSize = 250;
        rightContextMaxSize = rightContextTargetSize * 2;
        contextMeasurement = WORD.getCode();
    }

    @Override
    public void getFields(
            IRowMeta r,
            String name,
            IRowMeta[] info,
            TransformMeta nextTransform,
            IVariables variables,
            IHopMetadataProvider metadataProvider) {

        valueMetaString(r, name, "taxonomy_term");
        valueMetaString(r, name, "taxonomy_category");
        valueMetaString(r, name, "context_measurement");

        valueMetaInteger(r, name, "context_size");
        valueMetaInteger(r, name, "context_size_left_current");
        valueMetaInteger(r, name, "context_size_left_target");
        valueMetaInteger(r, name, "context_size_left_max");
        valueMetaInteger(r, name, "context_size_right_current");
        valueMetaInteger(r, name, "context_size_right_target");
        valueMetaInteger(r, name, "context_size_right_max");

        valueMetaString(r, name, "context_text");
    }

    private void valueMetaString(IRowMeta r, String name, String metaName) {
        IValueMeta sText = new ValueMetaString(metaName);
        sText.setOrigin(name);
        r.addValueMeta(sText);
    }

    private void valueMetaBoolean(IRowMeta r, String name, String metaName) {
        IValueMeta sText = new ValueMetaBoolean(metaName);
        sText.setOrigin(name);
        r.addValueMeta(sText);
    }

    private void valueMetaInteger(IRowMeta r, String name, String metaName) {
        IValueMeta sText = new ValueMetaInteger(metaName);
        sText.setOrigin(name);
        r.addValueMeta(sText);
    }

    @Override
    public String getXml() {
        return "    " + addTagValue("corpusField", corpusField) +
                "    " + addTagValue("taxonomyField", taxonomyField) +
                "    " + addTagValue("documentPartitionField", documentPartitionField) +
                "    " + addTagValue("contextMeasurement", contextMeasurement) +
                "    " + addTagValue("leftContextMaxSize", leftContextMaxSize) +
                "    " + addTagValue("leftContextTargetSize", leftContextTargetSize) +
                "    " + addTagValue("rightContextMaxSize", rightContextMaxSize) +
                "    " + addTagValue("rightContextTargetSize", rightContextTargetSize) +
                "    " + addTagValue("ignoreCase", ignoreCase);
    }

    @Override
    public void check(
            List<ICheckResult> remarks,
            PipelineMeta pipelineMeta,
            TransformMeta transformMeta,
            IRowMeta prev,
            String[] input,
            String[] output,
            IRowMeta info,
            IVariables variables,
            IHopMetadataProvider metadataProvider) {
        CheckResult cr;


        if (isEmpty(corpusField)) {
            cr =
                    new CheckResult(
                            TYPE_RESULT_ERROR,
                            getString(PKG, "TaxonomyContextExtractorMeta.CheckResult.CorpusFieldMissing"),
                            transformMeta);
        } else {
            cr =
                    new CheckResult(
                            TYPE_RESULT_OK,
                            getString(PKG, "TaxonomyContextExtractorMeta.CheckResult.CorpusFieldOK"),
                            transformMeta);
        }
        remarks.add(cr);

        if (isEmpty(taxonomyField)) {
            cr =
                    new CheckResult(
                            TYPE_RESULT_ERROR,
                            getString(PKG, "TaxonomyContextExtractorMeta.CheckResult.TaxonomyFieldMissing"),
                            transformMeta);
        } else {
            cr =
                    new CheckResult(
                            TYPE_RESULT_OK,
                            getString(PKG, "TaxonomyContextExtractorMeta.CheckResult.TaxonomyFieldOK"),
                            transformMeta);
        }
        remarks.add(cr);


        if (isEmpty(documentPartitionField)) {
            cr =
                    new CheckResult(
                            TYPE_RESULT_ERROR,
                            getString(PKG, "TaxonomyContextExtractorMeta.CheckResult.DocumentPartitionFieldMissing"),
                            transformMeta);
        } else {
            cr =
                    new CheckResult(
                            TYPE_RESULT_OK,
                            getString(PKG, "TaxonomyContextExtractorMeta.CheckResult.DocumentPartitionFieldOK"),
                            transformMeta);
        }
        remarks.add(cr);

        if (rightContextMaxSize < rightContextTargetSize || rightContextTargetSize <= 0) {
            cr =
                    new CheckResult(
                            TYPE_RESULT_ERROR,
                            getString(PKG, "TaxonomyContextExtractorMeta.CheckResult.RightContextInvalidNumber"),
                            transformMeta);
        } else {
            cr =
                    new CheckResult(
                            TYPE_RESULT_OK,
                            getString(PKG, "TaxonomyContextExtractorMeta.CheckResult.RightContextOK"),
                            transformMeta);
        }
        remarks.add(cr);


        if (leftContextMaxSize < leftContextTargetSize || leftContextTargetSize <= 0) {
            cr =
                    new CheckResult(
                            TYPE_RESULT_ERROR,
                            getString(PKG, "TaxonomyContextExtractorMeta.CheckResult.LeftContextInvalidNumber"),
                            transformMeta);
        } else {
            cr =
                    new CheckResult(
                            TYPE_RESULT_OK,
                            getString(PKG, "TaxonomyContextExtractorMeta.CheckResult.LeftContextOK"),
                            transformMeta);
        }
        remarks.add(cr);


        // See if we have input streams leading to this transform!
        if (input.length > 0) {
            cr =
                    new CheckResult(
                            TYPE_RESULT_OK,
                            getString(
                                    PKG, "TaxonomyContextExtractorMeta.CheckResult.ReceivingInfoFromOtherTransforms"),
                            transformMeta);
        } else {
            cr =
                    new CheckResult(
                            TYPE_RESULT_ERROR,
                            getString(PKG, "TaxonomyContextExtractorMeta.CheckResult.NoInpuReceived"),
                            transformMeta);
        }
        remarks.add(cr);

    }

    @Override
    public boolean supportsErrorHandling() {
        return true;
    }

    public String getCorpusField() {
        return corpusField;
    }

    public void setCorpusField(String corpusField) {
        this.corpusField = corpusField;
    }

    public String getTaxonomyField() {
        return taxonomyField;
    }

    public void setTaxonomyField(String taxonomyField) {
        this.taxonomyField = taxonomyField;
    }

    public String getDocumentPartitionField() {
        return documentPartitionField;
    }

    public void setDocumentPartitionField(String documentPartitionField) {
        this.documentPartitionField = documentPartitionField;
    }

    public String getContextMeasurement() {
        return contextMeasurement;
    }

    public void setContextMeasurement(String contextMeasurement) {
        this.contextMeasurement = contextMeasurement;
    }

    public long getLeftContextMaxSize() {
        return leftContextMaxSize;
    }

    public void setLeftContextMaxSize(long leftContextMaxSize) {
        this.leftContextMaxSize = leftContextMaxSize;
    }

    public long getLeftContextTargetSize() {
        return leftContextTargetSize;
    }

    public void setLeftContextTargetSize(long leftContextTargetSize) {
        this.leftContextTargetSize = leftContextTargetSize;
    }

    public long getRightContextMaxSize() {
        return rightContextMaxSize;
    }

    public void setRightContextMaxSize(long rightContextMaxSize) {
        this.rightContextMaxSize = rightContextMaxSize;
    }

    public long getRightContextTargetSize() {
        return rightContextTargetSize;
    }

    public void setRightContextTargetSize(long rightContextTargetSize) {
        this.rightContextTargetSize = rightContextTargetSize;
    }

    public boolean isIgnoreCase() {
        return ignoreCase;
    }

    public void setIgnoreCase(boolean ignoreCase) {
        this.ignoreCase = ignoreCase;
    }

    public enum ContextMeasurementType {

        WORD("WORD", getString(PKG, "TaxonomyContextExtractorDialog.ContextMeasurementType.WORD")),
        CHARACTER("CHARACTER", getString(PKG, "TaxonomyContextExtractorDialog.ContextMeasurementType.CHARACTER")),
        SENTENCE("SENTENCE", getString(PKG, "TaxonomyContextExtractorDialog.ContextMeasurementType.SENTENCE"));

        private final String code;
        private final String description;

        ContextMeasurementType(String code, String description) {
            this.code = code;
            this.description = description;
        }

        public static ContextMeasurementType getTypeFromDescription(String description) {
            for (ContextMeasurementType type : values()) {
                if (equalsIgnoreCase(type.description, description)) {
                    return type;
                }
            }
            return WORD;
        }

        public static String[] getDescriptions() {
            ContextMeasurementType[] types = ContextMeasurementType.values();
            String[] descriptions = new String[types.length];
            for (int i = 0; i < types.length; i++) {
                descriptions[i] = types[i].description;
            }
            return descriptions;
        }

        public String getCode() {
            return code;
        }

        public String getDescription() {
            return description;
        }
    }

}
