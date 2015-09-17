package liir.nlp.pg.virtual;

import edu.berkeley.nlp.entity.bp.Domain;
import liir.nlp.representation.Sentence;
import liir.nlp.representation.Text;
import liir.nlp.representation.Word;
import liir.nlp.representation.entities.Mention;
import liir.nlp.representation.entities.MentionCluster;
import liir.nlp.representation.srl.Predicate;
import liir.utils.types.Tuple2;
import liir.utils.types.Tuple3;
import org.apache.commons.lang3.tuple.Pair;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by quynhdo on 07/09/15.
 */
public class Mapper {

    public Mapper() {
    }

    private boolean checkDependencyCCRelation(Predicate p1, Predicate p2){
        if (p1.getSentence().getId().equals(p2.getSentence().getId())) //in same sentence
        {
            Sentence s = p1.getSentence();
            Word hOfp2 = s.getWord(p2.getHead());
            if (hOfp2 != null)
                if (hOfp2.getPos().equals("CC"))
                {
                    Word hOfhOfp2 = s.getWord(hOfp2.getHead());
                    if (hOfhOfp2!=null)
                        if (hOfhOfp2.getId().equals(p1.getId()))
                            return  true;
                }

        }
        return false;
    }


    private boolean checkDependencyTimeRelation(Predicate p1, Predicate p2){
        HashMap<String,String> rels=new HashMap<>();
        rels.put("at", "when");
        rels.put("while", "while");
        rels.put("when", "when");

        if (p1.getSentence().getId().equals(p2.getSentence().getId())) //in same sentence
        {
            Sentence s = p1.getSentence();
            Word hOfp2 = s.getWord(p2.getHead());
            if (hOfp2 != null)
                if ( rels.containsKey(hOfp2.getLemma()))
                {
                    Word hOfhOfp2 = s.getWord(hOfp2.getHead());
                    if (hOfhOfp2!=null)
                        if (hOfhOfp2.getId().equals(p1.getId()))
                            return  true;
                }

        }
        return false;
    }

    private boolean checkDependencyIfRelation(Predicate p1, Predicate p2){
        if (p1.getSentence().getId().equals(p2.getSentence().getId())) //in same sentence
        {
            Sentence s = p1.getSentence();
            Word hOfp2 = s.getWord(p2.getHead()); //p2 is if clause
            if (hOfp2 != null)
                if (hOfp2.getPos().startsWith("V"))
                {
                    Word hOfhOfp2 = s.getWord(hOfp2.getHead());
                    if (hOfhOfp2!=null)
                        if (hOfhOfp2.getLemma().equals("if"))
                        {
                           Word  hOfIfClause = s.getWord(hOfhOfp2.getHead());
                            if (hOfIfClause != null){
                                Word hOfp1 = s.getWord(p1.getHead());
                                if (hOfp1.getId().equals(hOfIfClause.getId()))
                                    return true;
                                Word hOfhOfp1 = s.getWord(hOfp1.getHead());
                                if (hOfhOfp1!=null){
                                    if (hOfhOfp1.getId().equals(hOfIfClause.getId()))
                                        return  true;
                                }
                            }

                        }
                }

        }
        return false;
    }


