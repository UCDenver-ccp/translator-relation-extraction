package edu.cuanschutz.ccp.bert_craft.used_for_2023_progress_report;

import static edu.cuanschutz.ccp.bert_craft.CraftToBertRelationTrainingFileMainDev.removeNamespace;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.zip.GZIPInputStream;

import edu.ucdenver.ccp.common.collections.CollectionsUtil;
import edu.ucdenver.ccp.common.file.CharacterEncoding;
import edu.ucdenver.ccp.common.file.FileUtil;
import edu.ucdenver.ccp.common.file.FileWriterUtil;
import edu.ucdenver.ccp.common.file.reader.Line;
import edu.ucdenver.ccp.common.file.reader.StreamLineIterator;
import edu.ucdenver.ccp.file.conversion.TextDocument;
import edu.ucdenver.ccp.file.conversion.conllu.CoNLLUDocumentReader;
import edu.ucdenver.ccp.file.conversion.knowtator2.Knowtator2DocumentReader;
import edu.ucdenver.ccp.nlp.core.annotation.Span;
import edu.ucdenver.ccp.nlp.core.annotation.TextAnnotation;
import edu.ucdenver.ccp.nlp.core.mention.ClassMention;
import edu.ucdenver.ccp.nlp.core.mention.ComplexSlotMention;

/**
 * Parses the CRAFT files and extracts relations into a graph structure mapped
 * to the sentence text from which it was extracted.
 */
public class CraftRelationSerializer {

	private static final CharacterEncoding ENCODING = CharacterEncoding.UTF_8;

	/**
	 * processes the directory of craft knowtator xml files that contain assertions
	 * 
	 * @param outputDir
	 * 
	 * @param knowtatorXmlFile
	 * @throws IOException
	 */
	public static void extractRelations(File knowtatorXmlDir, File txtDir, File conlluDir, File go2namespaceFile,
			File outputDir) throws IOException {
		Map<String, String> go2namespaceMap = loadMap(go2namespaceFile);
		System.out.println("go2namespace size: " + go2namespaceMap.size());
		for (Iterator<File> fileIterator = FileUtil.getFileIterator(knowtatorXmlDir, false); fileIterator.hasNext();) {
			File knowtatorXmlFile = fileIterator.next();
			String id = knowtatorXmlFile.getName().split("\\.")[0];
			File txtFile = new File(txtDir, id + ".txt");
			File conlluFile = new File(conlluDir, id + ".conllu");
			exportBionlp(id, knowtatorXmlFile, txtFile, conlluFile, outputDir, go2namespaceMap);

		}
	}

	private static Map<String, String> loadMap(File go2namespaceFile) throws IOException {
		Map<String, String> go2namespaceMap = new HashMap<>();
		for (StreamLineIterator lineIter = new StreamLineIterator(
				new GZIPInputStream(new FileInputStream(go2namespaceFile)), CharacterEncoding.UTF_8, null); lineIter
						.hasNext();) {
			Line line = lineIter.next();
			String[] cols = line.getText().split("\\t");
			go2namespaceMap.put(cols[0], cols[1]);
		}
		return go2namespaceMap;
	}

