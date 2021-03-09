import json
import re

def serialize_disease(disease):
    return {
        'name': disease["node"]['name'],
        'description': disease["node"]['description'],
        'altNames':disease["node"]['altNames'],
        'score':disease['score']
    }

def serialize_fulltext(node, dataset):
    if dataset == "disease":
        return {
            'name': node["node"]['name'],
            'description': node["node"]['description'],
            'altNames':node["node"]['altNames'],
            'score':node['score']
        }
    elif dataset == "movie":
        return {
            'name': node["node"]['name'],
            'description': node["node"]['description'],
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


def serialize_results(res, dataset="disease"):
    try:
        node = json.loads(res['node'])
    except:
        try:
            processed = re.sub(r'([A-Za-z0-9_.#😱\-\–/:;|\s,\'\w()\[\]\\]+)', r'"\1"', res['node'].replace('"', "\\'")).replace('=', ':').replace(', name', '", "name').replace(', description', '", "description').replace(', uri', '", "uri').replace("\\'", '\\"')

            processed = json.loads(processed)
            if dataset == "disease":
                return {
                    'name': processed['name'],
                    'description': processed['description'],
                    'altNames': processed['altNames']
                    # 'score': res['score']
                }
            elif dataset =="movie":
                return {
                    'name': processed['name'],
                    'description': processed['description'],
                    # 'altNames': processed['altNames']
                    # 'score': res['score']
                }
        except:
            print(res)
            return "no data"
    if dataset == "disease":
            return {
                'name': node['name'],
                'description': node['description'],
                'altNames': node['altNames'],
                'score': res['score']
            }
    elif dataset =="movie":
        return {
            'name': node['name'],
            'description': node['description'],
            # 'altNames': processed['altNames']
            'score': res['score']
        }
