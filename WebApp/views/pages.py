from flask import Flask, g, Response, jsonify, render_template, request, redirect, url_for, session
from WebApp import app, get_neo_db
from WebApp.db.sqlite_util import *
from WebApp.db.neo_util import *
from WebApp.util.serialize_search import *
import uuid


@app.route('/home/', methods=['GET','POST'])
def home():
    serialized_result = []


    query = session["queries"][0]
    query_id = query[0]
    q = query[1]
    query_description = query[2]

    # connect to neo4j db
    db = get_neo_db()

    method = session['methods'][session['index']]

    if method == "BM25":
        serialized_result = bm25_search(db, q)
    elif method == "BM25F":
        serialized_result = bm25f_search(db, q)
    elif method == "fulltext":
        serialized_result = fulltext_search(db, q)

    return render_template('home.html', query=q, query_id=query_id, query_result=serialized_result, query_description=query_description, method=method)
 

# render index
@app.route('/')
def index():
    return render_template('index.html')


@app.route('/landing', methods=['GET','POST'])
def landing():
    
    # continue unfinished survey
    if session.get('queries') is None:
        queries = get_random_disease_queries(2)
        session["queries"] = queries
        session['methods'] = ['BM25', 'BM25F','fulltext']
        session['index'] = 0

        user_id = uuid.uuid4()
        session['user_id'] = user_id
        add_new_user(str(user_id))

    return render_template("landing.html")


@app.route('/handleQuery', methods=['GET','POST'])
def handleQuery():
    # TODO ikke clear session og ha en sjekk om user_id har answered = 0
    if not session['queries']:
        user_finished(str(session['user_id']))
        session.clear()
        return redirect('/')
    return redirect(url_for('.home'))


@app.route('/handleForm', methods=['GET', 'POST'])
def handleForm():
    try:
        query_id = request.args['query_id']
        method = request.args['method']
        user_id = str(session['user_id'])
        query_id = int(query_id)

        for key,value in request.form.items():
            add_test_result_disease(method, key, value, query_id, user_id)

        session['index'] += 1
        if session['index'] > len(session['methods']) - 1:
            session['index'] = 0
            session['queries'] = session['queries'][1:]


    except KeyError:
        return redirect('/error')

    return redirect(url_for('.handleQuery'))