	/**
	 * Converts from knowtator 2 XML to bionlp format
	 * 
	 * @param id
	 * @param knowtatorXmlFile
	 * @param txtFile
	 * @param go2namespaceMap
	 * @throws IOException
	 */
	private static void exportBionlp(String id, File knowtatorXmlFile, File txtFile, File conlluFile, File outputDir,
			Map<String, String> go2namespaceMap) throws IOException {
		Knowtator2DocumentReader docReader = new Knowtator2DocumentReader();
		TextDocument td = docReader.readDocument(id, "craft", knowtatorXmlFile, txtFile, ENCODING);

//		TreebankDocumentReader treeDocReader = new TreebankDocumentReader();
//		TextDocument treeDoc = treeDocReader.readDocument(id, "craft", conlluFile, txtFile, ENCODING);

		CoNLLUDocumentReader conlluDocReader = new CoNLLUDocumentReader();
		TextDocument conlluDoc = conlluDocReader.readDocument(id, "craft", conlluFile, txtFile, ENCODING);

		List<TextAnnotation> sentenceAnnots = extractSentenceAnnots(conlluDoc);
		System.out.println("sentence count: " + sentenceAnnots.size());

		// map concept annotations to the sentence that they overlap
		Map<TextAnnotation, Set<String>> sentenceToConceptAnnotMap = mapConceptStrsToSentences(td.getAnnotations(),
				sentenceAnnots, go2namespaceMap);

		Set<String> observed = new HashSet<String>();
		Map<String, Set<String>> annotStrToRelationsMap = new HashMap<String, Set<String>>();

		for (TextAnnotation conceptAnnot : td.getAnnotations()) {
			getRelations(conceptAnnot, observed, annotStrToRelationsMap, go2namespaceMap);
		}

		File outputFile = new File(outputDir, id + ".relations");
		try (BufferedWriter writer = FileWriterUtil.initBufferedWriter(outputFile)) {
			// for each sentence, output the sentence, concept annots, and relations in some
			// structured format
			for (Entry<TextAnnotation, Set<String>> entry : sentenceToConceptAnnotMap.entrySet()) {
				TextAnnotation sentenceAnnot = entry.getKey();
				Set<String> conceptAnnotStrings = entry.getValue();

				// require there to at least be two concepts in the sentence
				if (conceptAnnotStrings != null && conceptAnnotStrings.size() > 1) {

					String sentenceText = td.getText().substring(sentenceAnnot.getAnnotationSpanStart(),
							sentenceAnnot.getAnnotationSpanEnd());

					writer.write(String.format("# DOCUMENT_ID\t%s\n", id));
					writer.write(String.format("# SENTENCE\t%d\t%s\n", sentenceAnnot.getAnnotationSpanStart(),
							sentenceText));

					// get all relations involved
					Set<String> relations = new HashSet<String>();
					for (String annotStr : conceptAnnotStrings) {
						writer.write(String.format("# ANNOT\t%s\n", annotStr));
						if (annotStrToRelationsMap.containsKey(annotStr)) {
							relations.addAll(annotStrToRelationsMap.get(annotStr));
						}
					}

					// print relations here
					List<String> relationsList = new ArrayList<String>(relations);
					Collections.sort(relationsList);
					for (String r : relationsList) {
						writer.write(String.format("# RELATION\t%s\n", r));
					}

					writer.write("\n");
				}
			}
		}

//		for (TextAnnotation conceptAnnot : td.getAnnotations()) {
//			List<String> relations = new ArrayList<String>();
//			Set<String> observed = new HashSet<String>();
//			if (hasCycle(conceptAnnot, observed, relations)) {
//
//			}
//		}

//		// map concept annotations to the sentence that they overlap
//		Map<TextAnnotation, Set<TextAnnotation>> sentenceToConceptAnnotMap = mapConceptAnnotsToSentences(
//				td.getAnnotations(), sentenceAnnots);
//
//		// extract all relations between concepts in each sentence
//		for (Entry<TextAnnotation, Set<TextAnnotation>> entry : sentenceToConceptAnnotMap.entrySet()) {
//			System.out.println("concept annot count: " + entry.getValue().size());
//		}

//		File outputFile = new File(outputDir, id + ".bionlp");
//		BioNLPDocumentWriter docWriter = new BioNLPDocumentWriter();
//		docWriter.serialize(td, outputFile, ENCODING);

	}

