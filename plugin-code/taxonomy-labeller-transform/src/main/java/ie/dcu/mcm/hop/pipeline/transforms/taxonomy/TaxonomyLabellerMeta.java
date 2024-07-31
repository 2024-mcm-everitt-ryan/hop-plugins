package ie.dcu.mcm.hop.pipeline.transforms.taxonomy;

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
import java.util.Set;
import java.util.TreeSet;

import static java.lang.Integer.MAX_VALUE;
import static java.util.List.of;
import static org.apache.commons.lang3.StringUtils.equalsIgnoreCase;
import static org.apache.commons.lang3.StringUtils.split;
import static org.apache.commons.lang3.math.NumberUtils.toInt;
import static org.apache.hop.core.ICheckResult.TYPE_RESULT_ERROR;
import static org.apache.hop.core.ICheckResult.TYPE_RESULT_OK;
import static org.apache.hop.core.util.Utils.isEmpty;
import static org.apache.hop.core.xml.XmlHandler.addTagValue;
import static org.apache.hop.core.xml.XmlHandler.getTagValue;
import static org.apache.hop.i18n.BaseMessages.getString;

@Transform(
        id = "TaxonomyLabeller",
        image = "taxonomy-labeller.svg",
        name = "i18n::BaseTransform.TypeLongDesc.TaxonomyLabeller",
        description = "i18n::BaseTransform.TypeTooltipDesc.TaxonomyLabeller",
        categoryDescription =
                "i18n:org.apache.hop.pipeline.transform:BaseTransform.Category.Transform",
        documentationUrl = "/pipeline/transforms/taxonomy-labeller.html")
public class TaxonomyLabellerMeta extends BaseTransformMeta<TaxonomyLabeller, TaxonomyLabellerData> {
    private static final Class<?> PKG = TaxonomyLabellerMeta.class; // For Translator


    private String corpusField;
    private String taxonomyField;
    private String taxonomyCategories = "";
    private String taxonomyCategoriesDelimiter = "|";
    private String outputPhraseFieldNamePrefix = "phrase_";
    private String outputLabelFieldNamePrefix = "terms_";
    private int outputMaxPhraseWordCount = MAX_VALUE;
    private boolean ignoreCase = false;
    private int parallelism = 1;

    public TaxonomyLabellerMeta() {
        super();
    }


    @Override
    public void loadXml(Node transformNode, IHopMetadataProvider metadataProvider)
            throws HopXmlException {
        try {
            corpusField = getTagValue(transformNode, "corpusField");
            taxonomyField = getTagValue(transformNode, "taxonomyField");
            taxonomyCategories = getTagValue(transformNode, "taxonomyCategories");
            taxonomyCategoriesDelimiter = getTagValue(transformNode, "taxonomyCategoriesDelimiter");
            outputPhraseFieldNamePrefix = getTagValue(transformNode, "outputPhraseFieldNamePrefix");
            outputLabelFieldNamePrefix = getTagValue(transformNode, "outputLabelFieldNamePrefix");
            outputMaxPhraseWordCount = toInt(getTagValue(transformNode, "outputMaxPhraseWordCount"), MAX_VALUE);
            ignoreCase = equalsIgnoreCase("Y", getTagValue(transformNode, "ignoreCase"));
            parallelism = toInt(getTagValue(transformNode, "parallelism"), 1);
        } catch (Exception e) {
            throw new HopXmlException(
                    getString(PKG, "TaxonomyLabellerMeta.Exception.UnableToReadTransformMeta"), e);
        }
    }

    @Override
    public void setDefault() {
        taxonomyCategories = "";
        taxonomyCategoriesDelimiter = "|";
        outputPhraseFieldNamePrefix = "phrase_";
        outputLabelFieldNamePrefix = "terms_";
        outputMaxPhraseWordCount = MAX_VALUE;
        ignoreCase = false;
        parallelism = 1;
    }

