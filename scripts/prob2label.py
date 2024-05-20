import csv
import sys
import numpy as np

model_key = sys.argv[1]

labels = {}
if model_key == 'bl_chemical_to_disease_or_phenotypic_feature':
    # labels[0]="treats"
    # labels[1]="causes_or_contributes_to"
    # labels[2]="false"
    # labels[0]="ameliorates"
    # labels[1]="associated_with"
    labels[0]="associated_with_resistance_to"
    labels[1]="decreases_risk_for"
    labels[2]="exacerbates"
    labels[3]="exposure_is_origin_of"
    labels[4]="has_excessive_amount_in"
    labels[5]="increases_risk_for"
    labels[6]="is_biomarker_for"
    labels[7]="is_contraindicated_for"
    labels[8]="is_deficient_in"
    labels[9]="prevents"
    labels[10]="results_in_side_effect"
    labels[11]="treats"
    labels[12]="other"
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
    labels[0]="contributes_to"
    labels[1]="false"
elif model_key == 'bl_gene_loss_gain_of_function_to_disease':
    labels[0]="contributes_to_via_loss_of_function"
    labels[1]="contributes_to_via_gain_of_function"
    labels[2]="false"
elif model_key == 'bl_gene_to_expression_site':
    labels[0]="expressed_in"
    labels[1]="false"
elif model_key == 'craft_relations':
    labels[0]="bears_constitution_of_or_situatedness_at_or_possession_by_or_derivation_from"
    labels[1]="coexists_as"
    labels[2]="realizes_or_attribute_or_bearer_of_occurrence_in_or_possession_by"
    labels[3]="realizes_or_bears_or_is_attribute_of_derivation_or_situatedness_or_possession_from"
    labels[4]="false"
elif model_key == 'bl_chemical_to_cell':
    labels[0]="metabolism"
    labels[1]="modulation"
    labels[2]="other"
    labels[3]="response"
    labels[4]="secretion"
    labels[5]="synthesis"
    labels[6]="transport"
elif model_key == 'bl_cell_to_disease':
    labels[0]="dysfunction"
    labels[1]="hyperactivity"
    labels[2]="other"
    labels[3]="proliferation"

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