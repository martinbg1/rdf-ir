import numpy as np
import sqlite3
import math
from statistics import mean
import matplotlib
import matplotlib.pyplot as plt

SQLITE_PATH = "WebApp/db/rdf-ir.db"
query_ideal_10 = {
    1: [3,3,2,2,2,2,0,0,0,0], # covid-19 (pandemic rated 1 maybe change to 2)
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

query_ideal_5 = {
    1: [3,3,2,2,2], # covid-19 (pandemic rated 1 maybe change to 2)
    2: [3,2,2,1,1], # yellow fever
    3: [3,3,3,3,3], # headache symptom
    4: [3,3,3,3,2], # influenza pandemix
    5: [3,3,2,2,1], # fear of social interaction (fear of medical procedures satt til 1, kanskje 2)
    6: [3,3,3,1,1], # matrix
    7: [3,3,3,1,1], # lotr
    8: [3,3,3,3,3], # movies by Christopher Nolan
    9: [3,2,2,2,2], # the circus chaplin
    10: [3,3,3,2,2] # Wachowski directors
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

def db_get_query_tester_result_disease(query_id, tester_id, models, n=10):
    conn = db_connect(SQLITE_PATH)
    cur = conn.cursor()
    result = {}
    for model in models:
        cur.execute("""
            SELECT result_rank, relevancy
            FROM DataDisease 
            WHERE query_id=? AND tester_id=? AND method=?""", (query_id, tester_id, model))
        result[model] = cur.fetchall()[:n]
    return result


def db_get_query_tester_result_movie(query_id, tester_id, models, n=10):
    conn = db_connect(SQLITE_PATH)
    cur = conn.cursor()
    result = {}
    for model in models:
        cur.execute("""
            SELECT result_rank, relevancy
            FROM DataMovie 
            WHERE query_id=? AND tester_id=? AND method=?""", (query_id, tester_id, model))
        result[model] = cur.fetchall()[:n]
    return result


def result_to_list(result):
    new_result = {}
    for k, v in result.items():
        new_result[k] = [i[1] for i in v]
        # new_result[k] = [i[1] for i in v][:5]
    
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


def dcg_per_step(queries, testers, models=["fulltext", "BM25", "BM25F"]):
    result = {
        "ideal": {},
        "fulltext": {},
        "BM25": {},
        "BM25F": {},
    }

    # Calculate ideal dcg per step
    for i in range(1, 11):
        tmp_dcg_ideal = []
        for q_id in queries:
            tmp_dcg_ideal.append(dcg_score(query_ideal_10[q_id][:i]))
        result["ideal"][i] = mean(tmp_dcg_ideal)


    # Calculate model's dcg per step
    for i in range(1, 11):
        tmp_dcg_model, tmp_dcg_ideal = [], []
        for model in models:
            for q_id in queries:
                for tester in testers:
                    if (queries[0] == 1):
                        tmp = db_get_query_tester_result_disease(q_id, tester, [model], i)
                    else:
                        tmp = db_get_query_tester_result_movie(q_id, tester, [model], i)
                    rankings = result_to_list(tmp)
                    dcg = dcg_score(rankings[model])
                    tmp_dcg_model.append(dcg)
            result[model][i] = mean(tmp_dcg_model)
    return result


def plot_ndcg_graph():

    # movie 10
    # ndcg_fulltext = [0.778, 0.857, 0.877, 0.841, 0.829]
    # ndcg_BM25 = [0.697, 0.790, 0.941, 0.674, 0.535]
    # ndcg_BM25F = [0.600, 0.494, 0.814, 0.651, 0.512]

    # movie 5
    # ndcg_fulltext = [0.842, 0.908, 0.774, 0.820, 0.933]
    # ndcg_BM25 = [0.676, 0.853, 0.922, 0.628, 0.559]
    # ndcg_BM25F = [0.672, 0.450, 0.670, 0.641, 0.436]

    # disease 10
    ndcg_fulltext = [0.868, 0.835, 0.717, 0.903, 0.622]
    ndcg_BM25 = [0.868, 0.816, 0.831, 0.938, 0.703]
    ndcg_BM25F = [0.914, 0.853, 0.792, 0.952, 0.779]

    # disease 5
    # ndcg_fulltext = [0.817, 0.846, 0.818, 0.910, 0.609]
    # ndcg_BM25 = [0.830, 0.853, 0.908, 0.990, 0.696]
    # ndcg_BM25F = [0.860, 0.882, 0.833, 0.997, 0.760]


    # unpack data
    # for k, v in data.items():
    #     ndcg_fulltext.append(round(v["fulltext"], 3))
    #     ndcg_BM25.append(round(v["BM25"], 3))
    #     ndcg_BM25F.append(round(v["BM25F"], 3))
    # print(ndcg_fulltext)


    labels = ["M_Q1", "M_Q2","M_Q3", "M_Q4", "M_Q5"]
    # labels = ["D_Q1", "D_Q2","D_Q3", "D_Q4", "D_Q5"]
    x =  np.arange(len(labels))

    width = 0.25

    fig, ax = plt.subplots()
    rects1 = ax.bar(x - width, ndcg_fulltext, width, label="fulltext")
    rects2 = ax.bar(x, ndcg_BM25, width, label="BM25")
    rects3 = ax.bar(x + width, ndcg_BM25F, width, label="BM25F")

    ax.set_ylabel('NDCG')
    # ax.set_title('Average NDCG scores for each movie query')
    ax.set_title('Average NDCG scores for each disease query')
    ax.set_xticks(x)
    ax.set_xticklabels(labels)
    ax.legend()

    ax.bar_label(rects1, padding=5)
    ax.bar_label(rects2, padding=5)
    ax.bar_label(rects3, padding=5)

    fig.tight_layout()
    plt.show()

    # y = np.array(ndcg_fulltext)
    # x = np.array([6,7,8,9,10])
    # # my_xticks = ["M_Q1", "M_Q2","M_Q3", "M_Q4", "M_Q5"]
    # my_xticks = ["D_Q1", "D_Q2","D_Q3", "D_Q4", "D_Q5"]
    # plt.xticks(x, my_xticks)
    # plt.plot(x, ndcg_fulltext, marker='o',  linestyle='', label='fulltext')
    # plt.plot(x, ndcg_BM25, marker='o',  linestyle='', label='BM25')
    # plt.plot(x, ndcg_BM25F, marker='o',  linestyle='', label='BM25F')
    # plt.xlabel('Query ID')
    # plt.ylabel('NDCG')
    # # plt.title('Average NDCG scores for each movie query')
    # plt.title('Average NDCG scores for each disease query')
    # plt.legend()
    # plt.show()


def plot_dcg_step_graph(data):
    unpacked_data = {}

    for k, v in data.items():
        unpacked_data[k] = []
        for k2, v2 in v.items():
            unpacked_data[k].append(v2)

    x = np.array([1,2,3,4,5,6,7,8,9,10])
    my_xticks = [1,2,3,4,5,6,7,8,9,10]
    plt.xticks(x, my_xticks)
    plt.plot(x, unpacked_data["ideal"], marker='o',  linestyle='--', color="black", label='Ideal')
    plt.plot(x, unpacked_data["fulltext"], marker='o',  linestyle='--', label='fulltext')
    plt.plot(x, unpacked_data["BM25"], marker='o',  linestyle='--', label='BM25')
    plt.plot(x, unpacked_data["BM25F"], marker='o',  linestyle='--', label='BM25F')

    plt.xlabel('Entities Retrieved')
    plt.ylabel('DCG')
    plt.title('Discounted cumulated gain (DCG) curves for the disease dataset')
    # plt.title('Discounted cumulated gain (DCG) curves for the movie dataset')
    plt.legend()
    plt.show()

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


    for q_id in query_disease:
        result["disease"][q_id] = []
        for tester in testers_disease:
            tmp = db_get_query_tester_result_disease(q_id, tester, models)
            rankings = result_to_list(tmp)

            # dcg_ideal = dcg_score(query_ideal_5[q_id])
            dcg_ideal = dcg_score(query_ideal_10[q_id])

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

            # dcg_ideal = dcg_score(query_ideal_5[q_id])
            dcg_ideal = dcg_score(query_ideal_10[q_id])

            tmp = {}
            for model in models:
                dcg = dcg_score(rankings[model])
                ndcg = ndcg_score(dcg_ideal, dcg)
                tmp[model] = ndcg
            result["movie"][q_id].append(tmp)

    mean_average_ndcg = {}
    mean_average_data_points = {
        "fulltext": [],
        "BM25": [],
        "BM25F": []
    }

    # dcg_steps = dcg_per_step(query_disease, testers_disease)

    # for k,v in dcg_steps.items():
    #     print(k)
    #     print(v)

    # plot_dcg_step_graph(dcg_steps)
    avg_movie = avg_ndcg_query(result["movie"])
    print(avg_movie)
    # avg_ndcg_dataset(avg_movie)
    plot_ndcg_graph()
