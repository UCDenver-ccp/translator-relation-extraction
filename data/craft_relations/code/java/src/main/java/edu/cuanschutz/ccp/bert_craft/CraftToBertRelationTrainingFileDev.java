package edu.cuanschutz.ccp.bert_craft;

import static edu.ucdenver.ccp.common.file.CharacterEncoding.UTF_8;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.tools.ant.util.StringUtils;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

import edu.ucdenver.ccp.common.collections.CollectionsUtil;
import edu.ucdenver.ccp.common.collections.CollectionsUtil.SortOrder;
import edu.ucdenver.ccp.common.file.FileUtil;
import edu.ucdenver.ccp.common.file.FileWriterUtil;
import edu.ucdenver.ccp.datasource.fileparsers.obo.OntologyUtil;
import edu.ucdenver.ccp.file.conversion.TextDocument;
import edu.ucdenver.ccp.file.conversion.knowtator2.Knowtator2DocumentReader;
import edu.ucdenver.ccp.file.conversion.treebank.SentenceTokenOnlyTreebankDocumentReader;
import edu.ucdenver.ccp.nlp.core.annotation.Span;
import edu.ucdenver.ccp.nlp.core.annotation.TextAnnotation;
import edu.ucdenver.ccp.nlp.core.annotation.TextAnnotationFactory;
import edu.ucdenver.ccp.nlp.core.mention.ClassMention;
import edu.ucdenver.ccp.nlp.core.mention.ComplexSlotMention;
import edu.ucdenver.ccp.nlp.core.mention.impl.DefaultClassMention;
import lombok.Data;

/**
 * This code parses annotations from a pre-release of CRAFT and creates
 * training/testing data to use for training BERT models.
 *
 */
public class CraftToBertRelationTrainingFileDev {

	protected static final String OBJECT_PLACEHOLDER = "@OBJECT$";
	protected static final String SUBJECT_PLACEHOLDER = "@SUBJECT$";

	public static void describeCraftAssertions(File craftBaseDirectory, File craftAnnotationDirectory,
			File craftMergedOntologyFile) throws IOException, OWLOntologyCreationException {
		Map<CategoryPair, AssertionGroup> categoryPairToPositiveAssertionGroupMap = compileAssertionGroups(
				craftBaseDirectory, craftAnnotationDirectory);

		Map<String, Integer> relationInstanceCountMap = new HashMap<String, Integer>();
		int assertionTotal = 0;
		for (Entry<CategoryPair, AssertionGroup> entry : categoryPairToPositiveAssertionGroupMap.entrySet()) {
			System.out.println(entry.getKey().toString() + " -- "
					+ entry.getValue().getRelationToAssertionMap().keySet().toString());

			AssertionGroup ag = entry.getValue();

			Map<String, Set<Assertion>> relationToAssertionMap = ag.getRelationToAssertionMap();
			for (Entry<String, Set<Assertion>> entry2 : relationToAssertionMap.entrySet()) {
				String relation = entry2.getKey();
				int assertionCount = entry2.getValue().size();
				assertionTotal += assertionCount;
				CollectionsUtil.addToCountMap(relation, assertionCount, relationInstanceCountMap);
			}
		}

		System.out.println("=======================================================================================");
		Map<String, Integer> sortedMap = CollectionsUtil.sortMapByValues(relationInstanceCountMap,
				SortOrder.DESCENDING);
		for (Entry<String, Integer> entry : sortedMap.entrySet()) {
			System.out.println(entry.getKey() + "\t" + entry.getValue());
		}
		System.out.println("Total assertions: " + assertionTotal);

		// use the property hierarchy to group relations such that they have a minimum
		// of N examples
		int minimumRelationCount = 50;
		System.out.println("=======================================================================================");
		// walk through the relationInstanceCountMap -- start with leaf properties and
		// work up the hierarchy; if the count is < minimumRelationCount then find the
		// super-property and add that count to the super-property if it exists in the
		// map, or create an entry if it doesn't

		Map<String, Set<String>> childToParentPropertyMap = getChildToParentProperty(craftMergedOntologyFile);

		while (keepProcessing(sortedMap, minimumRelationCount)) {
			for (String child : new HashSet<String>(sortedMap.keySet())) {
				int count = sortedMap.get(child);
				if (count > 0 && count < minimumRelationCount) {
					Set<String> parents = childToParentPropertyMap.get(child);
					if (parents == null) {
						sortedMap.put(child, -1 * count);
					} else {
						// for each parent, increment its count
						for (String parent : parents) {
							if (sortedMap.containsKey(parent)) {
								Integer parentCount = sortedMap.get(parent);
								sortedMap.put(parent, count + parentCount);
							} else {
								sortedMap.put(parent, count);
							}
						}
						sortedMap.remove(child);
					}
				}
			}
		}

		System.out.println("=======================================================================================");
		sortedMap = CollectionsUtil.sortMapByValues(sortedMap, SortOrder.DESCENDING);
		for (Entry<String, Integer> entry : sortedMap.entrySet()) {
			System.out.println(entry.getKey() + "\t" + entry.getValue());
		}
		System.out.println("Total assertions: " + assertionTotal);

	}

