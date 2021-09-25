import csv
import sys
import numpy as np

model_key = sys.argv[1]

labels = {}
if model_key == 'bl_chemical_to_disease_or_phenotypic_feature':
    labels[0]="treats"
    labels[1]="false"
elif model_key == 'bl_chemical_to_gene':
    labels[0]="positively_regulates"
    labels[1]="negatively_regulates"
    labels[2]="false"
elif model_key == 'bl_disease_to_phenotypic_feature':
    labels[0]="has_symptom"
    labels[1]="false"
elif model_key == 'bl_gene_regulatory_relationship':
    labels[0]="positively_regulates"
    labels[1]="negatively_regulates"
    labels[2]="false"
elif model_key == 'bl_gene_to_disease':
    labels[0]="causes"
    labels[1]="false"
elif model_key == 'bl_gene_loss_gain_of_function_to_disease':
    labels[0]="contributes_to_via_loss_of_function"
    labels[1]="contributes_to_via_gain_of_function"
    labels[2]="false"
elif model_key == 'bl_gene_to_expression_site':
    labels[0]="expressed_in"
    labels[1]="false"
# elif model_key == 'bl_gene_to_go_term':
#   labels[0]="????"
#   labels[1]="????"
#   labels[2]="false"

with open("/home/dev/output/test_results.tsv", "r") as probs_file, open("/home/dev/data/test.ids") as ids_file:
    probs_tsv = csv.reader(probs_file, delimiter="\t")
    ids_tsv = csv.reader(ids_file, delimiter="\t")

    # print the header from the ids file
    print('\t'.join(next(ids_tsv)))

    for probs_row, ids_row in zip(probs_tsv, ids_tsv):
        x = np.array(probs_row).astype(np.float)
        max_prob = max(x)
        #print("MAX PROB: " + str(max_prob))
        #print("X: " + str(x))
        indexes = np.where(x==max_prob)
        if not indexes:
            sys.exit('max value did not match in the array')
        elif len(indexes) > 1:
            sys.exit('max prob tie observed')
        # there should only be a single index at this point
        #print("====== indexes type: " + str(type(indexes)) + " -- " + str(indexes))
        index = indexes[0][0]
        label = labels.get(index)
        print('\t'.join(ids_row) + label)