import numpy as np
import sqlite3
import math
from statistics import mean

SQLITE_PATH = "WebApp/db/rdf-ir.db"
query_ideal= {
    1: [3,3,2,2,1,1,0,0,0,0], # covid-19 (pandemic rated 1 maybe change to 2)
    2: [3,2,2,1,1,1,1,1,1,1], # yellow fever
    3: [3,3,3,3,3,3,3,3,2,0], # headache symptom
    4: [3,3,3,3,2,2,2,2,2,2], # influenza pandemix
    5: [3,3,2,2,1,1,1,1,1,1], # fear of social interaction (fear of medical procedures satt til 1, kanskje 2)
    6: [3,3,3,1,1,1,1,1,1,1], # matrix
    7: [3,3,3,1,1,1,1,1,1,1], # lotr
    8: [3,3,3,3,3,3,3,2,1,1], # movies by Christopher Nolan
    9: [3,2,2,2,2,1,1,1,1,1], # the circus chaplin
    10: [3,3,3,2,2,2,1,1,1,1] # Wachowski directors
}


def db_connect(db_file):
    """ 
    Start a database connection to a sqlite database
    :param db_file
        path to sqlite3 db file
     """
    conn = None

    try:
        conn = sqlite3.connect(db_file)
        # print(sqlite3.version)
    except sqlite3.Error as e:
        print(e)
    return conn

def db_get_disease_testers():
    conn = db_connect(SQLITE_PATH)
    cur = conn.cursor()
    cur.execute("SELECT tester_id FROM Tester WHERE answered_disease=1")
    tester_id = [i[0] for i in cur.fetchall()]
    conn.close()
    return tester_id

def db_get_movie_testers():
    conn = db_connect(SQLITE_PATH)
    cur = conn.cursor()
    cur.execute("SELECT tester_id FROM Tester WHERE answered_movie=1")
    tester_id = [i[0] for i in cur.fetchall()]
    conn.close()
    return tester_id

def db_get_query_ids():
    conn = db_connect(SQLITE_PATH)
    cur = conn.cursor()
    cur.execute("SELECT query_id FROM Query")
    query_id = [i[0] for i in cur.fetchall()]
    conn.close()
    return query_id

def db_get_query_tester_result_disease(query_id, tester_id, models):
    conn = db_connect(SQLITE_PATH)
    cur = conn.cursor()
    result = {}
    for model in models:
        cur.execute("""
            SELECT result_rank, relevancy
            FROM DataDisease 
            WHERE query_id=? AND tester_id=? AND method=?""", (query_id, tester_id, model))
        result[model] = cur.fetchall()
    return result


def db_get_query_tester_result_movie(query_id, tester_id, models):
    conn = db_connect(SQLITE_PATH)
    cur = conn.cursor()
    result = {}
    for model in models:
        cur.execute("""
            SELECT result_rank, relevancy
            FROM DataMovie 
            WHERE query_id=? AND tester_id=? AND method=?""", (query_id, tester_id, model))
        result[model] = cur.fetchall()
    return result


def result_to_list(result):
    new_result = {}
    for k, v in result.items():
        new_result[k] = [i[1] for i in v]
    return new_result



def dcg_score(result_scores):
    dcg = 0
    for i, rel in enumerate(result_scores):
        dcg += rel / math.log(i + 2, 2)
    return dcg


def ndcg_score(idcg, dcg):
    return dcg / idcg


def avg_ndcg_query(result):
    res = {}
    for k, v in result.items():
        res[k] = {}
        ndcg_fulltext = []
        ndcg_BM25 = []
        ndcg_BM25F = []
        for ndcg in v:
            ndcg_fulltext.append(ndcg["fulltext"])
            ndcg_BM25.append(ndcg["BM25"])
            ndcg_BM25F.append(ndcg["BM25F"])
        res[k]["fulltext"] = mean(ndcg_fulltext)
        res[k]["BM25"] = mean(ndcg_BM25)
        res[k]["BM25F"] = mean(ndcg_BM25F)
    return res
        

def avg_ndcg_dataset(result):
    res = {}
    ndcg_fulltext = []
    ndcg_BM25 = []
    ndcg_BM25F = []
    for k, v in result.items():
        ndcg_fulltext.append(v["fulltext"])
        ndcg_BM25.append(v["BM25"])
        ndcg_BM25F.append(v["BM25F"])
    res["fulltext"] = mean(ndcg_fulltext)
    res["BM25"] = mean(ndcg_BM25)
    res["BM25F"] = mean(ndcg_BM25F)
    print(res)


if __name__ == '__main__':
    models = ["fulltext", "BM25", "BM25F"]
    query_disease = [1,2,3,4,5]
    query_movie = [6,7,8,9,10]

    result = {
        "disease": {
        },
        "movie": {

        }
            
    }

    testers_disease = db_get_disease_testers()
    testers_movie = db_get_movie_testers()
    tester_test = '9fe4d10a-d11e-4bf9-9751-f8698c14651f'
    query_id = 3


    for q_id in query_disease:
        result["disease"][q_id] = []
        for tester in testers_disease:
            tmp = db_get_query_tester_result_disease(q_id, tester, models)
            rankings = result_to_list(tmp)

            dcg_ideal = dcg_score(query_ideal[q_id])

            tmp = {}
            for model in models:
                dcg = dcg_score(rankings[model])
                ndcg = ndcg_score(dcg_ideal, dcg)
                tmp[model] = ndcg
            result["disease"][q_id].append(tmp)

    avg_disease = avg_ndcg_query(result["disease"])
    avg_ndcg_dataset(avg_disease)

    for q_id in query_movie:
        result["movie"][q_id] = []
        for tester in testers_movie:
            tmp = db_get_query_tester_result_movie(q_id, tester, models)
            rankings = result_to_list(tmp)

            dcg_ideal = dcg_score(query_ideal[q_id])

            tmp = {}
            for model in models:
                dcg = dcg_score(rankings[model])
                ndcg = ndcg_score(dcg_ideal, dcg)
                tmp[model] = ndcg
            result["movie"][q_id].append(tmp)
    
    
    avg_movie = avg_ndcg_query(result["movie"])
    print(avg_movie)
    avg_ndcg_dataset(avg_movie)
