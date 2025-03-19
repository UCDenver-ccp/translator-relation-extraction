#!/bin/bash

resources_bucket=$1
collection=$2
association_key_lc=$3
sentence_version=$4

# bert_input_file_name_with_metadata="bert-input-${association_key_lc}.metadata.${sentence_version}.${collection}.tsv.gz"
# to_be_classified_sentence_bucket="${resources_bucket}/output/sentences_tsv/${sentence_version}/${association_key_lc}/${collection}"
# sentence_file_prefix="${to_be_classified_sentence_bucket}/${association_key_lc}"
# bert_input_file_name="bert-input-${association_key_lc}.${sentence_version}.${collection}.tsv"
# bert_input_sentence_bucket="${to_be_classified_sentence_bucket}/bert-input"


# gs://translator-text-workflow-dev_work/output/sentences_simplified/0.4.0/bl_process_to_cell_component/PMC_SUBSET_41-00000-of-00091.PMC_SUBSET_41.tsv.gz
sentence_simplification_output_bucket="${resources_bucket}/output/sentences_simplified/${sentence_version}/${association_key_lc}/${collection}"
local_sent_simp_file="sent_simp.${association_key_lc}.${collection}.tsv.gz"
local_filtered_sent_ids_file="sent_ids_to_exclude_basedon_simp.${association_key_lc}.${collection}.tsv.gz"


echo "bert_input_file_name_with_metadata: ${bert_input_file_name_with_metadata}"
echo "to_be_classified_sentence_bucket: ${to_be_classified_sentence_bucket}"
echo "sentence_file_prefix: ${sentence_file_prefix}"
echo "bert_input_file_name: ${bert_input_file_name}"
echo "bert_input_sentence_bucket: ${bert_input_sentence_bucket}"

mkdir -p /home/airflow/gcs/data/to_bert && \
cd /home/airflow/gcs/data/to_bert && \
echo "step 1 - copy results of sentence simplification locally" && \
gsutil -m cat "${sentence_simplification_output_bucket}*.gz" > "${local_sent_simp_file}" && \
echo "step 2 - cut to get first couple of columns, then grep for None, then cut for id column" && \
gunzip -c "${local_sent_simp_file}" | cut -f 1,3 | grep 'None' | cut -f 1 > "${local_filtered_sent_ids_file}" && \
echo "step 3 - copy resultant id file to bucket" && \
gsutil -m cp "${local_filtered_sent_ids_file}" "${sentence_simplification_output_bucket}/"