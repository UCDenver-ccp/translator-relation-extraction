package edu.cuanschutz.ccp.bert_craft;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.SingleGraph;
import org.graphstream.ui.layout.HierarchicalLayout;
import org.graphstream.ui.view.Viewer;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

public class CraftPropertyVisualizer {

	private static Map<String, Integer> loadPropertyToCountMap() {
		Map<String, Integer> map = new HashMap<>();

		map.put("realization_or_attribute_or_bearer_of_situatedness_or_inherence_in", 2906);
		map.put("is_or_bears_constitution_of_or_situation_at_or_coming_from_or_possession_by", 2319);
		map.put("is_object_of_assertion_with_subject_instance_different_from_that_of", 1305);
		map.put("realizes_or_bears_or_is_attribute_of_or_of_something_associated_with", 947);
		map.put("has_attribute_of_being_agentive_or_causal_or_its_realization_with", 945);
		map.put("attribute_or_bearer_of_situatedness_or_possession_of_or_of_variant_of", 935);
		map.put("bears_situatedness_or_possession_of", 790);
		map.put("has_attribute_of_variant_absence_or_lack_of_functionality", 783);
		map.put("coexists_as", 731);
		map.put("occurrence_or_attribute_of_processing_or_bringing_about_of", 711);
		map.put("is_or_bears_derivational_relation_to_or_bears_information_of", 629);
		map.put("has_attribute_or_bearer_or_its_realization_with", 567);
		map.put("occurrence_or_attribute_or_effect_brought_about_or_carried_out_by", 499);
		map.put("occurrence_or_attribute_or_bearer_of_change_of", 463);
		map.put("has_attribute_of_variation_and_situatedness", 431);
		map.put("occurrence_or_attribute_or_bearer_of_involvement_of_or_with", 417);
		map.put("occurrence_or_attribute_or_bearer_of_involvement_of", 413);
		map.put("has_attribute_of_occurrence_or_its_realization_with", 379);
		map.put("occurrence_or_attribute_or_bearer_of_bringing_about_of", 375);
		map.put("has_attribute_of_activity_or_its_realization_with", 352);
		map.put("realization_or_attribute_or_bearer_of_occurrence_or_involvement_in_or_with", 350);
		map.put("occurrence_or_attribute_or_bearer_of_coming_from", 336);
		map.put("occurrence_or_attribute_or_bearer_of_realization_by", 305);
		map.put("occurrence_or_attribute_or_bearer_of_derivation_from", 289);
		map.put("has_attribute_of_undergoing_change_or_influence_or_its_realization_with", 259);
		map.put("realization_or_attribute_or_bearer_of_situatedness_at", 251);
		map.put("region_of", 245);
		map.put("has_attribute_of_being_negatively_or_reductively_influenced_or_its_realization_with", 245);
		map.put("has_variant_or_attribute_of_variation", 244);
		map.put("bearer_of_associativity_with", 242);
		map.put("coexists_as_subtype_in_nonexhaustive_partition", 239);
		map.put("has_attribute_of_being_processed_or_brought_about_or_its_realization_with", 229);
		map.put("occurrence_or_attribute_or_bearer_of_associative_arrangement_or_situation_of_or_with", 227);
		map.put("realizes_or_attribute_or_bearer_of_location_or_position_of", 220);
		map.put("has_occurrence_or_attribute_of_change", 214);
		map.put("occurrence_or_attribute_or_bearer_of_influence_on", 208);
		map.put("is_associated_with_or_situated_at_or_involves", 201);
		map.put("attribute_or_bearer_of_holding_within_of", 200);
		map.put("has_attribute_of_being_influenced_or_its_realization_with", 199);
		map.put("is_subject_of_assertion_with_object_instance_different_from_that_of", 198);
		map.put("has_attribute_of_being_brought_about_or_its_realization_with", 185);
		map.put("realization_or_attribute_or_bearer_of_occurrence_of_or_associated_with", 184);
		map.put("has_attribute_of_being_average_or_common_or_normal_or_standard_or_typical", 182);
		map.put("has_attribute_of_decreased_or_lower_dimension_or_its_realization_with", 179);
		map.put("occurrence_or_attribute_or_bearer_of_change_or_influence_or_realization_of", 178);
		map.put("is_or_bears_constitution_of_or_situation_at_or_coming_from_or_possession_by_or_encoded_information_of",
				177);
		map.put("occurrence_or_attribute_or_bearer_of_negative_or_reductive_influence_of", 176);
		map.put("has_attribute_of_increased_dimension_or_its_realization_with", 174);
		map.put("occurrence_or_attribute_or_bearer_of_arrangement_of_or_in", 167);
		map.put("occurrence_or_attribute_or_bearer_of_negative_or_reductive_change_or_influence_of", 167);
		map.put("occurrence_or_attribute_or_effect_of_loss_of", 166);
		map.put("has_attribute_of_development_or_its_realization_with", 163);
		map.put("has_occurrence_or_attribute_of_being_or_becoming_something_with_something_arranged_or_situated_in_or_through_or_among_it",
				159);
		map.put("absence_of", 158);
		map.put("has_attribute_of_positive_or_progressive_or_augmentive_change_or_influence_or_realization_or_its_realization_with",
				155);
		map.put("realization_or_attribute_or_bearer_of_associativity_of_or_with", 153);
		map.put("has_attribute_of_negative_or_reductive_change_or_influence_or_its_realization_with", 152);
		map.put("is_variant_sequence_bearing_derivational_relation_to_or_information_of", 151);
		map.put("realization_or_attribute_or_bearer_of_temporal_occurrence_during", 151);
		map.put("deficiency_of", 149);
		map.put("has_occurrence_or_attribute_of_being_or_becoming_associated_with_something", 146);
		map.put("has_occurrence_or_attribute_of_being_or_becoming_associatively_arranged_or_situated_to_or_with_something",
				145);
		map.put("attribute_or_bearer_of_variant_absence_or_lack_of_functionality_of", 144);
		map.put("effects_or_inherence_or_effect_of_association_with", 139);
		map.put("occurrence_or_attribute_or_bearer_of_aggregation_of", 136);
		map.put("has_realizational_function_for_or_application_to_or_involvement_in", 135);
		map.put("occurrence_or_attribute_or_bearer_of_activity_of_or_as", 133);
		map.put("occurrence_or_attribute_or_bearer_of_activation_of", 130);
		map.put("has_attribute_of_being_derived_or_its_realization_with", 123);
		map.put("occurrence_or_attribute_or_bearer_of_decrease_of", 122);
		map.put("has_attribute_or_realization_of_associative_arrangement_or_situation", 121);
		map.put("realization_or_attribute_or_bearer_of_temporal_occurrence_at", 115);
		map.put("is_or_bears_way_of_proceeding_or_doing_of_or_associated_with", 115);
		map.put("realization_or_attribute_or_bearer_of_temporal_occurrence_after", 114);
		map.put("occurrence_or_attribute_or_bearer_of_development_of", 114);
		map.put("occurrence_or_attribute_of_release_of", 112);
		map.put("has_attribute_of_progression_or_its_realization_with", 107);
		map.put("occurrence_or_attribute_of_specializing_development_of", 105);
		map.put("realization_or_attribute_or_bearer_of_positive_or_progressive_or_augmentive_occurrence_associated_with",
				105);
		map.put("has_attribute_of_greater_dimension_or_its_realization_with", 103);
		map.put("area_or_point_or_position_or_site_of", 102);
		map.put("has_occurrence_or_attribute_of_being_or_becoming_separate_or_separated", 101);
		map.put("occurrence_or_attribute_of_blocking_of", 92);
		map.put("occurrence_or_attribute_or_bearer_of_attachment_of_or_to", 92);
		map.put("has_attribute_of_positioning_or_situation_or_its_effecting_by", 92);
		map.put("course_or_path_of_or_associated_with", 92);
		map.put("occurrence_or_attribute_or_bearer_of_attachment_to", 90);
		map.put("realization_or_attribute_of_greater_dimension_of", 89);
		map.put("lack_of", 88);
		map.put("has_attribute_of_invariant_continuation_or_its_realization_with", 88);
		map.put("has_attribute_of_being_influenced_or_brought_about_or_its_realization_with", 88);
		map.put("has_attribute_of_situated_existence", 87);
		map.put("occurrence_or_attribute_of_interaction_with", 86);
		map.put("has_attribute_of_arrangement_or_situation_or_its_realization_with", 86);
		map.put("occurrence_or_attribute_of_existence_of", 84);
		map.put("realizes_or_attribute_or_bearer_of_alteration_of", 84);
		map.put("occurrence_or_attribute_of_interaction_of_or_with", 84);
		map.put("occurrence_or_attribute_of_activity_of_or_associated_or_involved_with", 83);
		map.put("in_metarelationship_with", 82);
		map.put("occurrence_or_attribute_or_bearer_of_connection_or_contact_or_interfacing_involving", 82);
		map.put("has_shared_instance_linked_to_different_subject_instances", 81);
		map.put("attribute_or_bearer_of_invariance_or_similarity_relative_to", 80);
		map.put("realization_or_attribute_of_situatedness_into", 79);
		map.put("has_attribute_of_existence_or_form_or_its_realization_with", 76);
		map.put("is_or_has_attribute_or_occurrence_or_effect_of_being_agentively_or_causally_active_or_responsible_for_something",
				76);
		map.put("occurrence_or_attribute_or_bearer_of_formation_of", 74);
		map.put("occurrence_or_attribute_or_effect_of_removal_or_causing_loss_of_existence_of", 73);
		map.put("is_or_bears_way_of_proceeding_or_doing_of_something_involving", 73);
		map.put("has_different_subject_and_object_instances_from_those_of", 72);
		map.put("occurrence_or_attribute_or_bearer_of_progression_of", 69);
		map.put("has_attribute_of_formation_or_its_realization_with", 69);
		map.put("presence_of", 68);
		map.put("occurrence_or_attribute_or_activity_of_or_associated_or_involved_with", 67);
		map.put("occurrence_or_attribute_or_bearer_of_connection_to", 64);
		map.put("occurrence_or_attribute_or_bearer_of_regulation_of", 64);
		map.put("is_involved_in", 63);
		map.put("occurrence_or_attribute_or_bearer_of_activity_as", 63);
		map.put("realizes_or_attribute_or_bearer_of_cooccurrence_with", 63);
		map.put("occurrence_or_attribute_of_disruption_or_disturbance_of", 63);
		map.put("occurrence_or_attribute_or_bearer_of_identification_or_indication_or_characterization_of", 63);
		map.put("has_attribute_of_loss_or_its_realization_with", 63);
		map.put("has_attribute_of_extent_or_its_realization_with", 62);
		map.put("realization_or_attribute_or_bearer_of_associativity_with", 62);
		map.put("attribute_or_bearer_of_being_composed_of", 61);
		map.put("involves_or_is_attribute_of_encoding_of", 61);
		map.put("realizes_or_bears_or_is_attribute_of_reaction_or_response_of_or_to_or_with", 60);
		map.put("has_attribute_of_encoding_information_or_its_realization_with", 58);
		map.put("is_or_effects_or_derives_from_integrative_positioning_of", 58);
		map.put("has_attribute_of_arrangement_or_extent_or_situation_or_its_realization_with", 57);
		map.put("occurrence_or_attribute_or_bearer_of_combination_of", 56);
		map.put("has_attribute_of_change_or_influence_or_realization_or_its_realization_with", 56);
		map.put("has_attribute_of_developmental_specialization_or_its_realization_with", 55);
		map.put("is_derived_from_and_probe_for", 54);
		map.put("occurrence_or_attribute_of_possession_of", 53);
		map.put("has_attribute_of_occurring_at_an_end_of_something", 52);
		map.put("has_attribute_of_being_activated_or_its_realization_with", 52);
		map.put("has_attribute_of_being_labeled_or_marked_or_its_realization_with", 52);
		map.put("has_attribute_of_being_gotten_or_gained_or_its_realization_with", 51);
		map.put("realizes_or_attribute_or_bearer_of_situatedness_between", 50);
		map.put("realization_or_attribute_or_bearer_of_situatedness_near", 50);

		return map;
	}

