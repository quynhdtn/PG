package liir.nlp.pg.virtual;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.Array;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import liir.nlp.representation.Text;
import liir.utils.types.Tuple3;
import org.apache.commons.lang3.tuple.Pair;
import org.xml.sax.SAXException;

import static org.joox.JOOX.$;


/**
 * Created by quynhdo on 04/09/15.
 * This class is used to read pddl file
 */
public class DomainReader {


    HashMap<String, ArrayList<String>> types;
    HashMap<String,ArrayList<String>> typeMaps ;
    HashMap<String,ArrayList<String>> headMaps ;
    ArrayList<PddlPredicate> predicates;
    ArrayList<PddlAction> actions;
    ArrayList<PddlFrame> frames;
    public DomainReader() {
        types =new HashMap<>();
        predicates = new ArrayList<>();
        actions=new ArrayList<>();
        frames = new ArrayList<>();
        typeMaps = new HashMap<>();
        headMaps=new HashMap<>();


    }

    public void readDictionary(String path) throws IOException, SAXException { //read the xml file

        StringReader sr = new StringReader(  new String(Files.readAllBytes(Paths.get(path))));

        $(sr).find("headMap")
                .each(ftx -> {



                    $(ftx).find("map").each(
                            mtx -> {
                                String mtext = $(mtx).text();
                                if (headMaps.containsKey($(ftx).attr("name"))) {
                                    ArrayList<String> tms = headMaps.get($(ftx).attr("name"));
                                    tms.add(mtext);
                                    headMaps.put($(ftx).attr("name"), tms);
                                } else {
                                    ArrayList<String> tms = new ArrayList<String>();
                                    tms.add(mtext);
                                    headMaps.put($(ftx).attr("name"), tms);

                                }


                            }
                    );
                });
        sr = new StringReader(  new String(Files.readAllBytes(Paths.get(path))));
        $(sr).find("typeAndConstant")
                .each(ftx -> {
                    String val =  $(ftx).attr("val");

                    if(!types.containsKey($(ftx).attr("name"))) {
                        ArrayList<String> tmp=new ArrayList<String>();
                        tmp.add(val);
                        types.put($(ftx).attr("name"), tmp);
                    }
                    else{
                        ArrayList<String> tmp= types.get($(ftx).attr("name"));
                        if (!tmp.contains(val))
                        {
                            tmp.add(val);
                            types.put($(ftx).attr("name"), tmp);
                        }
                    }

                    $(ftx).find("map").each(
                            mtx -> {
                                String mtext = $(mtx).text();
                                if (typeMaps.containsKey(val)) {
                                    ArrayList<String> tms = typeMaps.get(val);
                                    tms.add(mtext);
                                    typeMaps.put(val, tms);
                                } else {
                                    ArrayList<String> tms = new ArrayList<String>();
                                    tms.add(mtext);
                                    typeMaps.put(val, tms);

                                }


                            }
                    );
                });
        sr = new StringReader(  new String(Files.readAllBytes(Paths.get(path))));



        $(sr)
                .find("predicate")
                .each(ctx -> {
                    $(ctx).find("frame")
                            .each(ftx -> {
                                PddlFrame f = new PddlFrame($(ctx).attr("val"));

                                $(ftx).find("verb").each(v -> {
                                    f.addVerb($(v).attr("val"));
                                });
                                $(ftx).find("argument").each(arg -> {
                                    PddlFrameArgument farg= new PddlFrameArgument($(arg).attr("label"), $(arg).attr("val"));

                                    farg.setType($(arg).attr("type"));

                                    if ($(arg).attr("neg")!=null)
                                        farg.setIs_negation(Boolean.valueOf($(arg).attr("neg")));



                                    if ($(arg).attr("useLemma")!=null)
                                        farg.setMatch_lemma(Boolean.valueOf($(arg).attr("useLemma")));


                                    f.addArgument(farg);

                                });

                                $(ftx).find("map").each(arg -> {
                                    f.map.add(new Tuple3<>($(arg).attr("name"), $(arg).attr("type"), $(arg).attr("role")));
                                });

                                $(ftx).find("default").each(arg -> {
                                    f.addArgumentValDefault($(arg).attr("name"), $(arg).attr("val"));

                                });

                                frames.add(f);

                            });




                });
        sr = new StringReader(  new String(Files.readAllBytes(Paths.get(path))));



        $(sr)
                .find("action")
                .each(ctx -> {
                    $(ctx).find("frame")
                            .each(ftx -> {
                                PddlFrame f = new PddlFrame($(ctx).attr("val"));

                                $(ftx).find("verb").each(v -> {
                                    f.addVerb($(v).attr("val"));
                                });
                                $(ftx).find("argument").each(arg -> {


                                    PddlFrameArgument farg= new PddlFrameArgument($(arg).attr("label"), $(arg).attr("val"));

                                    farg.setType($(arg).attr("type"));

                                    if ($(arg).attr("neg")!=null)
                                        farg.setIs_negation(Boolean.valueOf($(arg).attr("neg")));



                                    if ($(arg).attr("useLemma")!=null)
                                        farg.setMatch_lemma(Boolean.valueOf($(arg).attr("useLemma")));


                                     f.addArgument(farg);

                                });


                                $(ftx).find("map").each(arg -> {
                                    f.map.add(new Tuple3<>($(arg).attr("name"), $(arg).attr("type"), $(arg).attr("role")));


                                });

                                $(ftx).find("default").each(arg -> {
                                    f.addArgumentValDefault($(arg).attr("name"), $(arg).attr("val"));

                                });


                                frames.add(f);

                            });




                });
    }


