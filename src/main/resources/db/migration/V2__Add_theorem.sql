CREATE TABLE Theorem (
  id      UUID PRIMARY KEY,
  name    TEXT UNIQUE,
  created TIMESTAMP NOT NULL,
  updated TIMESTAMP NOT NULL
);

CREATE TABLE Condition (
  id          UUID PRIMARY KEY,
  theoremId   UUID REFERENCES Theorem(id),
  propertyId  UUID REFERENCES Property(id),
  field       TEXT NOT NULL,
  created     TIMESTAMP NOT NULL,
  updated     TIMESTAMP NOT NULL
);

CREATE TABLE Conclusion (
  id          UUID PRIMARY KEY,
  theoremId   UUID REFERENCES Theorem(id),
  propertyId  UUID REFERENCES Property(id),
  field       TEXT NOT NULL,
  created     TIMESTAMP NOT NULL,
  updated     TIMESTAMP NOT NULL
);

ALTER TABLE Reference ADD COLUMN theoremId UUID REFERENCES Theorem(id);
