import json

def serialize_disease(disease):
    return {
        'name': disease["node"]['name'],
        'description': disease["node"]['description'],
        'altNames':disease["node"]['altNames'],
        'score':disease['score']
    }

def serialize_fulltext(node):
    return {
        'name': node["node"]['name'],
        'description': node["node"]['description'],
        'altNames':node["node"]['altNames'],
        'score':node['score']
    }


def serialize_symptom(symptom):
    return {
        'name': symptom['name'],
        'description': symptom['description'],
        'altNames': symptom['altNames']
    }


def serialize_drug(drug):
    return {
        'name': drug['name'],
        'altNames': drug['altNames']
    }


def serialize_results(res):
    try:
        node = json.loads(res['node'])
    except:
        return 'no data'
    return {
        'name': node['name'],
        'description': node['description'],
        'altNames': node['altNames'],
        'score': res['score']
    }
