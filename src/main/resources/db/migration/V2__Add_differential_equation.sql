CREATE TABLE DifferentialEquation (
  id          UUID PRIMARY KEY,
  name        TEXT UNIQUE,
  symbol      TEXT,
  description TEXT,
  variables   TEXT,
  parameters  TEXT,
  created     TIMESTAMP WITH TIME ZONE,
  updated     TIMESTAMP WITH TIME ZONE
);

CREATE TABLE DifferentialEquationProperty (
  id          UUID PRIMARY KEY,
  name        TEXT UNIQUE,
  description TEXT,
  created     TIMESTAMP WITH TIME ZONE,
  updated     TIMESTAMP WITH TIME ZONE
);

CREATE TABLE DifferentialEquationLink (
  id                             UUID PRIMARY KEY,
  differentialEquationId         UUID REFERENCES DifferentialEquation(id),
  differentialEquationPropertyId UUID REFERENCES DifferentialEquationProperty(id),
  hasProperty                    BOOLEAN,
  description                    TEXT,
  created                        TIMESTAMP WITH TIME ZONE,
  updated                        TIMESTAMP WITH TIME ZONE
);

ALTER TABLE Reference ADD COLUMN differentialEquationId UUID REFERENCES DifferentialEquation(id);
ALTER TABLE Reference ADD COLUMN differentialEquationPropertyId UUID REFERENCES DifferentialEquationProperty(id);
ALTER TABLE Reference ADD COLUMN differentialEquationLinkId UUID REFERENCES DifferentialEquationLink(id);
