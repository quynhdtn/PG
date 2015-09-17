package liir.nlp.pg.preprocessing;

import liir.nlp.io.XMLReader;
import liir.nlp.pg.virtual.DomainReader;
import liir.nlp.pg.virtual.Mapper;
import liir.nlp.representation.*;
import liir.nlp.sources.bekerley.coref.DriverExtended;
import liir.nlp.sources.bekerley.interfaces.BerParser;
import liir.nlp.sources.bekerley.interfaces.BerSentenceSplitter;
import liir.nlp.srl.sources.lth.interfaces.LundLemmatizer;
import liir.nlp.srl.sources.lth.interfaces.LundParser;
import liir.nlp.srl.sources.lth.interfaces.LundSRL;
import liir.nlp.srl.sources.lth.interfaces.LundTagger;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by quynhdo on 07/09/15.
 */
public class TextPreprocessing {
    static BerSentenceSplitter bs ;
    static LundLemmatizer ll;
    static  LundTagger lt;
    static LundParser lp;
    static LundSRL srl;
    static BerParser bp;
    static DriverExtended de;


    public TextPreprocessing (){
        bs= new BerSentenceSplitter("/Users/quynhdo/Documents/WORKING/PhD/workspace/Coref/models/sentsplit.txt.gz");

        try {
                ll = new LundLemmatizer("/Users/quynhdo/Downloads/CoNLL2009-ST-English-ALL.anna-3.3.lemmatizer.model");
                lt = new LundTagger("/Users/quynhdo/Downloads/CoNLL2009-ST-English-ALL.anna-3.3.postagger.model");
                lp = new LundParser("/Users/quynhdo/Downloads/CoNLL2009-ST-English-ALL.anna-3.3.parser.model");
                srl = new LundSRL("/Users/quynhdo/Downloads/CoNLL2009-ST-English-ALL.anna-3.3.srl-4.1.srl.model");
                bp = new BerParser("/Users/quynhdo/Documents/WORKING/PhD/workspace/Coref/models/eng_sm6.gr","/Users/quynhdo/Documents/WORKING/PhD/workspace/Coref/models/eng_sm1.gr");
                de = new DriverExtended("/Users/quynhdo/Documents/WORKING/PhD/workspace/Coref/models/coref-onto.ser.gz", "/Users/quynhdo/Documents/WORKING/PhD/workspace/Coref/data/gender.data");
            } catch (Exception e) {
                e.printStackTrace();
            }

    }

    public static Text process(String text) throws IOException, ClassNotFoundException, SAXException {

        String[] lines= text.split("\\n");
        int current_enum=-1;
        ArrayList<Integer> enumList= new ArrayList<>();
        HashMap<Integer,Integer> rootMap = new HashMap<>();

        for (int i=0; i<lines.length ; i++){
            String line = lines[i].trim();
            if (line.endsWith(":")){  // starting enumuration
                current_enum = i;
                continue;
            }

            Pattern p = Pattern.compile("([\\u2022\\u2023\\u25E6\\u2043\\u2219\\d-]+\\s)(.+)");
            Matcher m = p.matcher(line);

            if (!m.matches()){  // starting enumuration
                current_enum = i;
                continue;
            }

            if (m.matches()){
                boolean is_enum = true;
                for (int j=current_enum+1; j <=i-1;j++){
                    String prev_line = lines[j].trim();
                    Matcher m1 = p.matcher(prev_line);
                    if (! m1.matches())
                        is_enum = false;
                }
                if (is_enum){
                    ArrayList<String> words_at_enum = new ArrayList<>();
                    words_at_enum.add("eat");
                    words_at_enum.add("sit");
                    words_at_enum.add("avoid");
                    words_at_enum.add("assess");
                    words_at_enum.add("identify");


                    words_at_enum.add("determine");
                    words_at_enum.add("offer");

                    words_at_enum.add("warn");
                    words_at_enum.add("increase");

                    String[] tmps = m.group(2).split("\\s+");
                    System.out.println(tmps);
                    if (words_at_enum.contains(tmps[0].toLowerCase())){
                        String line2= "- To " + m.group(2);
                        lines[i]=line2;
                    }

                    enumList.add(i);
                    if (current_enum !=-1)
                        rootMap.put(i, current_enum);
                }
            }

        }


        HashMap<Integer, Integer> paragraphMapping = new HashMap<>(); //from line id to paragraph id
        Text txt = new Text();
        HashMap<Integer, Integer> enums = new HashMap<>();
        for (int i = 0;i <lines.length; i++)
        {
            String line = lines[i];
            Text p = bs.processWithTokenizer(line);
            if (p.size()>0) {
                txt.addParagraph(new Paragraph(txt.size(), txt.size() + p.size() - 1));
                paragraphMapping.put(i, txt.getParagraphs().size() - 1);
                for (Sentence s : p)
                    txt.add(s);
                if (enumList.contains(i)) {  //is enum
                    if (paragraphMapping.containsKey(rootMap.get(i)))
                    txt.addDiscourseRel(new DiscourseRelation(txt.getParagraphs().size() - 1, paragraphMapping.get(rootMap.get(i)), "enum"));

                }
            }
        }

        // clean word form


        txt.setAutomaticIndexing();

        String temp1 = ll.process(txt.toXMLString());
        String temp = lt.process(temp1);
        String txtt = lp.process(temp);

        Text text1 = XMLReader.readCorpus(srl.process(txtt)).get(0);
        text1.setAutomaticIndexing();
        System.out.print(text1.toXMLString());

        bp.processToXML(text1);
        System.out.println(text1.toXMLString());

        de.runPrediction(text1);
        text1.setParagraphs(txt.getParagraphs());
        text1.setDiscourseRels(txt.getDiscourseRels());
        System.out.println(text1.toXMLString());
        return text1;


    }

