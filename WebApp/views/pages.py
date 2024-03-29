from flask import Flask, g, Response, jsonify, render_template, request, redirect, url_for, session
from WebApp import app, get_neo_disease_db, get_neo_movie_db
from WebApp.db.sqlite_util import *
from WebApp.db.neo_util import *
from WebApp.util.serialize_search import *
import uuid
import random
import sys


# render index
@app.route('/searchbar')
def searchbar():
    return render_template('searchbar.html')


@app.route('/survey', methods=['GET','POST'])
def survey():
    serialized_result = []
    dataset = request.args['dataset']
    db = None

    if dataset == "disease":
        db = get_neo_disease_db()
        query = session["disease_queries"][0]
        method = session['methods'][session['disease_index']]
        index = session['disease_index']
        query_length = session['disease_query_length']
        query_index= query_length - len(session['disease_queries'])
    elif dataset == "movie":
        db = get_neo_movie_db()
        query = session["movie_queries"][0]
        method = session['methods'][session['movie_index']]
        index = session['movie_index']
        query_length = session['movie_query_length']
        query_index= query_length - len(session['movie_queries'])

    query_id = query[0]
    q = query[1]
    query_description = query[2]


    if method == "BM25":
        serialized_result = bm25_search(db, q, dataset)
    elif method == "BM25F":
        serialized_result = bm25f_search(db, q, dataset)
    elif method == "fulltext":
        serialized_result = fulltext_search(db, q, dataset)

    return render_template('survey.html', query=q, query_id=query_id, query_result=serialized_result, query_description=query_description, method=method, dataset=dataset, index=index, query_length=query_length, query_index=query_index)
 

@app.route('/', methods=['GET','POST'])
def about():
    # session.clear()
    disease_queries = []
    movie_queries = []

    if session.get('disease_queries') is None:
        disease_queries = get_random_disease_queries(5)
        movie_queries = get_random_movie_queries(5)
        session['disease_query_length']=len(disease_queries)
        session['movie_query_length']=len(movie_queries)
        session['disease_queries'] = disease_queries
        session['movie_queries'] = movie_queries
        # randomize method order when initializing 
        methodlist = ['BM25', 'BM25F','fulltext']
        random.shuffle(methodlist)
        session['methods'] = methodlist

        session['disease_index'] = 0
        session['movie_index'] = 0

        user_id = uuid.uuid4()
        session['user_id'] = user_id
        add_new_user(str(user_id))
    else:
        disease_queries = session['disease_queries']
        movie_queries = session['movie_queries']

    return render_template("about.html")

@app.route('/home', methods=['GET','POST'])
def home():
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
            
        return redirect(url_for('.home', disease_queries = disease_queries, movie_queries = movie_queries))
    

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
                # randomize order when index is set to 0
                methodlist = session['methods']
                random.shuffle(methodlist)
                session['methods']=methodlist
                
                session['disease_queries'] = session['disease_queries'][1:]

            for key,value in request.form.items():
                add_test_result_disease(method, key, value, query_id, user_id)

        elif dataset == "movie":
            session['movie_index'] +=1
            if session['movie_index'] > len(session['methods']) - 1:
                session['movie_index'] = 0
                # randomize order when index is set to 0
                methodlist = session['methods']
                random.shuffle(methodlist)
                session['methods']=methodlist

                session['movie_queries'] = session['movie_queries'][1:]

            for key,value in request.form.items():
                add_test_result_movie(method, key, value, query_id, user_id)


    except KeyError:
        return redirect('/error')

    return redirect(url_for('.handleQuery', dataset=dataset))