	/**
	 * @param sortedMap
	 * @return true if there are values in the input map that are >0 && <
	 *         minimumRelationCount
	 */
	private static boolean keepProcessing(Map<String, Integer> map, int minimumRelationCount) {
		boolean keepProcessing = false;
		for (Integer count : map.values()) {
			if (count > 0 && count < minimumRelationCount) {
				return true;
			}
		}

		return keepProcessing;
	}

	private static Map<String, Set<String>> getChildToParentProperty(File craftMergedOntologyFile)
			throws OWLOntologyCreationException, IOException {

		Map<String, Set<String>> map = new HashMap<String, Set<String>>();

		OntologyUtil ontUtil = new OntologyUtil(craftMergedOntologyFile);
		OWLOntology ont = ontUtil.getOnt();

		Set<OWLObjectProperty> objectPropertiesInSignature = ont.getObjectPropertiesInSignature();

		for (OWLObjectProperty prop : objectPropertiesInSignature) {
			Set<String> subProperties = getImmediateSubProperties(prop, ont);
			System.out.println("PROP: " + prop.getIRI().toString() + " SUBPROPS: " + subProperties.toString());
			for (String subProp : subProperties) {
				String parentProp = dropNamespace(prop.getIRI().toString());
				String childProp = dropNamespace(subProp);
				CollectionsUtil.addToOne2ManyUniqueMap(childProp, parentProp, map);
			}
		}
//		Set<String> relationsWithSubProperties = new HashSet<String>();
//		for (String relation : relationsOfInterest) {
//			System.out.println("Relation: " + relation);
//			OWLObjectProperty prop = getObjectProperty(relation, ont);
//			relationsWithSubProperties.add(relation);
//			Set<String> subProperties = getSubProperties(prop, ont);
//			relationsWithSubProperties.addAll(subProperties);
//
//			String relationNoNs = removeNamespace(relation);
//			propToLabelMap.put(relationNoNs, relationNoNs);
//			for (String subProp : subProperties) {
//				String subPropNoNs = removeNamespace(subProp);
//				if (!propToLabelMap.containsKey(subPropNoNs)) {
//					propToLabelMap.put(subPropNoNs, relationNoNs);
//				} else {
//					throw new IllegalStateException(
//							"possible multiple inheritance in property hierarchy: \nalready stored - subprop: "
//									+ subPropNoNs + " relation: " + propToLabelMap.get(subPropNoNs) + "\nnew: "
//									+ relationNoNs);
//				}
//			}
//		}

		ontUtil.close();

		Entry<String, Set<String>> entry = map.entrySet().iterator().next();
		System.out.println("CHILD: " + entry.getKey() + " -- PARENT: " + entry.getValue());
		System.out.println("CHILD: is_biomacromolecular_probe_sequence_of -- PARENT: "
				+ map.get("is_biomacromolecular_probe_sequence_of"));

		return map;
	}

	/**
	 * Find the final / and remove everything in front of it
	 * 
	 * @param string
	 * @return
	 */
	private static String dropNamespace(String iri) {
		int slashIndex = iri.lastIndexOf("/");
		return iri.substring(slashIndex + 1);
	}

	private static Set<String> getImmediateSubProperties(OWLObjectProperty prop, OWLOntology ont) {
		System.out.println("===== Getting SubProperties for: " + prop.getIRI().toString());
		Set<String> props = new HashSet<String>();
		Set<OWLObjectPropertyExpression> subProperties = prop.getSubProperties(ont);
		System.out.println("===== subprop count = " + subProperties.size());
		for (OWLObjectPropertyExpression subProp : subProperties) {
			OWLObjectProperty property = subProp.asOWLObjectProperty();
			props.add(property.getIRI().toString());
			System.out.println("Has subproperty: " + property.getIRI().toString());
//			props.addAll(getSubProperties(property, ont));
		}

		return props;
	}

