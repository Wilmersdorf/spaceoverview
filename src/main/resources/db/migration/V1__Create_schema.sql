CREATE TABLE UserData (
  id      UUID PRIMARY KEY,
  email   TEXT NOT NULL UNIQUE,
  hash    TEXT NOT NULL,
  isAdmin BOOLEAN NOT NULL,
  created TIMESTAMP NOT NULL,
  updated TIMESTAMP NOT NULL
);

CREATE TABLE SpaceData (
  id          UUID PRIMARY KEY,
  symbol      TEXT NOT NULL UNIQUE,
  norm        TEXT NOT NULL,
  description TEXT NOT NULL,
  field       TEXT NOT NULL,
  created     TIMESTAMP NOT NULL,
  updated     TIMESTAMP NOT NULL
);

CREATE TABLE Property (
  id          UUID PRIMARY KEY,
  name        TEXT NOT NULL UNIQUE,
  description TEXT NOT NULL,
  field       TEXT NOT NULL,
  created     TIMESTAMP NOT NULL,
  updated     TIMESTAMP NOT NULL
);

CREATE TABLE Link (
  id         UUID PRIMARY KEY,
  spaceId    UUID REFERENCES SpaceData(id),
  propertyId UUID REFERENCES Property(id),
  field      TEXT NOT NULL,
  created    TIMESTAMP NOT NULL,
  updated    TIMESTAMP NOT NULL
);

CREATE TABLE Reference (
  id          UUID PRIMARY KEY,
  spaceId     UUID REFERENCES SpaceData(id),
  propertyId  UUID REFERENCES Property(id),
  linkId      UUID REFERENCES Link(id),
  url         TEXT,
  title       TEXT NOT NULL,
  arxivId     TEXT,
  wikipediaId BIGINT,
  bibtex      TEXT,
  page        BIGINT,
  statement   TEXT,
  created     TIMESTAMP NOT NULL,
  updated     TIMESTAMP NOT NULL
);

CREATE TABLE Invite (
  id      UUID PRIMARY KEY,
  code    TEXT NOT NULL UNIQUE,
  created TIMESTAMP NOT NULL,
  updated TIMESTAMP NOT NULL
);
