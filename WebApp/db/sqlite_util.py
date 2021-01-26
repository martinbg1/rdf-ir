import sqlite3
from sqlite3 import Error


def db_connect(db_file):
    """ 
    Start a database connection to a sqlite database
    :param db_file
        path to sqlite3 db file
     """
    conn = None

    try:
        conn = sqlite3.connect(db_file)
        print(sqlite3.version)
    except Error as e:
        print(e)
    return conn


def run_sql_script(conn, sql_file_path):
    """ 
    Function to execute sql script
    :param conn
        sqlite3 connection
    :parm sql_file_path
        path to sql script file
     """
    sql_script = open(sql_file_path)
    sql_as_string = sql_script.read()

    cur = conn.cursor()
    cur.executescript(sql_as_string)
    conn.commit()
    

def add_test_result_disease(method, result_rank, relevancy, query_id, tester_id):
    """
    Function to store test result from a query result in the db
    """
    conn = db_connect("db/rdf-ir.db")
    cur = conn.cursor()
    cur.execute(
        "insert into DataDisease(method, result_rank, relevancy, query_id, tester_id) values (?, ?, ?, ?, ?)", 
        (method, result_rank, relevancy, query_id, tester_id)
        )
    conn.commit()

def add_test_result_movie(method, result_rank, relevancy, query_id, tester_id):
    """
    Function to store test result from a query result in the db
    """
    conn = db_connect("db/rdf-ir.db")
    cur = conn.cursor()
    cur.execute(
        "insert into DataMovie(method, result_rank, relevancy, query_id, tester_id) values (?, ?, ?, ?, ?)", 
        (method, result_rank, relevancy, query_id, tester_id)
        )
    conn.commit()


def get_disease_query():
    conn = db_connect("db/rdf-ir.db")
    cur = conn.cursor()
    cur.execute("SELECT * FROM Query WHERE dataset='Disease' LIMIT 1")
    return cur.fetchall()


def get_random_disease_queries(n):
    conn = db_connect("db/rdf-ir.db")
    cur = conn.cursor()
    cur.execute("SELECT * FROM Query WHERE dataset='Disease' ORDER BY RANDOM() LIMIT ?", (n,))
    return cur.fetchall()


def get_random_movie_queries(n):
    conn = db_connect("db/rdf-ir.db")
    cur = conn.cursor()
    cur.execute("SELECT * FROM Query WHERE dataset='Movie' ORDER BY RANDOM() LIMIT ?", (n,))
    return cur.fetchall()

def add_new_user(user_id):
    conn = db_connect("db/rdf-ir.db")
    cur = conn.cursor()
    cur.execute("INSERT INTO Tester(tester_id, answered_disease, answered_movie) VALUES (?, 0, 0)", (user_id,))
    conn.commit()


def user_finished(user_id, dataset):
    conn = db_connect("db/rdf-ir.db")
    cur = conn.cursor()
    cur.execute("UPDATE Tester SET %s = ? WHERE tester_id = ?" %("answered_"+dataset), (1, user_id))
    conn.commit()


    
# current working directory is ./WebApp/db
if __name__ == '__main__':
    build_path = "scripts/dbBuildTableScript.sql"
    drop_path = "scripts/dbDropTableScript.sql"

    conn = db_connect("rdf-ir.db")
    run_sql_script(conn, drop_path)
    run_sql_script(conn, build_path)

    conn.close()
