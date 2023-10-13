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
    labels[0]="contributes_to"
    labels[1]="false"
elif model_key == 'bl_gene_loss_gain_of_function_to_disease':
    labels[0]="contributes_to_via_loss_of_function"
    labels[1]="contributes_to_via_gain_of_function"
    labels[2]="false"
elif model_key == 'bl_gene_to_expression_site':
    labels[0]="expressed_in"
    labels[1]="false"
elif model_key == 'craft_pr_uberon':
    labels[0]="label1"
    labels[1]="label2"
    labels[2]="false"
elif model_key == 'craft_pr_taxon':
    labels[0]="label1"
    labels[1]="label2"
    labels[2]="false"
elif model_key == 'craft_relations':
    labels[0]="bears_constitution_of_or_situatedness_at_or_possession_by_or_derivation_from"
    labels[1]="coexists_as"
    labels[2]="realizes_or_attribute_or_bearer_of_occurrence_in_or_possession_by"
    labels[3]="realizes_or_bears_or_is_attribute_of_derivation_or_situatedness_or_possession_from"
    labels[4]="false"
elif model_key == 'craft_influence':
    labels[0]="coexists_as"
    labels[1]="occurrence_or_attribute_or_effect_brought_about_or_carried_out_by"
    labels[2]="occurrence_or_attribute_or_bearer_of_influence_on"
    labels[3]="has_effect_of_causal_activity"
    labels[4]="has_attribute_of_being_agentive_or_causal_or_its_realization_with"
    labels[5]="has_attribute_of_being_influenced_or_its_realization_with"
    labels[6]="false"

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