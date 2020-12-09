# translator-relation-extraction
This repository catalogs manually annotated sentences, code, and models used to mine relations between ontology concepts from text.

# Annotation classes
Most relations have two major classes, Y(es, asserts a relation) and N(o, does not assert the relation). The exception is the relations that encode regulation.

The regulation relations share the major class N(o, does not assert the relation) with the other relations. In lieu of the Y(es) class, they have the following:

1. I(increase)
2. D(ecrease)
3. U(unspecified direction)

There are a number of other relations that are applied across all relations. These include:

1. anaphora: relation is asserted, but requires coreference resolution to extract the pair of entities.
2. speculation: speculation, rather than assertion, of the relation.
3. entity: relation is asserted, but between one or more classes other than the ones that are specified as slot fillers.
4. process: relation is asserted, but between a named entity and a process, rather than between two of the named entities that are specified as slot fillers. Note that we use the term "process" not dans une optique théorique, but rather just informally.
5. Other relation: if a relation is asserted, but it's not the one that it is meant to be, then the utterance is annotated with the name of that relation. That does not imply anything about whether it would be labelled as Y, N, etc. for that relation.

### DIRECTORY STRUCTURE ###

data/unannotated
data/annotated

code

docs/drafts