	/**
	 * Recursively retrieves all relations for a given annotation and stores them in
	 * a map that links annots to the relations in which they participate
	 * 
	 * @param conceptAnnot
	 * @param observed
	 * @param go2namespaceMap
	 * @param relations
	 */
	private static void getRelations(TextAnnotation conceptAnnot, Set<String> observed,
			Map<String, Set<String>> annotToRelationsMap, Map<String, String> go2namespaceMap) {
		String conceptAnnotStr = getAnnotStr(conceptAnnot, go2namespaceMap);
		if (observed.contains(conceptAnnotStr)) {
			// then we've already processed this annotation so just return
			return;
		}
		observed.add(conceptAnnotStr);
		Collection<ComplexSlotMention> csms = conceptAnnot.getClassMention().getComplexSlotMentions();
		for (ComplexSlotMention csm : csms) {
			for (ClassMention cm : csm.getClassMentions()) {
				TextAnnotation annotation = cm.getTextAnnotation();
				String relation = String.format("%s\t%s\t%s", getAnnotStr(conceptAnnot, go2namespaceMap),
						removeNamespace(csm.getMentionName()), getAnnotStr(annotation, go2namespaceMap));
				String annotStr = getAnnotStr(annotation, go2namespaceMap);
				CollectionsUtil.addToOne2ManyUniqueMap(conceptAnnotStr, relation, annotToRelationsMap);
				CollectionsUtil.addToOne2ManyUniqueMap(annotStr, relation, annotToRelationsMap);
				getRelations(annotation, observed, annotToRelationsMap, go2namespaceMap);
			}
		}
	}

//	private static boolean hasCycle(TextAnnotation conceptAnnot, Set<String> observed, List<String> relations) {
//		String annotStr = getAnnotStr(conceptAnnot);
//		if (observed.contains(annotStr)) {
//			System.out.println("Has cycle: " + annotStr);
//			for (String relation : relations) {
//				System.out.println(relation);
//			}
//			System.out.println("---------");
//			return true;
//		}
//		observed.add(annotStr);
//		Collection<ComplexSlotMention> csms = conceptAnnot.getClassMention().getComplexSlotMentions();
//		for (ComplexSlotMention csm : csms) {
//			for (ClassMention cm : csm.getClassMentions()) {
//				TextAnnotation annotation = cm.getTextAnnotation();
//				String relation = String.format("%s|%s -- %s -- %s|%s", conceptAnnot.getSpans().toString(),
//						conceptAnnot.getClassMention().getMentionName(), csm.getMentionName(),
//						annotation.getSpans().toString(), annotation.getClassMention().getMentionName());
//				relations.add(relation);
//				if (hasCycle(annotation, observed, relations)) {
//					return true;
//				}
//			}
//		}
//		return false;
//	}

	/**
	 * @param a
	 * @param offset the character position for the start of the sentence. The spans
	 *               will be made relative to the start of the sentence by
	 *               subtracting the offset.
	 * @return
	 */
	private static String getAnnotStr(TextAnnotation a, Map<String, String> go2namespaceMap) {
		return String.format("%s|%s|%s", getSpanStr(a.getSpans()),
				transformGoId(removeNamespace(a.getClassMention().getMentionName()), go2namespaceMap),
				a.getCoveredText());
	}

	private static String transformGoId(String id, Map<String, String> go2namespaceMap) {
		if (id.startsWith("GO_") && go2namespaceMap.containsKey(id.replace("_", ":"))) {
			String ns = go2namespaceMap.get(id.replace("_", ":"));
			switch (ns) {
			case "biological_process":
				id = id.replace("GO_", "GO_BP:");
				break;

			case "molecular_function":
				id = id.replace("GO_", "GO_MF:");
				break;

			case "cellular_component":
				id = id.replace("GO_", "GO_CC:");
				break;
			default:
				throw new IllegalArgumentException("should not be possible to be here");
			}
		}
		return id;
	}

	private static String getSpanStr(List<Span> spans) {
		StringBuilder sb = new StringBuilder();
		for (Span span : spans) {
			if (sb.length() > 0) {
				sb.append(";");
			}
			sb.append((span.getSpanStart()) + "_" + (span.getSpanEnd()));
		}
		return sb.toString();
	}

