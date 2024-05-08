package edu.cuanschutz.ccp.bert_craft.may2024;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.tools.ant.util.StringUtils;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

import edu.ucdenver.ccp.datasource.fileparsers.obo.OntologyUtil;

public class CraftToBertRelationTrainingFileMainDev {

	private static final String OWL_UNNAMED_NS = "http://www.owl-ontologies.com/unnamed.owl#";
	private static final String OBO_NS = "http://purl.obolibrary.org/obo/";
	private static final String CCP_EXT_NS = "http://ccp.cuanschutz.edu/obo/ext/";

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		File craftBaseDirectory = new File("/Users/bill/projects/craft-shared-task/craft.git");

		// april 2024 dev
		File craftAnnotationDirectory = new File(
				"/Users/bill/projects/ncats-translator/relations/craft-relations/april-2024-development/20240417/Annotations");
		File craftOntologyFile = new File(
				"/Users/bill/projects/ncats-translator/relations/craft-relations/april-2024-development/20240417/CRAFT_aggregate_OWL_ontology.owl");

		File positivesOutputFile = new File(
				"/Users/bill/projects/ncats-translator/relations/craft-relations/may-2024-development/bert-training-files/craft-assertions.100.pos.bert");
		File negativesOutputFile = new File(
				"/Users/bill/projects/ncats-translator/relations/craft-relations/may-2024-development/bert-training-files/craft-assertions.100.neg.bert");

