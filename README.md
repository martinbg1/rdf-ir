# rdf-ir
This project is a part of Martin Bondkall Gjerde's and Ebba Fingarsen's Master's thesis. 

The project consists of two subprojects:

 - [A Neo4j Plugin for custom indexing and IR-ranking procedures](#neo4j-information-retrieval-plugin)
 - [A web application to query data from a Neo4j database using the custom procedures](#search-engine-web-application)

## Neo4j Information Retrieval Plugin
The plugin can be found in the [`neo4j-ir-procedures/`](https://github.com/martinbg1/rdf-ir/tree/master/neo4j-ir-procedures) directory.

This plugin is made to improve the fulltext search in Neo4j with more IR-ranking models including fielded variants. These ranking models require indexing included in the plugin.

### Plugin Setup
This project uses maven, to build a jar-file with the procedure in this project, simply package the project with maven:

Make sure to be in the plugin directory 

```
cd neo4j-ir-procedures
mvn clean package
```

This will produce a jar-file, `target/neo4j-ir-procedures-1.0.0-SNAPSHOT.jar`, that can be deployed in the plugin directory of your Neo4j instance.

### Procedures

Index variants and the procedure to run:

- Normal index 
    - `CALL improvedSearch.indexRDF("MATCH (d:<Nodes-type>) RETURN d")`
- Fielded index with global idf 
    - `CALL improvedSearch.indexRDFFielded("MATCH (d:<Node-type>) RETURN d")`
- Fielded index with fielded idf 
    - `CALL improvedSearch.indexRDFFieldedNew("MATCH (d:<Node-type>) RETURN d")`

If you want to include several node types in your index, you can write your cypher query inside the procedure the as following:

`MATCH (d:<Nodes-type> RETURN (d) UNION MATCH (d:<Nodes-type>) RETURN (d)` Here you can add as many unions as needed.

Setting parameters for different versions of BM25 search:

The default parameters set for all versions of BM25 is b=0.75 and k1=1.2.
These can be changed to tweak the methods. Parameter b should be set between 0 and 1 and parameter k1 should be set between 1 and 2.
BM25F and BM25FF operate with a separate b for each field.

- Setting parameters used by BM25
    - `CALL improvedSearch.setParameter(<k1-value>, <b-value>)`
- Setting parameters used by BM25F and BM25FF
    - `CALL improvedSearch.setFieldParameter(<k1-value>, "<field-name>", <b-value>)`

Ranking models and the procedure to run query:

- Vector Model (require normal index)
    - `CALL improvedSearch.vectorModelSearch("<query-terms>")`
- BM25 (require normal index)
    - `CALL improvedSearch.bm25Search("<query-terms>")`
- BM25F (require fielded index with global idf)
    - `CALL improvedSearch.bm25fSearch("<query-terms>")`
- BM25FF (require fielded index with fielded idf)
    - `CALL improvedSearch.bm25ffSearch("<query-terms>")`



## Search Engine Web Application
The web application can be found in the [`WebApp`](https://github.com/martinbg1/rdf-ir/tree/master/WebApp) directory.

The web application serves as a user interface to query data from a Neo4j database using the procedures described above.

### Setup
First setup virtualenv:
```
cd WebApp
virtualenv venv
source venv/Scripts/activate
```

Then install dependencies for the app:
```
pip install -r requirements.txt
```

Give execute permission to the run script:
```
chmod +x /bin/run-debug.sh
```

Make sure your database is running and start the app with:
```
bin/run-debug.sh
```

Now the app should be available at http://localhost:8080
