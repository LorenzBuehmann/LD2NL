package org.aksw.triple2nl.rest;

import java.io.ByteArrayInputStream;
import java.util.Arrays;

import com.google.gson.Gson;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;
import org.aksw.triple2nl.TripleConverter;
import org.apache.jena.graph.Triple;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.sparql.util.FmtUtils;
import spark.Request;
import spark.ResponseTransformer;
import static org.aksw.triple2nl.rest.RESTService.JsonUtil.json;
import static spark.Spark.*;

/**
 * REST Service for Triple2NL.
 *
 * @author Lorenz Buehmann
 */
public class RESTService  {

    static TripleConverter tripleConverter;

    static void setUp() {
        System.out.println("setting up triple converter ...");
        tripleConverter = new TripleConverter();
        System.out.println("... done.");
    }

    public static void main(String[] args) throws Exception {
        OptionParser parser = new OptionParser();
        OptionSpec<Integer> portSpec = parser.acceptsAll(Arrays.asList( "p", "port" ),
                "port number of REST service" )
                        .withRequiredArg().ofType( Integer.class )
                        .defaultsTo(4567);

        OptionSet options = parser.parse(args);

        // set REST service port if given as CLI arg
        if(options.has(portSpec)) {
            port(options.valueOf(portSpec));
        }

        // initial setup of triple converter // TODO no configurable options so far
        setUp();

        // simple GET path via /triple2nl with a single request param ?triple
        get("/triple2nl", (req, res) -> convertTriple(req), json());

        // convert to JSON response
        after((req, res) -> {
            res.type("application/json");
        });

        exception(IllegalArgumentException.class, (e, req, res) -> {
            res.status(400);
            res.body(JsonUtil.toJson(new ResponseError(e)));
            res.type("application/json");
        });

        exception(ConversionException.class, (e, req, res) -> {
            res.status(400);
            res.body(JsonUtil.toJson(new ResponseError(e)));
            res.type("application/json");
        });
    }

    private static ConversionResult convertTriple(Request req) {

        String tripleStr = req.queryParams("triple");

        if(tripleStr == null) {
            throw new IllegalArgumentException("Parameter 'triple' cannot be empty.");
        }

        // parse triples string
        try {
            Triple triple = RDFDataMgr.createIteratorTriples(new ByteArrayInputStream(tripleStr.getBytes()),
                    Lang.NTRIPLES, null).next();
            String text = tripleConverter.convert(triple);
            String parsedTripleStr = FmtUtils.stringForTriple(triple);

            return new ConversionResult(tripleStr, parsedTripleStr, text);
        } catch (Exception e) {
            throw new ConversionException(String.format("Failed to process input \" %s \".\\n Reason: %s",
                    tripleStr, e.getMessage()), e);
        }
    }

    static class JsonUtil {
        static String toJson(Object object) {
            return new Gson().toJson(object);
        }
        static ResponseTransformer json() {
            return JsonUtil::toJson;
        }
    }

    static class ConversionException extends RuntimeException {
        private static final long serialVersionUID = -5365630128856068164L;

        public ConversionException() {}

        public ConversionException(String var1) {
            super(var1);
        }

        public ConversionException(String var1, Throwable var2) {
            super(var1, var2);
        }

        public ConversionException(Throwable var1) {
            super(var1);
        }
    }

    static class ResponseError {
        private String message;

        ResponseError(String message, String... args) {
            this.message = String.format(message, args);
        }

        ResponseError(Exception e) {
            this.message = e.getMessage();
        }

        public String getMessage() {
            return this.message;
        }
    }

    static class ConversionResult {
        String inputString;
        String parsedTriple;
        String outputString;

        ConversionResult(String inputString, String parsedTriple, String outputString) {
            this.inputString = inputString;
            this.parsedTriple = parsedTriple;
            this.outputString = outputString;
        }
        public String getInputString() {
            return inputString;
        }
        public void setInputString(String inputString) {
            this.inputString = inputString;
        }
        public String getParsedTriple() {
            return parsedTriple;
        }
        public void setParsedTriple(String parsedTriple) {
            this.parsedTriple = parsedTriple;
        }
        public String getOutputString() {
            return outputString;
        }
        public void setOutputString(String outputString) {
            this.outputString = outputString;
        }
    }
}