	private static Map<TextAnnotation, Set<String>> mapConceptStrsToSentences(List<TextAnnotation> conceptAnnots,
			List<TextAnnotation> sentenceAnnots, Map<String, String> go2namespaceMap) {

		Map<TextAnnotation, Set<String>> sentToAnnotMap = new HashMap<TextAnnotation, Set<String>>();

		Collections.sort(conceptAnnots, TextAnnotation.BY_SPAN());
		Collections.sort(sentenceAnnots, TextAnnotation.BY_SPAN());

		int conceptIndex = 0;
		for (TextAnnotation sentenceAnnot : sentenceAnnots) {
			for (int i = conceptIndex; i < conceptAnnots.size(); i++) {
				TextAnnotation conceptAnnot = conceptAnnots.get(i);
				if (conceptAnnot.overlaps(sentenceAnnot)) {
					String conceptStr = getAnnotStr(conceptAnnot, go2namespaceMap);
					CollectionsUtil.addToOne2ManyUniqueMap(sentenceAnnot, conceptStr, sentToAnnotMap);
				} else {
					// if it doesn't overlap, then we need to move on to the next sentence
					// we mark where we are in the concept list
					conceptIndex = i;
					break;
				}
			}
		}

		return sentToAnnotMap;
	}

	private static Map<TextAnnotation, Set<TextAnnotation>> mapConceptAnnotsToSentences(
			List<TextAnnotation> conceptAnnots, List<TextAnnotation> sentenceAnnots) {

		Map<TextAnnotation, Set<TextAnnotation>> sentToAnnotMap = new HashMap<TextAnnotation, Set<TextAnnotation>>();

		Collections.sort(conceptAnnots, TextAnnotation.BY_SPAN());
		Collections.sort(sentenceAnnots, TextAnnotation.BY_SPAN());

		int conceptIndex = 0;
		for (TextAnnotation sentenceAnnot : sentenceAnnots) {
			for (int i = conceptIndex; i < conceptAnnots.size(); i++) {
				TextAnnotation conceptAnnot = conceptAnnots.get(i);
				if (conceptAnnot.overlaps(sentenceAnnot)) {
					CollectionsUtil.addToOne2ManyUniqueMap(sentenceAnnot, conceptAnnot, sentToAnnotMap);
				} else {
					// if it doesn't overlap, then we need to move on to the next sentence
					// we mark where we are in the concept list
					conceptIndex = i;
					break;
				}
			}
		}

		return sentToAnnotMap;
	}

//	@Data
//	private static class RelationPackage {
//		private final String documentId;
//
//	}

	/**
	 * Extract sentence annotations from the conllu document
	 * 
	 * @param conlluDoc
	 * @return
	 */
	private static List<TextAnnotation> extractSentenceAnnots(TextDocument conlluDoc) {
		List<TextAnnotation> sentenceAnnots = new ArrayList<TextAnnotation>();

		for (TextAnnotation annot : conlluDoc.getAnnotations()) {
			String mentionName = annot.getClassMention().getMentionName();
			if (mentionName.equals("sentence")) {
				sentenceAnnots.add(annot);
			}
		}

		return sentenceAnnots;

	}

	public static void main(String[] args) {
		File knowtatorXmlDir = new File(
				"/Users/bill/projects/ncats-translator/relations/craft-relations/may-2023-development/CRAFT_concepts+assertions_original/Annotations");
		File txtDir = new File(
				"/Users/bill/projects/ncats-translator/relations/craft-relations/may-2023-development/CRAFT_concepts+assertions_original/Articles");
		File conlluDir = new File(
				"/Users/bill/projects/craft-shared-task/craft.git/structural-annotation/dependency/conllu");
		File outputDir = new File(
				"/Users/bill/projects/ncats-translator/relations/craft-relations/may-2023-development/CRAFT_concepts+assertions_original/relations_and_sentences");
		File go2namespaceFile = new File(
				"/Users/bill/projects/ncats-translator/concept-recognition/july2023/go2namespace.tsv.gz");
		outputDir.mkdirs();

		try {
			extractRelations(knowtatorXmlDir, txtDir, conlluDir, go2namespaceFile, outputDir);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
