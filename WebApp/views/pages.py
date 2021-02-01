from flask import Flask, g, Response, jsonify, render_template, request, redirect, url_for, session
from WebApp import app, get_neo_db
from WebApp.db.sqlite_util import *
from WebApp.db.neo_util import *
from WebApp.util.serialize_search import *
import uuid


# render index
@app.route('/searchbar')
def searchbar():
    return render_template('searchbar.html')


@app.route('/survey', methods=['GET','POST'])
def survey():
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

    query_id = query[0]
    q = query[1]
    query_description = query[2]



    if method == "BM25":
        serialized_result = bm25_search(db, q)
    elif method == "BM25F":
        serialized_result = bm25f_search(db, q)
    elif method == "fulltext":
        serialized_result = fulltext_search(db, q)

    return render_template('survey.html', query=q, query_id=query_id, query_result=serialized_result, query_description=query_description, method=method, dataset=dataset)
 

@app.route('/', methods=['GET','POST'])
def index():
    # session.clear()
    disease_queries = []
    movie_queries = []

    if session.get('disease_queries') is None:
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
    else:
        disease_queries = session['disease_queries']
        movie_queries = session['movie_queries']

    return render_template("index.html", disease_queries = disease_queries, movie_queries = movie_queries)


@app.route('/handleQuery', methods=['GET','POST'])
def handleQuery():
    dataset = request.args['dataset']
    if (not session['disease_queries'] and dataset=="disease") or (not session['movie_queries'] and dataset=="movie"):
        user_finished(str(session['user_id']),dataset)
        disease_queries = session['disease_queries']
        movie_queries = session['movie_queries']
        if(not disease_queries and not movie_queries):
            return render_template('finished.html')
        # session.clear()
        return redirect(url_for('.index', disease_queries = disease_queries, movie_queries = movie_queries))
    

    return redirect(url_for('.survey', dataset=dataset))


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
