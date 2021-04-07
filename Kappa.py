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

def db_get_disease_testers():
    conn = db_connect(SQLITE_PATH)
    cur = conn.cursor()
    cur.execute("SELECT tester_id FROM Tester WHERE answered_disease=1")
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

# AB_score= cohen_kappa_score(A,B, labels=None, weights=None)
# print(AB_score)
result = []
models = ["fulltext"]
disease_testers = db_get_disease_testers()
# print(disease_testers)
for tester in disease_testers:
    # print(tester)
    disease_result = db_get_query_tester_result_disease(5, tester, models)
    result.append([n[1] for n in disease_result["fulltext"]])
    # for n in disease_result['fulltext'][1]:
print(result)

def formatting(result):
    temp = []
    for j,res in enumerate(result):
        temp += [[j,i,res[i]] for i in range(0,len(res))]
        # print("i for loop: " + temp)
    return temp

temp = formatting(result)
score_fleiss = agreement.AnnotationTask(data=temp)
print(score_fleiss.multi_kappa())

# [[0,i,A[i]] for i in range(0,len(A))]


# formatted_codes = [[0,i,A[i]] for i in range(0,len(A))] + [[1,i,B[i]] for i in range(0,len(B))] + [[2,i,C[i]] for i in range(0,len(C))]+ [[3,i,D[i]] for i in range(0,len(D))]
# print(formatted_codes)
# AB_score_fleiss = agreement.AnnotationTask(data=formatted_codes)
# print(AB_score_fleiss.multi_kappa())
