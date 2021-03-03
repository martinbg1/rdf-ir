import os
import json
from flask import Flask, g, Response, jsonify, render_template, request
from neo4j import GraphDatabase, basic_auth
from .util.serialize_search import *

app = Flask(__name__)

# connect to neo4j db
url_disease = os.getenv("NEO4J_URL_DISEASE", "bolt://localhost")
url_movie = os.getenv("NEO4J_URL_MOVIE", "bolt://localhost")

user = os.getenv("NEO4J_USER", "neo4j")

password = os.getenv("NEO4J_PASSWORD", "123")

driver_disease = GraphDatabase.driver(url_disease, auth=basic_auth(user, password),encrypted=False)
driver_movie = GraphDatabase.driver(url_movie, auth=basic_auth(user, password),encrypted=False)

app.config['SECRET_KEY'] = "secretkey"

sqlite_path = os.getenv("SQLITE_PATH", "db/rdf-ir.db")
# print(sqlite_path)

def get_neo_db():
    if not hasattr(g, 'neo4j_db'):
        g.neo4j_db = driver_disease.session()
    return g.neo4j_db

# import views after app and neo4j db is initialized
from .views import pages, search
