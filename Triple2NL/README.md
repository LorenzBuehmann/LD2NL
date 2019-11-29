### About
Triple2NL allows for converting triples into natural language.
### Example
Suppose we have the following triples about a famous theoretical physicist, below shown in [Turtle syntax](http://www.w3.org/TR/turtle/)
``` xml
@prefix : <http://dbpedia.org/resource/>
@prefix dbo: <http://dbpedia.org/ontology/>
@prefix xsd: <http://www.w3.org/2001/XMLSchema#>

:Albert_Einstein a dbo:Scientist;
                 dbo:birthPlace :Ulm;
                 dbo:birthDate "1879-03-14"^^xsd:date.

```
Triple2NL will convert the first triple into
> Albert Einstein is a scientist.

the second one into

> Albert Einstein's birth place is Ulm.

and the last one into

> Albert Einstein's birth date is March 14, 1879.

There is also the (very preliminary) option to convert all triples at once, which results in

> Albert Einstein is a scientist, whose's birth place is Ulm and whose's birth date is March 14, 1879.

### Setup

#### From Source
To install Triple2NL you need to download it via [Git](http://en.wikipedia.org/wiki/Git_(software)) and install it via [Maven](http://maven.apache.org/).
```bash
git clone https://github.com/dice-group/LD2NL.git
cd LD2NL/Triple2NL
mvn clean install
```
Afterwards, you have to add the dependency to your pom.xml
```xml
<dependency>
  <groupId>org.dice-research</groupId>
  <artifactId>triple2nl</artifactId>
  <version>0.2-SNAPSHOT</version>
</dependency>
```

### Usage
#### From Java
``` java
// create the triple we want to convert by using JENA API
Triple t = Triple.create(
			 NodeFactory.createURI("http://dbpedia.org/resource/Albert_Einstein"),
			 NodeFactory.createURI("http://dbpedia.org/ontology/birthPlace"),
			 NodeFactory.createURI("http://dbpedia.org/resource/Ulm"));

// Optionally, we can declare a knowledge base that contains the triple.
// This can be useful during the verbalization process, e.g. the KB could contain labels for entities.
// Here, we use the DBpedia SPARQL endpoint.
SparqlEndpoint endpoint = SparqlEndpoint.getEndpointDBpedia();

// create the triple converter
TripleConverter converter = new TripleConverter(endpoint);

// convert the triple into natural language
String text = converter.convert(t);
```
#### REST service
There is a very basic REST service based on SparkJava framework in class [`org.aksw.triple2nl.rest.RESTService`](https://github.com/LorenzBuehmann/LD2NL/blob/master/Triple2NL/src/main/java/org/aksw/triple2nl/rest/RESTService.java).
It used an embedded Jetty server on startup.

##### How to start: 
Simply run the `main()` method in class `org.aksw.triple2nl.rest.RESTService`, e.g. from Maven via
```bash
mvn exec:java -Dexec.mainClass=org.aksw.triple2nl.rest.RESTService -Dexec.args="-p4567"
```
The port can be specified optionally with CLI param `-p` or `--port`, default port is `4567`.
##### How to use:
This implementation only routes via a single path `/triple2nl`, i.e. the REST service URL will be 
http://localhost:4567/triple2nl . The only request param supported so far is `triple` which expects a
single RDF triple in N-Triples format.

An example request could be:
```
http://localhost:4567/triple2nl?triple=%3Chttp%3A%2F%2Fdbpedia.org%2Fresource%2FAlbert_Einstein%3E%20%3Chttp%3A%2F%2Fdbpedia.org%2Fontology%2FbirthPlace%3E%20%3Chttp%3A%2F%2Fdbpedia.org%2Fresource%2FUlm%3E%20.
```

The response will be a JSON object with the verbalized triple string being in `outputString` key/value pair.
```json
{
"inputString":"\u003chttp://dbpedia.org/resource/Albert_Marx\u003e \u003chttp://dbpedia.org/ontology/birthPlace\u003e \u003chttp://dbpedia.org/resource/Ulm\u003e .",
"parsedTriple":"\u003chttp://dbpedia.org/resource/Albert_Marx\u003e \u003chttp://dbpedia.org/ontology/birthPlace\u003e \u003chttp://dbpedia.org/resource/Ulm\u003e",
"outputString":"Albert Einstein\u0027s birth place is Ulm."
}
```