    public static ArrayList<String> paragraphSplitter (String textPath) throws FileNotFoundException {
        ArrayList<String> texts =  new ArrayList<>();

        ArrayList<String> sens =  new ArrayList<>();

        File file = new File(textPath);
        Scanner in = new Scanner(file);

        while (in.hasNextLine()) {
            String line = in.nextLine();
            if (line.matches("\\s*")){ //if we see an empty line
                if (sens.size() != 0){
                    {
                        StringBuilder sb=new StringBuilder();
                        sens.forEach(s -> sb.append(s + "\n"));
                        texts.add(sb.toString());
                        sens.clear();
                    }

                }

            }
            else{   //normal line

                sens.add(line);

            }
        }

        if (sens.size() >0)
        {
            StringBuilder sb=new StringBuilder();
            sens.forEach(s -> sb.append(s + "\n"));
            texts.add(sb.toString());
        }

        return texts;

    }

    public static void main (String[] args) throws IOException, SAXException, ClassNotFoundException {

        TextPreprocessing ts = new TextPreprocessing();
       // String txt = new String(Files.readAllBytes(Paths.get("/Users/quynhdo/Documents/WORKING/MUSE/data/HASTexts/PG.txt")));


     String txt = new String(Files.readAllBytes(Paths.get("/Users/quynhdo/Desktop/PGTemp2.txt")));

     // String txt= "Do not forget to tell any doctor who treats you that you have had obesity surgery.";
        Text text = ts.process(txt);
//

        Files.write(Paths.get("/Users/quynhdo/Desktop/PGTemp2.xml"), text.toXMLString().getBytes());



        Mapper mp = new Mapper();


        DomainReader dr =new DomainReader();
        dr.readPddlPredicates("/Users/quynhdo/Documents/WORKING/MUSE/pddl/muse-domain.pddl");
        dr.readPddlTypes("/Users/quynhdo/Documents/WORKING/MUSE/pddl/muse-domain.pddl", "types");
        dr.readPddlTypes("/Users/quynhdo/Documents/WORKING/MUSE/pddl/muse-domain.pddl", "constants");

        dr.readPddlActions("/Users/quynhdo/Documents/WORKING/MUSE/pddl/muse-domain.pddl");
        dr.readDictionary("/Users/quynhdo/Desktop/pg.dictionary.xml");

      List<Text> pgText = XMLReader.readCorpus(new String(Files.readAllBytes(Paths.get("/Users/quynhdo/Desktop/PGTemp2.xml"))));

     //   Files.write( Paths.get("/Users/quynhdo/Desktop/PG1.xml"),   pgText.get(0).toXMLString().getBytes());


  //
          mp.map(pgText.get(0),dr );
   //
   //
  //   mp.map(text,dr);
        System.out.println();

    }
}
