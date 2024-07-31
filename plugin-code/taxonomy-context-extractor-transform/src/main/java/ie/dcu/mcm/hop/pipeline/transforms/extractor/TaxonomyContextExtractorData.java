package ie.dcu.mcm.hop.pipeline.transforms.extractor;

import org.apache.hop.core.row.IRowMeta;
import org.apache.hop.pipeline.transform.BaseTransformData;
import org.apache.hop.pipeline.transform.ITransformData;

import java.util.Map;
import java.util.TreeMap;

public class TaxonomyContextExtractorData extends BaseTransformData implements ITransformData {
    public int indexOfCorpusField;
    public int indexOfTaxonomyField;
    public int indexOfDocumentPartitionField;
    public Map<String, Integer> indexOfMap = new TreeMap<>();
    public String[] contextFields = {
            "taxonomy_term",
            "taxonomy_category",
            "context_measurement",
            "context_size",
            "context_size_left_current",
            "context_size_left_target",
            "context_size_left_max",
            "context_size_right_current",
            "context_size_right_target",
            "context_size_right_max",
            "context_text"
    };

    public IRowMeta previousRowMeta;
    public IRowMeta outputRowMeta;
    public int NrPrevFields;

    public TaxonomyContextExtractorData() {
        super();
        indexOfCorpusField = -1;
        indexOfTaxonomyField = -1;
        indexOfDocumentPartitionField = -1;
    }
}
