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
elif model_key == 'craft_100':
    labels[0]="realization_or_attribute_or_bearer_of_situatedness_or_inherence_in"
    labels[1]="is_or_bears_constitution_of_or_situation_at_or_coming_from_or_possession_by"
    labels[2]="is_object_of_assertion_with_subject_instance_different_from_that_of"
    labels[3]="has_attribute_of_being_agentive_or_causal_or_its_realization_with"
    labels[4]="attribute_or_bearer_of_situatedness_or_possession_of_or_of_variant_of"
    labels[5]="has_attribute_of_variant_absence_or_lack_of_functionality" 
    labels[6]="coexists_as"
    labels[7]="bears_situatedness_or_possession_of" 
    labels[8]="occurrence_or_attribute_of_processing_or_bringing_about_of"
    labels[9]="is_or_bears_derivational_relation_to_or_bears_information_of"
    labels[10]="occurrence_or_attribute_or_effect_brought_about_or_carried_out_by"
    labels[11]="has_attribute_of_variation_and_situatedness"
    labels[12]="occurrence_or_attribute_or_bearer_of_involvement_of_or_with"
    labels[13]="occurrence_or_attribute_or_bearer_of_involvement_of"
    labels[14]="occurrence_or_attribute_or_bearer_of_change_of"
    labels[15]="occurrence_or_attribute_or_bearer_of_bringing_about_of"
    labels[16]="occurrence_or_attribute_or_bearer_of_coming_from"
    labels[17]="realization_or_attribute_or_bearer_of_occurrence_or_involvement_in_or_with"
    labels[18]="occurrence_or_attribute_or_bearer_of_realization_by"
    labels[19]="occurrence_or_attribute_or_bearer_of_derivation_from" 
    labels[20]="region_of" 
    labels[21]="bearer_of_associativity_with"
    labels[22]="has_variant_or_attribute_of_variation" 
    labels[23]="has_attribute_of_activity_or_its_realization_with"
    labels[24]="coexists_as_subtype_in_nonexhaustive_partition"
    labels[25]="has_attribute_of_being_processed_or_brought_about_or_its_realization_with"
    labels[26]="has_occurrence_or_attribute_of_change"
    labels[27]="realizes_or_attribute_or_bearer_of_location_or_position_of"
    labels[28]="is_associated_with_or_situated_at_or_involves" 
    labels[29]="attribute_or_bearer_of_holding_within_of"
    labels[30]="is_subject_of_assertion_with_object_instance_different_from_that_of"
    labels[31]="has_attribute_of_being_average_or_common_or_normal_or_standard_or_typical"
    labels[32]="has_attribute_of_decreased_or_lower_dimension_or_its_realization_with"
    labels[33]="is_or_bears_constitution_of_or_situation_at_or_coming_from_or_possession_by_or_encoded_information_of"
    labels[34]="has_attribute_of_being_brought_about_or_its_realization_with"
    labels[35]="occurrence_or_attribute_or_bearer_of_influence_on"
    labels[36]="occurrence_or_attribute_or_bearer_of_arrangement_of_or_in"
    labels[37]="realization_or_attribute_or_bearer_of_situatedness_at"
    labels[38]="has_occurrence_or_attribute_of_being_or_becoming_something_with_something_arranged_or_situated_in_or_through_or_among_it"
    labels[39]="has_attribute_of_development_or_its_realization_with"
    labels[40]="occurrence_or_attribute_or_effect_of_loss_of" 
    labels[41]="absence_of"
    labels[42]="realization_or_attribute_or_bearer_of_temporal_occurrence_during"
    labels[43]="has_occurrence_or_attribute_of_being_or_becoming_associated_with_something"
    labels[44]="is_variant_sequence_bearing_derivational_relation_to_or_information_of" 
    labels[45]="deficiency_of"
    labels[46]="has_occurrence_or_attribute_of_being_or_becoming_associatively_arranged_or_situated_to_or_with_something"
    labels[47]="attribute_or_bearer_of_variant_absence_or_lack_of_functionality_of"
    labels[48]="has_realizational_function_for_or_application_to_or_involvement_in"
    labels[49]="occurrence_or_attribute_or_bearer_of_activity_of_or_as"
    labels[50]="occurrence_or_attribute_or_bearer_of_aggregation_of"
    labels[51]="occurrence_or_attribute_or_bearer_of_activation_of"
    labels[52]="realization_or_attribute_or_bearer_of_temporal_occurrence_after"
    labels[53]="occurrence_or_attribute_of_release_of" 
    labels[54]="has_attribute_of_progression_or_its_realization_with"
    labels[55]="occurrence_or_attribute_of_specializing_development_of"
    labels[56]="has_attribute_of_greater_dimension_or_its_realization_with"
    labels[57]="occurrence_or_attribute_or_bearer_of_development_of"
    labels[58]="has_occurrence_or_attribute_of_being_or_becoming_separate_or_separated"
    labels[59]="false"
elif model_key == 'craft_1_vs_all':
    labels[0]="label1"
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