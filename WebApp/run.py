import sys
sys.path.append('../')

from WebApp import app

if __name__ == '__main__':
    app.run(debug=True, port=8080)