    public void readPddlTypes(String path, String groupLabel) throws FileNotFoundException {

        File file = new File(path);
        Scanner in = new Scanner(file);
        boolean start_group = false;
        while (in.hasNextLine()) {
            String line = in.nextLine().trim();

            if (line.startsWith("(:" + groupLabel))  //start reading types
            {
                start_group = true;
                continue;

            }
            if (start_group){
                if (line.startsWith(";") || line.matches("\\s*"))
                    continue;

                if (line.startsWith(")")) return;


                String[] tmps = line.split("\\s+");
                String label = tmps[tmps.length-1];
                if (types.containsKey(label)){
                        ArrayList<String> arr= types.get(label);
                        for (int i=0;i<tmps.length-2; i++) {
                            arr.add(tmps[i]);
                        }
                        types.put(label, arr);
                    }
                    else
                    {
                        ArrayList<String> arr=new ArrayList<>();
                        for (int i=0;i<tmps.length-2; i++) {
                            arr.add(tmps[i]);
                        }
                        types.put(label, arr);
                    }
                }



        }


    }

/*
    public void readPddlPredicates(String path) throws FileNotFoundException {

        File file = new File(path);
        Scanner in = new Scanner(file);
        boolean start_group = false;
        while (in.hasNextLine()) {
            String line = in.nextLine().trim();

            if (line.startsWith("(:predicates" ))  //start reading types
            {
                start_group = true;
                continue;

            }
            if (start_group){
                if (line.startsWith(";") || line.matches("\\s*"))
                    continue;

                if (line.startsWith(")")) return;

                Pattern p = Pattern.compile("\\((.+)\\)(.*)");
                Matcher m = p.matcher (line);

                if (m.matches()) {
                    String func = m.group(1);
                    String[] tmps = func.split("\\?");
                    String name = tmps[0].trim();
                    ArrayList<Pair<String,String>> arguments= new ArrayList<>();
                    for (int j =1 ; j<tmps.length ; j++)
                    {


                        String[] args = tmps[j].split("\\s-\\s");
                        if (args.length>1) {
                            arguments.add(Pair.of(args[0].trim(), args[1].trim()));
                        }

                    }
                    if (arguments.size()>0)
                    predicates.put(name, arguments);
                }

                }
            }



        }

*/
public void readPddlPredicates(String path) throws FileNotFoundException {

    File file = new File(path);
    Scanner in = new Scanner(file);
    boolean start_group = false;
    while (in.hasNextLine()) {
        String line = in.nextLine().trim();

        if (line.startsWith("(:predicates" ))  //start reading types
        {
            start_group = true;
            continue;

        }
        if (start_group){
            if (line.startsWith(";") || line.matches("\\s*"))
                continue;

            if (line.startsWith(")")) return;

            Pattern p = Pattern.compile("\\((.+)\\)(.*)");
            Matcher m = p.matcher (line);

            if (m.matches()) {
                PddlPredicate pred=new PddlPredicate();

                String func = m.group(1);
                String[] tmps = func.split("\\?");
                pred.name = tmps[0].trim();
                ArrayList<Pair<String,String>> arguments= new ArrayList<>();
                for (int j =1 ; j<tmps.length ; j++)
                {


                    String[] args = tmps[j].split("\\s-\\s");
                    if (args.length>1) {
                        PddlArgument parg=new PddlArgument(args[0].trim(), args[1].trim());
                        pred.arguments.add(parg);
                    }

                }
                    predicates.add(pred);
            }

        }
    }



}
    public void readPddlActions(String path) throws FileNotFoundException {

        File file = new File(path);
        Scanner in = new Scanner(file);
        boolean start_group = false;
        boolean start_condition = false;
        boolean start_effect = false;
        boolean start_and = false;



        PddlAction  act = new PddlAction();
        while (in.hasNextLine()) {
            String line = in.nextLine();

            if (line.startsWith("(:action" ))  //start reading types
            {
                start_group = true;
                Pattern p = Pattern.compile("\\((:action\\s)([^\\s]+).*");
                Matcher m = p.matcher (line);

                if (m.matches()) {
                    String func = m.group(2);
                    act.name = func;

                }
                continue;

            }
            if (start_group){
                if (line.startsWith(";") || line.matches("\\s*"))
                    continue;

                if (line.startsWith(")")) {
                    start_group = false;
                    start_condition = false;
                    if (act.name != null)
                    {
                        actions.add(new PddlAction(act));
                        act = new PddlAction();
                    }
                }

                Pattern p = Pattern.compile("\\s+(:parameters\\s+)\\(([^;]+)\\).*");  //detect parameters
                Matcher m = p.matcher (line);

                if (m.matches()) {
                    String temp = m.group(2);
                    String[] tmps = temp.split("\\?");
                    for (int j =1 ; j<tmps.length ; j++)
                    {


                        String[] args = tmps[j].split("\\s-\\s");
                        if (args.length>1) {

                            PddlArgument parg = new PddlArgument(args[0],args[1]);
                            act.arguments.add(parg);
                        }

                    }

                }


                if (line.trim().startsWith(":precondition")){
                    start_condition = true;
                    continue;
                }
                if (start_condition){

                    if (line.trim().startsWith("(and")){
                        start_and = true;
                        continue;
                    }

                }
            }
        }



    }

