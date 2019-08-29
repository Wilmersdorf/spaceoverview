CREATE TABLE Computation (
  id         UUID PRIMARY KEY,
  spaceId    UUID REFERENCES SpaceData(id),
  propertyId UUID REFERENCES Property(id),
  theoremId  UUID REFERENCES Theorem(id),
  field      TEXT NOT NULL,
  created    TIMESTAMP NOT NULL,
  updated    TIMESTAMP NOT NULL
);
