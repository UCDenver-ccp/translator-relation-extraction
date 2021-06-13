# Compiling data for the biolink:GeneRegulatoryRelationshipAssociation model

The [GENEREG corpus](https://julielab.de/Resources/GeneReg.html)\[1\] is used to train a model to classify sentences that relate genes to each other in a regulatory relationship. Populating (or adding to) the data.tsv file that exists in this directory (and is used as the training data for the model) is facilitated via the `genereg.Dockerfile` container. To download and process the GeneReg corpus, build the image:

```
docker build -t genereg -f genereg.Dockerfile .
```

And then run the following from inside this directory:
```
docker run --rm -v "$PWD":/home/dev/output genereg
```



\[1\] [The GeneReg Corpus for Gene Expression Regulation Events — An Overview of the Corpus and its In-Domain and Out-of-Domain Interoperability. Ekaterina Buyko, Elena Beisswanger, Udo Hahn. LREC 2010.](https://www.aclweb.org/anthology/L10-1280/)

