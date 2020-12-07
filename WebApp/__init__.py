import os
import json
from flask import Flask, g, Response, jsonify, render_template, request
from neo4j import GraphDatabase, basic_auth


app = Flask(__name__)

url = os.getenv("NEO4J_URL","bolt://localhost")
password = os.getenv("NEO4J_PASSWORD", "test")

driver = GraphDatabase.driver(url,auth=basic_auth("neo4j", '123'),encrypted=False)

def get_db():
    if not hasattr(g, 'neo4j_db'):
        g.neo4j_db = driver.session()
    return g.neo4j_db


# render index
@app.route('/')
def index():
    return render_template('index.html')


def serialize_disease(disease):
    return {
        'name': disease["node"]['name'],
        'description': disease["node"]['description'],
        'altNames':disease["node"]['altNames'],
        'score':disease['score']
    }


def serialize_symptom(symptom):
    return {
        'name': symptom['name'],
        'description': symptom['description'],
        'altNames': symptom['altNames']
    }

def serialize_drug(drug):
    return {
        'name': drug['name'],
        'altNames': drug['altNames']
    }


@app.route('/search')
def get_search():
    try:
        q = request.args["q"]
    except KeyError:
        return render_template('index.html')
    if q:
        db = get_db()
        print(q)
        # db.run("call db.index.fulltext.createNodeIndex('NameDescAlias',['Disease','Symptom'],['name','description','altNames']) " )
        # call db.index.fulltext.createNodeIndex('NameDescAlias',['Disease'],['name','description','altNames'], {analyzer: "english"})
        results = db.run("call db.index.fulltext.queryNodes('NameDescAlias','name:"+ q +"^3 OR altNames:" + q + "^2 OR description:" + q + "') "
         "YIELD node,score " 
         "RETURN node,score limit 20"
        )
        
        return Response(json.dumps([serialize_disease(record) for record in results]),
                        mimetype="application/json")
    return 'No data'


@app.route('/symptom/')
def get_symptom():
    try:
        disease = request.args["d"]
    except KeyError:
        return render_template('index.html')
    if disease:
        #print(disease)
        db = get_db()
        results = db.run("match (d:Disease)-[:hasSymptom]->(s:Symptom) "
                    "where d.name = $disease "
                    "return s", {"disease": disease})
        return Response(json.dumps([serialize_symptom(record['s']) for record in results]),
                            mimetype="application/json")
    return 'No symptoms' 

@app.route('/drug/')
def get_drug():
    try:
        disease = request.args["d"]
    except KeyError:
        return render_template('index.html')
    if disease:
        #print(disease)
        db = get_db()
        results = db.run("match (d:Disease)-[:usesDrug]->(dr:Drug) "
                    "where d.name = $disease "
                    "return dr", {"disease": disease})
        return Response(json.dumps([serialize_drug(record['dr']) for record in results]),
                            mimetype="application/json")
    return 'No drugs'

    """
    Test for bruk av våre procedures :)
    """
@app.route('/test/')
def get_search():
    try:
        q = request.args["d"]
    except KeyError:
        return render_template('index.html')
    if q:
        db = get_db()
        print(q)
        """
        yield og return blir nok annerledes her..
        """
        results = db.run("CALL improvedSearch.bm15Search("+ q +"') "
         "YIELD node,score "
         "RETURN node,score limit 20"
        )

        return Response(json.dumps([serialize_disease(record) for record in results]),
                        mimetype="application/json")
    return 'No data'

#@app.route('/search')
#def get_disease_symptom():
#    try:
#        q = request.args["q"]
#    except KeyError:
#        return render_template('index.html')
#    if q:
#        db = get_db()
#        results = db.run("match (d:Disease)-[:hasSymptom]->(s:Symptom) "
#                    "where d.name =~ $disease "
#                    "return d, s", {"disease": "(?i).*" + q + ".*"}

#        )
#        print(results.single())
#        return Response(json.dumps([serialize_symptom(record['d']) for record in results]),
#                        mimetype="application/json")
#    else:
#        return 'no data'


"""
Alternativ til den andre søkefunksjonen. Her finner man disease og symptoms til disease i en spørring
i steden for å splitte det opp i to spørringer.
TODO: Finne ut hvordan man serialiserer resultatet.
"""
@app.route('/searchAlt')
def get_search_alt():
    try:
        q = request.args["q"]
    except KeyError:
        return render_template('index.html')
    if q:
        db = get_db()
        results = db.run("match (d:Disease)-[:hasSymptom]->(s:Symptom) "
                    "where d.name =~ $disease "
                    "return d, s", {"disease": "(?i).*" + q + ".*"}

        )
        # return Response(json.dumps([serialize_disease(record['d']) for record in results]),
        #                 mimetype="application/json")

    return 'No data'

