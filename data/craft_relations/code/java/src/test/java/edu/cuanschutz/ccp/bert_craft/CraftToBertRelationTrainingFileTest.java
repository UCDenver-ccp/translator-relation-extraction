package edu.cuanschutz.ccp.bert_craft;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import edu.ucdenver.ccp.nlp.core.annotation.TextAnnotation;
import edu.ucdenver.ccp.nlp.core.annotation.TextAnnotationFactory;

public class CraftToBertRelationTrainingFileTest {

	@Test
	public void testAddPlaceholder() {

		TextAnnotationFactory factory = TextAnnotationFactory.createFactoryWithDefaults();
		TextAnnotation proteinAnnot = factory.createAnnotation(6, 10, "ABC3", "PR:12345");
		TextAnnotation taxonAnnot = factory.createAnnotation(0, 5, "Human", "NCBITaxon:12345");

		// 01234567890123456789012345678901234567890123456789
		String sentenceText = "Human ABC3 is known to interact with XYZ4.";
		String sentenceWithPlaceholder = CraftToBertRelationTrainingFile.addPlaceholder(sentenceText, 0, proteinAnnot,
				CraftToBertRelationTrainingFile.SUBJECT_PLACEHOLDER);

		String expectedSentenceWithPlaceholder = String.format("Human %s is known to interact with XYZ4.",
				CraftToBertRelationTrainingFile.SUBJECT_PLACEHOLDER);

		assertEquals(expectedSentenceWithPlaceholder, sentenceWithPlaceholder);

		sentenceWithPlaceholder = CraftToBertRelationTrainingFile.addPlaceholder(sentenceWithPlaceholder, 0, taxonAnnot,
				CraftToBertRelationTrainingFile.OBJECT_PLACEHOLDER);

		expectedSentenceWithPlaceholder = String.format("%s %s is known to interact with XYZ4.",
				CraftToBertRelationTrainingFile.OBJECT_PLACEHOLDER,
				CraftToBertRelationTrainingFile.SUBJECT_PLACEHOLDER);

		assertEquals(expectedSentenceWithPlaceholder, sentenceWithPlaceholder);

	}

	@Test
	public void testAddPlaceholderWithSetenceOffsetNonZero() {

		TextAnnotationFactory factory = TextAnnotationFactory.createFactoryWithDefaults();
		TextAnnotation proteinAnnot = factory.createAnnotation(15, 19, "ABC3", "PR:12345");
		TextAnnotation taxonAnnot = factory.createAnnotation(9, 14, "Human", "NCBITaxon:12345");

		// 01234567890123456789012345678901234567890123456789
		String sentenceText = "Human ABC3 is known to interact with XYZ4.";
		String sentenceWithPlaceholder = CraftToBertRelationTrainingFile.addPlaceholder(sentenceText, 9, proteinAnnot,
				CraftToBertRelationTrainingFile.SUBJECT_PLACEHOLDER);

		String expectedSentenceWithPlaceholder = String.format("Human %s is known to interact with XYZ4.",
				CraftToBertRelationTrainingFile.SUBJECT_PLACEHOLDER);

		assertEquals(expectedSentenceWithPlaceholder, sentenceWithPlaceholder);

		sentenceWithPlaceholder = CraftToBertRelationTrainingFile.addPlaceholder(sentenceWithPlaceholder, 9, taxonAnnot,
				CraftToBertRelationTrainingFile.OBJECT_PLACEHOLDER);

		expectedSentenceWithPlaceholder = String.format("%s %s is known to interact with XYZ4.",
				CraftToBertRelationTrainingFile.OBJECT_PLACEHOLDER,
				CraftToBertRelationTrainingFile.SUBJECT_PLACEHOLDER);

		assertEquals(expectedSentenceWithPlaceholder, sentenceWithPlaceholder);

	}

}
