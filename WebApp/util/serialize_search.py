import json
import re

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
        try:
            processed = re.sub(r'([A-Za-z0-9_.\-/:\s,]+)', r'"\1"', res['node']).replace('=', ':').replace(', name', '", "name').replace(', description', '", "description').replace(', uri', '", "uri')

            processed = json.loads(processed)
            return {
                'name': processed['name'],
                'description': processed['description'],
                'altNames': processed['altNames']
                # 'score': res['score']
            }
        except:
            return "no data"
    return {
        'name': node['name'],
        'description': node['description'],
        'altNames': node['altNames'],
        'score': res['score']
    }
