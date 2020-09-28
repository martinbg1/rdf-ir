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


@app.route('/search')
def get_search():
    try:
        q = request.args["q"]
    except KeyError:
        return render_template('index.html')
    if q:
        db = get_db()
        print(q)
        #db.run("call db.index.fulltext.createNodeIndex('NameDescAlias',['Disease','Symptom'],['name','description','altNames']) " )
        results = db.run("call db.index.fulltext.queryNodes('NameDescAlias','name:"+ q +"^3 OR altNames:" + q + "^2') "
         "YIELD node,score " 
         "RETURN node,score "
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

