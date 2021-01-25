from WebApp.util.serialize_search import serialize_results, serialize_disease


def bm25_search(db, q):
    results = db.run('CALL improvedSearch.bm25Search("'+ q +'") ')
    return [serialize_results(record) for record in results]


def bm25f_search(db, q):
    results = db.run('CALL improvedSearch.bm25fSearch("'+ q +'") ')
    return [serialize_results(record) for record in results]

def fulltext_search(db, q):
    results = db.run("call db.index.fulltext.queryNodes('NameDescAlias','name:"+ q +" OR altNames:" + q + " OR description:" + q + "')"+
    " YIELD node, score " +
    "RETURN node, score limit 10")
    return [serialize_disease(record) for record in results]
