import os
import json
from flask import Flask, g, Response, jsonify
from neo4j import GraphDatabase, basic_auth


app = Flask(__name__)

url = os.getenv("NEO4J_URL","bolt://localhost")
password = os.getenv("NEO4J_PASSWORD", "test")

driver = GraphDatabase.driver(url,auth=basic_auth("neo4j", password),encrypted=False)

def get_db():
    if not hasattr(g, 'neo4j_db'):
        g.neo4j_db = driver.session()
    return g.neo4j_db


# a simple page that says hello
@app.route('/')
def hello():
    return 'Hello!'


def serialize_disease(disease):
    return {
        'name': disease['diseaseName'],
        'description': disease['diseaseDescription'],
    }


@app.route('/result')
def get_result():
    db = get_db()
    results = db.run("MATCH (d:Disease) "
                 "RETURN d LIMIT 5")
    return Response(json.dumps([serialize_disease(record['d']) for record in results]),
                        mimetype="application/json")