	/**
	 * @param craftBaseDirectory
	 * @param craftAnnotationDirectory - the annotations are separate b/c we are
	 *                                 using a prerelease (they are not currently
	 *                                 part of the public distribution)
	 * @param relations
	 * @param outputFile
	 * @param negativesOutputFile
	 * @param relationToLabelMap
	 * @throws IOException
	 */
	public static void createBertTrainingFile(File craftBaseDirectory, File craftAnnotationDirectory,
			Set<String> relations, File positivesOutputFile, File negativesOutputFile,
			Map<String, String> relationToLabelMap) throws IOException {

//		Set<String> relations = removeNamespaces(relationIris);

//		Map<String, Integer> countMap = new HashMap<String, Integer>();

		// outer key is a combination of the subject and object categories. Innermap is
		// a count of relation types for that subject-object pair.
//		Map<String, Map<String, Integer>> subjObjToRelationCountMap = new HashMap<String, Map<String, Integer>>();

		// this map contains all assertions for all relations in CRAFT
		Map<CategoryPair, AssertionGroup> categoryPairToPositiveAssertionGroupMap = compileAssertionGroups(
				craftBaseDirectory, craftAnnotationDirectory);

		// this map contains the positive assertions for the relations of interest only
		Map<CategoryPair, Set<Assertion>> categoryPairToPositiveAssertionsMap = new HashMap<CategoryPair, Set<Assertion>>();

		// populate the categoryPairToPositiveAssertions map
		for (AssertionGroup ag : categoryPairToPositiveAssertionGroupMap.values()) {
			Map<String, Set<Assertion>> relationToAssertionMap = ag.getRelationToAssertionMap();
			for (String relation : relations) {
				if (relationToAssertionMap.containsKey(relation)) {
					CategoryPair cp = new CategoryPair(ag.getSubjectCategory(), ag.getObjectCategory());
					Set<Assertion> newAssertions = relationToAssertionMap.get(relation);
					for (Assertion assertion : newAssertions) {
						CollectionsUtil.addToOne2ManyUniqueMap(cp, assertion, categoryPairToPositiveAssertionsMap);
					}
				}
			}
		}

		int assertionCount = countAssertions(categoryPairToPositiveAssertionsMap);
		Map<String, Integer> relationToCountMap = populateRelationToCountMap(categoryPairToPositiveAssertionsMap);

		System.out.println("Assertion Count: " + assertionCount);
		System.out.println("Category Pair Count: " + categoryPairToPositiveAssertionsMap.keySet().size());
		for (Entry<String, Integer> entry : relationToCountMap.entrySet()) {
			System.out.println(entry.getKey() + " -- " + entry.getValue());
		}

		// get negatives

		// here we are using the Assertion data structure to store negative examples for
		// a given relation, i.e. find sentences that contain two entity types but that
		// are not related by the specified relation
		Map<CategoryPair, Set<Assertion>> categoryPairToNegativeAssertionsMap = getNegatives(relations,
				craftBaseDirectory, craftAnnotationDirectory, categoryPairToPositiveAssertionsMap);

		// output masked sentences
		Set<String> alreadyWrittenSentenceIds = new HashSet<String>();

		writeAssertions(positivesOutputFile, categoryPairToPositiveAssertionsMap, alreadyWrittenSentenceIds,
				relationToLabelMap);
		writeAssertions(negativesOutputFile, categoryPairToNegativeAssertionsMap, alreadyWrittenSentenceIds,
				relationToLabelMap);
	}

	private static void writeAssertions(File outputFile, Map<CategoryPair, Set<Assertion>> categoryPairToAssertionsMap,
			Set<String> alreadyWrittenSentenceIds, Map<String, String> relationToLabelMap)
			throws IOException, FileNotFoundException {
		try (BufferedWriter writer = FileWriterUtil.initBufferedWriter(outputFile)) {
			for (Entry<CategoryPair, Set<Assertion>> entry : categoryPairToAssertionsMap.entrySet()) {
				for (Assertion assertion : entry.getValue()) {
					writeMaskedSentence(writer, assertion, alreadyWrittenSentenceIds, relationToLabelMap);
				}
			}
		}
	}

	private static void writeMaskedSentence(BufferedWriter writer, Assertion assertion,
			Set<String> alreadyWrittenSentenceIds, Map<String, String> relationToLabelMap) throws IOException {
		TextAnnotation sentenceAnnot = assertion.getSentenceAnnot();
		TextAnnotation subjectAnnot = assertion.getSubjectAnnot();
		TextAnnotation objectAnnot = assertion.getObjectAnnot();
		if (sentenceAnnot != null) {
			if (subjectAnnot.getSpans().size() == 1 && objectAnnot.getSpans().size() == 1) {
				// exclude any assertions that make use of an annotation with multiple spans
				// since we can't represent that using the placeholder schema

				String sentenceText = sentenceAnnot.getCoveredText();

				// add placeholders working from the end of the sentence to the beginning --
				// this way the character offsets at the end don't change when you adjust the
				// sentence at the beginning.

				String subjectPlaceholder = SUBJECT_PLACEHOLDER;
				String objectPlaceholder = OBJECT_PLACEHOLDER;

//			String subjectPlaceholder = "@" + getCategory(subjectAnnot.getClassMention().getMentionName()) + "_SUBJ$";
//			String objectPlaceholder = "@" + getCategory(objectAnnot.getClassMention().getMentionName()) + "_OBJ$";

				if (subjectAnnot.getAnnotationSpanStart() > objectAnnot.getAnnotationSpanStart()) {
					// do subject then object
					sentenceText = addPlaceholder(sentenceText, sentenceAnnot.getAnnotationSpanStart(), subjectAnnot,
							subjectPlaceholder);
					if (sentenceText != null) {
						sentenceText = addPlaceholder(sentenceText, sentenceAnnot.getAnnotationSpanStart(), objectAnnot,
								objectPlaceholder);
					}
				} else {
					// do object then subject
					sentenceText = addPlaceholder(sentenceText, sentenceAnnot.getAnnotationSpanStart(), objectAnnot,
							objectPlaceholder);
					if (sentenceText != null) {
						sentenceText = addPlaceholder(sentenceText, sentenceAnnot.getAnnotationSpanStart(),
								subjectAnnot, subjectPlaceholder);
					}
				}
				if (sentenceText != null) {
					String sentenceId = DigestUtils.sha256Hex(sentenceText);

					if (!alreadyWrittenSentenceIds.contains(sentenceId)) {
						alreadyWrittenSentenceIds.add(sentenceId);

						String label;
						if (assertion.getRelation() == null) {
							label = "false";
						} else {
							label = relationToLabelMap.get(assertion.getRelation());
							if (label == null) {
								throw new IllegalStateException(
										"Expectd non-null label for relation: " + assertion.getRelation());
							}
						}
						writer.write(String.format("%s\t%s\t%s\n", sentenceId, sentenceText, label));
					}
				}
			}
		}

	}

