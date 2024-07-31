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

import com.squareup.moshi.Moshi;
import org.apache.hop.core.exception.HopException;
import org.apache.hop.core.exception.HopTransformException;
import org.apache.hop.core.row.IRowMeta;
import org.apache.hop.i18n.BaseMessages;
import org.apache.hop.pipeline.Pipeline;
import org.apache.hop.pipeline.PipelineMeta;
import org.apache.hop.pipeline.transform.BaseTransform;
import org.apache.hop.pipeline.transform.TransformMeta;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.atomic.AtomicInteger;

import static java.util.Arrays.sort;
import static java.util.Collections.addAll;
import static java.util.concurrent.ForkJoinPool.commonPool;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.trim;
import static org.apache.hop.core.util.Utils.isEmpty;
import static ie.dcu.mcm.hop.pipeline.transforms.bias.sentences.ParsedSentence.Builder.parsedSentenceBuilder;
import static ie.dcu.mcm.hop.pipeline.transforms.bias.sentences.TaxonomyTermAnnotation.Builder.biasTermBuilder;

public class SentenceBiasDetector extends BaseTransform<SentenceBiasDetectorMeta, SentenceBiasDetectorData> {
    private static final Class<?> PKG = SentenceBiasDetector.class; // For Translator

    public SentenceBiasDetector(
            TransformMeta transformMeta,
            SentenceBiasDetectorMeta meta,
            SentenceBiasDetectorData data,
            int copyNr,
            PipelineMeta pipelineMeta,
            Pipeline pipeline) {
        super(transformMeta, meta, data, copyNr, pipelineMeta, pipeline);
    }

    public static final int OVER_ALLOCATE_SIZE = 10;
    private final int cores = Runtime.getRuntime().availableProcessors();
    private final ForkJoinPool executor = commonPool();
    private final AtomicInteger jobs = new AtomicInteger(0);

    private final int maxJobs = cores * 100;
    private final Object lock = new Object();

    private ParsedSentencePerTerm.Builder sentenceBuilder;

