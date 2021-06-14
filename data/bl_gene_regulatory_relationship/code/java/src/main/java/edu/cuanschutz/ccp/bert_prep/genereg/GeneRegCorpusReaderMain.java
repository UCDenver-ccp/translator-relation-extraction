package edu.cuanschutz.ccp.bert_prep.genereg;

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.io.IOUtils;

import edu.ucdenver.ccp.common.file.CharacterEncoding;
import edu.ucdenver.ccp.common.file.FileUtil;
import edu.ucdenver.ccp.common.file.FileWriterUtil;
import edu.ucdenver.ccp.common.file.FileWriterUtil.FileSuffixEnforcement;
import edu.ucdenver.ccp.common.file.FileWriterUtil.WriteMode;
import edu.ucdenver.ccp.nlp.core.annotation.TextAnnotation;

public class GeneRegCorpusReaderMain {

	/**
	 * @param args: <br>
	 *              args[0] = the GeneReg_V_1.0 directory<br>
	 *              args[1] = output file <br>
	 *              args[2] = WriteMode (APPEND or OVERWRITE)
	 */
	public static void main(String[] args) {
		File dir = new File(args[0]);
		File outputFile = new File(args[1]);
		WriteMode writeMode = WriteMode.valueOf(args[2]);
		try {
			try (BufferedWriter writer = FileWriterUtil.initBufferedWriter(outputFile, CharacterEncoding.UTF_8,
					writeMode, FileSuffixEnforcement.OFF)) {
				for (Iterator<File> fileIterator = FileUtil.getFileIterator(dir, false, ".txt"); fileIterator
						.hasNext();) {
					File txtFile = fileIterator.next();

					String id = txtFile.getName().split("\\.")[0];
					File a1File = new File(dir, id + ".a1");
					File a2File = new File(dir, id + ".a2");

					String a1Str = IOUtils.toString(new FileInputStream(a1File),
							CharacterEncoding.UTF_8.getCharacterSetName());
					String a2Str = IOUtils.toString(new FileInputStream(a2File),
							CharacterEncoding.UTF_8.getCharacterSetName());

					String a12Str = a1Str + "\n" + a2Str;

					List<TextAnnotation> entityAnnots = GeneRegCorpusReader.getAnnotations(id,
							new FileInputStream(txtFile), new ByteArrayInputStream(a12Str.getBytes()));
					List<TextAnnotation> sentences = GeneRegCorpusReader.getSentences(new FileInputStream(txtFile));
					GeneRegCorpusReader.generateBertStyleTrainingData(sentences, entityAnnots, writer);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(-1);
		}
	}
}
