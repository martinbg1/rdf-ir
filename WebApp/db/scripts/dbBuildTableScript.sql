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

INSERT INTO Query(query_text, query_description, dataset) values ("covid-19", "Find disease named covid-19", "Disease");
INSERT INTO Query(query_text, query_description, dataset) values ("yellow fever", "Find the disease named yellow fever", "Disease");
INSERT INTO Query(query_text, query_description, dataset) values ("headache symptom", "Find diseases/disorders of which headache is a symptom for", "Disease");
INSERT INTO Query(query_text, query_description, dataset) values ("influenza pandemic", "Find pandemics caused by influenza", "Disease");
INSERT INTO Query(query_text, query_description, dataset) values ("fear of social interaction", "Find the phobia related to social interaction", "Disease");

INSERT INTO Query(query_text, query_description, dataset) values ("matrix movies", "Find movies in the matrix franchise", "Movie");
INSERT INTO Query(query_text, query_description, dataset) values ("lord of the rings", "Find movies in the “The lord of the rings” franchise", "Movie");
INSERT INTO Query(query_text, query_description, dataset) values ("movies by Christopher Nolan", "Find movies directed by Christopher Nolan", "Movie");
INSERT INTO Query(query_text, query_description, dataset) values ("the circus chaplin", "Find the 1928 Charlie Chaplin movie “The Circus”", "Movie");
INSERT INTO Query(query_text, query_description, dataset) values ("Wachowski directors", " Find the Wachowski sisters (directors of The Matrix franchise)", "Movie");
