package edu.cuanschutz.ccp.bert_prep.genereg;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import edu.cuanschutz.ccp.bert_prep.genereg.GeneRegCorpusReader;
import edu.ucdenver.ccp.common.file.CharacterEncoding;
import edu.ucdenver.ccp.common.file.FileComparisonUtil;
import edu.ucdenver.ccp.common.file.FileWriterUtil;
import edu.ucdenver.ccp.common.file.FileComparisonUtil.ColumnOrder;
import edu.ucdenver.ccp.common.file.FileComparisonUtil.LineOrder;
import edu.ucdenver.ccp.common.file.FileReaderUtil;
import edu.ucdenver.ccp.common.io.ClassPathUtil;
import edu.ucdenver.ccp.nlp.core.annotation.TextAnnotation;

public class GeneRegCorpusReaderTest {

	@Rule
	public TemporaryFolder folder = new TemporaryFolder();

	@Test
	public void testGetAnnotations() throws IOException {

		InputStream txtStream = ClassPathUtil.getResourceStreamFromClasspath(getClass(), "10760150.txt");
		InputStream a12Stream = ClassPathUtil.getResourceStreamFromClasspath(getClass(), "10760150.a12");

		List<TextAnnotation> annotations = GeneRegCorpusReader.getAnnotations("10760150", txtStream, a12Stream);

		assertEquals("There are 25 entity annotations; events are modeled using complex slot mentions", 25,
				annotations.size());

//		for (TextAnnotation ta : annotations) {
//			System.out.println(ta.toString());
//		}

	}

	@Test
	public void testGetSentences() throws IOException {
		InputStream txtStream = ClassPathUtil.getResourceStreamFromClasspath(getClass(), "10760150.txt");
		List<TextAnnotation> annotations = GeneRegCorpusReader.getSentences(txtStream);

		assertEquals("There are 9 sentences", 9, annotations.size());

//		for (TextAnnotation ta : annotations) {
//			System.out.println(ta.getCoveredText());
//		}

	}

