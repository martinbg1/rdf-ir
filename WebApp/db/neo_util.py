from WebApp.util.serialize_search import serialize_results, serialize_fulltext


def bm25_search(db, q, dataset):
    results = db.run('CALL improvedSearch.bm25Search("'+ q +'") ')
    return [serialize_results(record, dataset) for record in results]


def bm25f_search(db, q, dataset):
    results = db.run('CALL improvedSearch.bm25fSearch("'+ q +'") ')
    return [serialize_results(record, dataset) for record in results]

def fulltext_search(db, q, dataset):
    results = db.run("call db.index.fulltext.queryNodes('NameDescAlias','name:"+ q +" OR altNames:" + q + " OR description:" + q + "')"+
    " YIELD node, score " +
    "RETURN node, score limit 10")
    return [serialize_fulltext(record, dataset) for record in results]
