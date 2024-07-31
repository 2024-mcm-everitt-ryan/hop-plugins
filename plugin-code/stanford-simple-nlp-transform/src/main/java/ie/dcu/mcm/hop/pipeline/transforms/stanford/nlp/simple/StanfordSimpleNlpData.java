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

package ie.dcu.mcm.hop.pipeline.transforms.stanford.nlp.simple;

import org.apache.hop.core.row.IRowMeta;
import org.apache.hop.pipeline.transform.BaseTransformData;
import org.apache.hop.pipeline.transform.ITransformData;

import java.util.Map;
import java.util.TreeMap;

public class StanfordSimpleNlpData extends BaseTransformData implements ITransformData {
    public int indexOfCorpusField;
    public Map<String, Integer> indexOfMap = new TreeMap<>();
    public String[] sentenceFields = {
            "text",
            "index",
            "index_start",
            "index_end",
            "character_count",
            "word_count"
    };
    public String[] posFields = {
            "pos_tagged",
            "pos_tags"
    };

    public IRowMeta previousRowMeta;
    public IRowMeta outputRowMeta;
    public int NrPrevFields;

    public StanfordSimpleNlpData() {
        super();
        indexOfCorpusField = -1;
    }
}