    @Override
    public void getFields(
            IRowMeta r,
            String name,
            IRowMeta[] info,
            TransformMeta nextTransform,
            IVariables variables,
            IHopMetadataProvider metadataProvider) {
        valueMetaInteger(r, name, outputPhraseFieldNamePrefix + "index");
        valueMetaInteger(r, name, outputPhraseFieldNamePrefix + "series");
        valueMetaInteger(r, name, outputPhraseFieldNamePrefix + "max_word_count");
        valueMetaInteger(r, name, outputPhraseFieldNamePrefix + "word_count");
        valueMetaInteger(r, name, outputPhraseFieldNamePrefix + "character_count");

        Set<String> categories = new TreeSet<>(of(split(taxonomyCategories, taxonomyCategoriesDelimiter)));
        for (String category : categories) {
            valueMetaInteger(r, name, outputLabelFieldNamePrefix + "count_" + category);
            valueMetaString(r, name, outputLabelFieldNamePrefix + "list_" + category);
        }
        valueMetaString(r, name, outputPhraseFieldNamePrefix + "text");
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
                "    " + addTagValue("taxonomyCategories", taxonomyCategories) +
                "    " + addTagValue("taxonomyCategoriesDelimiter", taxonomyCategoriesDelimiter) +
                "    " + addTagValue("outputPhraseFieldNamePrefix", outputPhraseFieldNamePrefix) +
                "    " + addTagValue("outputLabelFieldNamePrefix", outputLabelFieldNamePrefix) +
                "    " + addTagValue("outputMaxPhraseWordCount", outputMaxPhraseWordCount) +
                "    " + addTagValue("parallelism", parallelism) +
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
                            getString(PKG, "TaxonomyLabellerMeta.CheckResult.CorpusFieldMissing"),
                            transformMeta);
        } else {
            cr =
                    new CheckResult(
                            TYPE_RESULT_OK,
                            getString(PKG, "TaxonomyLabellerMeta.CheckResult.CorpusFieldOK"),
                            transformMeta);
        }
        remarks.add(cr);

        if (isEmpty(taxonomyField)) {
            cr =
                    new CheckResult(
                            TYPE_RESULT_ERROR,
                            getString(PKG, "TaxonomyLabellerMeta.CheckResult.TaxonomyFieldMissing"),
                            transformMeta);
        } else {
            cr =
                    new CheckResult(
                            TYPE_RESULT_OK,
                            getString(PKG, "TaxonomyLabellerMeta.CheckResult.TaxonomyFieldOK"),
                            transformMeta);
        }
        remarks.add(cr);


        if (isEmpty(taxonomyCategories)) {
            cr =
                    new CheckResult(
                            TYPE_RESULT_ERROR,
                            getString(PKG, "TaxonomyLabellerMeta.CheckResult.TaxonomyCategoriesMissing"),
                            transformMeta);
        } else {
            cr =
                    new CheckResult(
                            TYPE_RESULT_OK,
                            getString(PKG, "TaxonomyLabellerMeta.CheckResult.TaxonomyCategoriesOK"),
                            transformMeta);
        }
        remarks.add(cr);


        if (isEmpty(taxonomyCategoriesDelimiter)) {
            cr =
                    new CheckResult(
                            TYPE_RESULT_ERROR,
                            getString(PKG, "TaxonomyLabellerMeta.CheckResult.TaxonomyCategoriesDelimiterMissing"),
                            transformMeta);
        } else {
            cr =
                    new CheckResult(
                            TYPE_RESULT_OK,
                            getString(PKG, "TaxonomyLabellerMeta.CheckResult.TaxonomyCategoriesDelimiterOK"),
                            transformMeta);
        }
        remarks.add(cr);


        if (outputMaxPhraseWordCount > 0) {
            cr =
                    new CheckResult(
                            TYPE_RESULT_ERROR,
                            getString(PKG, "TaxonomyLabellerMeta.CheckResult.OutputMaxPhraseWordCountInvalidNumber"),
                            transformMeta);
        } else {
            cr =
                    new CheckResult(
                            TYPE_RESULT_OK,
                            getString(PKG, "TaxonomyLabellerMeta.CheckResult.OutputMaxPhraseWordCountOK"),
                            transformMeta);
        }
        remarks.add(cr);


        // See if we have input streams leading to this transform!
        if (input.length > 0) {
            cr =
                    new CheckResult(
                            TYPE_RESULT_OK,
                            getString(
                                    PKG, "TaxonomyLabellerMeta.CheckResult.ReceivingInfoFromOtherTransforms"),
                            transformMeta);
        } else {
            cr =
                    new CheckResult(
                            TYPE_RESULT_ERROR,
                            getString(PKG, "TaxonomyLabellerMeta.CheckResult.NoInpuReceived"),
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

    public boolean isIgnoreCase() {
        return ignoreCase;
    }

    public void setIgnoreCase(boolean ignoreCase) {
        this.ignoreCase = ignoreCase;
    }

    public int getParallelism() {
        return parallelism;
    }

    public void setParallelism(int parallelism) {
        this.parallelism = parallelism;
    }

    public String getOutputLabelFieldNamePrefix() {
        return outputLabelFieldNamePrefix;
    }

    public void setOutputLabelFieldNamePrefix(String outputLabelFieldNamePrefix) {
        this.outputLabelFieldNamePrefix = outputLabelFieldNamePrefix;
    }

    public String getOutputPhraseFieldNamePrefix() {
        return outputPhraseFieldNamePrefix;
    }

    public void setOutputPhraseFieldNamePrefix(String outputPhraseFieldNamePrefix) {
        this.outputPhraseFieldNamePrefix = outputPhraseFieldNamePrefix;
    }

    public String getTaxonomyCategories() {
        return taxonomyCategories;
    }

    public void setTaxonomyCategories(String taxonomyCategories) {
        this.taxonomyCategories = taxonomyCategories;
    }

    public int getOutputMaxPhraseWordCount() {
        return outputMaxPhraseWordCount;
    }

    public void setOutputMaxPhraseWordCount(int outputMaxPhraseWordCount) {
        this.outputMaxPhraseWordCount = outputMaxPhraseWordCount;
    }

    public String getTaxonomyCategoriesDelimiter() {
        return taxonomyCategoriesDelimiter;
    }

    public void setTaxonomyCategoriesDelimiter(String taxonomyCategoriesDelimiter) {
        this.taxonomyCategoriesDelimiter = taxonomyCategoriesDelimiter;
    }

}
