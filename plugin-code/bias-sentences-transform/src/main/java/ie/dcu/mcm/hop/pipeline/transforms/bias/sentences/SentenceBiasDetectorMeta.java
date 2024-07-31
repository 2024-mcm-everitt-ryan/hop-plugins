/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ie.dcu.mcm.hop.pipeline.transforms.bias.sentences;

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
import org.apache.hop.i18n.BaseMessages;
import org.apache.hop.metadata.api.IHopMetadataProvider;
import org.apache.hop.pipeline.PipelineMeta;
import org.apache.hop.pipeline.transform.BaseTransformMeta;
import org.apache.hop.pipeline.transform.TransformMeta;
import org.w3c.dom.Node;

import java.util.List;

import static org.apache.commons.lang3.StringUtils.equalsIgnoreCase;
import static org.apache.hop.core.ICheckResult.TYPE_RESULT_ERROR;
import static org.apache.hop.core.ICheckResult.TYPE_RESULT_OK;
import static org.apache.hop.core.util.Utils.isEmpty;
import static org.apache.hop.core.xml.XmlHandler.addTagValue;
import static org.apache.hop.core.xml.XmlHandler.getTagValue;

@Transform(
        id = "SentenceBiasDetector",
        image = "sentence-bias-detector.svg",
        name = "i18n::BaseTransform.TypeLongDesc.SentenceBiasDetector",
        description = "i18n::BaseTransform.TypeTooltipDesc.SentenceBiasDetector",
        categoryDescription =
                "i18n:org.apache.hop.pipeline.transform:BaseTransform.Category.Transform",
        documentationUrl = "/pipeline/transforms/sentence-bias-detector.html")
public class SentenceBiasDetectorMeta extends BaseTransformMeta<SentenceBiasDetector, SentenceBiasDetectorData> {
    private static final Class<?> PKG = SentenceBiasDetectorMeta.class; // For Translator


    private String taxonomyField;
    private String subjectivityField;
    private String corpusField;
    //private String taxonomyJsonBlockName = "taxonomy";
    private boolean groupSentences = true;
    private boolean sentencesOnly = false;
    private boolean ignoreCase = false;

    public SentenceBiasDetectorMeta() {
        super();
    }


    @Override
    public void loadXml(Node transformNode, IHopMetadataProvider metadataProvider)
            throws HopXmlException {
        try {
            taxonomyField = getTagValue(transformNode, "taxonomyField");
            subjectivityField = getTagValue(transformNode, "subjectivityField");
            corpusField = getTagValue(transformNode, "corpusField");
            //taxonomyJsonBlockName = getTagValue(transformNode, "taxonomyJsonBlockName");
            groupSentences = equalsIgnoreCase("Y", getTagValue(transformNode, "groupSentences"));
            ignoreCase = equalsIgnoreCase("Y", getTagValue(transformNode, "ignoreCase"));
            sentencesOnly = equalsIgnoreCase("Y", getTagValue(transformNode, "sentencesOnly"));

        } catch (Exception e) {
            throw new HopXmlException(
                    BaseMessages.getString(PKG, "SentenceBiasDetectorMeta.Exception.UnableToReadTransformMeta"), e);
        }
    }

    @Override
    public void setDefault() {
        groupSentences = true;
        sentencesOnly = false;
        ignoreCase = false;
        //taxonomyJsonBlockName = "taxonomy";
    }

