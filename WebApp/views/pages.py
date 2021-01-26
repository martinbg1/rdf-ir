from flask import Flask, g, Response, jsonify, render_template, request, redirect, url_for, session
from WebApp import app, get_neo_db
from WebApp.db.sqlite_util import *
from WebApp.db.neo_util import *
from WebApp.util.serialize_search import *
import uuid


@app.route('/home/', methods=['GET','POST'])
def home():
    serialized_result = []

    dataset = request.args['dataset']
    

    # connect to neo4j db
    db = get_neo_db()

    if dataset == "disease":
        query = session["disease_queries"][0]
        method = session['methods'][session['disease_index']]
    elif dataset == "movie":
        query = session["movie_queries"][0]
        method = session['methods'][session['movie_index']]

    print(query) 
    query_id = query[0]
    q = query[1]
    query_description = query[2]



    if method == "BM25":
        serialized_result = bm25_search(db, q)
    elif method == "BM25F":
        serialized_result = bm25f_search(db, q)
    elif method == "fulltext":
        serialized_result = fulltext_search(db, q)

    return render_template('home.html', query=q, query_id=query_id, query_result=serialized_result, query_description=query_description, method=method, dataset=dataset)
 

# render index
@app.route('/')
def index():
    return render_template('index.html')


@app.route('/landing', methods=['GET','POST'])
def landing():
    session.clear()
    # continue unfinished survey
    if session.get('queries') is None:
        disease_queries = get_random_disease_queries(2)
        movie_queries = get_random_movie_queries(2)
        session['disease_queries'] = disease_queries
        session['movie_queries'] = movie_queries
        session['methods'] = ['BM25', 'BM25F','fulltext']
        session['disease_index'] = 0
        session['movie_index'] = 0

        user_id = uuid.uuid4()
        session['user_id'] = user_id
        add_new_user(str(user_id))

    return render_template("landing.html")


@app.route('/handleQuery', methods=['GET','POST'])
def handleQuery():
    dataset = request.args['dataset']
    # TODO ikke clear session og ha en sjekk om user_id har answered = 0
    if not session['disease_queries'] or not session['movie_queries']:
        user_finished(str(session['user_id']),dataset)
        session.clear()
        return redirect('/')

    return redirect(url_for('.home', dataset=dataset))


@app.route('/handleForm', methods=['GET', 'POST'])
def handleForm():
    try:
        dataset = request.args['dataset']
        query_id = request.args['query_id']
        method = request.args['method']
        user_id = str(session['user_id'])
        query_id = int(query_id)

        if dataset == "disease":
            session['disease_index'] += 1
            if session['disease_index'] > len(session['methods']) - 1:
                session['disease_index'] = 0
                session['disease_queries'] = session['disease_queries'][1:]

            for key,value in request.form.items():
                add_test_result_disease(method, key, value, query_id, user_id)

        elif dataset == "movie":
            session['movie_index'] +=1
            if session['movie_index'] > len(session['methods']) - 1:
                session['movie_index'] = 0
                session['movie_queries'] = session['movie_queries'][1:]

            for key,value in request.form.items():
                add_test_result_movie(method, key, value, query_id, user_id)


    except KeyError:
        return redirect('/error')

    return redirect(url_for('.handleQuery', dataset=dataset))
