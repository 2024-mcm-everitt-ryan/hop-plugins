package ie.dcu.mcm.hop.pipeline.transforms.taxonomy;

import org.apache.hop.core.row.IRowMeta;
import org.apache.hop.pipeline.transform.BaseTransformData;
import org.apache.hop.pipeline.transform.ITransformData;

import java.util.Map;
import java.util.TreeMap;

public class TaxonomyLabellerData extends BaseTransformData implements ITransformData {
    public int indexOfCorpusField;
    public int indexOfTaxonomyField;
    public Map<String, Integer> indexOfMap = new TreeMap<>();

    public IRowMeta previousRowMeta;
    public IRowMeta outputRowMeta;
    public int NrPrevFields;

    public TaxonomyLabellerData() {
        super();
        indexOfCorpusField = -1;
        indexOfTaxonomyField = -1;
    }
}
