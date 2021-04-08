import math
# from sklearn.metrics import cohen_kappa_score
from nltk import agreement
import sqlite3

SQLITE_PATH = "WebApp/db/rdf-ir.db"

A = [3,3,2,2,1,1,0,0,0,0]
B = [0,2,2,2,1,1,0,0,0,0]
C = [3,3,2,2,1,1,0,0,0,0]
D = [3,3,2,2,1,1,0,0,0,0]

models = ["fulltext", "BM25", "BM25F"]
# models = ["fulltext"]
query_disease = [1,2,3,4,5]
query_movie = [6,7,8,9,10]

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

# hente alle testere
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
    return formatted

# CALCULATE FOR DISEASE:
disease_testers = db_get_testers("SELECT tester_id FROM Tester WHERE answered_disease=1")

fleiss_scores_four = []
def calculate_feiss_four_disease(q_id, model):
    temp_disease =[]
    for tester in disease_testers:
        disease_result = db_get_query_tester_result_disease(q_id, tester, models)
        temp_disease.append([n[1] for n in disease_result[model]])
    # print(temp_disease)
    formatted_disease= formatting(temp_disease)
    fleiss_agreement = agreement.AnnotationTask(data=formatted_disease)
    fleiss_scores_four.append(fleiss_agreement.multi_kappa())

def convert_to_two_level(input):
    temp_disease_two = []
    for n in input:
            tmp = []
            for i in n:
                if i > 1:
                    tmp.append(1)
                else:
                    tmp.append(0)
            temp_disease_two.append(tmp)
    return temp_disease_two

fleiss_scores_two = []
def calculate_feiss_two_disease(q_id, model):
    two_temp = []
    for tester in disease_testers:
        disease_result = db_get_query_tester_result_disease(q_id, tester, models)
        two_temp.append([n[1] for n in disease_result[model]])
    temp_disease_two = convert_to_two_level(two_temp)
    formatted_disease= formatting(temp_disease_two)
    fleiss_agreement = agreement.AnnotationTask(data=formatted_disease)
    fleiss_scores_two.append(fleiss_agreement.multi_kappa())

def avg_fleiss(scores):
    total = 0
    for score in scores:
        print(score)
        total +=score
    return total/len(scores)

#calculate for 4 levels (fungerer tror jeg, er kun for 1 modell nå, men er avg av alle queries)
for q_id in query_disease:
    calculate_feiss_four_disease(q_id,"fulltext")

disease_avg_four=avg_fleiss(fleiss_scores_four)
print("disease avg four: " + str(disease_avg_four))

# caculate for 2 levels (fungerer kanksje?)
for q_id in query_disease:
    calculate_feiss_two_disease(q_id, "fulltext")

disease_avg_two=avg_fleiss(fleiss_scores_two)
print("disease avg two: " + str(disease_avg_two))


# CALCULATE FOR MOVIE:
movie_testers = db_get_testers("SELECT tester_id FROM Tester WHERE answered_movie=1")

fleiss_scores_four_movie = []
def calculate_feiss_four_movie(q_id, model):
    temp_movie =[]
    for tester in movie_testers:
        movie_result = db_get_query_tester_result_movie(q_id, tester, models)
        temp_movie.append([n[1] for n in movie_result[model]])
    formatted_movie= formatting(temp_movie)
    fleiss_agreement = agreement.AnnotationTask(data=formatted_movie)
    fleiss_scores_four_movie.append(fleiss_agreement.multi_kappa())

# temp_movie_two = []
# def convert_to_two_level_movie(input):
#     for n in input:
#             tmp = []
#             for i in n:
#                 if i > 1:
#                     tmp.append(1)
#                 else:
#                     tmp.append(0)
#             temp_movie_two.append(tmp)

fleiss_scores_two_movie = []
def calculate_feiss_two_movie(q_id, model):
    two_temp = []
    for tester in movie_testers:
        movie_result = db_get_query_tester_result_movie(q_id, tester, models)
        two_temp.append([n[1] for n in movie_result[model]])
    temp_movie_two = convert_to_two_level(two_temp)
    print(temp_movie_two)
    formatted_movie= formatting(temp_movie_two)
    fleiss_agreement = agreement.AnnotationTask(data=formatted_movie)
    fleiss_scores_two_movie.append(fleiss_agreement.multi_kappa())

#calculate for 4 levels (fungerer tror jeg, er kun for 1 modell nå, men er avg av alle queries)
for q_id in query_movie:
    calculate_feiss_four_movie(q_id,"fulltext")

movie_avg_four=avg_fleiss(fleiss_scores_four_movie)
print("movie avg four: " + str(movie_avg_four))

# caculate for 2 levels (fungerer kanksje?)
for q_id in query_movie:
    calculate_feiss_two_movie(q_id, "fulltext")

movie_avg_two=avg_fleiss(fleiss_scores_two_movie)
print("movie avg two: " + str(movie_avg_two))