	/**
	 * Here we assume that the concept annotation is a single span, i.e. not
	 * discontinuous -- this representational scheme cannot handle discontinuous
	 * annotations
	 * 
	 * @param sentenceText
	 * @param sentenceOffset
	 * @param conceptAnnot
	 * @param placeholder
	 * @return
	 */
	protected static String addPlaceholder(String sentenceText, int sentenceOffset, TextAnnotation conceptAnnot,
			String placeholder) {
		if (conceptAnnot.getSpans().size() > 1) {
			throw new IllegalArgumentException(
					"Encountered concept annotation with multiple spans. This is not currently handled. These concept annotation should be excluded prior to this step.");
		}
		Span span = conceptAnnot.getAggregateSpan();
		try {
			String sentenceWithPlaceholder = sentenceText.substring(0, span.getSpanStart() - sentenceOffset)
					+ placeholder + sentenceText.substring(span.getSpanEnd() - sentenceOffset);
			return sentenceWithPlaceholder;
		} catch (StringIndexOutOfBoundsException e) {
			System.err.println("String Index OOB Exception");
			return null;
		}

	}

	private static Map<String, Integer> populateRelationToCountMap(
			Map<CategoryPair, Set<Assertion>> categoryPairToPositiveAssertionsMap) {
		Map<String, Integer> relationToCountMap = new HashMap<String, Integer>();
		for (Set<Assertion> assertionSet : categoryPairToPositiveAssertionsMap.values()) {
			for (Assertion assertion : assertionSet) {
				CollectionsUtil.addToCountMap(assertion.getRelation(), relationToCountMap);
			}
		}
		return relationToCountMap;
	}

	/**
	 * @param map
	 * @return the count of assertions that appear in the map
	 */
	private static int countAssertions(Map<CategoryPair, Set<Assertion>> map) {
		int count = 0;
		for (Set<Assertion> set : map.values()) {
			count += set.size();
		}
		return count;
	}

	@Data
	private static class CategoryPair {
		private final String subjectCategory;
		private final String objectCategory;
	}

	/**
	 * Get Assertions involving the specified category pairs (that were found as
	 * positives) but that are not connected via the specified relations.
	 * 
	 * @param relations
	 * @param craftBaseDirectory
	 * @param craftAnnotationDirectory
	 * @param categoryPairs
	 * @return
	 * @throws IOException
	 */
	private static Map<CategoryPair, Set<Assertion>> getNegatives(Set<String> relations, File craftBaseDirectory,
			File craftAnnotationDirectory, Map<CategoryPair, Set<Assertion>> categoryPairToPositiveAssertionsMap)
			throws IOException {
		Map<CategoryPair, Set<Assertion>> categoryPairToAssertionsMap = new HashMap<CategoryPair, Set<Assertion>>();

		for (Iterator<File> fileIterator = FileUtil.getFileIterator(craftAnnotationDirectory, false,
				".xml"); fileIterator.hasNext();) {
			File annotationFile = fileIterator.next();
			System.out.println("Processing file: " + annotationFile.getAbsolutePath());

			String pmid = annotationFile.getName().split("\\.")[0];

			File txtFile = getTextFile(craftBaseDirectory, pmid);
			Map<Span, TextAnnotation> spanToSentenceAnnotMap = getSpanToSentenceAnnotMap(craftBaseDirectory, pmid,
					txtFile);
			TextDocument annotDoc = getConceptAnnotationDocument(annotationFile, pmid, txtFile);

			Set<Assertion> positiveAssertionsForDocument = getAssertionsForDocument(pmid,
					categoryPairToPositiveAssertionsMap.values());

			// find sentence-level pairs that are negative assertions, i.e. they are pairs
			// of entities that match category pairs found in all positive assertions, but
			// do not participate in the positive assertions
			Map<CategoryPair, Set<Assertion>> categoryPairToNegativeAssertionsMap = populateCategoryPairToNegativeAssertionsMap(
					pmid, spanToSentenceAnnotMap, annotDoc.getAnnotations(), annotDoc.getText(),
					positiveAssertionsForDocument, categoryPairToPositiveAssertionsMap.keySet());

			for (Entry<CategoryPair, Set<Assertion>> entry : categoryPairToNegativeAssertionsMap.entrySet()) {
				CategoryPair cp = entry.getKey();
				Set<Assertion> assertions = entry.getValue();

				for (Assertion assertion : assertions) {
					CollectionsUtil.addToOne2ManyUniqueMap(cp, assertion, categoryPairToAssertionsMap);
				}
			}

//			for (Entry<TextAnnotation, Set<TextAnnotation>> entry : sentenceToEntityAnnotsMap.entrySet()) {
//				TextAnnotation sentenceAnnot = entry.getKey();
//				// for each sentence, include it as a negative example if it contains a pair of
//				// entity categories that are in the categoryPairs set
//				Map<String, Set<TextAnnotation>> categoryToAnnotMap = createCategoryToAnnotMap(entry.getValue());
//
//				for (CategoryPair cp : categoryPairToPositiveAssertionsMap.keySet()) {
//					if (categoryToAnnotMap.containsKey(cp.getSubjectCategory())
//							&& categoryToAnnotMap.containsKey(cp.getObjectCategory())) {
//						Set<TextAnnotation> cat1Annots = categoryToAnnotMap.get(cp.getSubjectCategory());
//						Set<TextAnnotation> cat2Annots = categoryToAnnotMap.get(cp.getObjectCategory());
//
//						for (TextAnnotation cat1Annot : cat1Annots) {
//							for (TextAnnotation cat2Annot : cat2Annots) {
//								if (!cat1Annot.equals(cat2Annot)) {
//									Assertion assertion = new Assertion(pmid, cat1Annot, cat2Annot, sentenceAnnot, null);
//									CollectionsUtil.addToOne2ManyUniqueMap(cp, assertion, categoryPairToAssertionsMap);
//								}
//							}
//						}
//					}
//				}
//			}
		}
		return categoryPairToAssertionsMap;
	}

