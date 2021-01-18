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
    

def add_test_result_disease(conn, method, result_rank, relevancy, query_id, tester_id):
    """
    Function to store test result from a query result in the db
    """
    cur = conn.cursor()
    cur.execute(
        "insert into DataDisease(method, result_rank, relevancy, query_id, tester_id) values (?, ?, ?, ?, ?)", 
        (method, result_rank, relevancy, query_id, tester_id)
        )
    conn.commit()


# current working directory is ./WebApp/db
if __name__ == '__main__':
    build_path = "scripts/dbBuildTableScript.sql"
    drop_path = "scripts/dbDropTableScript.sql"

    conn = db_connect("rdf-ir.db")
    conn.close()