	public static void main(String[] args) {
		System.setProperty("org.graphstream.ui", "swing");

		File craftOntologyFile = new File(
				"/Users/bill/projects/ncats-translator/relations/craft-relations/april-2024-development/20240417/CRAFT_aggregate_OWL_ontology.owl");

		Map<String, Integer> propToCountMap = loadPropertyToCountMap();

		Graph graph = new SingleGraph("Tutorial 1");

		Set<String> nodes = new HashSet<String>();
		Set<String> edges = new HashSet<String>();
		int edgeId = 0;
		try {
			Map<String, Set<String>> childToParentPropertyMap = CraftToBertRelationTrainingFileDev
					.getChildToParentProperty(craftOntologyFile);

			// add a node for each property that has a count
			for (String prop : propToCountMap.keySet()) {
				addNode(graph, nodes, prop, propToCountMap.get(prop));
			}

			// for each node, add any missing ancestor nodes and all subprop edges
			for (String prop : propToCountMap.keySet()) {
				edgeId = addAncestors(graph, nodes, edges, edgeId, childToParentPropertyMap, prop, propToCountMap);
			}

//			for (Entry<String, Set<String>> entry : childToParentPropertyMap.entrySet()) {
//				String prop = entry.getKey().trim();
//				System.out.println("PROP: " + prop);
//				if (propToCountMap.containsKey(prop)) {
//					addNode(graph, nodes, prop);
//					for (String parentProp : entry.getValue()) {
//						addNode(graph, nodes, parentProp);
//						edgeId = addEdge(graph, edges, edgeId, prop, parentProp);
//					}
//				}
//			}

		} catch (OWLOntologyCreationException | IOException e) {
			e.printStackTrace();
		}

		System.out.println("Node count: " + nodes.size());
		System.out.println("Edge count: " + edges.size());

//		graph.display();
		
		Viewer viewer = graph.display();
		HierarchicalLayout hl = new HierarchicalLayout();
		viewer.enableAutoLayout(hl);
	}

