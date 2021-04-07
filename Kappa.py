import math
# from sklearn.metrics import cohen_kappa_score
from nltk import agreement
import sqlite3

SQLITE_PATH = "WebApp/db/rdf-ir.db"

A = [3,3,2,2,1,1,0,0,0,0]
B = [0,2,2,2,1,1,0,0,0,0]
C = [3,3,2,2,1,1,0,0,0,0]
D = [3,3,2,2,1,1,0,0,0,0]

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

# "SELECT tester_id FROM Tester WHERE answered_disease=1"
# "SELECT tester_id FROM Tester WHERE answered_movie=1"
def db_get_testers(query):
    conn = db_connect(SQLITE_PATH)
    cur = conn.cursor()
    cur.execute(query)
    tester_id = [i[0] for i in cur.fetchall()]
    conn.close()
    return tester_id

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

def formatting(result):
    formatted = []
    for j,res in enumerate(result):
        formatted += [[j,i,res[i]] for i in range(0,len(res))]
        # print("i for loop: " + temp)
    return formatted

# models = ["fulltext", "BM25", "BM25F"]
models = ["fulltext"]

# CALCULATE FOR DISEASE:
four_level_disease = []
disease_testers = db_get_testers("SELECT tester_id FROM Tester WHERE answered_disease=1")
for tester in disease_testers:
    disease_result = db_get_query_tester_result_disease(5, tester, models)
    four_level_disease.append([n[1] for n in disease_result["fulltext"]])
# print(four_level_disease)

two_level_disease = []
for n in four_level_disease:
    tmp = []
    for i in n:
        if i > 1:
            tmp.append(1)
        else:
            tmp.append(0)
    two_level_disease.append(tmp)
# print(two_level_disease)

disease_four = formatting(four_level_disease)
score_fleiss = agreement.AnnotationTask(data=disease_four)
print("Disease: four level multi kappa ~")
print(score_fleiss.multi_kappa())
print("\t")

disease_two = formatting(two_level_disease)
score_fleiss = agreement.AnnotationTask(data=disease_two)
print("Disease: two level multi kappa ~")
print(score_fleiss.multi_kappa())

# CALCULATE FOR MOVIE:
four_level_movie = []
movie_testers = db_get_testers("SELECT tester_id FROM Tester WHERE answered_movie=1")
for tester in movie_testers:
    movie_result = db_get_query_tester_result_movie(6, tester, models)
    four_level_movie.append([n[1] for n in disease_result["fulltext"]])
# print(four_level_movie)

two_level_movie = []
for n in four_level_movie:
    tmp = []
    for i in n:
        if i > 1:
            tmp.append(1)
        else:
            tmp.append(0)
    two_level_movie.append(tmp)
# print(two_level_movie)

movie_four = formatting(four_level_movie)
score_fleiss = agreement.AnnotationTask(data=movie_four)
print("Movie: four level multi kappa ~")
print(score_fleiss.multi_kappa())
print("\t")

movie_two = formatting(two_level_movie)
score_fleiss = agreement.AnnotationTask(data=movie_two)
print("Movie: two level multi kappa ~")
print(score_fleiss.multi_kappa())


# [[0,i,A[i]] for i in range(0,len(A))]


# formatted_codes = [[0,i,A[i]] for i in range(0,len(A))] + [[1,i,B[i]] for i in range(0,len(B))] + [[2,i,C[i]] for i in range(0,len(C))]+ [[3,i,D[i]] for i in range(0,len(D))]
# print(formatted_codes)
# AB_score_fleiss = agreement.AnnotationTask(data=formatted_codes)
# print(AB_score_fleiss.multi_kappa())
