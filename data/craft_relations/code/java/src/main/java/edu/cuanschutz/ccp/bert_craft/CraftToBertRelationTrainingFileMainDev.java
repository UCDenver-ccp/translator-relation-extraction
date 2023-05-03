package edu.cuanschutz.ccp.bert_craft;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.tools.ant.util.StringUtils;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

import edu.cuanschutz.ccp.bert_craft.used_for_2022_progress_report.CraftToBertRelationTrainingFile;
import edu.ucdenver.ccp.common.collections.CollectionsUtil;
import edu.ucdenver.ccp.common.collections.CollectionsUtil.SortOrder;
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
		File craftAnnotationDirectory = new File(
				"/Users/bill/projects/ncats-translator/relations/craft-relations/march-2023-development/CRAFT_concepts+assertions/Annotations");
		File craftOntologyFile = new File(
				"/Users/bill/projects/ncats-translator/relations/craft-relations/march-2023-development/CRAFT_concepts+assertions/Ontologies/CRAFT_aggregate_OWL_ontology.owl");

		File positivesOutputFile = new File(
				"/Users/bill/projects/ncats-translator/relations/craft-relations/march-2023-development/bert-training-files/craft-assertions.pos.bert");
		File negativesOutputFile = new File(
				"/Users/bill/projects/ncats-translator/relations/craft-relations/march-2023-development/bert-training-files/craft-assertions.neg.bert");

		try {

			CraftToBertRelationTrainingFileDev.describeCraftAssertions(craftBaseDirectory, craftAnnotationDirectory,
					craftOntologyFile);
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

	private static String removeNamespace(String iri) {
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