	@Test
	public void testGenerateBertStyleTrainingData() throws IOException {
		InputStream txtStream = ClassPathUtil.getResourceStreamFromClasspath(getClass(), "10760150.txt");
		List<TextAnnotation> sentences = GeneRegCorpusReader.getSentences(txtStream);

		txtStream = ClassPathUtil.getResourceStreamFromClasspath(getClass(), "10760150.txt");
		InputStream a12Stream = ClassPathUtil.getResourceStreamFromClasspath(getClass(), "10760150.a12");

		List<TextAnnotation> entityAnnots = GeneRegCorpusReader.getAnnotations("10760150", txtStream, a12Stream);

		File outputFile = folder.newFile();
		try (BufferedWriter writer = FileWriterUtil.initBufferedWriter(outputFile)) {
			GeneRegCorpusReader.generateBertStyleTrainingData(sentences, entityAnnots, writer);
		}

		List<String> lines = FileReaderUtil.loadLinesFromFile(outputFile, CharacterEncoding.UTF_8);

		List<String> expectedTrainingExamples = Arrays.asList(
				"1557e57d7abd28bf58dabab90d5fd6856f6952e0	The @REGULATED_GENE$ (YheB) protein of Escherichia coli K-12 is an endochitinase whose gene is negatively controlled by the nucleoid-structuring protein @GENE_REGULATOR$.	" + GeneRegCorpusReader.NEGATIVELY_REGULATES,
				"e6334881f4dbf446a4e2791fa7b1d872652a886a	The @GENE_REGULATOR$ (YheB) protein of Escherichia coli K-12 is an endochitinase whose gene is negatively controlled by the nucleoid-structuring protein @REGULATED_GENE$.	false",
				"c7eb7c0bc471d621fd6ad1c8c81c3832c4532fab	Transcription of @REGULATED_GENE$ in vivo is driven by a single sigma70 promoter and is derepressed in an @GENE_REGULATOR$ mutant.	" + GeneRegCorpusReader.POSITIVELY_REGULATES,
				"9e1176b909dc93526d9b60a8b63fc92ef640faf3	Transcription of @GENE_REGULATOR$ in vivo is driven by a single sigma70 promoter and is derepressed in an @REGULATED_GENE$ mutant.	false",
				"7d6860113b2517d6ac2daec94976b5e68679030e	In addition to hns, other E. coli mutations causing defects in global regulatory proteins, such as fis, @GENE_REGULATOR$ or stpA in combination with hns, increased @REGULATED_GENE$ expression to different extents, as did decreasing the growth temperature from 37 degrees C to 30 degrees C. A possible physiological function of ChiA (YheB) endochitinase in E. coli K-12 is discussed.	" + GeneRegCorpusReader.NEGATIVELY_REGULATES,
				"792435a077079fd5523d5c1bfcd3534547f739ca	In addition to hns, other E. coli mutations causing defects in global regulatory proteins, such as fis, @REGULATED_GENE$ or stpA in combination with hns, increased @GENE_REGULATOR$ expression to different extents, as did decreasing the growth temperature from 37 degrees C to 30 degrees C. A possible physiological function of ChiA (YheB) endochitinase in E. coli K-12 is discussed.	false",
				"11b94b7ad2d0824950a7e8324b42c1d1de15ecc9	In addition to hns, other E. coli mutations causing defects in global regulatory proteins, such as fis, crp or @GENE_REGULATOR$ in combination with hns, increased @REGULATED_GENE$ expression to different extents, as did decreasing the growth temperature from 37 degrees C to 30 degrees C. A possible physiological function of ChiA (YheB) endochitinase in E. coli K-12 is discussed.	" + GeneRegCorpusReader.NEGATIVELY_REGULATES,
				"2b877b2e08ee43b0fe619fb277342306f70d59a7	In addition to hns, other E. coli mutations causing defects in global regulatory proteins, such as fis, crp or @REGULATED_GENE$ in combination with hns, increased @GENE_REGULATOR$ expression to different extents, as did decreasing the growth temperature from 37 degrees C to 30 degrees C. A possible physiological function of ChiA (YheB) endochitinase in E. coli K-12 is discussed.	false",
				"1d0b4064029824194750f51a8330a57a553755cd	In addition to hns, other E. coli mutations causing defects in global regulatory proteins, such as fis, crp or stpA in combination with @GENE_REGULATOR$, increased @REGULATED_GENE$ expression to different extents, as did decreasing the growth temperature from 37 degrees C to 30 degrees C. A possible physiological function of ChiA (YheB) endochitinase in E. coli K-12 is discussed.	" + GeneRegCorpusReader.NEGATIVELY_REGULATES,
				"2e2e44638d1131284e9a9a84a3c1e4663e87e975	In addition to hns, other E. coli mutations causing defects in global regulatory proteins, such as fis, crp or stpA in combination with @REGULATED_GENE$, increased @GENE_REGULATOR$ expression to different extents, as did decreasing the growth temperature from 37 degrees C to 30 degrees C. A possible physiological function of ChiA (YheB) endochitinase in E. coli K-12 is discussed.	false",
				"13a918c5c14ddabca26e34604ab40981058521ce	In addition to hns, other E. coli mutations causing defects in global regulatory proteins, such as @GENE_REGULATOR$, crp or stpA in combination with hns, increased @REGULATED_GENE$ expression to different extents, as did decreasing the growth temperature from 37 degrees C to 30 degrees C. A possible physiological function of ChiA (YheB) endochitinase in E. coli K-12 is discussed.	" + GeneRegCorpusReader.NEGATIVELY_REGULATES,
				"83c21d1cc54414d1fc6a153c64d9c440a80cb0de	In addition to hns, other E. coli mutations causing defects in global regulatory proteins, such as @REGULATED_GENE$, crp or stpA in combination with hns, increased @GENE_REGULATOR$ expression to different extents, as did decreasing the growth temperature from 37 degrees C to 30 degrees C. A possible physiological function of ChiA (YheB) endochitinase in E. coli K-12 is discussed.	false",
				"ec3537db5a24a0152f14e4d03a6e2db43296f64a	In addition to @GENE_REGULATOR$, other E. coli mutations causing defects in global regulatory proteins, such as fis, crp or stpA in combination with hns, increased @REGULATED_GENE$ expression to different extents, as did decreasing the growth temperature from 37 degrees C to 30 degrees C. A possible physiological function of ChiA (YheB) endochitinase in E. coli K-12 is discussed.	" + GeneRegCorpusReader.NEGATIVELY_REGULATES,
				"eac2ae7745c490fd06cc77e4009dfe749304dfee	In addition to @REGULATED_GENE$, other E. coli mutations causing defects in global regulatory proteins, such as fis, crp or stpA in combination with hns, increased @GENE_REGULATOR$ expression to different extents, as did decreasing the growth temperature from 37 degrees C to 30 degrees C. A possible physiological function of ChiA (YheB) endochitinase in E. coli K-12 is discussed.	false",
				"d3841f3e9b678d3696d3ac5a5ff43a7c6b27b1a7	In addition to @REGULATED_GENE$, other E. coli mutations causing defects in global regulatory proteins, such as fis, @GENE_REGULATOR$ or stpA in combination with hns, increased chiA expression to different extents, as did decreasing the growth temperature from 37 degrees C to 30 degrees C. A possible physiological function of ChiA (YheB) endochitinase in E. coli K-12 is discussed.	false",
				"a3f823291476d99371c4f38f3ad6124cdf60c331	In addition to @GENE_REGULATOR$, other E. coli mutations causing defects in global regulatory proteins, such as fis, @REGULATED_GENE$ or stpA in combination with hns, increased chiA expression to different extents, as did decreasing the growth temperature from 37 degrees C to 30 degrees C. A possible physiological function of ChiA (YheB) endochitinase in E. coli K-12 is discussed.	false",
				"01641343b053f53c9352ecb75c70077c36aa5e9b	In addition to hns, other E. coli mutations causing defects in global regulatory proteins, such as fis, @GENE_REGULATOR$ or stpA in combination with @REGULATED_GENE$, increased chiA expression to different extents, as did decreasing the growth temperature from 37 degrees C to 30 degrees C. A possible physiological function of ChiA (YheB) endochitinase in E. coli K-12 is discussed.	false",
				"9127ddc62e05963f1eea2694a5d2d18fd1f4d451	In addition to hns, other E. coli mutations causing defects in global regulatory proteins, such as fis, @REGULATED_GENE$ or stpA in combination with @GENE_REGULATOR$, increased chiA expression to different extents, as did decreasing the growth temperature from 37 degrees C to 30 degrees C. A possible physiological function of ChiA (YheB) endochitinase in E. coli K-12 is discussed.	false",
				"654707eb508aede40cadce5fb0e7d85a00d25877	In addition to hns, other E. coli mutations causing defects in global regulatory proteins, such as @REGULATED_GENE$, @GENE_REGULATOR$ or stpA in combination with hns, increased chiA expression to different extents, as did decreasing the growth temperature from 37 degrees C to 30 degrees C. A possible physiological function of ChiA (YheB) endochitinase in E. coli K-12 is discussed.	false",
				"83a0c19d95c79c3f8d245f10027571d92e7e2d22	In addition to hns, other E. coli mutations causing defects in global regulatory proteins, such as @GENE_REGULATOR$, @REGULATED_GENE$ or stpA in combination with hns, increased chiA expression to different extents, as did decreasing the growth temperature from 37 degrees C to 30 degrees C. A possible physiological function of ChiA (YheB) endochitinase in E. coli K-12 is discussed.	false",
				"a7339a69fbf89b22e90b97df13ba13ee39c3deea	In addition to hns, other E. coli mutations causing defects in global regulatory proteins, such as fis, @GENE_REGULATOR$ or @REGULATED_GENE$ in combination with hns, increased chiA expression to different extents, as did decreasing the growth temperature from 37 degrees C to 30 degrees C. A possible physiological function of ChiA (YheB) endochitinase in E. coli K-12 is discussed.	false",
				"0b3cb851565edd6e2a8a3bf06d9df6483afc5f39	In addition to hns, other E. coli mutations causing defects in global regulatory proteins, such as fis, @REGULATED_GENE$ or @GENE_REGULATOR$ in combination with hns, increased chiA expression to different extents, as did decreasing the growth temperature from 37 degrees C to 30 degrees C. A possible physiological function of ChiA (YheB) endochitinase in E. coli K-12 is discussed.	false",
				"4efad47953feedd8ce8c78b3373628d211e190ae	In addition to @REGULATED_GENE$, other E. coli mutations causing defects in global regulatory proteins, such as fis, crp or stpA in combination with @GENE_REGULATOR$, increased chiA expression to different extents, as did decreasing the growth temperature from 37 degrees C to 30 degrees C. A possible physiological function of ChiA (YheB) endochitinase in E. coli K-12 is discussed.	false",
				"093119e0d34f6e3c389d3bde979f5499d05874a1	In addition to @GENE_REGULATOR$, other E. coli mutations causing defects in global regulatory proteins, such as fis, crp or stpA in combination with @REGULATED_GENE$, increased chiA expression to different extents, as did decreasing the growth temperature from 37 degrees C to 30 degrees C. A possible physiological function of ChiA (YheB) endochitinase in E. coli K-12 is discussed.	false",
				"4006a648d8e8f0f6f984302c425fd506ecbd87b4	In addition to hns, other E. coli mutations causing defects in global regulatory proteins, such as @REGULATED_GENE$, crp or stpA in combination with @GENE_REGULATOR$, increased chiA expression to different extents, as did decreasing the growth temperature from 37 degrees C to 30 degrees C. A possible physiological function of ChiA (YheB) endochitinase in E. coli K-12 is discussed.	false",
				"6f4ebc1443ed0ce08fd8d03a5c05bc97f5db3532	In addition to hns, other E. coli mutations causing defects in global regulatory proteins, such as @GENE_REGULATOR$, crp or stpA in combination with @REGULATED_GENE$, increased chiA expression to different extents, as did decreasing the growth temperature from 37 degrees C to 30 degrees C. A possible physiological function of ChiA (YheB) endochitinase in E. coli K-12 is discussed.	false",
				"55ce16e8b179f13e4db57541911c23053e2ef390	In addition to @REGULATED_GENE$, other E. coli mutations causing defects in global regulatory proteins, such as @GENE_REGULATOR$, crp or stpA in combination with hns, increased chiA expression to different extents, as did decreasing the growth temperature from 37 degrees C to 30 degrees C. A possible physiological function of ChiA (YheB) endochitinase in E. coli K-12 is discussed.	false",
				"ec7df2a9c214c9c963ac66d99ebd7caef5fcdedf	In addition to @GENE_REGULATOR$, other E. coli mutations causing defects in global regulatory proteins, such as @REGULATED_GENE$, crp or stpA in combination with hns, increased chiA expression to different extents, as did decreasing the growth temperature from 37 degrees C to 30 degrees C. A possible physiological function of ChiA (YheB) endochitinase in E. coli K-12 is discussed.	false",
				"475e20a331101dc123fa81c204cc7bb7af9ba802	In addition to hns, other E. coli mutations causing defects in global regulatory proteins, such as @REGULATED_GENE$, crp or @GENE_REGULATOR$ in combination with hns, increased chiA expression to different extents, as did decreasing the growth temperature from 37 degrees C to 30 degrees C. A possible physiological function of ChiA (YheB) endochitinase in E. coli K-12 is discussed.	false",
				"d867c3d9d34d0eb583ed8ba58361d5325b368e10	In addition to hns, other E. coli mutations causing defects in global regulatory proteins, such as @GENE_REGULATOR$, crp or @REGULATED_GENE$ in combination with hns, increased chiA expression to different extents, as did decreasing the growth temperature from 37 degrees C to 30 degrees C. A possible physiological function of ChiA (YheB) endochitinase in E. coli K-12 is discussed.	false",
				"ef50b2a115772c8d33b0723710074a536299b90c	In addition to @REGULATED_GENE$, other E. coli mutations causing defects in global regulatory proteins, such as fis, crp or @GENE_REGULATOR$ in combination with hns, increased chiA expression to different extents, as did decreasing the growth temperature from 37 degrees C to 30 degrees C. A possible physiological function of ChiA (YheB) endochitinase in E. coli K-12 is discussed.	false",
				"89ac88fd6404dd9440166b52ed60cf26de5b7964	In addition to @GENE_REGULATOR$, other E. coli mutations causing defects in global regulatory proteins, such as fis, crp or @REGULATED_GENE$ in combination with hns, increased chiA expression to different extents, as did decreasing the growth temperature from 37 degrees C to 30 degrees C. A possible physiological function of ChiA (YheB) endochitinase in E. coli K-12 is discussed.	false",
				"83eacce46fcfa9be8cd55ac66913b5d44108e23e	In addition to hns, other E. coli mutations causing defects in global regulatory proteins, such as fis, crp or @GENE_REGULATOR$ in combination with @REGULATED_GENE$, increased chiA expression to different extents, as did decreasing the growth temperature from 37 degrees C to 30 degrees C. A possible physiological function of ChiA (YheB) endochitinase in E. coli K-12 is discussed.	false",
				"f4ba5023e5fa96b87726c7295a8c2dacba4fa419	In addition to hns, other E. coli mutations causing defects in global regulatory proteins, such as fis, crp or @REGULATED_GENE$ in combination with @GENE_REGULATOR$, increased chiA expression to different extents, as did decreasing the growth temperature from 37 degrees C to 30 degrees C. A possible physiological function of ChiA (YheB) endochitinase in E. coli K-12 is discussed.	false");

		assertTrue(FileComparisonUtil.hasExpectedLines(outputFile, CharacterEncoding.UTF_8, expectedTrainingExamples,
				"\\t", LineOrder.ANY_ORDER, ColumnOrder.AS_IN_FILE));

	}

}
