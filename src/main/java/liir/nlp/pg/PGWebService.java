package liir.nlp.pg;

import com.sun.jersey.api.container.httpserver.HttpServerFactory;
import com.sun.net.httpserver.HttpServer;
import liir.nlp.pg.preprocessing.TextPreprocessing;
import liir.nlp.pg.virtual.DomainReader;
import liir.nlp.pg.virtual.Mapper;
import liir.nlp.representation.Text;
import org.xml.sax.SAXException;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import java.io.IOException;

/**
 * Created by quynhdo on 09/09/15.
 */
@Path("/pg")

public class PGWebService {
    static TextPreprocessing prep ;
    static  DomainReader dr =new DomainReader();
    static  Mapper mp = new Mapper();
    public PGWebService() {
        super();
        prep = new TextPreprocessing();
        try {

            dr.readPddlPredicates("/Users/quynhdo/Documents/WORKING/MUSE/pddl/muse-domain.pddl");
            dr.readPddlTypes("/Users/quynhdo/Documents/WORKING/MUSE/pddl/muse-domain.pddl", "types");
            dr.readPddlTypes("/Users/quynhdo/Documents/WORKING/MUSE/pddl/muse-domain.pddl", "constants");

            dr.readPddlActions("/Users/quynhdo/Documents/WORKING/MUSE/pddl/muse-domain.pddl");
            dr.readDictionary("/Users/quynhdo/Desktop/pg.dictionary.xml");
        } catch (IOException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        }

    }

    void process(String text){

        try {
            Text txt = TextPreprocessing.process(text);
            mp.map(txt, dr);

        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        }


    }

    @GET
    // The Java method will produce content identified by the MIME Media type "text/plain"
    @Path("/prep")
    @Produces(MediaType.TEXT_HTML)
    public void getMessageQueryParam(@QueryParam("param1") String text){

       process(text);


    }

    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServerFactory.create("http://localhost:9998/");
        server.start();

        System.out.println("Server running");
        System.out.println("Visit: http://localhost:9998/pg/prep");
        System.out.println("Hit return to stop...");
        System.in.read();
        System.out.println("Stopping server");
        server.stop(0);
        System.out.println("Server stopped");
    }

}