	/**
	 * @param pmid
	 * @param values
	 * @return the assertions that have the specified document ID
	 */
	private static Set<Assertion> getAssertionsForDocument(String documentId,
			Collection<Set<Assertion>> assertionsCollection) {
		Set<Assertion> assertionsForDocument = new HashSet<Assertion>();

		for (Set<Assertion> assertionSet : assertionsCollection) {
			for (Assertion assertion : assertionSet) {
				if (assertion.getDocumentId().equals(documentId)) {
					assertionsForDocument.add(assertion);
				}
			}
		}

		return assertionsForDocument;
	}

	/**
	 * @param conceptAnnots
	 * @return a mapping from category e.g. protein (based on the id) and
	 *         annotations
	 */
	private static Map<String, Set<TextAnnotation>> createCategoryToAnnotMap(Set<TextAnnotation> conceptAnnots) {

		Map<String, Set<TextAnnotation>> map = new HashMap<String, Set<TextAnnotation>>();

		for (TextAnnotation annot : conceptAnnots) {
			String category = getCategory(annot.getClassMention().getMentionName());
			CollectionsUtil.addToOne2ManyUniqueMap(category, annot, map);
		}

		return map;
	}

	/**
	 * This function will return a mapping from sentence annotation to entity
	 * annotation, but will filter any annotations involved in the specified
	 * relations.
	 * 
	 * This function also returns "shallow" annotations, i.e. annotations that have
	 * no slot mentions, to avoid a potential StackOverflowError
	 * 
	 * @param spanToSentenceAnnotMap
	 * @param annotations
	 * @param set
	 * @param relations
	 * @return
	 */
	private static Map<CategoryPair, Set<Assertion>> populateCategoryPairToNegativeAssertionsMap(String documentId,
			Map<Span, TextAnnotation> spanToSentenceAnnotMap, List<TextAnnotation> annotations, String documentText,
			Set<Assertion> positiveAssertionsForDocument, Set<CategoryPair> targetCategoryPairs) {

		Map<CategoryPair, Set<Assertion>> categoryPairToNegativeAssertionsMap = new HashMap<CategoryPair, Set<Assertion>>();
		Map<TextAnnotation, Set<TextAnnotation>> sentenceToEntityAnnotMap = new HashMap<TextAnnotation, Set<TextAnnotation>>();

//		List<TextAnnotation> annotationsWithIds = addIdsToAnnotations(annotations);

		// log pairs of annotation ids that participate in the relations of interest
		// are the annotations in the positive assertions set adjusted with IDs by the
		// addIdsToAnnotations method, i.e., are they linked?
		Set<IdPair> idPairsInRelationsOfInterest = getIdPairsInRelationsOfInterest(positiveAssertionsForDocument);
//		Set<String> annotsIdsInRelationsOfInterest = getIdsOfAnnotationsInRelationsOfInterest(annotationsWithIds,
//				relations);

		// assertions are on the sentence level, so for each sentence, find overlapping
		// concept annotations. If there is a pair of concept annotations that match a
		// categoryPair, then include that pair of concept annotations as a negative
		// assertion assuming that pair is not already a positive assertion.
		TextAnnotationFactory factory = TextAnnotationFactory.createFactoryWithDefaults(documentId);
		for (Entry<Span, TextAnnotation> entry : spanToSentenceAnnotMap.entrySet()) {
			TextAnnotation sentenceAnnot = entry.getValue();
			List<TextAnnotation> conceptAnnotsInSentence = getOverlappingAnnots(sentenceAnnot, annotations,
					documentText, factory);

			// iterator over all pairs of concept annotations,
			for (TextAnnotation concept1 : conceptAnnotsInSentence) {
				for (TextAnnotation concept2 : conceptAnnotsInSentence) {
					// if the concept annotation is not itself then proceed
					if (!concept1.equals(concept2)) {
						createNegativeAssertion(documentId, documentText, targetCategoryPairs,
								categoryPairToNegativeAssertionsMap, idPairsInRelationsOfInterest, factory,
								sentenceAnnot, concept1, concept2);
						// and try it with the concepts switched as well
						createNegativeAssertion(documentId, documentText, targetCategoryPairs,
								categoryPairToNegativeAssertionsMap, idPairsInRelationsOfInterest, factory,
								sentenceAnnot, concept2, concept1);
					}
				}
			}
		}

		return categoryPairToNegativeAssertionsMap;

//		for (TextAnnotation conceptAnnot : annotationsWithIds) {
//			String annotationId = conceptAnnot.getAnnotationID();
//			if (!annotsIdsInRelationsOfInterest.contains(annotationId)) {
//				// if the annotation isn't involved in one of the specified relations, then we
//				// want to find the sentence it is in and add it to the map
//
//				TextAnnotation shallowConceptAnnot = getShallowAnnotation(factory, documentText, conceptAnnot);
//
//				TextAnnotation sentenceAnnot = findSentenceForEntityAnnot(shallowConceptAnnot, spanToSentenceAnnotMap);
//				if (sentenceAnnot != null) {
//					CollectionsUtil.addToOne2ManyUniqueMap(sentenceAnnot, shallowConceptAnnot,
//							sentenceToEntityAnnotMap);
//				}
//
//			}
//
//		}
//
//		return sentenceToEntityAnnotMap;
	}