    public ArrayList<PddlFrame> mergeFrames(Text txt,  ArrayList<PddlFrame> rs)
    {
        ArrayList<PddlFrame> removes=new ArrayList<>();
        for (Sentence s : txt){
            ArrayList<PddlFrame > fs= new ArrayList<>();


            for (PddlFrame f : rs)
                if (f.getPred().getSentence().getId().equals(s.getId()))
                    fs.add(f);

            for (int i=0; i<fs.size()-1;i++){
                for (int j=i+1;j<fs.size(); j++)

                {
                    PddlFrame f = fs.get(i);
                    PddlFrame ff = fs.get(j);
                    if (f != ff){
                        if (f.name.equals(ff.name)){
                            if (ff.argumentVals.size() > f.argumentVals.size()) {
                                boolean toremove=true;
                                for (String k: f.argumentVals.keySet()){
                                    if (!ff.argumentVals.containsKey(k))
                                        toremove=false;
                                }
                                if (toremove)
                                removes.add(f);
                            }
                            else {
                                boolean toremove=true;
                                for (String k: ff.argumentVals.keySet()){
                                    if (!f.argumentVals.containsKey(k))
                                        toremove=false;
                                }
                                if (toremove)

                                removes.add(ff);
                            }

                        }
                    }
                }
            }
        }

        for (PddlFrame k : removes){
            rs.remove(k);
        }
        return removes;
    }
    public void postProcessing(Text txt, DomainReader dr, ArrayList<PddlFrame> rs){

        ArrayList<Tuple3<Integer,Integer,String>> rels= new ArrayList<>();
        //post-process consultation and providing information
              for (int i=1;i<rs.size();i++){
                    if (rs.get(i).name.equals("providing-information")) {
                        if (rs.get(i-1).name.equals("consultation")){

                            rs.get(i).moreVals.put("scenario", String.valueOf(rs.get(i-1).id));
                            if ((!rs.get(i).getArgumentVals().containsKey("char_sub")) && rs.get(i-1).getArgumentVals().containsKey("char_sub"))
                                rs.get(i).argumentVals.put("char_sub", rs.get(i - 1).getArgumentVals().get("char_sub"));


                        }
                    }

                  if (checkDependencyCCRelation(rs.get(i - 1).getPred(), rs.get(i).getPred())){

                      if ((!rs.get(i).getArgumentVals().containsKey("char_sub")) && rs.get(i-1).getArgumentVals().containsKey("char_sub"))
                          rs.get(i).argumentVals.put("char_sub", rs.get(i - 1).getArgumentVals().get("char_sub"));

                      if ((!rs.get(i-1).getArgumentVals().containsKey("char_sub")) && rs.get(i).getArgumentVals().containsKey("char_sub"))
                          rs.get(i-1).argumentVals.put("char_sub", rs.get(i).getArgumentVals().get("char_sub"));

                  }


                  if (checkDependencyIfRelation(rs.get(i).getPred(), rs.get(i - 1).getPred())){

                      rs.get(i-1).moreVals.put("isIfClauseOf", String.valueOf(rs.get(i).id));
                      rs.get(i-1).moreVals.put("modality", "if_clause");

                      rs.get(i).moreVals.put("modality", "conditional");

                  }

                  if (checkDependencyIfRelation(rs.get(i-1).getPred(), rs.get(i ).getPred())){

                      rs.get(i).moreVals.put("isIfClauseOf", String.valueOf(rs.get(i-1).id));
                      rs.get(i).moreVals.put("modality", "if_clause");

                      rs.get(i-1).moreVals.put("modality", "conditional");

                  }

                  if (checkDependencyTimeRelation(rs.get(i - 1).getPred(), rs.get(i).getPred())){

                      rs.get(i-1).moreVals.put("occurWhen", String.valueOf(rs.get(i).id));


                  }
              }



        for (int i=0;i<rs.size()-1;i++) {
            if (rs.get(i).name.equals("providing-information")) {
                if (rs.get(i + 1).name.equals("consultation")) {

                    rs.get(i).moreVals.put("scenario", String.valueOf(rs.get(i + 1).id));


                }
            }
        }



    }


    public  void map (Text txt, DomainReader dr ){

        ArrayList<PddlFrame> matchedFrames=new ArrayList<>();

        HashMap<Sentence,ArrayList<PddlFrame>> rs=new HashMap<>();

        for (Predicate pred : txt.getPredicates()){

            for (PddlFrame f : dr.frames) {
                if (matchFrame(pred, f,dr)) {

                    PddlFrame realf = new PddlFrame(f.name);
                    realf.map = f.map;
                    realf.argumentValDefault= f.argumentValDefault;
                    realf.setPred(pred);

                    ArrayList<Tuple2<String,String>> args = matchArgument(pred, f, dr);
                    for (Tuple2<String,String> arg : args)
                        realf.addArgumentVal(arg.getFirst(),arg.getSecond());
                    realf.id= matchedFrames.size();

                    matchedFrames.add(realf);

                    if (rs.containsKey(pred.getSentence())){
                        ArrayList<PddlFrame> arr =rs.get(pred.getSentence());
                        arr.add(realf);
                        rs.put(pred.getSentence(), arr);
                    }
                    else
                    {
                        ArrayList<PddlFrame>arr =new ArrayList<>();
                        arr.add(realf);
                        rs.put(pred.getSentence(), arr);
                    }
                }
            }
        }




        postProcessing(txt, dr, matchedFrames);

        for (PddlFrame f : matchedFrames){
            f.postProcessing(dr);
            f.setModality();
            f.cleanCharSub();
        }


        /*
        for (PddlFrame f : matchedFrames){
            System.out.println(f.getPred().toPredicateXMLString());
            System.out.println(f.toString());
        }
*/
      ArrayList<PddlFrame> removes=  mergeFrames(txt,  matchedFrames);
        for (Sentence s : txt){
            System.out.println( s.toString());
            if (rs.containsKey(s))
            for (PddlFrame f : rs.get(s)){
                {
                    if (!removes.contains(f)){
                        System.out.println(f.toString());
                    }

                }
            }
        }

    }


