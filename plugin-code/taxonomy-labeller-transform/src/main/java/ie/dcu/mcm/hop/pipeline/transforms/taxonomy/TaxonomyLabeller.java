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

package ie.dcu.mcm.hop.pipeline.transforms.taxonomy;

import com.squareup.moshi.Moshi;
import ie.dcu.mcm.hop.pipeline.transforms.taxonomy.internals.Phrase;
import ie.dcu.mcm.hop.pipeline.transforms.taxonomy.internals.Taxonomy;
import org.apache.hop.core.exception.HopException;
import org.apache.hop.core.exception.HopTransformException;
import org.apache.hop.core.row.IRowMeta;
import org.apache.hop.i18n.BaseMessages;
import org.apache.hop.pipeline.Pipeline;
import org.apache.hop.pipeline.PipelineMeta;
import org.apache.hop.pipeline.transform.BaseTransform;
import org.apache.hop.pipeline.transform.TransformMeta;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;

import static ie.dcu.mcm.hop.pipeline.transforms.taxonomy.internals.Phrase.Builder.phraseBuilder;
import static java.lang.System.arraycopy;
import static java.util.Arrays.sort;
import static java.util.List.of;
import static java.util.concurrent.Executors.newWorkStealingPool;
import static org.apache.commons.lang3.StringUtils.*;
import static org.apache.commons.lang3.math.NumberUtils.max;
import static org.apache.hop.core.util.Utils.isEmpty;

public class TaxonomyLabeller extends BaseTransform<TaxonomyLabellerMeta, TaxonomyLabellerData> {
    public static final int OVER_ALLOCATE_SIZE = 10;
    private static final Class<?> PKG = TaxonomyLabeller.class; // For Translator
    private Phrase.Builder phraseBuilder;
    private String[] taxonomyCategories;

    private final AtomicInteger jobs = new AtomicInteger(0);
    ;
    private final Object lock = new Object();
    private int maxJobs;
    private ExecutorService executor;

    public TaxonomyLabeller(
            TransformMeta transformMeta,
            TaxonomyLabellerMeta meta,
            TaxonomyLabellerData data,
            int copyNr,
            PipelineMeta pipelineMeta,
            Pipeline pipeline) {
        super(transformMeta, meta, data, copyNr, pipelineMeta, pipeline);
    }

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

