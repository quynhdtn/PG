package liir.nlp.pg.preprocessing;

import liir.nlp.representation.DiscourseRelation;
import liir.nlp.representation.Paragraph;
import liir.nlp.representation.Sentence;
import liir.nlp.representation.Text;
import liir.nlp.sources.bekerley.interfaces.BerSentenceSplitter;

import java.io.*;
import java.lang.reflect.Array;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by quynhdo on 03/09/15.
 */
public class TextSplitter {


    public static Text process(String text){

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

            if (m.matches()){
                boolean is_enum = true;
                for (int j=current_enum+1; j <=i-1;j++){
                    String prev_line = lines[j].trim();
                    Matcher m1 = p.matcher(prev_line);
                    if (! m1.matches())
                        is_enum = false;
                }
                if (is_enum){
                   String[] tmps= m.group(2).split("\\s+");
                    ArrayList<String> begin_enum_words= new ArrayList<>();
                    begin_enum_words.add("eat");
                    begin_enum_words.add("avoid");
                    begin_enum_words.add("sit");
                    begin_enum_words.add("assess");
                    begin_enum_words.add("identify");

                    begin_enum_words.add("determine");
                    begin_enum_words.add("offer");

                    begin_enum_words.add("warn");
                    begin_enum_words.add("increase");
                    if (begin_enum_words.contains(tmps[0].toLowerCase()))
                    {
                        String line2 = "to " + m.group(2);
                        lines[i]=line2;
                    }
                    enumList.add(i);
                    if (current_enum !=-1)
                        rootMap.put(i, current_enum);
                }
            }

        }

        BerSentenceSplitter bs = new BerSentenceSplitter("../Coref/models/sentsplit.txt.gz");

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
                    txt.addDiscourseRel(new DiscourseRelation(txt.getParagraphs().size() - 1, paragraphMapping.get(rootMap.get(i)), "enum"));

                }
            }
        }


        return txt;
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
            sens.forEach(s -> {   sb.append(s + "\n"); });
            texts.add(sb.toString());
        }

        return texts;

    }

    public static void main (String[] args) throws IOException {
        TextSplitter ts = new TextSplitter();
        String txt = new String(Files.readAllBytes(Paths.get("/Users/quynhdo/Documents/WORKING/MUSE/data/HASTexts/PG1.txt")));

        Text text = ts.process(txt);
        System.out.println();

    }
}
