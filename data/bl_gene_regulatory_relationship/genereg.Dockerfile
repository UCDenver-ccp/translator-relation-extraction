#
# When run, this container downloads the GeneReg corpus and populates the
# data.tsv file with training data for the gene-gene regulatory association
# sentence classifier.
#
FROM adoptopenjdk:8-jdk

RUN apt-get update && apt-get install -y \
    maven \
    wget

# # create the dev user
# RUN groupadd --gid 9001 dev && \
#     useradd --create-home --shell /bin/bash --no-log-init -u 9001 -g dev dev

# download the GeneReg corpus
WORKDIR /home/dev/data
RUN wget https://julielab.de/downloads/resources/GeneReg_tar.gz && \
    tar -xzf GeneReg_tar.gz

# copy the pom.xml file, then install dependencies
# Doing this separately makes interative development easier as
# dependencies will only be redownloaded when the pom.xml file changes.
COPY code/java/pom.xml /home/dev/code/
# RUN chown -R dev:dev /home/dev
# USER dev
WORKDIR /home/dev/code/
RUN mvn verify

# copy the code (and the pom file) and then build and install
COPY code/java /home/dev/code

# build and install code
WORKDIR /home/dev/code/
RUN mvn clean install

ENV MAVEN_OPTS "-Xmx4G"
ENTRYPOINT mvn exec:java -Dexec.mainClass='edu.cuanschutz.ccp.bert_prep.genereg.GeneRegCorpusReaderMain' -Dexec.args='/home/dev/data/GeneReg/GeneReg_V_1.0 /home/dev/output/data.tsv APPEND'



# To run:
#
# docker build -t genereg -f genereg.Dockerfile .
#
# docker run --rm -v "$PWD":/home/dev/output genereg