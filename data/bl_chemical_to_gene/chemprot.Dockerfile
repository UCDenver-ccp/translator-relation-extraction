FROM python:3.7

RUN apt-get update && apt-get install -y \
    git \
    unzip \
    wget \
    less \
    vim 

# create the 'dev' user
RUN groupadd dev && \
    useradd --create-home --shell /bin/bash --no-log-init -g dev dev

USER dev:dev

# Download tghe ChemProt corpus and unzip it
WORKDIR /home/dev/data
RUN wget https://biocreative.bioinformatics.udel.edu/media/store/files/2017/ChemProt_Corpus.zip && \
    unzip ChemProt_Corpus.zip && \
    cd ChemProt_Corpus && \
    unzip chemprot_development.zip && \
    unzip chemprot_test_gs.zip && \
    unzip chemprot_training.zip

# Download the BLUE_Benchmark code as it has a script to convert the ChemProt corpus into the
# format expected by BioBERT.
WORKDIR /home/dev/
RUN mkdir data/bert-input && \
    git clone https://github.com/ncbi-nlp/BLUE_Benchmark.git ./blue-benchmark.git && \
    cd blue-benchmark.git && \
    python3 -m pip install --upgrade pip && \
    pip install -r requirements.txt && \
    pip install fire && \
    pip install tqdm && \
    pip install sympy && \
    pip install bioc && \
    pip install https://github.com/explosion/spacy-models/releases/download/en_core_web_md-2.0.0/en_core_web_md-2.0.0.tar.gz && \
    python -m spacy download en_core_web_sm

ENV PYTHONPATH '.'

WORKDIR /home/dev/blue-benchmark.git

# the create_chemprot_bert.py script works on the training data, 
# but has commented out the code to process the dev and test data. 
# Here, we uncomment that code be deleting the first 5 characters on 
# lines 175-182 which are some leading spaces followed by the '#' character
# to comment out the line. The removed text is replaced by spaces to preserve
# the indenting required by Python.
#
# We also fix a type - replacing train.tsv with test.tsv on line 182
RUN sed -i '175,182s/...../   /' blue/bert/create_chemprot_bert.py && \
    sed -i '182s/train/test/' blue/bert/create_chemprot_bert.py

RUN python blue/bert/create_chemprot_bert.py /home/dev/data/ChemProt_Corpus /home/dev/data/bert-input

WORKDIR /home/dev/output

ENTRYPOINT tail -n +2 /home/dev/data/bert-input/train.tsv >> data.tsv && tail -n +2 /home/dev/data/bert-input/dev.tsv >> data.tsv && tail -n +2 /home/dev/data/bert-input/test.tsv >> data.tsv


# To run:
#
# docker build -t chemprot -f chemprot.Dockerfile .
#
# docker run --rm -v "$PWD":/home/dev/output chemprot