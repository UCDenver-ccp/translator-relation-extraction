package edu.cuanschutz.ccp.bert_prep.genereg;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;

import edu.ucdenver.ccp.common.collections.CollectionsUtil;
import edu.ucdenver.ccp.common.file.CharacterEncoding;
import edu.ucdenver.ccp.common.io.ClassPathUtil;
import edu.ucdenver.ccp.file.conversion.TextDocument;
import edu.ucdenver.ccp.nlp.core.annotation.Annotator;
import edu.ucdenver.ccp.nlp.core.annotation.TextAnnotation;
import edu.ucdenver.ccp.nlp.core.annotation.impl.DefaultTextAnnotation;
import edu.ucdenver.ccp.nlp.core.mention.ClassMention;
import edu.ucdenver.ccp.nlp.core.mention.ComplexSlotMention;
import edu.ucdenver.ccp.nlp.core.mention.impl.DefaultClassMention;
import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;
import opennlp.tools.util.Span;

public class GeneRegCorpusReader {

	protected static final String NEGATIVELY_REGULATES = "negatively_regulates";
	protected static final String POSITIVELY_REGULATES = "positively_regulates";
	private static final String GENE_REGULATOR_PLACEHOLDER = "@GENE_REGULATOR$";
	private static final String REGULATED_GENE_PLACEHOLDER = "@REGULATED_GENE$";

	public static void generateBertStyleTrainingData(List<TextAnnotation> sentences, List<TextAnnotation> entityAnnots,
			BufferedWriter writer) throws IOException {
		Map<TextAnnotation, Set<TextAnnotation>> sentToEntityMap = new HashMap<TextAnnotation, Set<TextAnnotation>>();

		populateSentenceToEntityMap(sentences, entityAnnots, sentToEntityMap);

		for (Entry<TextAnnotation, Set<TextAnnotation>> entry : sentToEntityMap.entrySet()) {
			TextAnnotation sentence = entry.getKey();
			Set<TextAnnotation> geneAnnots = getGeneAnnots(entry.getValue());
			/* we are interested in sentences that have >1 genes */
			if (geneAnnots.size() > 1) {

				Set<String> allPossibleGenePairs = getGeneIdPairs(geneAnnots);
				Map<String, TextAnnotation> geneIdToGeneMap = getGeneIdToGeneMap(geneAnnots);

				Set<String> alreadyPrinted = new HashSet<String>();

				for (TextAnnotation causeAnnot : geneAnnots) {
					for (ComplexSlotMention csm : causeAnnot.getClassMention().getComplexSlotMentions()) {
						String regulationType = csm.getMentionName();
						for (ClassMention themeCm : csm.getSlotValues()) {
							TextAnnotation themeAnnot = themeCm.getTextAnnotation();

							/*
							 * these two genes are part of a regulation event, so remove them from the all
							 * possible pairs set
							 */
							String causeId = causeAnnot.getAnnotationID();
							String themeId = themeAnnot.getAnnotationID();
							String pair = getOrderedString(causeId, themeId);
							allPossibleGenePairs.remove(pair);

							// output the training example
							String type = null;
							if (regulationType.equals("PositiveRegulationOfGeneExpression")) {
								type = POSITIVELY_REGULATES;
							} else if (regulationType.equals("NegativeRegulationOfGeneExpression")) {
								type = NEGATIVELY_REGULATES;
							} else {
								type = "false";
							}
							writeTrainingExample(writer, sentence, alreadyPrinted, causeAnnot, themeAnnot, type);
							// add the reverse as a false example
							writeTrainingExample(writer, sentence, alreadyPrinted, themeAnnot, causeAnnot, "false");
						}
					}
				}

				/*
				 * now for remaining pairs, output false example, i.e. there is no regulation
				 * relation between the genes. Here we annotate two examples for each, one where
				 * gene 1 is the regulator and gene 2 is the regulatee, and one where gene 1 is
				 * the regulatee and gene 2 is the regulator
				 */
				for (String pair : allPossibleGenePairs) {
					String[] ids = pair.split("_");
					TextAnnotation geneAnnot1 = geneIdToGeneMap.get(ids[0]);
					TextAnnotation geneAnnot2 = geneIdToGeneMap.get(ids[1]);

					writeTrainingExample(writer, sentence, alreadyPrinted, geneAnnot1, geneAnnot2, "false");
					writeTrainingExample(writer, sentence, alreadyPrinted, geneAnnot2, geneAnnot1, "false");

				}
			}
		}
	}