    private void finish() {
        // Wait until all jobs are finished.
        synchronized (lock) {
            while (jobs.get() > 0) {
                try {
                    lock.wait();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
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

            if (isEmpty(meta.getTaxonomyField())) {
                throw new HopException(
                        BaseMessages.getString(PKG, "SentenceBiasDetector.Error.TaxonomyFieldMissing"));
            }
            if (isEmpty(meta.getSubjectivityField())) {
                throw new HopException(
                        BaseMessages.getString(PKG, "SentenceBiasDetector.Error.SubjectivityFieldMissing"));
            }
            if (isEmpty(meta.getCorpusField())) {
                throw new HopException(
                        BaseMessages.getString(PKG, "SentenceBiasDetector.Error.CorpusFieldMissing"));
            }
            // cache the position of the field
            cacheIndexPositions();


            String subjectivityLexiconJson = (String) r[data.indexOfSubjectivityField];
            SubjectivityLexicon[] subjectivityLexicons;
            try {
                Map<Object, Object> troot = new Moshi.Builder().build().adapter(Map.class).fromJson(subjectivityLexiconJson);
                if (troot.isEmpty()) {
                    throw new HopException(
                            BaseMessages.getString(PKG, "SentenceBiasDetector.Error.SubjectivityFieldInvalidJson"));
                }

                // The root node can be anything, but only the first one gets looked at.
                Collection<Map<String, Object>> tuples = (Collection<Map<String, Object>>) troot.values().iterator().next();

                subjectivityLexicons = new SubjectivityLexicon[tuples.size()];
                int i = 0;
                for (Map<String, Object> tuple : tuples) {
                    subjectivityLexicons[i] = new SubjectivityLexicon(tuple); // always ignore case, meta.isIgnoreCase());
                    i++;
                }
                sort(subjectivityLexicons);

            } catch (IOException e) {
                throw new HopException(
                        BaseMessages.getString(PKG, "SentenceBiasDetector.Error.SubjectivityFieldInvalidJson"), e);
            }


            String biasTaxonomyJson = (String) r[data.indexOfTaxonomyField];
            TaxonomyTerm[] taxonomyTerms;
            try {
                Map<Object, Object> troot = new Moshi.Builder().build().adapter(Map.class).fromJson(biasTaxonomyJson);
                if (troot.isEmpty()) {
                    throw new HopException(
                            BaseMessages.getString(PKG, "SentenceBiasDetector.Error.TaxonomyFieldInvalidJson"));
                }

                // The root node can be anything, but only the first one gets looked at.
                Collection<Map<String, Object>> tuples = (Collection<Map<String, Object>>) troot.values().iterator().next();

                taxonomyTerms = new TaxonomyTerm[tuples.size()];
                int i = 0;
                for (Map<String, Object> tuple : tuples) {
                    taxonomyTerms[i] = new TaxonomyTerm(tuple, subjectivityLexicons, meta.isIgnoreCase());
                    i++;
                }
                sort(taxonomyTerms);

            } catch (IOException e) {
                throw new HopException(
                        BaseMessages.getString(PKG, "SentenceBiasDetector.Error.TaxonomyFieldInvalidJson"), e);
            }

            sentenceBuilder = parsedSentenceBuilder()
                    .groupSentences(meta.isGroupSentences())
                    .sentencesOnly(meta.isSentencesOnly())
                    .taxonomyTerms(taxonomyTerms)
                    .subjectivityLexicons(subjectivityLexicons)
                    .ignoreCase(meta.isIgnoreCase());

        } // End If first

        boolean sendToErrorRow = false;
        String errorMessage = null;

        process(r);

        try {
            if (log.isRowLevel()) {
                logRowlevel(
                        BaseMessages.getString(
                                PKG,
                                "SentenceBiasDetector.LineNumber",
                                getLinesRead() + " : " + getInputRowMeta().getString(r)));
            }
        } catch (
                Exception e) {
            if (getTransformMeta().isDoingErrorHandling()) {
                sendToErrorRow = true;
                errorMessage = e.toString();
            } else {
                logError(
                        BaseMessages.getString(PKG, "SentenceBiasDetector.ErrorInTransformRunning") + e.getMessage());
                setErrors(1);
                stopAll();
                setOutputDone(); // signal end to receiver(s)
                return false;
            }
            if (sendToErrorRow) {
                // Simply add this row to the error row
                putError(
                        getInputRowMeta(), r, 1, errorMessage, meta.getTaxonomyField(), "SentenceBiasDetector001");
            }
        }

        return true;
    }


    private void process(Object[] inputRow) {
        String corpus = trim((String) inputRow[data.indexOfCorpusField]);
        if (isBlank(corpus)) {
            return;
        }


        synchronized (lock) {
            while (jobs.get() > maxJobs) {
                try {
                    lock.wait();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        executor.submit(() -> {
            try {
                List<ParsedSentence> sentences = sentenceBuilder.buildSentences(inputRow, corpus);
                for (ParsedSentence sentence : sentences) {
                    try {
                        if (meta.isSentencesOnly()) {
                            processSentencesOnly(sentence);
                        } else if (meta.isGroupSentences()) {
                            processGroupedSentences(sentence);
                        } else {
                            processSentencePerTerm(sentence);
                        }
                        //  log.logBasic("processed");
                    } catch (HopTransformException e) {
                        throw new RuntimeException(e);
                    }
                }
                jobs.decrementAndGet();

                synchronized (lock) {
                    lock.notifyAll();
                }
            } catch (Exception e) {
                log.logError(e.getMessage(), e);
            }
        });

        jobs.incrementAndGet();

    }

    private void cacheIndexPositions() throws HopException {
        if (data.indexOfTaxonomyField < 0) {
            data.indexOfTaxonomyField = data.previousRowMeta.indexOfValue(meta.getTaxonomyField());
            if (data.indexOfTaxonomyField < 0) {
                // The field is unreachable !
                throw new HopException(
                        BaseMessages.getString(
                                PKG, "SentenceBiasDetector.Exception.CouldnotFindField", meta.getTaxonomyField()));
            }
        }
        if (data.indexOfSubjectivityField < 0) {
            data.indexOfSubjectivityField = data.previousRowMeta.indexOfValue(meta.getSubjectivityField());
            if (data.indexOfSubjectivityField < 0) {
                // The field is unreachable !
                throw new HopException(
                        BaseMessages.getString(
                                PKG, "SentenceBiasDetector.Exception.CouldnotFindField", meta.getSubjectivityField()));
            }
        }
        if (data.indexOfCorpusField < 0) {
            data.indexOfCorpusField = data.previousRowMeta.indexOfValue(meta.getCorpusField());
            if (data.indexOfCorpusField < 0) {
                // The field is unreachable !
                throw new HopException(
                        BaseMessages.getString(
                                PKG, "SentenceBiasDetector.Exception.CouldnotFindField", meta.getCorpusField()));
            }
        }
    }

    private void processSentencesOnly(ParsedSentence sentence) throws HopTransformException {
        int rowMetaSize = data.outputRowMeta.size() + OVER_ALLOCATE_SIZE;
        List<Object> outputRow = new ArrayList<>(rowMetaSize);
        addAll(outputRow, sentence.getInputRow());

        // Common across all outputs
        outputRow.add(sentence.getOriginalText());
        //outputRow.add(join(sentence.getOriginalWords(), "¬"));
        outputRow.add(sentence.getIndex());
        outputRow.add(sentence.getIndexBegin());
        outputRow.add(sentence.getIndexEnd());
        outputRow.add(sentence.getCharacterCount());
        outputRow.add(sentence.getWordCount());
        //outputRow.add(join(sentence.getSentencePosTags(), ","));
        outputRow.add(sentence.getOriginalTextPosTagged());

        putRow(data.outputRowMeta, outputRow.toArray());
    }

    private void processGroupedSentences(ParsedSentence sentence) throws HopTransformException {

        TaxonomyTermAnnotations annotations = new TaxonomyTermAnnotations();
        for (TaxonomyTerm term : sentence.getPositiveMatchTerms()) {
            TaxonomyTermAnnotation b = biasTermBuilder()
                    .term(term.getOriginalTerm())
                    .pos(term.getTermPosTags())
                    .tokens(term.getOriginalWords())
                    .tokenCount(term.getWordCount())
                    .startIdx(term.getTermStartIndex())
                    .endIdx(term.getTermEndIndex())
                    .build();

            annotations.put(term.getCategory(), b);
        }

        int rowMetaSize = data.outputRowMeta.size() + OVER_ALLOCATE_SIZE;
        List<Object> outputRow = new ArrayList<>(rowMetaSize);
        addAll(outputRow, sentence.getInputRow());

        // Common across all outputs
        outputRow.add(sentence.getOriginalText());
        //outputRow.add(join(sentence.getOriginalWords(), "¬"));
        outputRow.add(sentence.getIndex());
        outputRow.add(sentence.getIndexBegin());
        outputRow.add(sentence.getIndexEnd());
        outputRow.add(sentence.getCharacterCount());
        outputRow.add(sentence.getWordCount());
        //outputRow.add(join(sentence.getSentencePosTags(), ","));
        outputRow.add(sentence.getOriginalTextPosTagged());

        // Grouped sentence specific
        outputRow.add((long) sentence.getPositiveMatchTerms().size()); // bias_term_count
        outputRow.add(annotations.toJson()); // bias_terms

        putRow(data.outputRowMeta, outputRow.toArray());
    }

    private void processSentencePerTerm(ParsedSentence sentence) throws HopTransformException {
        int rowMetaSize = data.outputRowMeta.size() + OVER_ALLOCATE_SIZE;
        for (TaxonomyTerm term : sentence.getPositiveMatchTerms()) {
            List<Object> outputRow = new ArrayList<>(rowMetaSize);
            addAll(outputRow, sentence.getInputRow());

            // Common across all outputs
            outputRow.add(sentence.getOriginalText());
            //outputRow.add(join(sentence.getOriginalWords(), "¬"));
            outputRow.add(sentence.getIndex());
            outputRow.add(sentence.getIndexBegin());
            outputRow.add(sentence.getIndexEnd());
            outputRow.add(sentence.getCharacterCount());
            outputRow.add(sentence.getWordCount());
            //outputRow.add(join(sentence.getSentencePosTags(), ","));
            outputRow.add(sentence.getOriginalTextPosTagged());

            // sentence-per-term specific
            outputRow.add(term.getCategory()); // bias_category
            outputRow.add(term.getOriginalTerm()); // bias_term
            //outputRow.add(join(term.getTermPosTags(), ",")); // bias_term_pos
            //outputRow.add(join(term.getOriginalWords(), "¬")); // bias_term_tokens
            outputRow.add(term.getOriginalTermPosTagged()); // bias_term_pos

            ParsedSentencePerTerm perTermSentence = (ParsedSentencePerTerm) sentence;
            outputRow.add(perTermSentence.getPreviousPos1()); // previous_pos1
            outputRow.add(perTermSentence.getPreviousPos2()); // previous_pos2
            outputRow.add(perTermSentence.getNextPos1()); // next_pos1
            outputRow.add(perTermSentence.getNextPos2()); // next_pos2

            outputRow.add(term.getWordCount()); // bias_term_token_count
            outputRow.add(term.getTermStartIndex()); // bias_term_index_start
            outputRow.add(term.getTermEndIndex()); // bias_term_index_end

            // Term subjectivity
            outputRow.add(perTermSentence.isSubjectivityInContext()); // subjectivity_in_context
            outputRow.add(term.getSubjectivity().isWeak()); // subjectivity_weak
            outputRow.add(term.getSubjectivity().isStrong()); // subjectivity_strong
            outputRow.add(term.getSubjectivity().getPriorPolarity()); // subjectivity_polarity

            // Count occurrences of the annotations, typically there will be only 1.
            // However, it supports larger search terms that might have more than one.
            for (PennTreebankPartOfSpeech tag : PennTreebankPartOfSpeech.values()) {
                outputRow.add((long) term.getTermPosBag().getCount(tag.name()));
            }

            putRow(data.outputRowMeta, outputRow.toArray());
        }
    }

    @Override
    public void putRow(IRowMeta rowMeta, Object[] row) throws HopTransformException {
        synchronized (this) {
            super.putRow(rowMeta, row);
        }
    }
}
