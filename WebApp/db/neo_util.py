from WebApp.util.serialize_search import serialize_results


def bm25_search(db, q):
    results = db.run('CALL improvedSearch.bm25Search("'+ q +'") ')
    return [serialize_results(record) for record in results]


def bm25f_search(db, q):
    results = db.run('CALL improvedSearch.bm25fSearch("'+ q +'") ')
    return [serialize_results(record) for record in results]