	private static void writeTrainingExample(BufferedWriter writer, TextAnnotation sentence, Set<String> alreadyPrinted,
			TextAnnotation causeAnnot, TextAnnotation themeAnnot, String type) throws IOException {
		String out = getTrainingExampleLine(sentence, causeAnnot, themeAnnot, type, alreadyPrinted);
		if (writer != null && out != null) {
			writer.write(out + "\n");
		}
	}

	/**
	 * Populates the sentToEntityMap by looking for entity annotations that overlap
	 * with sentence annotations
	 * 
	 * @param sentences
	 * @param entityAnnots
	 * @param sentToEntityMap
	 */
	private static void populateSentenceToEntityMap(List<TextAnnotation> sentences, List<TextAnnotation> entityAnnots,
			Map<TextAnnotation, Set<TextAnnotation>> sentToEntityMap) {
		for (TextAnnotation entityAnnot : entityAnnots) {
			for (TextAnnotation sentence : sentences) {
				if (entityAnnot.overlaps(sentence)) {
					CollectionsUtil.addToOne2ManyUniqueMap(sentence, entityAnnot, sentToEntityMap);
					/*
					 * an entity should only overlap with a single sentence, so once it's found we
					 * can break out of the sentence loop
					 */
					break;
				}
			}
		}
	}

	protected static String getTrainingExampleLine(TextAnnotation sentence, TextAnnotation causeAnnot,
			TextAnnotation themeAnnot, String label, Set<String> alreadyPrinted) {

		/*
		 * setting annotation IDs so that the proper placeholder can be used to replace
		 * the genes in the sentence
		 */

		Map<String, String> annotationIdToPlaceholderMap = new HashMap<String, String>();
		annotationIdToPlaceholderMap.put(causeAnnot.getAnnotationID(), GENE_REGULATOR_PLACEHOLDER);
		annotationIdToPlaceholderMap.put(themeAnnot.getAnnotationID(), REGULATED_GENE_PLACEHOLDER);

//		causeAnnot.getClassMention().createPrimitiveSlotMention(PLACEHOLDER_SLOT, GENE_REGULATOR_PLACEHOLDER);
//		themeAnnot.getClassMention().createPrimitiveSlotMention(PLACEHOLDER_SLOT, REGULATED_GENE_PLACEHOLDER);

//		TextAnnotationUtil.addSlotValue(causeAnnot, PLACEHOLDER_SLOT, GENE_REGULATOR_PLACEHOLDER);
//		TextAnnotationUtil.addSlotValue(themeAnnot, PLACEHOLDER_SLOT, REGULATED_GENE_PLACEHOLDER);

		// make sure both genes are in the same sentence; don't allow the genes to
		// overlap either
		if (sentence.overlaps(causeAnnot) && sentence.overlaps(themeAnnot) && !causeAnnot.overlaps(themeAnnot)) {

			String sentenceText = sentence.getCoveredText();
			List<TextAnnotation> sortedGeneAnnots = Arrays.asList(causeAnnot, themeAnnot);
			Collections.sort(sortedGeneAnnots, TextAnnotation.BY_SPAN());

			// must replace in decreasing span order
			for (int i = 1; i >= 0; i--) {
				TextAnnotation geneAnnot = sortedGeneAnnots.get(i);
				int geneStart = geneAnnot.getAggregateSpan().getSpanStart() - sentence.getAnnotationSpanStart();
				int geneEnd = geneAnnot.getAggregateSpan().getSpanEnd() - sentence.getAnnotationSpanStart();

//				String placeholder = geneAnnot.getClassMention().getPrimitiveSlotMentionByName(PLACEHOLDER_SLOT)
//						.getSingleSlotValue().toString();

				String placeholder = annotationIdToPlaceholderMap.get(geneAnnot.getAnnotationID());

				sentenceText = sentenceText.substring(0, geneStart) + placeholder + sentenceText.substring(geneEnd);
			}

//			String orderedIds = getOrderedString(geneAnnot1.getAnnotationID(), geneAnnot2.getAnnotationID());

//			System.out.println("SENTENCE: " + sentenceText);
			String id = DigestUtils.shaHex(sentenceText);

			if (!alreadyPrinted.contains(id)) {
				alreadyPrinted.add(id);
				String output = id + "\t" + sentenceText + "\t" + label;
				return output;
			}
		}
		return null;

	}

