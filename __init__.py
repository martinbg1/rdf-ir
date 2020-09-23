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
@app.route('/', methods=['GET'])
def index():
    return render_template('index.html')


def serialize_disease(disease):
    return {
        'name': disease['name'],
        'description': disease['description'],
    }


@app.route('/search')
def get_search():
    try:
        q = request.args["q"]
    except KeyError:
        return render_template('index.html')
    if q:
        db = get_db()
        results = db.run("match (d:Disease) "
                    "where d.name =~ $disease "
                    "return d", {"disease": "(?i).*" + q + ".*"}
        )
        return Response(json.dumps([serialize_disease(record['d']) for record in results]),
                        mimetype="application/json")
    else:
        return 'No data'