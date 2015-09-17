package liir.nlp.pg.virtual;

import java.util.ArrayList;

/**
 * Created by quynhdo on 10/09/15.
 */
public class PddlFrameArgument {
    String type;
    String val;
    String label;
    Boolean is_negation = false;
    Boolean match_lemma = true;

    public PddlFrameArgument( String label, String val) {
        this.val = val;
        this.label = label;
    }

    public String getType() {
        return type;
    }

    public String getVal() {
        return val;
    }

    public String getLabel() {
        return label;
    }

    public Boolean getIs_negation() {
        return is_negation;
    }

    public Boolean getMatch_lemma() {
        return match_lemma;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setVal(String val) {
        this.val = val;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public void setIs_negation(Boolean is_negation) {
        this.is_negation = is_negation;
    }

    public void setMatch_lemma(Boolean match_lemma) {
        this.match_lemma = match_lemma;
    }


}
