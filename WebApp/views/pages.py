from flask import Flask, g, Response, jsonify, render_template, request, redirect
from WebApp import app, get_neo_db
from WebApp.db.db_util import *
from WebApp.util.serialize_search import *
from WebApp.forms import RadioForm


@app.route('/home', methods=['GET','POST'])
def home():
    # connect to sqlite3 db
    conn = db_connect("./db/rdf-ir.db")
    query = get_disease_query(conn)[0]

    # connect to neo4j db
    db = get_neo_db()
    results = db.run('CALL improvedSearch.bm25Search("'+ query[1] +'") ')
    serialized_result = [serialize_results(record) for record in results]
    form = RadioForm()
    method ='BM25'

    if form.is_submitted():
        result = request.form
        return render_template('result.html', result=result)

    return render_template('home.html', query=query, query_result=serialized_result, form=form)


# render index
@app.route('/')
def index():
    return render_template('index.html')


@app.route('/landing')
def landing():
    return render_template("landing.html")


@app.route('/handleForm', methods=['GET', 'POST'])
def handleForm():
    print(request.form)
    query_id = ''
    try:
        query_id = request.args['queryId']
    except KeyError:
        return redirect('/home')

    query_id = int(query_id)
    # connect to sqlite3 db
    conn = db_connect("./db/rdf-ir.db")
    for key,value in request.form.items():
        add_test_result_disease(conn, 'BM25', key, value, query_id, 7)
    # add_test_result_disease(conn, 'BM25', result_rank, relevancy, query_id, tester_id)

    return redirect('/home')