    private  boolean matchEntity (Mention m, String s, Text txt, boolean use_lemma, ArrayList<String> heads){

        String[] tmps = s.split("\\s+");
        List<String>  strs;
        if (use_lemma)
        strs = m.asStringLemmaList(txt);
        else
            strs = m.asStringList(txt);

        boolean match = true;
        for (int i = 0 ; i<tmps.length ; i++)
        if (!strs.contains(tmps[i]))
            match = false;

        if (m.getHeadAsWord(txt).getPos().startsWith("N")   ) {
            String head;

            if (use_lemma)
                head = m.getHeadAsWord(txt).getLemma();

            else
                head = m.getHeadAsWord(txt).getStr().toLowerCase();

            boolean m_head = false;
            for (int i = 0; i < tmps.length; i++)
                if (tmps[i].equals(head))
                    m_head = true;

            if (!m_head) {

                if (heads != null) {
                    if (!heads.contains(head))
                        match = false;


                } else


                    match = false;
            }
        }

      //  if (strs.size() > tmps.length +2)
        //    match = false;
        if(!match) {

            List<Mention> detailedMention = m.getDetailedMention(txt);
            for (Mention mm : detailedMention) {

           //     System.out.println(mm.toXMLString());
                if (matchEntity(mm, s, txt, use_lemma, heads))

                    return true;
            }
        }


        if (!match){

                for (MentionCluster mc : txt.getMentionClusters()) {
                    List<Mention> inSameChain = mc.inSameChainWith(m.getId(), txt);
                    if (inSameChain.size()!=0){
                        for (Mention mm : inSameChain) {

                            if (Integer.parseInt(mm.getSenId()) < Integer.parseInt(m.getSenId()))
                                if (matchEntity(mm, s, txt, use_lemma, heads))
                                    return true;
                        }
                    }

            }

        }

        return match;




    }

    private boolean matchFrame(Predicate pred, PddlFrame f, DomainReader dr){

        if (f.verbs.contains(pred.getLemma()))  // match verb
        {
            if (f.arguments.size()==0)
                return true;
            for (PddlFrameArgument arg: f.arguments){

                boolean match_arg= false;
                String srlLabel = arg.getLabel();

                List<Mention> argm = pred.getArgumentAsMention(srlLabel);

                if (arg.getVal()!=null) {
                    if (!arg.getVal().equals("")) {


                        for (Mention m : argm) {
                            if (matchEntity(m, arg.getVal(), pred.getSentence().getText(), arg.getMatch_lemma(), null))
                                match_arg = true;
                        }

                    }
                    else
                    {
                        match_arg = true;
                    }
                }

                if (!match_arg) {
                    if (arg.getType() != null)
                        if (!arg.getType().equals("")) {


                            ArrayList<String> possible_pddl_vals = new ArrayList<>();
                            if (dr.types.containsKey(arg.getType()))
                                possible_pddl_vals = dr.types.get(arg.getType());
                            possible_pddl_vals.add(arg.getType());
                            for (String possible_pddl_val : possible_pddl_vals) {
                                ArrayList<String> possible_vals = new ArrayList<>();

                                if (dr.typeMaps.containsKey(possible_pddl_val)) {
                                    possible_vals.addAll(dr.typeMaps.get(possible_pddl_val));
                                }
                                possible_vals.add(possible_pddl_val);

                                for (Mention m : argm) {
                                    for (String val : possible_vals)
                                        if (matchEntity(m, val, pred.getSentence().getText(), arg.getMatch_lemma(), dr.headMaps.get(arg.getType()))) {
                                            match_arg = true;

                                        }
                                }

                            }


                        }
                }

                if (!arg.is_negation)
                if (!match_arg)
                    return false;

            }

        return true;
        }

        return false;
    }

    private  ArrayList<Tuple2<String,String>> matchArgument(Predicate pred, PddlFrame f,  DomainReader dr){

        ArrayList<Tuple2<String,String>> rs = new ArrayList<>();
        for (Tuple3<String,String,String> argument : f.map){
            if (!argument.getThird().equals("")){
                String r = argument.getThird();
                List<Mention> srl_args = pred.getArgumentAsMention(r);

                String t = argument.getSecond();
                if (!t.equals("")) {
                    ArrayList<String> possible_pddl_vals = new ArrayList<>();
                    if (dr.types.containsKey(t))
                     possible_pddl_vals = dr.types.get(t);
                    possible_pddl_vals.add(t);
                    for (String possible_pddl_val : possible_pddl_vals) {
                        ArrayList<String> possible_vals = new ArrayList<>();

                        if (dr.typeMaps.containsKey(possible_pddl_val)) {
                            possible_vals.addAll(dr.typeMaps.get(possible_pddl_val));
                        }
                        possible_vals.add(possible_pddl_val);

                        for (Mention m : srl_args) {
                            for (String val : possible_vals)
                                if (matchEntity(m, val, pred.getSentence().getText(), true, dr.headMaps.get(t))) {
                                    rs.add(new Tuple2<String, String>(argument.getFirst(), possible_pddl_val));

                                }
                        }

                    }
                }else {
                    for (Mention m : srl_args) {
                        rs.add(new Tuple2<String, String>(argument.getFirst(), m.toString(pred.getSentence().getText())));

                    }
                }



            }
            else
            {
                rs.add(new Tuple2<>(argument.getFirst(), argument.getSecond()));
            }
        }

        return rs;
    }



}