	private static Map<String, TextAnnotation> getGeneIdToGeneMap(Set<TextAnnotation> geneAnnots) {
		Map<String, TextAnnotation> geneIdToGeneMap = new HashMap<String, TextAnnotation>();

		for (TextAnnotation ta : geneAnnots) {
			geneIdToGeneMap.put(ta.getAnnotationID(), ta);
		}

		return geneIdToGeneMap;
	}

	private static Set<String> getGeneIdPairs(Set<TextAnnotation> geneAnnots) {
		Set<String> pairs = new HashSet<String>();

		for (TextAnnotation ta1 : geneAnnots) {
			for (TextAnnotation ta2 : geneAnnots) {
				String id1 = ta1.getAnnotationID();
				String id2 = ta2.getAnnotationID();
				if (!id1.equals(id2)) {
					pairs.add(getOrderedString(id1, id2));
				}
			}
		}

		return pairs;
	}

	private static String getOrderedString(String id1, String id2) {
		List<String> list = Arrays.asList(id1, id2);
		Collections.sort(list);
		return list.get(0) + "_" + list.get(1);
	}

	private static Set<TextAnnotation> getGeneAnnots(Set<TextAnnotation> entityAnnots) {
		Set<TextAnnotation> geneAnnots = new HashSet<TextAnnotation>();
		int index = 0;
		for (TextAnnotation ta : entityAnnots) {
			String type = ta.getClassMention().getMentionName();
			if (type.equals("Gene")) {
				ta.setAnnotationID("gene" + index++);
				geneAnnots.add(ta);
			}
		}
		return geneAnnots;
	}

	public static List<TextAnnotation> getAnnotations(String sourceId, InputStream txtStream, InputStream a12Stream)
			throws IOException {
		BioNLPDocumentReader reader = new BioNLPDocumentReader();
		TextDocument td = reader.readDocument(sourceId, "PubMed", a12Stream, txtStream, CharacterEncoding.UTF_8);
		return td.getAnnotations();
	}

	public static List<TextAnnotation> getSentences(InputStream txtStream) throws IOException {
		String docText = IOUtils.toString(txtStream, CharacterEncoding.UTF_8.getCharacterSetName());
		InputStream modelStream = ClassPathUtil.getResourceStreamFromClasspath(GeneRegCorpusReader.class,
				"/de/tudarmstadt/ukp/dkpro/core/opennlp/lib/sentence-en-maxent.bin");
		SentenceModel model = new SentenceModel(modelStream);
		SentenceDetectorME sentenceDetector = new SentenceDetectorME(model);

		List<TextAnnotation> annots = new ArrayList<TextAnnotation>();
		Span[] spans = sentenceDetector.sentPosDetect(docText);
		for (Span span : spans) {
			span.getStart();
			span.getEnd();
			span.getType();
			TextAnnotation annot = createSentenceAnnot(span.getStart(), span.getEnd(),
					span.getCoveredText(docText).toString());
			annots.add(annot);
		}

		List<TextAnnotation> toKeep = splitSentencesOnLineBreaks(annots);

		return toKeep;
	}

	private static List<TextAnnotation> splitSentencesOnLineBreaks(List<TextAnnotation> annots) {
		/*
		 * divide any sentences with line breaks into multiple sentences, splitting at
		 * the line breaks
		 */
		List<TextAnnotation> toKeep = new ArrayList<TextAnnotation>();
		for (TextAnnotation annot : annots) {
			String coveredText = annot.getCoveredText();
			if (coveredText.contains("\n")) {
				String[] sentences = coveredText.split("\\n");
				int index = annot.getAnnotationSpanStart();
				for (String s : sentences) {
					if (!s.isEmpty()) {
						TextAnnotation sentAnnot = createSentenceAnnot(index, index + s.length(), s);
						index = index + s.length() + 1;
						toKeep.add(sentAnnot);
					} else {
						index++;
					}
				}
				// validate - span end of more recently added sentence should be equal to the
				// span end of the original annot
				int originalSpanEnd = annot.getAnnotationSpanEnd();
				int end = toKeep.get(toKeep.size() - 1).getAnnotationSpanEnd();
				assert end == originalSpanEnd;
			} else {
				toKeep.add(annot);
			}
		}
		return toKeep;
	}

	private static TextAnnotation createSentenceAnnot(int spanStart, int spanEnd, String coveredText) {
		DefaultTextAnnotation annot = new DefaultTextAnnotation(spanStart, spanEnd);
		annot.setCoveredText(coveredText);
		DefaultClassMention cm = new DefaultClassMention("sentence");
		annot.setClassMention(cm);
		annot.setAnnotator(new Annotator(null, "OpenNLP", "OpenNLP"));
		return annot;
	}

}
