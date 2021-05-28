from WebApp import app, get_neo_disease_db
from flask import Flask, g, Response, jsonify, render_template, request
from WebApp.util.serialize_search import *
from neo4j import GraphDatabase, basic_auth
import json
import os

@app.route('/fulltextSearch')
def get_fulltext_search():
    try:
        q = request.args["q"]
    except KeyError:
        return render_template('index.html')
    if q:
        db = get_neo_disease_db()
        print(q)
        # db.run("call db.index.fulltext.createNodeIndex('NameDescAlias',['Disease','Symptom'],['name','description','altNames']) " )
        # call db.index.fulltext.createNodeIndex('NameDescAlias',['Disease'],['name','description','altNames'], {analyzer: "english"})
        results = db.run("call db.index.fulltext.queryNodes('NameDescAlias','name:"+ q +" OR altNames:" + q + " OR description:" + q + "') "
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
        db = get_neo_disease_db()
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
        db = get_neo_disease_db()
        results = db.run("match (d:Disease)-[:usesDrug]->(dr:Drug) "
                    "where d.name = $disease "
                    "return dr", {"disease": disease})
        return Response(json.dumps([serialize_drug(record['dr']) for record in results]),
                            mimetype="application/json")
    return 'No drugs'

"""
Test for bruk av våre procedures :)
"""
@app.route('/BM25Search')
def get_BM25_search():
    try:
        q = request.args["q"]
    except KeyError:
        return render_template('index.html')
    if q:
        db = get_neo_disease_db()
        results = db.run('CALL improvedSearch.bm25Search("'+ q +'") ')

        return Response(json.dumps([serialize_results(record) for record in results]),
                                mimetype="application/json")
    return 'No data'

@app.route('/BM25FSearch')
def get_BM25F_search():
    try:
        q = request.args["q"]
    except KeyError:
        return render_template('index.html')
    if q:
        db = get_neo_disease_db()
        results = db.run('CALL improvedSearch.bm25fSearch("'+ q +'") ')

        return Response(json.dumps([serialize_results(record) for record in results]),
                                mimetype="application/json")
    return 'No data'


"""
Alternativ til den andre søkefunksjonen. Her finner man disease og symptoms til disease i en spørring
i steden for å splitte det opp i to spørringer.
"""
@app.route('/searchAlt')
def get_search_alt():
    try:
        q = request.args["q"]
    except KeyError:
        return render_template('index.html')
    if q:
        db = get_neo_disease_db()
        results = db.run("match (d:Disease)-[:hasSymptom]->(s:Symptom) "
                    "where d.name =~ $disease "
                    "return d, s", {"disease": "(?i).*" + q + ".*"}

        )
        # return Response(json.dumps([serialize_disease(record['d']) for record in results]),
        #                 mimetype="application/json")

    return 'No data'
