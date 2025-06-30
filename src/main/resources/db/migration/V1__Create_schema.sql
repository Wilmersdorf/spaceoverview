CREATE TABLE UserData (
  id      UUID PRIMARY KEY,
  email   TEXT UNIQUE,
  hash    TEXT,
  isAdmin BOOLEAN,
  created TIMESTAMP WITH TIME ZONE,
  updated TIMESTAMP WITH TIME ZONE
);

CREATE TABLE SpaceData (
  id          UUID PRIMARY KEY,
  symbol      TEXT UNIQUE,
  norm        TEXT,
  description TEXT,
  field       TEXT,
  created     TIMESTAMP WITH TIME ZONE,
  updated     TIMESTAMP WITH TIME ZONE
);

CREATE TABLE Property (
  id          UUID PRIMARY KEY,
  name        TEXT UNIQUE,
  description TEXT,
  field       TEXT,
  created     TIMESTAMP WITH TIME ZONE,
  updated     TIMESTAMP WITH TIME ZONE
);

CREATE TABLE Link (
  id          UUID PRIMARY KEY,
  spaceId     UUID REFERENCES SpaceData(id),
  propertyId  UUID REFERENCES Property(id),
  field       TEXT,
  description TEXT,
  created     TIMESTAMP WITH TIME ZONE,
  updated     TIMESTAMP WITH TIME ZONE
);

CREATE TABLE Theorem (
  id          UUID PRIMARY KEY,
  name        TEXT UNIQUE,
  description TEXT,
  created     TIMESTAMP WITH TIME ZONE,
  updated     TIMESTAMP WITH TIME ZONE
);

CREATE TABLE Condition (
  id          UUID PRIMARY KEY,
  theoremId   UUID REFERENCES Theorem(id),
  propertyId  UUID REFERENCES Property(id),
  field       TEXT,
  created     TIMESTAMP WITH TIME ZONE,
  updated     TIMESTAMP WITH TIME ZONE
);

CREATE TABLE Conclusion (
  id          UUID PRIMARY KEY,
  theoremId   UUID REFERENCES Theorem(id),
  propertyId  UUID REFERENCES Property(id),
  field       TEXT,
  created     TIMESTAMP WITH TIME ZONE,
  updated     TIMESTAMP WITH TIME ZONE
);

CREATE TABLE Computation (
  id         UUID PRIMARY KEY,
  spaceId    UUID REFERENCES SpaceData(id),
  propertyId UUID REFERENCES Property(id),
  theoremId  UUID REFERENCES Theorem(id),
  field      TEXT,
  created    TIMESTAMP WITH TIME ZONE,
  updated    TIMESTAMP WITH TIME ZONE
);

CREATE TABLE Reference (
  id          UUID PRIMARY KEY,
  spaceId     UUID REFERENCES SpaceData(id),
  propertyId  UUID REFERENCES Property(id),
  linkId      UUID REFERENCES Link(id),
  theoremId   UUID REFERENCES Theorem(id),
  title       TEXT,
  url         TEXT,
  arxivId     TEXT,
  wikipediaId NUMERIC,
  bibtex      TEXT,
  page        NUMERIC,
  statement   TEXT,
  created     TIMESTAMP WITH TIME ZONE,
  updated     TIMESTAMP WITH TIME ZONE
);

CREATE TABLE Invite (
  id      UUID PRIMARY KEY,
  code    TEXT UNIQUE,
  created TIMESTAMP WITH TIME ZONE,
  updated TIMESTAMP WITH TIME ZONE
);
