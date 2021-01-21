from flask import Flask, g, Response, jsonify, render_template, request, redirect, url_for, session
from WebApp import app, get_neo_db
from WebApp.db.db_util import *
from WebApp.util.serialize_search import *
import re


@app.route('/home/', methods=['GET','POST'])
def home():
    query = request.args['query']
    query_id = request.args['query_id']
    query_description = request.args['query_description']
    rest_queries = session['rest_queries']

    # connect to neo4j db
    db = get_neo_db()
    results = db.run('CALL improvedSearch.bm25Search("'+ query +'") ')
    serialized_result = [serialize_results(record) for record in results]

    return render_template('home.html', query=query, query_id = query_id, query_result=serialized_result, query_description=query_description, rest_queries=rest_queries)


# render index
@app.route('/')
def index():
    return render_template('index.html')


@app.route('/landing', methods=['GET','POST'])
def landing():
    conn = db_connect("./db/rdf-ir.db")
    queries = get_random_disease_queries(conn, 2)
    return render_template("landing.html", queries=queries)


@app.route('/handleQuery', methods=['GET','POST'])
def handleQuery():
    queries = request.args['queries']

    parsed_queries = [tuple(x.replace("'", "").split(',')) for x in re.findall("\((.*?)\)", queries)]
    if not parsed_queries:
        return redirect('/')
    
    query = parsed_queries.pop(0)
    session['rest_queries'] = parsed_queries

    return redirect(url_for('.home', query_id=query[0], query=query[1], query_description=query[2]))


@app.route('/handleForm', methods=['GET', 'POST'])
def handleForm():
    query_id = ''
    rest_queries = ''
    try:
        query_id = request.args['query_id']
        rest_queries = request.args['rest_queries']
    except KeyError:
        return redirect('/error')


    query_id = int(query_id.replace("'", ""))
    # connect to sqlite3 db
    conn = db_connect("./db/rdf-ir.db")
    for key,value in request.form.items():
        add_test_result_disease(conn, 'BM25', key, value, query_id, 7)

    return redirect(url_for('.handleQuery', queries=rest_queries))