        if(executor != null) {
            executor.shutdownNow();
        }
        executor = null;
        setOutputDone();
    }


    /////////////////

    private void process(Object[] inputRow) {
        String corpus = trim((String) inputRow[data.indexOfCorpusField]);
        if (isBlank(corpus)) {
            return;
        }

        if (meta.getParallelism() == 1) {
            processSync(corpus, inputRow);
        } else {
            processAsync(corpus, inputRow);
        }
    }


    private void processAsync(String corpus, Object[] inputRow) {
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
                processSync(corpus, inputRow);
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

    private void processSync(String corpus, Object[] inputRow) {
        List<Phrase> phrases = phraseBuilder.buildPhrases(inputRow, corpus);
        for (Phrase phrase : phrases) {
            try {
                processPhrase(phrase);
            } catch (HopTransformException e) {
                throw new RuntimeException(e);
            }
        }
    }
    //////////////////////////


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
                        BaseMessages.getString(PKG, "TaxonomyLabeller.Error.CorpusFieldMissing"));
            }
            if (isEmpty(meta.getTaxonomyField())) {
                throw new HopException(
                        BaseMessages.getString(PKG, "TaxonomyLabeller.Error.TaxonomyFieldMissing"));
            }
            // cache the position of the field
            cacheIndexPositions();


            String taxonomyJson = (String) r[data.indexOfTaxonomyField];
            Taxonomy[] taxonomies;
            try {
                Map<Object, Object> troot = new Moshi.Builder().build().adapter(Map.class).fromJson(taxonomyJson);
                if (troot.isEmpty()) {
                    throw new HopException(
                            BaseMessages.getString(PKG, "TaxonomyLabeller.Error.TaxonomyFieldInvalidJson"));
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
                        BaseMessages.getString(PKG, "TaxonomyLabeller.Error.TaxonomyFieldInvalidJson"), e);
            }

            int parallelism = meta.getParallelism();
            if (parallelism == 0) {
                parallelism = Runtime.getRuntime().availableProcessors();
            } else if (parallelism < 0) {
                parallelism = Runtime.getRuntime().availableProcessors() - parallelism;
                parallelism = max(1, parallelism);
            }
            if (executor != null) {
                executor.shutdownNow();
            }
            executor = newWorkStealingPool(parallelism);
            maxJobs = parallelism * 100;

            phraseBuilder = phraseBuilder()
                    .maxPhraseWordCount(meta.getOutputMaxPhraseWordCount())
                    .taxonomies(taxonomies)
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
                                "TaxonomyLabeller.LineNumber",
                                getLinesRead() + " : " + getInputRowMeta().getString(r)));
            }
        } catch (
                Exception e) {
            if (getTransformMeta().isDoingErrorHandling()) {
                sendToErrorRow = true;
                errorMessage = e.toString();
            } else {
                logError(
                        BaseMessages.getString(PKG, "TaxonomyLabeller.ErrorInTransformRunning") + e.getMessage());
                setErrors(1);
                stopAll();
                setOutputDone(); // signal end to receiver(s)
                return false;
            }
            if (sendToErrorRow) {
                // Simply add this row to the error row
                putError(
                        getInputRowMeta(), r, 1, errorMessage, meta.getCorpusField(), "TaxonomyLabeller001");
            }
        }

        return true;
    }

    private void cacheIndexPositions() throws HopException {
        if (data.indexOfCorpusField < 0) {
            data.indexOfCorpusField = data.previousRowMeta.indexOfValue(meta.getCorpusField());
            if (data.indexOfCorpusField < 0) {
                // The field is unreachable !
                throw new HopException(
                        BaseMessages.getString(
                                PKG, "TaxonomyLabeller.Exception.CouldnotFindField", meta.getCorpusField()));
            }
        }
        if (data.indexOfTaxonomyField < 0) {
            data.indexOfTaxonomyField = data.previousRowMeta.indexOfValue(meta.getTaxonomyField());
            if (data.indexOfTaxonomyField < 0) {
                // The field is unreachable !
                throw new HopException(
                        BaseMessages.getString(
                                PKG, "TaxonomyLabeller.Exception.CouldnotFindField", meta.getTaxonomyField()));
            }
        }

        for (String field : of("index", "series", "max_word_count", "word_count", "character_count", "text")) {
            field = meta.getOutputPhraseFieldNamePrefix() + field;
            if (data.indexOfMap.getOrDefault(field, -1) < 0) {
                data.indexOfMap.put(field, data.outputRowMeta.indexOfValue(field));
            }
        }

        Set<String> categories = new TreeSet<>(of(split(meta.getTaxonomyCategories(), meta.getTaxonomyCategoriesDelimiter())));
        this.taxonomyCategories = new String[categories.size()];
        int i = 0;
        for (String c : categories) {
            this.taxonomyCategories[i] = trim(c);
            i++;
        }

        for (String category : taxonomyCategories) {
            String count = meta.getOutputLabelFieldNamePrefix() + "count_" + category;
            String list = meta.getOutputLabelFieldNamePrefix() + "list_" + category;
            if (data.indexOfMap.getOrDefault(list, -1) < 0) {
                data.indexOfMap.put(list, data.outputRowMeta.indexOfValue(list));
            }
            if (data.indexOfMap.getOrDefault(count, -1) < 0) {
                data.indexOfMap.put(count, data.outputRowMeta.indexOfValue(count));
            }
        }
    }

    private void processPhrase(Phrase phrase) throws HopTransformException {

        Object[] outputRow = new Object[phrase.getInputRow().length + data.indexOfMap.size() + OVER_ALLOCATE_SIZE];
        arraycopy(phrase.getInputRow(), 0, outputRow, 0, phrase.getInputRow().length);

        outputRow[data.indexOfMap.get(meta.getOutputPhraseFieldNamePrefix() + "index")] = phrase.getIndex();
        outputRow[data.indexOfMap.get(meta.getOutputPhraseFieldNamePrefix() + "series")] = phrase.getSeries();
        outputRow[data.indexOfMap.get(meta.getOutputPhraseFieldNamePrefix() + "max_word_count")] = phrase.getMaxPhraseWordCount();
        outputRow[data.indexOfMap.get(meta.getOutputPhraseFieldNamePrefix() + "word_count")] = phrase.getWordCount();
        outputRow[data.indexOfMap.get(meta.getOutputPhraseFieldNamePrefix() + "character_count")] = phrase.getCharacterCount();
        for (String category : taxonomyCategories) {
            Collection<String> terms = phrase.getTerms(category);
            outputRow[data.indexOfMap.get(meta.getOutputLabelFieldNamePrefix() + "count_" + category)] = (long) terms.size();
            outputRow[data.indexOfMap.get(meta.getOutputLabelFieldNamePrefix() + "list_" + category)] = join(terms, ",");
        }

        outputRow[data.indexOfMap.get(meta.getOutputPhraseFieldNamePrefix() + "text")] = phrase.getText();

        putRow(data.outputRowMeta, outputRow);
    }

    @Override
    public void putRow(IRowMeta rowMeta, Object[] row) throws HopTransformException {
        synchronized (this) {
            super.putRow(rowMeta, row);
        }
    }
}
