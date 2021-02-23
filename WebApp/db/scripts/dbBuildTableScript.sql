-- Generates empty tables

CREATE TABLE Tester (
   tester_id Text PRIMARY KEY,
   answered_disease BOOLEAN NOT NULL CHECK (answered_disease IN (0,1)),
   answered_movie BOOLEAN NOT NULL CHECK (answered_movie IN (0,1))
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
    relevancy INTEGER NOT NULL,
    query_id INTEGER,
    tester_id INTEGER,
    FOREIGN KEY (query_id) REFERENCES Query(query_id) 
        ON DELETE CASCADE,
    FOREIGN KEY (tester_id) REFERENCES Tester(tester_id) 
        ON DELETE CASCADE
);

INSERT INTO Query(query_text, query_description, dataset) values ("Covid-19", "Find disease named covid-19", "Disease");
INSERT INTO Query(query_text, query_description, dataset) values ("Leukemia", "Find different variations of leukemia", "Disease");
INSERT INTO Query(query_text, query_description, dataset) values ("Yellow fever", "Find the disease named yellow fever", "Disease");
--INSERT INTO Query(query_text, query_description, dataset) values ("Headache", "Find the symptom headache and/or diseases related to having a headache", "Disease");
INSERT INTO Query(query_text, query_description, dataset) values ("matrix", "Find movies in the matrix franchise", "Movie");
