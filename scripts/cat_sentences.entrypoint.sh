#!/bin/bash

resources_bucket=$1
collection=$2
association_key_lc=$3
sentence_version=$4

bert_input_file_name_with_metadata="bert-input-${association_key_lc}.metadata.${sentence_version}.${collection}.tsv.gz"
to_be_classified_sentence_bucket="${resources_bucket}/output/sentences_tsv/${sentence_version}/${association_key_lc}/${collection}"
sentence_file_prefix="${to_be_classified_sentence_bucket}/${association_key_lc}"
bert_input_file_name="bert-input-${association_key_lc}.${sentence_version}.${collection}.tsv"
bert_input_sentence_bucket="${to_be_classified_sentence_bucket}/bert-input"

echo "bert_input_file_name_with_metadata: ${bert_input_file_name_with_metadata}"
echo "to_be_classified_sentence_bucket: ${to_be_classified_sentence_bucket}"
echo "sentence_file_prefix: ${sentence_file_prefix}"
echo "bert_input_file_name: ${bert_input_file_name}"
echo "bert_input_sentence_bucket: ${bert_input_sentence_bucket}"

mkdir -p /home/airflow/gcs/data/to_bert && \
cd /home/airflow/gcs/data/to_bert && \
gsutil -m cat "${sentence_file_prefix}*" > "${bert_input_file_name_with_metadata}" && \
gsutil -m cp "${bert_input_file_name_with_metadata}" "${to_be_classified_sentence_bucket}" && \
gunzip -c "${bert_input_file_name_with_metadata}" | cut -f 1-2 > "${bert_input_file_name}" && \
gsutil -m cp "${bert_input_file_name}" "${bert_input_sentence_bucket}/"