from flask import Flask, g, Response, jsonify, render_template, request
from WebApp import app


@app.route('/home')
def home():
    return render_template('home.html')


# render index
@app.route('/')
def index():
    return render_template('index.html')