	private static void createNegativeAssertion(String documentId, String documentText,
			Set<CategoryPair> targetCategoryPairs,
			Map<CategoryPair, Set<Assertion>> categoryPairToNegativeAssertionsMap,
			Set<IdPair> idPairsInRelationsOfInterest, TextAnnotationFactory factory, TextAnnotation sentenceAnnot,
			TextAnnotation concept1, TextAnnotation concept2) {
		String annotId1 = concept1.getAnnotationID();
		String annotId2 = concept2.getAnnotationID();
		IdPair idPair = new IdPair(annotId1, annotId2);
		// if this id pair does not represent a positive assertion, then proceed
		if (!idPairsInRelationsOfInterest.contains(idPair)) {
			String category1 = getCategory(concept1.getClassMention().getMentionName());
			String category2 = getCategory(concept2.getClassMention().getMentionName());
			CategoryPair cp = new CategoryPair(category1, category2);
			// if the category pair for these two concept annotations is one of the target
			// category pairs, then proceed with making the negative assertion
			if (targetCategoryPairs.contains(cp)) {

				TextAnnotation shallow1 = getShallowAnnotation(factory, documentText, concept1);
				TextAnnotation shallow2 = getShallowAnnotation(factory, documentText, concept2);
				Assertion negAssertion = new Assertion(documentId, shallow1, shallow2, sentenceAnnot, null);
				CollectionsUtil.addToOne2ManyUniqueMap(cp, negAssertion, categoryPairToNegativeAssertionsMap);
			}
		}
	}

	/**
	 * @param sentenceAnnot
	 * @param annotations
	 * @param factory
	 * @return a list of annotations that overlap the specified sentence annotation
	 */
	private static List<TextAnnotation> getOverlappingAnnots(TextAnnotation sentenceAnnot,
			List<TextAnnotation> annotations, String documentText, TextAnnotationFactory factory) {
		List<TextAnnotation> overlappingAnnots = new ArrayList<TextAnnotation>();

		for (TextAnnotation annot : annotations) {
			if (annot.overlaps(sentenceAnnot)) {
				overlappingAnnots.add(getShallowAnnotation(factory, documentText, annot));
			}
		}

		return overlappingAnnots;
	}

	@Data
	private static class IdPair {
		private final String subjectId;
		private final String objectId;
	}

	private static Set<IdPair> getIdPairsInRelationsOfInterest(Set<Assertion> positiveAssertionsForDocument) {
		Set<IdPair> idPairs = new HashSet<IdPair>();

		for (Assertion assertion : positiveAssertionsForDocument) {
			String subjectAnnotId = assertion.getSubjectAnnot().getAnnotationID();
			String objectAnnotId = assertion.getObjectAnnot().getAnnotationID();

			IdPair idPair = new IdPair(subjectAnnotId, objectAnnotId);
			idPairs.add(idPair);

		}

		return idPairs;
	}

	/**
	 * return the set of annotation IDs from annotations that don't participate in
	 * one of the specified relations -- these will be used to create negative
	 * assertions used for training
	 * 
	 * @param annotations
	 * @param relations
	 * @return
	 */
	private static Set<String> getIdsOfAnnotationsInRelationsOfInterest(List<TextAnnotation> annotations,
			Set<String> relations) {
		Set<String> ids = new HashSet<String>();

		for (TextAnnotation annot : annotations) {
			Collection<ComplexSlotMention> csms = annot.getClassMention().getComplexSlotMentions();
			if (csms != null) {
				for (ComplexSlotMention csm : csms) {
					String relation = csm.getMentionName();
					if (relations.contains(relation)) {
						// if the annotation participates in one of the specified relations, then add
						// its id to the set as well as the id of the annotation at the other end of the
						// relations
						ids.add(annot.getAnnotationID());
						for (ClassMention cm : csm.getClassMentions()) {
							ids.add(cm.getTextAnnotation().getAnnotationID());
						}
					}
				}
			}
		}

		return ids;
	}