    @Override
    public void getFields(
            IRowMeta r,
            String name,
            IRowMeta[] info,
            TransformMeta nextTransform,
            IVariables variables,
            IHopMetadataProvider metadataProvider) {

        valueMetaString(r, name, "sentence_text");
        //valueMetaString(r, name, "sentence_words");
        valueMetaInteger(r, name, "sentence_index");
        valueMetaInteger(r, name, "sentence_index_start");
        valueMetaInteger(r, name, "sentence_index_end");
        valueMetaInteger(r, name, "sentence_character_count");
        valueMetaInteger(r, name, "sentence_word_count");
        valueMetaString(r, name, "sentence_pos_tags");

        if(!sentencesOnly) {
            if (groupSentences) {
                valueMetaInteger(r, name, "bias_term_count");
                valueMetaString(r, name, "bias_terms");
            } else {
                valueMetaString(r, name, "bias_category");
                valueMetaString(r, name, "bias_term");
                valueMetaString(r, name, "bias_term_pos");

                valueMetaString(r, name, "previous_pos1");
                valueMetaString(r, name, "previous_pos2");
                valueMetaString(r, name, "next_pos1");
                valueMetaString(r, name, "next_pos2");

                //valueMetaString(r, name, "bias_term_tokens");
                valueMetaInteger(r, name, "bias_term_token_count");
                valueMetaInteger(r, name, "bias_term_index_start");
                valueMetaInteger(r, name, "bias_term_index_end");

                valueMetaBoolean(r, name, "subjectivity_in_context");
                valueMetaBoolean(r, name, "subjectivity_weak");
                valueMetaBoolean(r, name, "subjectivity_strong");
                valueMetaString(r, name, "subjectivity_polarity");

                for (PennTreebankPartOfSpeech e : PennTreebankPartOfSpeech.values()) {
                    valueMetaInteger(r, name, "penn_treebank_pos_" + e.name());
                }
            }

        }
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
        return "    " + addTagValue("taxonomyField", taxonomyField) +
                "    " + addTagValue("subjectivityField", subjectivityField) +
                "    " + addTagValue("corpusField", corpusField) +
                "    " + addTagValue("sentencesOnly", sentencesOnly) +
                "    " + addTagValue("ignoreCase", ignoreCase) +
                //"    " + addTagValue("taxonomyJsonBlockName", taxonomyJsonBlockName) +
                "    " + addTagValue("groupSentences", groupSentences);
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


        if (isEmpty(taxonomyField)) {
            cr =
                    new CheckResult(
                            TYPE_RESULT_ERROR,
                            BaseMessages.getString(PKG, "SentenceBiasDetectorMeta.CheckResult.TaxonomyFieldMissing"),
                            transformMeta);
        } else {
            cr =
                    new CheckResult(
                            TYPE_RESULT_OK,
                            BaseMessages.getString(PKG, "SentenceBiasDetectorMeta.CheckResult.TaxonomyFieldOK"),
                            transformMeta);
        }
        remarks.add(cr);

        if (isEmpty(subjectivityField)) {
            cr =
                    new CheckResult(
                            TYPE_RESULT_ERROR,
                            BaseMessages.getString(PKG, "SentenceBiasDetectorMeta.CheckResult.SubjectivityFieldMissing"),
                            transformMeta);
        } else {
            cr =
                    new CheckResult(
                            TYPE_RESULT_OK,
                            BaseMessages.getString(PKG, "SentenceBiasDetectorMeta.CheckResult.SubjectivityFieldOK"),
                            transformMeta);
        }
        remarks.add(cr);

        if (isEmpty(corpusField)) {
            cr =
                    new CheckResult(
                            TYPE_RESULT_ERROR,
                            BaseMessages.getString(PKG, "SentenceBiasDetectorMeta.CheckResult.CorpusFieldMissing"),
                            transformMeta);
        } else {
            cr =
                    new CheckResult(
                            TYPE_RESULT_OK,
                            BaseMessages.getString(PKG, "SentenceBiasDetectorMeta.CheckResult.CorpusFieldOK"),
                            transformMeta);
        }
        remarks.add(cr);
/*
        if (isEmpty(taxonomyJsonBlockName)) {
            cr =
                    new CheckResult(
                            TYPE_RESULT_ERROR,
                            BaseMessages.getString(PKG, "SentenceBiasDetectorMeta.CheckResult.TaxonomyJsonBlockNameMissing"),
                            transformMeta);
        } else {
            cr =
                    new CheckResult(
                            TYPE_RESULT_OK,
                            BaseMessages.getString(PKG, "SentenceBiasDetectorMeta.CheckResult.TaxonomyJsonBlockNameOK"),
                            transformMeta);
        }
        remarks.add(cr);

 */

        // See if we have input streams leading to this transform!
        if (input.length > 0) {
            cr =
                    new CheckResult(
                            TYPE_RESULT_OK,
                            BaseMessages.getString(
                                    PKG, "SentenceBiasDetectorMeta.CheckResult.ReceivingInfoFromOtherTransforms"),
                            transformMeta);
        } else {
            cr =
                    new CheckResult(
                            TYPE_RESULT_ERROR,
                            BaseMessages.getString(PKG, "SentenceBiasDetectorMeta.CheckResult.NoInpuReceived"),
                            transformMeta);
        }
        remarks.add(cr);

    }

    @Override
    public boolean supportsErrorHandling() {
        return true;
    }

    public String getTaxonomyField() {
        return taxonomyField;
    }

    public void setTaxonomyField(String taxonomyField) {
        this.taxonomyField = taxonomyField;
    }

    public String getSubjectivityField() {
        return subjectivityField;
    }

    public void setSubjectivityField(String subjectivityField) {
        this.subjectivityField = subjectivityField;
    }

    public String getCorpusField() {
        return corpusField;
    }

    public void setCorpusField(String corpusField) {
        this.corpusField = corpusField;
    }

    public boolean isGroupSentences() {
        return groupSentences;
    }

    public void setGroupSentences(boolean groupSentences) {
        this.groupSentences = groupSentences;
    }

    public boolean isSentencesOnly() {
        return sentencesOnly;
    }

    public void setSentencesOnly(boolean sentencesOnly) {
        this.sentencesOnly = sentencesOnly;
    }

    public boolean isIgnoreCase() {
        return ignoreCase;
    }

    public void setIgnoreCase(boolean ignoreCase) {
        this.ignoreCase = ignoreCase;
    }
/*
    public String getTaxonomyJsonBlockName() {
        return taxonomyJsonBlockName;
    }

    public void setTaxonomyJsonBlockName(String taxonomyJsonBlockName) {
        this.taxonomyJsonBlockName = taxonomyJsonBlockName;
    }

 */
}