		try {

			CraftToBertRelationTrainingFileDev.describeCraftAssertions(craftBaseDirectory, craftAnnotationDirectory,
					craftOntologyFile);

			// @formatter=off
			// predicates with at least 100 examples
			Set<String> relationsOfInterest = new HashSet<String>(Arrays.asList(
					"realization_or_attribute_or_bearer_of_situatedness_or_inherence_in",
					"is_or_bears_constitution_of_or_situation_at_or_coming_from_or_possession_by",
					"is_object_of_assertion_with_subject_instance_different_from_that_of",
					"has_attribute_of_being_agentive_or_causal_or_its_realization_with",
					"attribute_or_bearer_of_situatedness_or_possession_of_or_of_variant_of",
					"has_attribute_of_variant_absence_or_lack_of_functionality", "coexists_as",
					"bears_situatedness_or_possession_of", 
					"occurrence_or_attribute_of_processing_or_bringing_about_of",
					"is_or_bears_derivational_relation_to_or_bears_information_of",
					"occurrence_or_attribute_or_effect_brought_about_or_carried_out_by",
					"has_attribute_of_variation_and_situatedness",
					"occurrence_or_attribute_or_bearer_of_involvement_of_or_with",
					"occurrence_or_attribute_or_bearer_of_involvement_of",
					"occurrence_or_attribute_or_bearer_of_change_of",
					"occurrence_or_attribute_or_bearer_of_bringing_about_of",
					"occurrence_or_attribute_or_bearer_of_coming_from",
					"realization_or_attribute_or_bearer_of_occurrence_or_involvement_in_or_with",
					"occurrence_or_attribute_or_bearer_of_realization_by",
					"occurrence_or_attribute_or_bearer_of_derivation_from", 
					"region_of", 
					"bearer_of_associativity_with",
					"has_variant_or_attribute_of_variation", 
					"has_attribute_of_activity_or_its_realization_with",
					"coexists_as_subtype_in_nonexhaustive_partition",
					"has_attribute_of_being_processed_or_brought_about_or_its_realization_with",
					"has_occurrence_or_attribute_of_change",
					"realizes_or_attribute_or_bearer_of_location_or_position_of",
					"is_associated_with_or_situated_at_or_involves", 
					"attribute_or_bearer_of_holding_within_of",
					"is_subject_of_assertion_with_object_instance_different_from_that_of",
					"has_attribute_of_being_average_or_common_or_normal_or_standard_or_typical",
					"has_attribute_of_decreased_or_lower_dimension_or_its_realization_with",
					"is_or_bears_constitution_of_or_situation_at_or_coming_from_or_possession_by_or_encoded_information_of",
					"has_attribute_of_being_brought_about_or_its_realization_with",
					"occurrence_or_attribute_or_bearer_of_influence_on",
					"occurrence_or_attribute_or_bearer_of_arrangement_of_or_in",
					"realization_or_attribute_or_bearer_of_situatedness_at",
					"has_occurrence_or_attribute_of_being_or_becoming_something_with_something_arranged_or_situated_in_or_through_or_among_it",
					"has_attribute_of_development_or_its_realization_with",
					"occurrence_or_attribute_or_effect_of_loss_of", 
					"absence_of",
					"realization_or_attribute_or_bearer_of_temporal_occurrence_during",
					"has_occurrence_or_attribute_of_being_or_becoming_associated_with_something",
					"is_variant_sequence_bearing_derivational_relation_to_or_information_of", 
					"deficiency_of",
					"has_occurrence_or_attribute_of_being_or_becoming_associatively_arranged_or_situated_to_or_with_something",
					"attribute_or_bearer_of_variant_absence_or_lack_of_functionality_of",
					"has_realizational_function_for_or_application_to_or_involvement_in",
					"occurrence_or_attribute_or_bearer_of_activity_of_or_as",
					"occurrence_or_attribute_or_bearer_of_aggregation_of",
					"occurrence_or_attribute_or_bearer_of_activation_of",
					"realization_or_attribute_or_bearer_of_temporal_occurrence_after",
					"occurrence_or_attribute_of_release_of", 
					"has_attribute_of_progression_or_its_realization_with",
					"occurrence_or_attribute_of_specializing_development_of",
					"has_attribute_of_greater_dimension_or_its_realization_with",
					"occurrence_or_attribute_or_bearer_of_development_of",
					"has_occurrence_or_attribute_of_being_or_becoming_separate_or_separated"));
			// @formatter=on

			Map<String, String> relationToLabelMap = null;

			CraftToBertRelationTrainingFileDev.createBertTrainingFile(craftBaseDirectory, craftAnnotationDirectory,
					relationsOfInterest, positivesOutputFile, negativesOutputFile, relationToLabelMap);

		} catch (IOException | OWLOntologyCreationException e) {
			e.printStackTrace();
		}

	}

	/**
	 * The relations are used in the CRAFT annotation files without namespaces, so
	 * this method returns a list of the relations minus the namespaces
	 * 
	 * @param relationsOfInterest
	 * @return
	 */
	private static Set<String> removeNamespaces(Set<String> relationIris) {
		Set<String> relations = new HashSet<String>();

		for (String iri : relationIris) {
			relations.add(removeNamespace(iri));
		}

		return relations;
	}

	public static String removeNamespace(String iri) {
		if (iri.startsWith("http:http:")) {
			iri = iri.replace("http:http:", "http:");
		}
		if (iri.startsWith("http://whttp:")) {
			iri = iri.replace("http://whttp:", "http:");
		}

		if (iri.startsWith(CCP_EXT_NS)) {
			return StringUtils.removePrefix(iri, CCP_EXT_NS);
		} else if (iri.startsWith(OBO_NS)) {
			return StringUtils.removePrefix(iri, OBO_NS);
		} else if (iri.startsWith(OWL_UNNAMED_NS)) {
			return StringUtils.removePrefix(iri, OWL_UNNAMED_NS);
		} else {
			throw new IllegalArgumentException("Unexpected namespace on object property: " + iri);
		}
	}

	/**
	 * TODO - this needs to be recursive
	 * 
	 * @param relationsOfInterest
	 * @param craftOntologyFile
	 * @return
	 * @throws OWLOntologyCreationException
	 * @throws IOException
	 */
	private static Set<String> addSubProperties(Set<String> relationsOfInterest, File craftOntologyFile,
			Map<String, String> propToLabelMap) throws OWLOntologyCreationException, IOException {

		System.out.println("Adding subproperties for relations of interest");
		OntologyUtil ontUtil = new OntologyUtil(craftOntologyFile);
		OWLOntology ont = ontUtil.getOnt();

		Set<String> relationsWithSubProperties = new HashSet<String>();
		for (String relation : relationsOfInterest) {
			System.out.println("Relation: " + relation);
			OWLObjectProperty prop = getObjectProperty(relation, ont);
			relationsWithSubProperties.add(relation);
			Set<String> subProperties = getSubProperties(prop, ont);
			relationsWithSubProperties.addAll(subProperties);

			String relationNoNs = removeNamespace(relation);
			propToLabelMap.put(relationNoNs, relationNoNs);
			for (String subProp : subProperties) {
				String subPropNoNs = removeNamespace(subProp);
				if (!propToLabelMap.containsKey(subPropNoNs)) {
					propToLabelMap.put(subPropNoNs, relationNoNs);
				} else {
					throw new IllegalStateException(
							"possible multiple inheritance in property hierarchy: \nalready stored - subprop: "
									+ subPropNoNs + " relation: " + propToLabelMap.get(subPropNoNs) + "\nnew: "
									+ relationNoNs);
				}
			}
		}

		ontUtil.close();

		return relationsWithSubProperties;

	}

	private static OWLObjectProperty getObjectProperty(String relation, OWLOntology ont) {
		Set<OWLObjectProperty> objectProperties = ont.getObjectPropertiesInSignature();
		for (OWLObjectProperty prop : objectProperties) {
			String iri = prop.getIRI().toString();
			if (relation.equals(iri)) {
				return prop;
			}
		}
		throw new IllegalArgumentException("Relation not found in ontology: " + relation);
	}

	private static Set<String> getSubProperties(OWLObjectProperty prop, OWLOntology ont) {
		System.out.println("===== Getting SubProperties for: " + prop.getIRI().toString());
		Set<String> props = new HashSet<String>();
		Set<OWLObjectPropertyExpression> subProperties = prop.getSubProperties(ont);
		System.out.println("===== subprop count = " + subProperties.size());
		for (OWLObjectPropertyExpression subProp : subProperties) {
			OWLObjectProperty property = subProp.asOWLObjectProperty();
			props.add(property.getIRI().toString());
			System.out.println("Has subproperty: " + property.getIRI().toString());
			props.addAll(getSubProperties(property, ont));
		}

		return props;
	}

}