	/**
	 * Add an ID to each annotation in the input list
	 * 
	 * @param annotations
	 * @return
	 */
	private static void addIdsToAnnotations(List<TextAnnotation> annotations) {
		int id = 0;
		for (TextAnnotation annot : annotations) {
			annot.setAnnotationID("annot-" + id++);
		}
	}

	/**
	 * given a concept annotation and the span-to-sentence map, return the sentence
	 * that encompasses the concept annotation
	 * 
	 * @param conceptAnnot
	 * @param spanToSentenceAnnotMap
	 * @return
	 */
	private static TextAnnotation findSentenceForEntityAnnot(TextAnnotation conceptAnnot,
			Map<Span, TextAnnotation> spanToSentenceAnnotMap) {
		for (Entry<Span, TextAnnotation> entry : spanToSentenceAnnotMap.entrySet()) {
			Span sentenceSpan = entry.getKey();
			if (conceptAnnot.getAggregateSpan().overlaps(sentenceSpan)) {
				return entry.getValue();
			}
		}

		System.err.println("No sentence found for: \n" + conceptAnnot.getSingleLineRepresentation());

		return null;
	}

	private static Map<CategoryPair, AssertionGroup> compileAssertionGroups(File craftBaseDirectory,
			File craftAnnotationDirectory) throws IOException {
		Map<CategoryPair, AssertionGroup> subjObjKeyToAssertionGroupMap = new HashMap<CategoryPair, AssertionGroup>();
		TextAnnotationFactory factory = TextAnnotationFactory.createFactoryWithDefaults();
		for (Iterator<File> fileIterator = FileUtil.getFileIterator(craftAnnotationDirectory, false,
				".xml"); fileIterator.hasNext();) {
			File annotationFile = fileIterator.next();
			System.out.println("Processing file: " + annotationFile.getAbsolutePath());

			String pmid = annotationFile.getName().split("\\.")[0];

			File txtFile = getTextFile(craftBaseDirectory, pmid);
			Map<Span, TextAnnotation> spanToSentenceAnnotMap = getSpanToSentenceAnnotMap(craftBaseDirectory, pmid,
					txtFile);
			TextDocument annotDoc = getConceptAnnotationDocument(annotationFile, pmid, txtFile);
			addIdsToAnnotations(annotDoc.getAnnotations());

			for (TextAnnotation subjectAnnot : annotDoc.getAnnotations()) {
				String subjectId = id2Curie(subjectAnnot.getClassMention().getMentionName());
				Collection<ComplexSlotMention> complexSlotMentions = subjectAnnot.getClassMention()
						.getComplexSlotMentions();
				for (ComplexSlotMention csm : complexSlotMentions) {
					String relation = id2Curie(csm.getMentionName());
					Collection<ClassMention> objectCms = csm.getSlotValues();
					for (ClassMention cm : objectCms) {
						TextAnnotation objectAnnot = cm.getTextAnnotation();
						String objectId = id2Curie(objectAnnot.getClassMention().getMentionName());

						String subjectCategory = getCategory(subjectId);
						String objectCategory = getCategory(objectId);

//						String key = subjectCategory + "_" + objectCategory;

						CategoryPair cp = new CategoryPair(subjectCategory, objectCategory);

						TextAnnotation sentenceAnnot = getSentenceAnnot(subjectAnnot, objectAnnot,
								spanToSentenceAnnotMap);

						// in order to work around a StackOverflowError, we create annotations that
						// don't contain the relations embedded as ComplexSlotMentions
						TextAnnotation subjAnnotShallow = getShallowAnnotation(factory, annotDoc.getText(),
								subjectAnnot);

						TextAnnotation objAnnotShallow = getShallowAnnotation(factory, annotDoc.getText(), objectAnnot);

						// make sure there are no discontinuous spans in the entity annotations
						if (subjAnnotShallow.getSpans().size() == 1 && objAnnotShallow.getSpans().size() == 1) {
							Assertion assertion = new Assertion(pmid, subjAnnotShallow, objAnnotShallow, sentenceAnnot,
									relation);
							if (subjObjKeyToAssertionGroupMap.containsKey(cp)) {
								AssertionGroup ag = subjObjKeyToAssertionGroupMap.get(cp);
								ag.addAssertion(assertion);
							} else {
								AssertionGroup ag = new AssertionGroup(subjectCategory, objectCategory);
								ag.addAssertion(assertion);
								subjObjKeyToAssertionGroupMap.put(cp, ag);
							}
						}
					}
				}
			}
		}
		return subjObjKeyToAssertionGroupMap;
	}

	/**
	 * @param factory
	 * @param annotDoc
	 * @param subjectAnnot
	 * @return a version of the input annotation with no slot mentions, i.e. shallow
	 */
	private static TextAnnotation getShallowAnnotation(TextAnnotationFactory factory, String documentText,
			TextAnnotation subjectAnnot) {
		TextAnnotation annot = factory.createAnnotation(subjectAnnot.getSpans(), documentText,
				new DefaultClassMention(subjectAnnot.getClassMention().getMentionName()));
		annot.setAnnotationID(subjectAnnot.getAnnotationID());
		return annot;
	}

