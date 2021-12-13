#!/bin/bash
#
# This script downloads the biobert model from a Google Drive link
fileid="1GJpGjQj6aZPV-EfbiQELpBkvlGtoKiyA"
filename="biobert_large_v1.1_pubmed.tar.gz"
curl -c ./cookie -s -L "https://drive.google.com/uc?export=download&id=${fileid}" > /dev/null
curl -Lb ./cookie "https://drive.google.com/uc?export=download&confirm=`awk '/download/ {print $NF}' ./cookie`&id=${fileid}" -o ${filename}
