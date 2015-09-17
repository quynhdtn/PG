package liir.nlp.pg.virtual;

import edu.berkeley.nlp.entity.bp.Domain;
import liir.nlp.representation.Sentence;
import liir.nlp.representation.Word;
import liir.nlp.representation.srl.Predicate;
import liir.utils.types.Tuple3;
import org.apache.commons.lang3.tuple.Pair;
import scala.Array;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by quynhdo on 07/09/15.
 */
public class PddlFrame {
    int id;
    String name;
    ArrayList<String> verbs;
    ArrayList<PddlFrameArgument> arguments;

    HashMap<String,ArrayList<String>> argumentVals;
    HashMap<String,String> argumentValDefault;

    HashMap<String,String> moreVals;

    ArrayList<Tuple3<String,String,String>> map;

    Predicate pred;
    public PddlFrame(String name){
        this.name = name;
        verbs = new ArrayList<>();
        arguments = new  ArrayList<>();
        map = new ArrayList<>();
        argumentVals = new HashMap<>();
        argumentValDefault=new HashMap<>();
        moreVals = new HashMap<>();

    }

    public void addVerb(String v){
        verbs.add(v);
    }

    public void addArgument(PddlFrameArgument arg){
        arguments.add(arg);
    }

    public void setPred(Predicate pred) {
        this.pred = pred;
    }

    public void setArgumentVals(HashMap<String, ArrayList<String>> argumentVals) {
        this.argumentVals = argumentVals;
    }

    public HashMap<String, ArrayList<String>> getArgumentVals() {

        return argumentVals;
    }

    public Predicate getPred() {
        return pred;
    }

    public void addArgumentVal(String name, String v){
        if (argumentVals.containsKey(name)){
            ArrayList<String> arr = argumentVals.get(name);
            if (! arr.contains(v))
            arr.add(v);
            argumentVals.put(name,arr);
        }
        else
        {
            ArrayList<String> arr =new ArrayList<>();
            arr.add(v);
            argumentVals.put(name,arr);
        }
    }

    public void addArgumentValDefault(String name, String v){
        argumentValDefault.put(name,v);
    }

    public HashMap<String, String> getArgumentValDefault() {
        return argumentValDefault;
    }


    public void postProcessing(DomainReader dr){
        HashMap<String,ArrayList<String>> newvals = new HashMap<>();
        for (String k : argumentVals.keySet()){

            ArrayList<String> vals = argumentVals.get(k);
            ArrayList<String> valsNew= new ArrayList<>();

            if (k.equals("char_sub")){
                for (String v : vals){
                    if (v.contains("team")){
                        if (dr.types.containsKey(v))
                        for (String x : dr.types.get(v))
                        if (!valsNew.contains(x))

                            valsNew.add(x);
                    }
                }

            }


            if (k.equals("time")){

                if (moreVals.containsKey("occurWhen")) {

                    continue;


                }
            }

            for (String v : vals){




                if (dr.types.containsKey(v)){
                    boolean toRemove=false;
                    for (String v1 : vals){
                        if (dr.types.get(v).contains(v1)){
                         toRemove = true;
                            break;

                        }
                    }
                    if (!toRemove)
                        if (!valsNew.contains(v))
                        valsNew.add(v);
                }
                else
                {
                    if (!valsNew.contains(v))
                    valsNew.add(v);
                }
            }
            newvals.put(k, valsNew);
        }

        argumentVals=newvals;

        ///negation

        for (String k : argumentVals.keySet()){
            if (k.equals("neg")){
                ArrayList<String> arr = argumentVals.get(k);

                if (arr.contains("not")) {
                    arr.clear();
                    arr.add("yes");
                    argumentVals.put(k, arr);
                }
            }


        }

        for (String k : argumentValDefault.keySet()){
            if (k.equals("neg")){
                if (argumentValDefault.get(k).equals("not"))
                    argumentValDefault.put(k, "yes");
            }
        }



        newvals = new HashMap<>();
        for (String k : argumentVals.keySet()){

            ArrayList<String> vals = argumentVals.get(k);
            ArrayList<String> valsNew= new ArrayList<>();


            for (String v : vals){




                if (dr.types.containsKey(v)){
                    boolean toRemove=false;
                    for (String v1 : vals){
                        if (dr.types.get(v).contains(v1)){
                            toRemove = true;
                            break;

                        }
                    }
                    if (!toRemove)
                        if (!valsNew.contains(v))
                        valsNew.add(v);
                }
                else
                {
                    if (!valsNew.contains(v))
                    valsNew.add(v);
                }
            }
            newvals.put(k, valsNew);
        }

        argumentVals=newvals;


    }


    public String toString()
    {
        StringBuilder sb=new StringBuilder();
        sb.append("FRAME------\n");
        sb.append("Name: " + name + " ID: " + String.valueOf(id) +  "\n");

        ArrayList<String> argNames=new ArrayList<>();
        for (Tuple3<String,String,String> m: map){
            if (!argNames.contains(m.getFirst()))
                argNames.add(m.getFirst());
        }

        for (String m: argNames){
            if (argumentVals.containsKey(m)){
                for (String s : argumentVals.get(m)){
                    sb.append(m + ": " + s + "\n");
                }
            }
            else
            {
                if (argumentValDefault.containsKey(m))
                    sb.append(m + ": " + argumentValDefault.get(m) + "\n");
            }
        }


        for (String  k:  moreVals.keySet()){
            sb.append(k + ": " + moreVals.get(k) + "\n");
        }
        sb.append("******\n");

        return sb.toString();

    }


    public void setModality() {
        Sentence s = pred.getSentence();
        Word h = s.getWord(pred.getHead());
        if (h != null) {
            if (h.getLemma().equals("will") || h.getLemma().equals("shall")) {
                moreVals.put("modality", "future");
                return;
            }
            if (h.getLemma().startsWith("V")) {
                Word hOfh = s.getWord(h.getId());
                if (hOfh != null) {
                    if (hOfh.getLemma().equals("will") || hOfh.getLemma().equals("shall")) {
                        moreVals.put("modality", "future");
                        return;
                    }
                }

            }
        }

    }


    public void cleanCharSub() {

        if (name.startsWith("require")){
            if (argumentVals.containsKey("char_sub")){

                if (argumentVals.get("char_sub").size() > 0){
                    argumentVals.remove("char_sub");
                    return;
                }
            }

        }

    }
}
