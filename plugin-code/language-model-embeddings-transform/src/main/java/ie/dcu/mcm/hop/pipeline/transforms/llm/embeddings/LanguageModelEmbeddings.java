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

package ie.dcu.mcm.hop.pipeline.transforms.llm.embeddings;

import com.squareup.moshi.Moshi;
import ie.dcu.mcm.hop.pipeline.transforms.llm.embeddings.LanguageModelEmbeddingsMeta.ContextMeasurementType;
import ie.dcu.mcm.hop.pipeline.transforms.llm.embeddings.internals.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.hop.core.exception.HopException;
import org.apache.hop.core.exception.HopTransformException;
import org.apache.hop.i18n.BaseMessages;
import org.apache.hop.pipeline.Pipeline;
import org.apache.hop.pipeline.PipelineMeta;
import org.apache.hop.pipeline.transform.BaseTransform;
import org.apache.hop.pipeline.transform.TransformMeta;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static ie.dcu.mcm.hop.pipeline.transforms.llm.embeddings.internals.ParsedSentence.Builder.parsedSentenceBuilder;
import static java.lang.System.arraycopy;
import static java.util.Arrays.sort;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.trim;
import static org.apache.hop.core.util.Utils.isEmpty;

public class LanguageModelEmbeddings extends BaseTransform<LanguageModelEmbeddingsMeta, LanguageModelEmbeddingsData> {
    public static final int OVER_ALLOCATE_SIZE = 10;
    private static final Class<?> PKG = LanguageModelEmbeddings.class; // For Translator
    private ParsedSentence.Builder sentenceBuilder;
    private ContextMeasurementType contextMeasurementType;
    private ParsedContext context;
    private Object[] lastInputRow;
    private String documentPartitionValue;

    public LanguageModelEmbeddings(
            TransformMeta transformMeta,
            LanguageModelEmbeddingsMeta meta,
            LanguageModelEmbeddingsData data,
            int copyNr,
            PipelineMeta pipelineMeta,
            Pipeline pipeline) {
        super(transformMeta, meta, data, copyNr, pipelineMeta, pipeline);
    }

    private void finish() throws HopTransformException {
        if (this.context != null && this.lastInputRow != null) {
            // Flush out any valid context still getting built
            processContext(this.context, this.lastInputRow);
            this.lastInputRow = null;
            this.context = null;
            this.documentPartitionValue = null;
        }
        setOutputDone();
    }

    @Override
    public boolean processRow() throws HopException {
        Object[] r = getRow(); // Get row from input rowset & set row busy!
        if (r == null) { // no more input to be expected...
            finish();
            return false;
        }

        if (first) {
            first = false;

            // get the RowMeta
            data.previousRowMeta = getInputRowMeta().clone();
            data.NrPrevFields = data.previousRowMeta.size();
            data.outputRowMeta = getInputRowMeta().clone();
            meta.getFields(data.outputRowMeta, getTransformName(), null, null, this, metadataProvider);

            if (isEmpty(meta.getCorpusField())) {
                throw new HopException(
                        BaseMessages.getString(PKG, "LanguageModelEmbeddings.Error.CorpusFieldMissing"));
            }
            if (isEmpty(meta.getTaxonomyField())) {
                throw new HopException(
                        BaseMessages.getString(PKG, "LanguageModelEmbeddings.Error.TaxonomyFieldMissing"));
            }
            if (isEmpty(meta.getDocumentPartitionField())) {
                throw new HopException(
                        BaseMessages.getString(PKG, "LanguageModelEmbeddings.Error.DocumentPartitionFieldMissing"));
            }
            // cache the position of the field
            cacheIndexPositions();


            String taxonomyJson = (String) r[data.indexOfTaxonomyField];
            Taxonomy[] taxonomies;
            try {
                Map<Object, Object> troot = new Moshi.Builder().build().adapter(Map.class).fromJson(taxonomyJson);
                if (troot.isEmpty()) {
                    throw new HopException(
                            BaseMessages.getString(PKG, "LanguageModelEmbeddings.Error.TaxonomyFieldInvalidJson"));
                }

                // The root node can be anything, but only the first one gets looked at.
                Collection<Map<String, Object>> tuples = (Collection<Map<String, Object>>) troot.values().iterator().next();

                taxonomies = new Taxonomy[tuples.size()];
                int i = 0;
                for (Map<String, Object> tuple : tuples) {
                    taxonomies[i] = new Taxonomy(tuple, meta.isIgnoreCase());
                    i++;
                }
                sort(taxonomies);

            } catch (IOException e) {
                throw new HopException(
                        BaseMessages.getString(PKG, "LanguageModelEmbeddings.Error.TaxonomyFieldInvalidJson"), e);
            }

            sentenceBuilder = parsedSentenceBuilder()
                    .taxonomies(taxonomies)
                    .ignoreCase(meta.isIgnoreCase());

            this.contextMeasurementType = ContextMeasurementType.valueOf(meta.getContextMeasurement());

        } // End If first

        boolean sendToErrorRow = false;
        String errorMessage = null;

        process(r);

        try {
            if (log.isRowLevel()) {
                logRowlevel(
                        BaseMessages.getString(
                                PKG,
                                "LanguageModelEmbeddings.LineNumber",
                                getLinesRead() + " : " + getInputRowMeta().getString(r)));
            }
        } catch (
                Exception e) {
            if (getTransformMeta().isDoingErrorHandling()) {
                sendToErrorRow = true;
                errorMessage = e.toString();
            } else {
                logError(
                        BaseMessages.getString(PKG, "LanguageModelEmbeddings.ErrorInTransformRunning") + e.getMessage());
                setErrors(1);
                stopAll();
                setOutputDone(); // signal end to receiver(s)
                return false;
            }
            if (sendToErrorRow) {
                // Simply add this row to the error row
                putError(
                        getInputRowMeta(), r, 1, errorMessage, meta.getCorpusField(), "LanguageModelEmbeddings001");
            }
        }

        return true;
    }