	private static int addAncestors(Graph graph, Set<String> nodes, Set<String> edges, int edgeId,
			Map<String, Set<String>> childToParentPropertyMap, String prop, Map<String, Integer> propToCountMap) {
		Set<String> parentProps = childToParentPropertyMap.get(prop);
		if (parentProps != null) {
			for (String parentProp : parentProps) {
				addNode(graph, nodes, parentProp, propToCountMap.get(parentProp));
				edgeId = addEdge(graph, edges, edgeId, prop, parentProp);
				edgeId = addAncestors(graph, nodes, edges, edgeId, childToParentPropertyMap, parentProp, propToCountMap);
			}
		}
		return edgeId;
	}

	private static int addEdge(Graph graph, Set<String> edges, int edgeId, String prop, String parentProp) {
		String edgeKey = String.format("%s|%s", prop, parentProp);
		if (!edges.contains(edgeKey)) {
			graph.addEdge(Integer.toString(edgeId++), prop, parentProp);
			edges.add(edgeKey);
		}
		return edgeId;
	}

	private static void addNode(Graph graph, Set<String> nodes, String prop, int count) {
		if (!nodes.contains(prop)) {
			graph.addNode(prop);
			Node node = graph.getNode(prop);
			node.addAttribute("ui.label", node.getId());
			int size = Math.round((float) count / 10.0f);
			node.setAttribute("ui.size", size);
			nodes.add(prop);
		}
	}

}
