CREATE TABLE DifferentialEquation (
  id          UUID PRIMARY KEY,
  name        TEXT NOT NULL UNIQUE,
  symbol      TEXT NOT NULL,
  description TEXT NOT NULL,
  variables   TEXT NOT NULL,
  parameters  TEXT NOT NULL,
  created     TIMESTAMP NOT NULL,
  updated     TIMESTAMP NOT NULL
);

CREATE TABLE DifferentialEquationProperty (
  id          UUID PRIMARY KEY,
  name        TEXT NOT NULL UNIQUE,
  description TEXT NOT NULL,
  created     TIMESTAMP NOT NULL,
  updated     TIMESTAMP NOT NULL
);

CREATE TABLE DifferentialEquationLink (
  id                             UUID PRIMARY KEY,
  differentialEquationId         UUID REFERENCES DifferentialEquation(id),
  differentialEquationPropertyId UUID REFERENCES DifferentialEquationProperty(id),
  hasProperty                    BOOLEAN NOT NULL,
  description                    TEXT,
  created                        TIMESTAMP NOT NULL,
  updated                        TIMESTAMP NOT NULL
);

ALTER TABLE Reference ADD COLUMN differentialEquationId UUID REFERENCES DifferentialEquation(id);
ALTER TABLE Reference ADD COLUMN differentialEquationPropertyId UUID REFERENCES DifferentialEquationProperty(id);
ALTER TABLE Reference ADD COLUMN differentialEquationLinkId UUID REFERENCES DifferentialEquationLink(id);