    public void parseActionString(String[] lines){

    }

    private PddlPredicate parsePredicateInAction(String line){


        Pattern p = Pattern.compile("\\((.+)\\)(.*)");
        Matcher m = p.matcher (line);

        if (m.matches()) {
            PddlPredicate pred=new PddlPredicate();

            String func = m.group(1);
            String[] tmps = func.split("\\s+");
            pred.name = tmps[0].trim();
            for (int j =1 ; j<tmps.length ; j++)
            {
                    if (tmps[j].startsWith("?")) {
                        PddlArgument parg = new PddlArgument();
                        parg.name = tmps[j].substring(1);
                        pred.arguments.add(parg);
                    }
                else
                    {
                        PddlArgument parg = new PddlArgument();
                        parg.value = tmps[j];
                        pred.arguments.add(parg);
                    }


            }
        return pred;
        }

        return null;
    }
    public void makeDictionary(String output) throws IOException {
        StringBuilder sb= new StringBuilder();
        sb.append("<dict>\n");

        for (String t : types.keySet()){
            ArrayList<String> arr = types.get(t);
            arr.forEach(val -> sb.append("\t<typeAndConstant name=\"" + t + "\" val=\"" + val + "\">\n" + "\t</typeAndConstant>\n"));
        }
        for (PddlPredicate t : predicates){

            sb.append("\t<predicate val=\"" + t.name + "\">\n");

            for (PddlArgument arg : t.arguments){
                sb.append("\t<argument name=\"" + arg.name + "\" type=\"" +arg.type + "\"/>\n");
            }

            sb.append("\t</predicate>\n");
        }

        for (PddlAction t : actions){

            sb.append("\t<action val=\"" + t.name + "\">\n");
            for (PddlArgument arg : t.arguments)
            {
                sb.append("\t\t<argument val=\"" + arg.name + "\"   type=\""+ arg.type +"\">\n");
                sb.append("\t\t</argument>\n");
            }


            sb.append("\t</action>\n");
        }

        sb.append("</dict>");

        Files.write(Paths.get(output), sb.toString().getBytes());
    }


    public static void main(String[] args) throws IOException, SAXException {
        DomainReader dr =new DomainReader();
        dr.readPddlPredicates("/Users/quynhdo/Documents/WORKING/MUSE/pddl/muse-domain.pddl");
        dr.readPddlTypes("/Users/quynhdo/Documents/WORKING/MUSE/pddl/muse-domain.pddl", "types");
        dr.readPddlTypes("/Users/quynhdo/Documents/WORKING/MUSE/pddl/muse-domain.pddl", "constants");

        dr.readPddlActions("/Users/quynhdo/Documents/WORKING/MUSE/pddl/muse-domain.pddl");
  //      dr.readDictionary("/Users/quynhdo/Desktop/pg.dictionary.xml");
    //    System.out.println("");

       dr.makeDictionary("/Users/quynhdo/Desktop/pg.dictionary.xml");
    }



    class PddlPredicate{
        String name;
        ArrayList<PddlArgument> arguments;

        protected PddlPredicate(){
            arguments = new ArrayList<PddlArgument>();
        }
    }

    class PddlArgument{
        String name;
        String type;
        String value;

        public PddlArgument(String name, String value, String type) {
            this.name = name;
            this.value = value;
            this.type = type;
        }

        public PddlArgument(String name, String type) {
            this.name = name;
            this.type = type;
        }
        public PddlArgument() {

        }


    }
    class PddlAction{
        String name;
        ArrayList<PddlArgument> arguments;  //argument-name argument-type
        ArrayList<PddlPredicate> predConditions;
        ArrayList<PddlPredicate> negativePredConditions;

        ArrayList<PddlPredicate> effects;
        ArrayList<PddlPredicate> negativeEffects;

        public PddlAction(){

            arguments = new ArrayList<>();
            predConditions = new ArrayList<>();
            negativePredConditions = new ArrayList<>();
            effects = new ArrayList<>();
            negativeEffects= new ArrayList<>();
        }


        public PddlAction(PddlAction act){

            arguments = new ArrayList<>();
            predConditions = new ArrayList<>();
            negativePredConditions = new ArrayList<>();
            effects = new ArrayList<>();
            negativeEffects= new ArrayList<>();
            this.name = act.name;
            this.arguments = act.arguments;
            this.predConditions = act.predConditions;
            this.negativePredConditions = act.negativePredConditions;
            this.effects = act.effects;
            this.negativeEffects = act.negativeEffects;
        }
    }
}
