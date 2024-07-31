package ie.dcu.mcm.hop.pipeline.transforms.html2text;

import org.apache.hop.core.row.IRowMeta;
import org.apache.hop.pipeline.transform.BaseTransformData;
import org.apache.hop.pipeline.transform.ITransformData;

public class Html2TextData extends BaseTransformData implements ITransformData {
    public int indexOfHtmlField;
    public int indexOfOutputField;
    public IRowMeta previousRowMeta;
    public IRowMeta outputRowMeta;
    public int NrPrevFields;

    public Html2TextData() {
        super();
        indexOfHtmlField = -1;
        indexOfOutputField = -1;
    }
}