	private static TextDocument getConceptAnnotationDocument(File annotationFile, String pmid, File txtFile)
			throws IOException {
		Knowtator2DocumentReader k2Reader = new Knowtator2DocumentReader();
		TextDocument annotDoc = k2Reader.readDocument(pmid, "PMC", annotationFile, txtFile, UTF_8);
		return annotDoc;
	}

	private static File getTextFile(File craftBaseDirectory, String pmid) {
		File txtDir = new File(craftBaseDirectory, "articles");
		txtDir = new File(txtDir, "txt");
		File txtFile = new File(txtDir, pmid + ".txt");
		return txtFile;
	}

	private static Map<Span, TextAnnotation> getSpanToSentenceAnnotMap(File craftBaseDirectory, String pmid,
			File txtFile) throws IOException {
		File treebankDir = new File(craftBaseDirectory, "structural-annotation");
		treebankDir = new File(treebankDir, "treebank");
		treebankDir = new File(treebankDir, "penn");
		File treebankFile = new File(treebankDir, pmid + ".tree");

		SentenceTokenOnlyTreebankDocumentReader tbReader = new SentenceTokenOnlyTreebankDocumentReader();
		TextDocument tbDoc = tbReader.readDocument(pmid, "PMC", treebankFile, txtFile, UTF_8);

		Map<Span, TextAnnotation> spanToSentenceAnnotMap = populateSpanToSentenceAnnotMap(tbDoc);
		return spanToSentenceAnnotMap;
	}

	private static TextAnnotation getSentenceAnnot(TextAnnotation subjectAnnot, TextAnnotation objectAnnot,
			Map<Span, TextAnnotation> spanToSentenceAnnotMap) {

		for (Entry<Span, TextAnnotation> entry : spanToSentenceAnnotMap.entrySet()) {
			Span sentenceSpan = entry.getKey();
			if (subjectAnnot.getAggregateSpan().overlaps(sentenceSpan)
					&& objectAnnot.getAggregateSpan().overlaps(sentenceSpan)) {
				return entry.getValue();
			}
		}

		System.out.println("Sentence count: " + spanToSentenceAnnotMap.size());

//		throw new IllegalStateException("no sentence found");
		System.err.println("No sentence found for: \n" + subjectAnnot.getSingleLineRepresentation() + "\n"
				+ objectAnnot.getSingleLineRepresentation());

		return null;

	}

	private static Map<Span, TextAnnotation> populateSpanToSentenceAnnotMap(TextDocument tbDoc) {
		Map<Span, TextAnnotation> map = new HashMap<Span, TextAnnotation>();

		for (TextAnnotation annot : tbDoc.getAnnotations()) {
			if (annot.getClassMention().getMentionName().equals("sentence")) {
				map.put(annot.getAggregateSpan(), annot);
			}
		}

		return map;
	}

	@Data
	private static class AssertionGroup {
		private final String subjectCategory;
		private final String objectCategory;
		private Map<String, Set<Assertion>> relationToAssertionMap;

		public void addAssertion(Assertion assertion) {
			if (relationToAssertionMap == null) {
				relationToAssertionMap = new HashMap<String, Set<Assertion>>();
			}

			if (relationToAssertionMap.containsKey(assertion.getRelation())) {
				relationToAssertionMap.get(assertion.getRelation()).add(assertion);
			} else {
				Set<Assertion> assertions = new HashSet<Assertion>();
				assertions.add(assertion);
				relationToAssertionMap.put(assertion.getRelation(), assertions);
			}
		}
	}

	@Data
	private static class Assertion {
		private final String documentId;
		private final TextAnnotation subjectAnnot;
		private final TextAnnotation objectAnnot;
		private final TextAnnotation sentenceAnnot;
		private final String relation;
	}

	private static String getCategory(String id) {
		if (id.contains("PR_")) {
			return "protein";
		}
		if (id.contains("CHEBI_")) {
			return "chemical";
		}
		if (id.contains("NCBITaxon_")) {
			return "taxon";
		}
		if (id.contains("UBERON_")) {
			return "anatomy";
		}
		if (id.contains("SO_")) {
			return "sequence";
		}
		if (id.contains("MONDO_")) {
			return "disease";
		}
		if (id.contains("CL_")) {
			return "cell";
		}
		if (id.contains("MOP_")) {
			return "mop";
		}
		if (id.contains("GO:0006412") || id.contains("GO:0010467") || id.contains("GO_EXT:transcription")) {
			return "expression";
		}
		if (id.contains("GO_")) {
			return "geneontology";
		}
//		System.out.println("SOMETHING ID: " + id);
		return "something";
	}

	/**
	 * removes the namespace from an id to create a curie
	 * 
	 * @param id
	 * @return
	 */
	private static String id2Curie(String id) {
		String prefix = "http://www.owl-ontologies.com/unnamed.owl#";
		if (id.startsWith(prefix)) {
			return StringUtils.removePrefix(id, prefix);
		}
		prefix = "http://ccp.cuanschutz.edu/obo/ext/";
		if (id.startsWith(prefix)) {
			return StringUtils.removePrefix(id, prefix);
		}
		prefix = "http://purl.obolibrary.org/obo/";
		if (id.startsWith(prefix)) {
			return StringUtils.removePrefix(id, prefix);
		}
		return id;
	}

}
