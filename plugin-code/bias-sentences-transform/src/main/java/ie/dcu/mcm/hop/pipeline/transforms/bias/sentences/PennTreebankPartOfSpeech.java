package ie.dcu.mcm.hop.pipeline.transforms.bias.sentences;

import java.util.Collection;

import static java.util.Set.of;

// Part-of-Speech Standford Tagset (Penn Treebank)
// Source: https://stanfordnlp.github.io/CoreNLP/tools_pos_tagger_faq.html
// https://www.cis.upenn.edu/~bies/manuals/tagguide.pdf
public enum PennTreebankPartOfSpeech {
    CC("Coordinating conjunction"),
    CD("Cardinal number"),
    DT("Determiner"),
    EX("Existential there"),
    FW("Foreign word"),
    IN("Preposition or subordinating conjunction"),
    JJ("Adjective"),
    JJR("Adjective, comparative"),
    JJS("Adjective, superlative"),
    LS("List item marker"),
    MD("Modal"),
    NN("Noun, singular or mass"),
    NNS("Noun, plural"),
    NNP("Proper noun, singular"),
    NNPS("Proper noun, plural"),
    PDT("Predeterminer"),
    POS("Possessive ending"),
    PRP("Personal pronoun"),
    PRP$("Possessive pronoun"),
    RB("Adverb"),
    RBR("Adverb, comparative"),
    RBS("Adverb, superlative"),
    RP("Particle"),
    SYM("Symbol"),
    TO("to"),
    UH("Interjection"),
    VB("Verb, base form"),
    VBD("Verb, past tense"),
    VBG("Verb, gerund or present participle"),
    VBN("Verb, past participle"),
    VBP("Verb, non-3rd person singular present"),
    VBZ("Verb, 3rd person singular present"),
    WDT("Wh-determiner"),
    WP("Wh-pronoun"),
    WP$("Possessive wh-pronoun"),
    WRB("Wh-adverb"),
    AFX("Affix"),
    GW("Additional word in multi-word expression");;

    public static final Collection<PennTreebankPartOfSpeech> NOUNS = of(NN, NNP, NNPS, NNS);
    public static final Collection<PennTreebankPartOfSpeech> VERBS = of(VB, VBD, VBG, VBN, VBP, VBZ);
    public static final Collection<PennTreebankPartOfSpeech> ADVERBS = of(RB, RBR, RBS, WRB);
    public static final Collection<PennTreebankPartOfSpeech> ADJECTIVES = of(JJ, JJR, JJS);

    private final String description;

    PennTreebankPartOfSpeech(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }


}
