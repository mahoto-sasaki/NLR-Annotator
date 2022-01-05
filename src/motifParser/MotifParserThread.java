package motifParser;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.concurrent.Callable;

import core.Motif;
import core.MotifList;
import core.SignatureDefinition;
import support.BioSequence;
import support.FastaReader;

public class MotifParserThread implements Callable<File>{

	
	MotifDefinition motifDefinition;
	int min_sequence_length;
	SignatureDefinition signatureDefinition;
	File inputFile;
	File outputFile;
	
	public MotifParserThread(MotifDefinition motifDefinition,SignatureDefinition signatureDefinition, File inputFile, File outputFile) {
		
		
		this.motifDefinition = motifDefinition;
		this.signatureDefinition = signatureDefinition;
		this.inputFile = inputFile;
		this.outputFile = outputFile;
		
	}
	
	
	
	
	public File call()throws IOException{
		BufferedWriter out = new BufferedWriter(new FileWriter(outputFile));

		
		FastaReader fastaReader = new FastaReader(inputFile);
		for (BioSequence seq = fastaReader.readEntry(); seq != null; seq = fastaReader.readEntry()) {
			
			boolean isDNA = false;
			
			BioSequence[] seqs;
			if( seq.isDNA()) {
				isDNA=true;
				seqs = seq.translate2Protein();
			}else {
				seqs = new BioSequence[1];
				seqs[0]=seq;
			}
			
			for( int i = 0;i< seqs.length; i++) {
				BioSequence proteinSequence = seqs[i];
				MotifParser motifParser = new MotifParser(this.motifDefinition, proteinSequence);
				MotifList motifList = motifParser.findMotifs();
				
				
					
				if( motifList != null && motifList.has_NLR_signature(this.signatureDefinition)) {
					for(Iterator<Motif > iterator = motifList.getMotifs().iterator(); iterator.hasNext();) {
						Motif motif = iterator.next();
						
						if(isDNA) {
							String[] split = proteinSequence.getIdentifier().split("_");
							int l = split.length -1;
							
							
							String strand = split[l].substring(5, 6);
							boolean forwardStrand = true;
							if( strand.equalsIgnoreCase("-")) {
								forwardStrand = false;
							}
							int frame = Integer.parseInt(split[l].substring(6));
							int offset = Integer.parseInt(split[l-1]);
							
							String id = split[0];
							for( int j = 1; j< l-1; j++) { //in case id has an underscore in name
								id = id + "_" + split[j];
								
							}
							motif.setDNA(id, offset, seq.getLength(), frame, forwardStrand);
						}
						
						
						
						out.write(motif.getExportString());
						out.newLine();
					}
					
				}
			}
			
			
			
		
		}
		fastaReader.close();
		out.close();
		
		
		return outputFile;
	}
	
	
	
}
