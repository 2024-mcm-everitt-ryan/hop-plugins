package ie.dcu.mcm.hop.pipeline.transforms.bias.sentences;

import org.apache.commons.collections4.multimap.HashSetValuedHashMap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map.Entry;

import static java.lang.String.format;
import static org.apache.commons.lang3.StringUtils.join;
import static ie.dcu.mcm.hop.pipeline.transforms.bias.sentences.TaxonomyTermAnnotation.Builder.biasTermBuilder;

public class TaxonomyTermAnnotations extends HashSetValuedHashMap<String, TaxonomyTermAnnotation> {

    public String toJson() {

        List<String> categories = new ArrayList<>(this.keySet().size());
        for (Entry<String, Collection<TaxonomyTermAnnotation>> e : this.asMap().entrySet()) {
            List<String> terms = new ArrayList<>(e.getValue().size());
            for (TaxonomyTermAnnotation v : e.getValue()) {
                terms.add(v.toJson());
            }

            categories.add(format("\"%s\":[%s]", e.getKey(), join(terms, ",")));
        }

        StringBuilder b = new StringBuilder();
        b.append("{");
        b.append(join(categories, ","));
        b.append("}");
        return b.toString();
    }

    public static void main(String[] args) {
        TaxonomyTermAnnotations annotations = new TaxonomyTermAnnotations();
        annotations.put("masculine", biasTermBuilder().term("Training & Development").build());
        annotations.put("masculine", biasTermBuilder().term("under pressure").build());
        annotations.put("masculine", biasTermBuilder().term("self-starter").build());
        annotations.put("masculine", biasTermBuilder().term("he").build());

        annotations.put("feminine", biasTermBuilder().term("communication skills").build());
        annotations.put("feminine", biasTermBuilder().term("coaching").build());
        annotations.put("feminine", biasTermBuilder().term("she").build());

        annotations.put("general", biasTermBuilder().term("implement").build());

        System.out.println(annotations.toJson());
    }
}
