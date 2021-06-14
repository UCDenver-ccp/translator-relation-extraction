# Compiling data for the biolink:ChemicalToGeneAssociation model

The [ChemProt corpus](https://biocreative.bioinformatics.udel.edu/news/corpora/chemprot-corpus-biocreative-vi/) is used to train a model to classify sentences that relate chemicals to genes. Populating (or adding to) the data.tsv file that exists in this directory (and is used as the training data for the model) is facilitated via the `chemprot.Dockerfile` container. To download and process the ChemProt corpus, build the image:

```
docker build -t chemprot -f chemprot.Dockerfile .
```

And then run the following from inside this directory:
```
docker run --rm -v "$PWD":/home/dev/output chemprot
```