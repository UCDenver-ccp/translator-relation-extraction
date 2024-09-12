#!/bin/bash

resources_bucket=$1
collection=$2
association_key_lc=$3
sentence_version=$4
model_version=$5

# gs://translator-text-workflow-dev_work/output/classified_sentences/sent_0.6.0/bl_chemical_to_cell/model_0.4/filtered/bl_chemical_to_cell.0.6.0_0.4.PMC_SUBSET_31.classified_sentences.filtered-00000-of-00383.tsv.gz
input_bucket="${resources_bucket}/output/classified_sentences/sent_${sentence_version}/${association_key_lc}/model_${model_version}/filtered/${collection}"
sentence_file_prefix="${input_bucket}/${association_key_lc}.${sentence_version}_${model_version}.${collection}.classified_sentences.*.gz"

aggregated_bert_output_file_name="bert-output-${association_key_lc}.sent_${sentence_version}.model_${model_version}.${collection}.tsv.gz"

echo "aggregated_bert_output_file_name: ${aggregated_bert_output_file_name}"
echo "input_bucket: ${input_bucket}"
echo "sentence_file_prefix: ${sentence_file_prefix}"

mkdir -p /home/airflow/gcs/data/to_bert && \
cd /home/airflow/gcs/data/to_bert && \
echo "step 1" && \
gsutil -m cat "${sentence_file_prefix}" > "${aggregated_bert_output_file_name}" && \
echo "step 2" && \
gsutil -m cp "${aggregated_bert_output_file_name}" "${input_bucket}" && \