    private void process(Object[] inputRow) throws HopTransformException {
        String corpus = trim((String) inputRow[data.indexOfCorpusField]);
        if (isBlank(corpus)) {
            return;
        }

        this.lastInputRow = inputRow;

        String documentPartitionValue = (String) inputRow[data.indexOfDocumentPartitionField];



        if(!StringUtils.equals(this.documentPartitionValue, documentPartitionValue)) {
            // Document changed, process any outstanding context as-is
            if(this.context != null) {
                processContext(this.context, inputRow);
            }

            this.documentPartitionValue = documentPartitionValue;
            this.context = null; // Force a new context
        }

        if (this.context == null) {
            this.context = new ParsedContextImpl(
                    contextMeasurementType,
                    meta.getLeftContextTargetSize(),
                    meta.getLeftContextMaxSize(),
                    meta.getRightContextTargetSize(),
                    meta.getRightContextMaxSize());
        }

        List<ParsedSentence> sentences = sentenceBuilder.buildSentences(inputRow, corpus);

        try {
            this.context.append(sentences);
        } catch (ContextFullException e) {
            processContext(e.getContext(), inputRow);
            this.context = null;
        }
    }

    private void cacheIndexPositions() throws HopException {
        if (data.indexOfCorpusField < 0) {
            data.indexOfCorpusField = data.previousRowMeta.indexOfValue(meta.getCorpusField());
            if (data.indexOfCorpusField < 0) {
                // The field is unreachable !
                throw new HopException(
                        BaseMessages.getString(
                                PKG, "LanguageModelEmbeddings.Exception.CouldnotFindField", meta.getCorpusField()));
            }
        }
        if (data.indexOfTaxonomyField < 0) {
            data.indexOfTaxonomyField = data.previousRowMeta.indexOfValue(meta.getTaxonomyField());
            if (data.indexOfTaxonomyField < 0) {
                // The field is unreachable !
                throw new HopException(
                        BaseMessages.getString(
                                PKG, "LanguageModelEmbeddings.Exception.CouldnotFindField", meta.getTaxonomyField()));
            }
        }
        if (data.indexOfDocumentPartitionField < 0) {
            data.indexOfDocumentPartitionField = data.previousRowMeta.indexOfValue(meta.getDocumentPartitionField());
            if (data.indexOfDocumentPartitionField < 0) {
                // The field is unreachable !
                throw new HopException(
                        BaseMessages.getString(
                                PKG, "LanguageModelEmbeddings.Exception.CouldnotFindField", meta.getDocumentPartitionField()));
            }
        }

        for (String field : data.contextFields) {
            if (data.indexOfMap.getOrDefault(field, -1) < 0) {
                data.indexOfMap.put(field, data.outputRowMeta.indexOfValue(field));
                if (data.indexOfMap.getOrDefault(field, -1) < 0) {
                    // The field is unreachable!
                    throw new HopException(
                            BaseMessages.getString(
                                    PKG, "LanguageModelEmbeddings.Exception.CouldnotFindField", field));
                }
            }
        }
    }

    private void processContext(ParsedContext context, Object[] inputRow) throws HopTransformException {
        // No matches, no rows.
        // Each row for each type of taxonomy, further pipeline steps can then decide how to handle it
        try {
            context.buildContext();
        } catch (ContextFullException e) {
            // Ignore, we are going to process it now
        }
        for (Taxonomy taxonomy : context.getTaxonomies()) {
            Object[] outputRow = new Object[inputRow.length + data.indexOfMap.size() + OVER_ALLOCATE_SIZE];
            arraycopy(inputRow, 0, outputRow, 0, inputRow.length);

            outputRow[data.indexOfMap.get("taxonomy_term")] = taxonomy.getOriginalTerm();
            outputRow[data.indexOfMap.get("taxonomy_category")] = taxonomy.getCategory();

            outputRow[data.indexOfMap.get("context_measurement")] = context.getMeasurementType().getCode();
            outputRow[data.indexOfMap.get("context_size")] = context.getContextSize();

            outputRow[data.indexOfMap.get("context_size_left_current")] = context.getLeftCurrentSize();
            outputRow[data.indexOfMap.get("context_size_left_target")] = context.getLeftContextSize();
            outputRow[data.indexOfMap.get("context_size_left_max")] = context.getLeftContextMax();

            outputRow[data.indexOfMap.get("context_size_right_current")] = context.getRightCurrentSize();
            outputRow[data.indexOfMap.get("context_size_right_target")] = context.getRightContextSize();
            outputRow[data.indexOfMap.get("context_size_right_max")] = context.getRightContextMax();

            outputRow[data.indexOfMap.get("context_text")] = context.getContext();

            putRow(data.outputRowMeta, outputRow);
        }
    }
}
