import sqlite3
from sqlite3 import Error


def db_connect(db_file):
    """ Start a database connection to a sqlite database """
    conn = None

    try:
        conn = sqlite3.connect(db_file)
        print(sqlite3.version)
    except Error as e:
        print(e)
    return conn


def run_sql_script(conn, sql_file_path):
    sql_script = open(sql_file_path)
    sql_as_string = sql_script.read()

    cursor = conn.cursor()
    cursor.executescript(sql_as_string)
    conn.commit()
    


# current working directory is ./WebApp/db
if __name__ == '__main__':
    build_path = "scripts/dbBuildTableScript.sql"
    drop_path = "scripts/dbDropTableScript.sql"

    conn = db_connect("rdf-ir.db")

    run_sql_script(conn, build_path)

    conn.close()
