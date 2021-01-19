-- Generates empty tables

CREATE TABLE Tester (
   tester_id INTEGER PRIMARY KEY AUTOINCREMENT,
   answered BOOLEAN NOT NULL CHECK (answered IN (0,1))
);


CREATE TABLE Query (
   query_id INTEGER PRIMARY KEY AUTOINCREMENT,
   query_text TEXT NOT NULL,
   query_description TEXT NOT NULL,
   dataset TEXT NOT NULL
);

CREATE TABLE DataDisease (
    data_id INTEGER PRIMARY KEY AUTOINCREMENT,
    method TEXT NOT NULL,
    result_rank INTEGER NOT NULL,
    relevancy INTEGER NOT NULL,
    query_id INTEGER,
    tester_id INTEGER,
    FOREIGN KEY (query_id) REFERENCES Query(query_id) 
        ON DELETE CASCADE,
    FOREIGN KEY (tester_id) REFERENCES Tester(tester_id) 
        ON DELETE CASCADE

);


CREATE TABLE DataMovie (
    data_id INTEGER PRIMARY KEY AUTOINCREMENT,
    method TEXT NOT NULL,
    result_rank INTEGER NOT NULL,
    score INTEGER NOT NULL,
    query_id INTEGER,
    tester_id INTEGER,
    FOREIGN KEY (query_id) REFERENCES Query(query_id) 
        ON DELETE CASCADE,
    FOREIGN KEY (tester_id) REFERENCES Tester(tester_id) 
        ON DELETE CASCADE
);

INSERT INTO Query(query_text, query_description, dataset) values ("covid", "Find disease named covid", "Disease");
INSERT INTO Query(query_text, query_description, dataset) values ("leukemia", "Find disease named leukemia", "Disease");
INSERT INTO Query(query_text, query_description, dataset) values ("matrix", "Find movies in the matrix franchise", "Movie");
