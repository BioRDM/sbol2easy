/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ed.biordm.sbol.toolkit.scrapbook;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.HashSet;

import javax.xml.namespace.QName;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.stream.XMLStreamException;
import org.sbolstandard.core.io.CoreIoException;

import org.sbolstandard.core2.AccessType;
import org.sbolstandard.core2.Component;
import org.sbolstandard.core2.ComponentDefinition;
import org.sbolstandard.core2.DirectionType;
import org.sbolstandard.core2.GenericTopLevel;
import org.sbolstandard.core2.Interaction;
import org.sbolstandard.core2.Module;
import org.sbolstandard.core2.ModuleDefinition;
import org.sbolstandard.core2.OrientationType;
import org.sbolstandard.core2.RefinementType;
import org.sbolstandard.core2.RestrictionType;
import org.sbolstandard.core2.SBOLConversionException;
import org.sbolstandard.core2.SBOLDocument;
import org.sbolstandard.core2.SBOLReader;
import org.sbolstandard.core2.SBOLValidate;
import org.sbolstandard.core2.SBOLValidationException;
import org.sbolstandard.core2.SBOLWriter;
import org.sbolstandard.core2.Sequence;
import org.sbolstandard.core2.SequenceAnnotation;
import org.sbolstandard.core2.SequenceOntology;
import org.sbolstandard.core2.SystemsBiologyOntology;

/*
 * Plasmid template
 * 
 * @author tzielins
 * 
 */

public class CyanoTemplates {

    static String SeqenceOntoPref = "http://identifiers.org/so/";
    
	public static void main(String[] args) throws SBOLValidationException, SBOLConversionException, IOException, URISyntaxException {
		
		SBOLDocument doc = new SBOLDocument();

		doc.setDefaultURIprefix("http://bio.ed.ac.uk/a_mccormick/cyano_source/");
		doc.setComplete(true);
		doc.setCreateDefaults(true);
                
		
		String version = "1.0.0";
                
                ComponentDefinition insert = createCodAInsert(doc, version);
		
                ComponentDefinition ampROrg = createAmpROrg(doc, version);

                ComponentDefinition endGap = createEndGap(doc, version);
                
                ComponentDefinition leftFlank = createLeftFlank(doc, version);

                ComponentDefinition rightFlank = createRightFlank(doc, version);
                
                ComponentDefinition plasmid = createTemplatePlasmid(doc, version, ampROrg, leftFlank, insert, rightFlank, endGap);
                
                ComponentDefinition sll00199Plasmid = createSll00199Plasmid(doc, version, ampROrg, leftFlank, insert, rightFlank, endGap);
                
                ComponentDefinition sll00199PlasmidFlat = createSll00199PlasmidFlat(doc, version, ampROrg, leftFlank, insert, rightFlank, endGap);
                
                String fName = "cyano_full_template";
                SBOLValidate.validateSBOL(doc, true, true, true);
		if (SBOLValidate.getNumErrors() > 0) {
                    for (String error : SBOLValidate.getErrors()) {
                            System.out.println(error);
                    }
                    throw new IllegalStateException("Stoping cause of validation errors");
		}
                
		try {
			SBOLWriter.write(doc, "E:/Temp/"+fName+".xml");
                } catch (IOException e) {
			e.printStackTrace();
                        throw e;
		}
		
	}

    static ComponentDefinition createCodAInsert(SBOLDocument doc, String version) throws SBOLValidationException, URISyntaxException {
        
        String name = "codA_Km";
        ComponentDefinition region = doc.createComponentDefinition(name, version, ComponentDefinition.DNA_REGION);
        region.setName("codA Km");
        //region.addRole(SequenceOntology.ENGINEERED_REGION);
        // engineered insert
        region.addRole(new URI(SeqenceOntoPref+"SO:0000915"));
        
        String seqStr = "ATGaGAAGAGCACGGTAGCCTTNNNNNNNNNNNNNNNNNNTGCCCAGTCTTCTGCCTAAGGCAGGTGCCGCGGTGCGGGTGCCAGGGCGTGCCCCCGGGCTCCCCGGGCGCGTACTCCACtttacagctagctcagtcctaggtattatgctagctattgtgagcggataacaatttcacacatactagagaaagaggagaaatactaaATGTCTAACAACGCGCTGCAAACCATCATCAATGCACGCCTGCCTGGAGAGGAAGGGTTGTGGCAGATTCACTTACAGGACGGCAAAATCTCCGCGATCGACGCACAATCTGGGGTTATGCCGATCACCGAAAACTCTTTGGATGCCGAACAAGGGTTAGTCATTCCCCCATTCGTTGAACCACATATTCACCTGGATACTACTCAGACAGCCGGTCAGCCCAATTGGAACCAGTCCGGTACGCTGTTCGAAGGTATCGAACGATGGGCGGAGCGAAAAGCTCTACTCACGCATGACGATGTCAAGCAACGGGCCTGGCAGACCCTGAAGTGGCAGATCGCCAACGGAATACAGCACGTACGCACTCACGTGGATGTTTCCGATGCCACTTTGACGGCATTGAAGGCAATGCTCGAAGTTAAGCAGGAAGTAGCCCCGTGGATTGACTTGCAAATCGCTGCCTTCCCTCAGGAAGGCATCCTAAGTTATCCGAATGGAGAAGCGCTCCTGGAGGAGGCATTGCGGTTAGGAGCAGACGTGGTGGGAGCGATTCCCCATTTCGAGTTTACCCGCGAGTACGGTGTTGAATCTCTGCATAAAACATTTGCTTTAGCTCAGAAGTATGACCGTCTGATCGACGTACACTGCGACGAGATCGATGACGAACAGAGTCGCTTCGTGGAGACGGTGGCTGCGCTGGCGCATCACGAAGGCATGGGTGCACGTGTAACTGCAAGCCATACGACGGCTATGCACAGCTATAATGGGGCATATACATCTCGTTTGTTCCGATTACTAAAAATGAGCGGAATCAACTTTGTTGCCAATCCATTGGTCAACATTCATCTACAAGGACGCTTCGACACCTACCCGAAACGGCGAGGAATCACACGAGTTAAGGAAATGCTAGAGTCTGGTATCAATGTGTGTTTCGGGCATGATGACGTGTGTGGTCCCTGGTACCCTCTAGGAACAGCCAACATGCTGCAAGTTCTCCACATGGGTCTACACGTGTGTCAACTCATGGGGTATGGACAAATTAACGATGGACTCAATCTAATTACACACCATTCCGCCCGAACACTGAACCTCCAGGATTACGGGATCGCGGCGGGAAATTCTGCCAACCTCATCATTCTGCCCGCGGAAAACGGGTTCGACGCTCTACGCCGTCAAGTGCCAGTTCGGTATTCTGTTCGTGGGGGTAAGGTAATTGCAAGTACCCAACCGGCTCAGACCACGGTCTATTTAGAGCAACCGGAAGCTATCGACTACAAACGATGAgcttcaaataaaacgaaaggctcagtcgaaagactgggcctttcgttttatctgttgtttgtcggtgaacgctctctactagagtcacactggctcaccttcgggtgggcctttctgcgcgctCTGAGGTCTGCCTCGTGAAGAAGGTGTTGCTGACTCATACCAGGCCTGAATCGCCCCATCATCCAGCCAGAAAGTGAGGGAGCCACGGTTGATGAGAGCTTTGTTGTAGGTGGACCAGTTGGTGATTTTGAACTTTTGCTTTGCCACGGAACGGTCTGCGTTGTCGGGAAGATGCGTGATCTGATCCTTCAACTCAGCAAAAGTTCGATTTATTCAACAAAGCCGCCGTCCCGTCAAGTCAGCGTAATGCTCTGCCAGTGTTACAACCAATTAACCAATTCTGATTAGAAAAACTCATCGAGCATCAAATGAAACTGCAATTTATTCATATCAGGATTATCAATACCATATTTTTGAAAAAGCCGTTTCTGTAATGAAGGAGAAAACTCACCGAGGCAGTTCCATAGGATGGCAAGATCCTGGTATCGGTCTGCGATTCCGACTCGTCCAACATCAATACAACCTATTAATTTCCCCTCGTCAAAAATAAGGTTATCAAGTGAGAAATCACCATGAGTGACGACTGAATCCGGTGAGAATGGCAAAAGCTTATGCATTTCTTTCCAGACTTGTTCAACAGGCCAGCCATTACGCTCGTCATCAAAATCACTCGCATCAACCAAACCGTTATTCATTCGTGATTGCGCCTGAGCGAGACGAAATACGCGATCGCTGTTAAAAGGACAATTACAAACAGGAATCGAATGCAACCGGCGCAGGAACACTGCCAGCGCATCAACAATATTTTCACCTGAATCAGGATATTCTTCTAATACCTGGAATGCTGTTTTCCCGGGGATCGCAGTGGTGAGTAACCATGCATCATCAGGAGTACGGATAAAATGCTTGATGGTCGGAAGAGGCATAAATTCCGTCAGCCAGTTTAGTCTGACCATCTCATCTGTAACATCATTGGCAACGCTACCTTTGCCATGTTTCAGAAACAACTCTGGCGCATCGGGCTTCCCATACAATCGATAGATTGTCGCACCTGATTGCCCGACATTATCGCGAGCCCATTTATACCCATATAAATCAGCATCCATGTTGGAATTTAATCGCGGCCTCGAGCAAGACGTTTCCCGTTGAATATGGCTCATAACACCCCTTGTATTACTGTTTATGTAAGCAGACAGTTTTATTGTTCATGATGATATATTTTTATCTTGTGCAATGTAACATCAGAGATTTTGAGACACAACGTGGCTTTCCGCGGTGCGGGTGCCAGGGCGTGCCCTTGGGCTCCCCGGGCGCGTACTCCACCACCTGCCATTGGGAGAAGACTTGGGAGCTCTTCataa";
        Sequence seq = doc.createSequence(name+"_seq", version, seqStr, Sequence.IUPAC_DNA);
        region.addSequence(seq);

        final int SS = 2573;
        SequenceAnnotation an = region.createSequenceAnnotation("SapI_ATG_over", "SapI_ATG_over", 2574-SS, 2584-SS);
        //an.addRole(new URI(SeqenceOntoPref+"SO:0001695"));
        an.addRole(new URI(SeqenceOntoPref+"SO:0001933"));
        an.setName("SapI-ATG overhang");
    
        
        an = region.createSequenceAnnotation("barcode", "barcode", 2596-SS, 2613-SS);
        an.addRole(new URI(SeqenceOntoPref+"SO:0000730"));
        
        an = region.createSequenceAnnotation("BpiI_TGCC_over", "BpiI_TGCC_over", 2614-SS, 2625-SS);
        //an.addRole(new URI(SeqenceOntoPref+"SO:0001695"));
        an.addRole(new URI(SeqenceOntoPref+"SO:0001933"));
        an.setName("BpiI-TGCC overhang");
        
        an = region.createSequenceAnnotation("AarI_TGCC_over", "AarI_TGCC_over", 2626-SS, 2640-SS);
        //an.addRole(new URI(SeqenceOntoPref+"SO:0001695"));
        an.addRole(new URI(SeqenceOntoPref+"SO:0001933"));
        an.setName("AarI-TGCC overhang");
        
        an = region.createSequenceAnnotation("attB_CC", "attB_CC", 2641-SS, 2693-SS);
        an.addRole(new URI(SeqenceOntoPref+"SO:0000001"));
        an.setName("attB CC");
        
        an = region.createSequenceAnnotation("J23101MH_prom", "J23101MH_prom", 2694-SS, 2782-SS);
        an.addRole(SequenceOntology.PROMOTER); 
        an.setName("J23101MH prom");
        an.createAnnotation(new QName("http://sbols.org/genBankConversion#", "note","gbconv"), 
                "pC0.031  \nhttps://doi.org/10.1104/pp.18.01401");
        
        an = region.createSequenceAnnotation("codA", "codA", 2783-SS, 4066-SS);
        an.addRole(SequenceOntology.CDS);
        an.createAnnotation(new QName("http://sbols.org/genBankConversion#", "product","gbconv"), 
                "Codon optimised (V153A, F317C)  \ndoi:10.1111/tpj.12675");
        
        an = region.createSequenceAnnotation("rrnBT1_T7_term", "rrnBT1_T7_term", 4071-SS, 4185-SS);
        an.addRole(SequenceOntology.TERMINATOR);
        an.setName("rrnBT1/T7 term");
        an.createAnnotation(new QName("http://sbols.org/genBankConversion#", "note","gbconv"), 
                "BBa_B0015 double terminator (B0010-B0012)  \npC0.082  \nhttps://doi.org/10.1104/pp.18.01401");
        
        an = region.createSequenceAnnotation("KanR_term", "KanR_term", 4190-SS, 4473-SS, OrientationType.REVERSECOMPLEMENT);
        an.addRole(SequenceOntology.TERMINATOR);
        an.setName("KanR term");
        
        an = region.createSequenceAnnotation("KanR", "KanR", 4474-SS, 5289-SS, OrientationType.REVERSECOMPLEMENT);
        an.addRole(SequenceOntology.CDS); 
        an.setDescription("Gene: aph(3')-Ia\n aminoglycoside phosphotransferase confers resistance to kanamycin in bacteria or G418 (Geneticin(R)) in eukaryotes");
        
        an.createAnnotation(new QName("http://sbols.org/genBankConversion#", "note","gbconv"), 
                "confers resistance to kanamycin in bacteria or G418 (Geneticin(R)) in eukaryotes");
        an.createAnnotation(new QName("http://sbols.org/genBankConversion#", "gene","gbconv"), 
                "aph(3')-Ia");
        an.createAnnotation(new QName("http://sbols.org/genBankConversion#", "product","gbconv"), 
                "aminoglycoside phosphotransferase");

        an = region.createSequenceAnnotation("KanR_prom", "KanR_prom", 5290-SS, 5399-SS, OrientationType.REVERSECOMPLEMENT);
        an.addRole(SequenceOntology.PROMOTER);
        an.setName("KanR prom");
        
        an = region.createSequenceAnnotation("attB_TT", "attB_TT", 5400-SS, 5452-SS);
        an.addRole(new URI(SeqenceOntoPref+"SO:0000001"));
        an.setName("attB TT");
        an.createAnnotation(new QName("http://sbols.org/genBankConversion#", "note","gbconv"), 
                "https://doi.org/10.1002/bit.26854");

        an = region.createSequenceAnnotation("AarI_GGGA_over", "AarI_GGGA_over", 5453-SS, 5467-SS);
        //an.addRole(new URI(SeqenceOntoPref+"SO:0001695"));
        an.addRole(new URI(SeqenceOntoPref+"SO:0001933"));
        an.setName("AarI-GGGA overhang");

        an = region.createSequenceAnnotation("BpiI_GGGA_over", "BpiI_GGGA_over", 5468-SS, 5479-SS);
        //an.addRole(new URI(SeqenceOntoPref+"SO:0001695"));
        an.addRole(new URI(SeqenceOntoPref+"SO:0001933"));
        an.setName("BpiI-GGGA overhang");

        an = region.createSequenceAnnotation("SapI_TAA_over", "SapI_TAA_over", 5480-SS, 5490-SS);
        //an.addRole(new URI(SeqenceOntoPref+"SO:0001695"));
        an.addRole(new URI(SeqenceOntoPref+"SO:0001933"));
        an.setName("SapI-TAA overhang");
        
        
        return region;
    }

    static ComponentDefinition createAmpROrg(SBOLDocument doc, String version) throws SBOLValidationException, URISyntaxException {

        String name = "ampr_origin";
        ComponentDefinition region = doc.createComponentDefinition(name, version, ComponentDefinition.DNA_REGION);
        region.addRole(SequenceOntology.ENGINEERED_REGION);
        
        String seqStr = "AAAGGGCCTCGTGATACGCCTATTTTTATAGGTTAATGTCATGATAATAATGGTTTCTTAGACGTCAGGTGGCACTTTTCGGGGAAATGTGCGCGGAACCCCTATTTGTTTATTTTTCTAAATACATTCAAATATGTATCCGCTCATGAGACAATAACCCTGATAAATGCTTCAATAATATTGAAAAAGGAAGAGTATGAGTATTCAACATTTCCGTGTCGCCCTTATTCCCTTTTTTGCGGCATTTTGCCTTCCTGTTTTTGCTCACCCAGAAACGCTGGTGAAAGTAAAAGATGCTGAAGATCAGTTGGGTGCACGAGTGGGTTACATCGAACTGGATCTCAACAGCGGTAAGATCCTTGAGAGTTTTCGCCCCGAAGAACGTTTTCCAATGATGAGCACTTTTAAAGTTCTGCTATGTGGCGCGGTATTATCCCGTATTGACGCCGGGCAAGAGCAACTCGGTCGCCGCATACACTATTCTCAGAATGACTTGGTTGAGTACTCACCAGTCACAGAAAAGCATCTTACGGATGGCATGACAGTAAGAGAATTATGCAGTGCTGCCATAACCATGAGTGATAACACTGCGGCCAACTTACTTCTGACAACGATCGGAGGACCGAAGGAGCTAACCGCTTTTTTGCACAACATGGGGGATCATGTAACTCGCCTTGATCGTTGGGAACCGGAGCTGAATGAAGCCATACCAAACGACGAGCGTGACACCACGATGCCTGTAGCAATGGCAACAACGTTGCGCAAACTATTAACTGGCGAACTACTTACTCTAGCTTCCCGGCAACAATTAATAGACTGGATGGAGGCGGATAAAGTTGCAGGACCACTTCTGCGCTCGGCCCTTCCGGCTGGCTGGTTTATTGCTGATAAATCTGGAGCCGGTGAGCGTGGTTCTCGCGGTATCATTGCAGCACTGGGGCCAGATGGTAAGCCCTCCCGTATCGTAGTTATCTACACGACGGGGAGTCAGGCAACTATGGATGAACGAAATAGACAGATCGCTGAGATAGGTGCCTCACTGATTAAGCATTGGTAACTGTCAGACCAAGTTTACTCATATATACTTTAGATTGATTTAAAACTTCATTTTTAATTTAAAAGGATCTAGGTGAAGATCCTTTTTGATAATCTCATGACCAAAATCCCTTAACGTGAGTTTTCGTTCCACTGAGCGTCAGACCCCGTAGAAAAGATCAAAGGATCTTCTTGAGATCCTTTTTTTCTGCGCGTAATCTGCTGCTTGCAAACAAAAAAACCACCGCTACCAGCGGTGGTTTGTTTGCCGGATCAAGAGCTACCAACTCTTTTTCCGAAGGTAACTGGCTTCAGCAGAGCGCAGATACCAAATACTGTTCTTCTAGTGTAGCCGTAGTTAGGCCACCACTTCAAGAACTCTGTAGCACCGCCTACATACCTCGCTCTGCTAATCCTGTTACCAGTGGCTGCTGCCAGTGGCGATAAGTCGTGTCTTACCGGGTTGGACTCAAGACGATAGTTACCGGATAAGGCGCAGCGGTCGGGCTGAACGGGGGGTTCGTGCACACAGCCCAGCTTGGAGCGAACGACCTACACCGAACTGAGATACCTACAGCGTGAGCTATGAGAAAGCGCCACGCTTCCCGAAGGGAGAAAGGCGGACAGGTATCCGGTAAGCGGCAGGGTCGGAACAGGAGAGCGCACGAGGGAGCTTCCAGGGGGAAACGCCTGGTATCTTTATAGTCCTGTCGGGTTTCGCCACCTCTGACTTGAGCGTCGATTTTTGTGATGCTCGTCAGGGGGGCGGAGCCTATGGAAAAACGCCAGCAACGCGGCCTTTTTACGGTTCCTGGCCTTTTGCTGGCCTTTTGCTCACATGTTCTTTCCTGCGTTATCCCCTGATTCTGTGGATAACCGTATTACCGCCTTTGAGTGAGCTGATACCGCTCGCCGCAGCCGAACGACCGAGCGCAGCGAGTCAGTGAGCGAGGAAGCGGATGAGCGCCCAATACGCAAACCGCCTCTCCCCGCGCGTTGGCCGATTCATTAATGCAGCTGGCACGACAGGTTTCggag";
        Sequence seq = doc.createSequence(name+"_seq", version, seqStr, Sequence.IUPAC_DNA);
        region.addSequence(seq);
        
        SequenceAnnotation an = region.createSequenceAnnotation("AmpR_prom", "AmpR_prom", 92, 196);
        an.addRole(SequenceOntology.PROMOTER);
        an.createAnnotation(new QName("http://sbols.org/genBankConversion#", "gene","gbconv"), 
                "bla");
        
        an = region.createSequenceAnnotation("AmpR", "AmpR", 197, 1057);
        an.addRole(SequenceOntology.CDS);    
        an.createAnnotation(new QName("http://sbols.org/genBankConversion#", "note","gbconv"), 
                "confers resistance to ampicillin, carbenicillin, and related antibiotics");
        an.createAnnotation(new QName("http://sbols.org/genBankConversion#", "gene","gbconv"), 
                "bla");
        an.createAnnotation(new QName("http://sbols.org/genBankConversion#", "product","gbconv"), 
                "beta-lactamase");        
        
        ComponentDefinition originD = doc.createComponentDefinition("ori", version, ComponentDefinition.DNA_REGION);
        originD.addRole(SequenceOntology.ORIGIN_OF_REPLICATION);
        originD.createAnnotation(new QName("http://sbols.org/genBankConversion#", "note","gbconv"), 
                "high-copy-number ColE1/pMB1/pBR322/pUC origin of replication");        
        
        //Component origin = region.createComponent("ori_instance", AccessType.PUBLIC, originD.getIdentity());
        
        an = region.createSequenceAnnotation("ori", "ori", 1228, 1816);
        //an.setComponent(origin.getIdentity());
        return region;
    }

    static ComponentDefinition createEndGap(SBOLDocument doc, String version) throws SBOLValidationException, URISyntaxException {

        String name = "gap";
        ComponentDefinition region = doc.createComponentDefinition(name, version, ComponentDefinition.DNA_REGION);
        region.addRole(SequenceOntology.ENGINEERED_REGION);
        
        String seqStr = "CGCTGCTTACAGACAAGCTGTGACCGTCTCCGGGAGCTGCATGTGTCAGAGGTTTTCACCGTCATCACCGAAACGCGCGAGACG";
        Sequence seq = doc.createSequence(name+"_seq", version, seqStr, Sequence.IUPAC_DNA);
        region.addSequence(seq);

        return region;
    }

    static ComponentDefinition createLeftFlank(SBOLDocument doc, String version) throws SBOLValidationException, URISyntaxException {

        String name = "left_flank";
        ComponentDefinition region = doc.createComponentDefinition(name, version, ComponentDefinition.DNA_REGION);
        //region.addRole(SequenceOntology.ENGINEERED_REGION);
        // recombination feature
        region.addRole(new URI(SeqenceOntoPref+"SO:0000298"));
        
        return region;
    }

    static ComponentDefinition createRightFlank(SBOLDocument doc, String version) throws SBOLValidationException, URISyntaxException {

        String name = "right_flank";
        ComponentDefinition region = doc.createComponentDefinition(name, version, ComponentDefinition.DNA_REGION);
        //region.addRole(SequenceOntology.ENGINEERED_REGION);
        // recombination feature
        region.addRole(new URI(SeqenceOntoPref+"SO:0000298"));
        //region.addRole(OrientationType.REVERSECOMPLEMENT);
        return region;
    }

    static ComponentDefinition createTemplatePlasmid(SBOLDocument doc, String version, 
            ComponentDefinition ampROrg, 
            ComponentDefinition leftFlank, 
            ComponentDefinition insert, 
            ComponentDefinition rightFlank, 
            ComponentDefinition endGap) throws SBOLValidationException, URISyntaxException {

        return createTemplatePlasmid(doc, version, ampROrg.getIdentity(), leftFlank.getIdentity(), insert.getIdentity(), 
                rightFlank.getIdentity(), endGap.getIdentity());
        
    }

    static ComponentDefinition createTemplatePlasmid(SBOLDocument doc, String version, 
            URI ampROrg, 
            URI leftFlank, 
            URI insert, 
            URI rightFlank, 
            URI endGap) throws SBOLValidationException, URISyntaxException {

        String name = "cyano_codA_Km";
        ComponentDefinition plasmid = doc.createComponentDefinition(name, version, ComponentDefinition.DNA_REGION);
        plasmid.addType(SequenceOntology.CIRCULAR);
        //engineered plasmid
        plasmid.addRole(new URI(SeqenceOntoPref+"SO:0000637"));
        
        Component aC = plasmid.createComponent("ampR", AccessType.PUBLIC, ampROrg);
        Component lC = plasmid.createComponent("left", AccessType.PUBLIC, leftFlank);
        Component iC = plasmid.createComponent("insert", AccessType.PUBLIC, insert);
        Component rC = plasmid.createComponent("right", AccessType.PUBLIC, rightFlank);
        Component gC = plasmid.createComponent("gap", AccessType.PUBLIC, endGap);

        plasmid.createSequenceConstraint("cs1", RestrictionType.PRECEDES, aC.getIdentity(), lC.getIdentity());
        plasmid.createSequenceConstraint("cs2", RestrictionType.PRECEDES, lC.getIdentity(), iC.getIdentity());
        plasmid.createSequenceConstraint("cs3", RestrictionType.PRECEDES, iC.getIdentity(), rC.getIdentity());
        plasmid.createSequenceConstraint("cs4", RestrictionType.PRECEDES, rC.getIdentity(), gC.getIdentity());
                
        return plasmid;
    }
    
    static ComponentDefinition createSll00199Plasmid(SBOLDocument doc, String version, 
            ComponentDefinition ampROrg, 
            ComponentDefinition leftFlank, 
            ComponentDefinition insert, 
            ComponentDefinition rightFlank, 
            ComponentDefinition endGap) throws SBOLValidationException, URISyntaxException {
        
        String name = "sll00199_codA_Km";
        ComponentDefinition plasmid = doc.createComponentDefinition(name, version, ComponentDefinition.DNA_REGION);
        plasmid.addType(SequenceOntology.CIRCULAR);
        //engineered plasmid
        plasmid.addRole(new URI(SeqenceOntoPref+"SO:0000637"));
        
        Component aC = plasmid.createComponent("ampR", AccessType.PUBLIC, ampROrg.getIdentity());
        
        leftFlank = (ComponentDefinition)doc.createCopy(leftFlank, "sll00199_left", version);
        String seqStr = "caaggcaaaaccaccgttatcagcagaacgacggcgggaaaaaatgattaaacgaaaaaatttgcaaggattcatagcggttgcccaatctaactcagggagcgacttcagcccacaaaaaacaccactgggcctactgggctattcccattatcatctacattgaagggatagcaagctaatttttatgacggcgatcgccaaaaacaaagaaaattcagcaattaccgtgggtagcaaaaaatccccatctaaagttcagtaaatatagctagaacaaccaagcattttcggcaaagtactattcagatagaacgagaaatgagcttgttctatccgcccggggctgaggctgtataatctacgacgggctgtcaaacattgtgataccatgggcagaagaaaggaaaaacgtccctgatcgcctttttgggcacggagtagggcgttaccccggcccgttcaaccacaagtccctatAGATACAATCGCCAAGAAGT";
        Sequence seq = doc.createSequence("sll00199_left_seq", version, seqStr, Sequence.IUPAC_DNA);
        leftFlank.addSequence(seq);
        
        Component lC = plasmid.createComponent("left", AccessType.PUBLIC, leftFlank.getIdentity());
        Component iC = plasmid.createComponent("insert", AccessType.PUBLIC, insert.getIdentity());
        
        rightFlank = (ComponentDefinition)doc.createCopy(rightFlank, "sll00199_right", version);
        seqStr = "tcagccagctcaatctgtgtgtcgttgatttaagcttaatgctacggggtctgtctccaactccctcagcttctcgcaatggcaaggcaaataatgtttctcttgctgagtagatgttcaggaggacggatcgaaagtctacaaaacagattcttgaccaagccatctacttagaaaaacttctgcgttttggcgatcgcatcttttaagcgagatgcgatttttttgtccattagtttgtattttaatactcttttgttgtttgatttcgtccaagcttttcttggtatgtgggatcttccgtgcccaaaattttatcccagaaagtgaaatatagtcatttcaattaacgatgagagaatttaatgtaaaattatggagtgtacaaaatgaacaggtttaaacaatggcttacagtttagatttaaggcaaagggtagtagcttatatagaagctggaggaaaaataactgaggcttccaagatatataaaataggaaaagcctcgatatacagatggttaaatagagtagatttaagcccaacaaaagtagagcgtcgccatagg";
        seq = doc.createSequence("sll00199_right_seq", version, seqStr, Sequence.IUPAC_DNA);
        rightFlank.addSequence(seq);
        
        Component rC = plasmid.createComponent("right", AccessType.PUBLIC, rightFlank.getIdentity());
        Component gC = plasmid.createComponent("gap", AccessType.PUBLIC, endGap.getIdentity());

        plasmid.createSequenceConstraint("cs1", RestrictionType.PRECEDES, aC.getIdentity(), lC.getIdentity());
        plasmid.createSequenceConstraint("cs2", RestrictionType.PRECEDES, lC.getIdentity(), iC.getIdentity());
        plasmid.createSequenceConstraint("cs3", RestrictionType.PRECEDES, iC.getIdentity(), rC.getIdentity());
        plasmid.createSequenceConstraint("cs4", RestrictionType.PRECEDES, rC.getIdentity(), gC.getIdentity());
                
        return plasmid;        
    }
    
    static ComponentDefinition createSll00199PlasmidFlat(SBOLDocument doc, String version, 
            ComponentDefinition ampROrg, 
            ComponentDefinition leftFlank, 
            ComponentDefinition insert, 
            ComponentDefinition rightFlank, 
            ComponentDefinition endGap) throws SBOLValidationException, URISyntaxException {
        
        String name = "sll00199_codA_Km_flat";
        ComponentDefinition plasmid = doc.createComponentDefinition(name, version, ComponentDefinition.DNA_REGION);
        plasmid.addType(SequenceOntology.CIRCULAR);
        //engineered plasmid
        plasmid.addRole(new URI(SeqenceOntoPref+"SO:0000637"));
        
        Component aC = plasmid.createComponent("ampR", AccessType.PUBLIC, ampROrg.getIdentity());
        
        
        if (doc.getComponentDefinition("sll00199_left", version) == null) {
            leftFlank = (ComponentDefinition)doc.createCopy(leftFlank, "sll00199_left", version);
            String seqStr = "caaggcaaaaccaccgttatcagcagaacgacggcgggaaaaaatgattaaacgaaaaaatttgcaaggattcatagcggttgcccaatctaactcagggagcgacttcagcccacaaaaaacaccactgggcctactgggctattcccattatcatctacattgaagggatagcaagctaatttttatgacggcgatcgccaaaaacaaagaaaattcagcaattaccgtgggtagcaaaaaatccccatctaaagttcagtaaatatagctagaacaaccaagcattttcggcaaagtactattcagatagaacgagaaatgagcttgttctatccgcccggggctgaggctgtataatctacgacgggctgtcaaacattgtgataccatgggcagaagaaaggaaaaacgtccctgatcgcctttttgggcacggagtagggcgttaccccggcccgttcaaccacaagtccctatAGATACAATCGCCAAGAAGT";
            Sequence seq = doc.createSequence("sll00199_left_seq", version, seqStr, Sequence.IUPAC_DNA);
            leftFlank.addSequence(seq);
        } else {
            leftFlank = doc.getComponentDefinition("sll00199_left", version);
        }
        
        Component lC = plasmid.createComponent("left", AccessType.PUBLIC, leftFlank.getIdentity());
        Component iC = plasmid.createComponent("insert", AccessType.PUBLIC, insert.getIdentity());
        
        if (doc.getComponentDefinition("sll00199_right", version) == null) {
            rightFlank = (ComponentDefinition)doc.createCopy(rightFlank, "sll00199_right", version);
            String seqStr = "tcagccagctcaatctgtgtgtcgttgatttaagcttaatgctacggggtctgtctccaactccctcagcttctcgcaatggcaaggcaaataatgtttctcttgctgagtagatgttcaggaggacggatcgaaagtctacaaaacagattcttgaccaagccatctacttagaaaaacttctgcgttttggcgatcgcatcttttaagcgagatgcgatttttttgtccattagtttgtattttaatactcttttgttgtttgatttcgtccaagcttttcttggtatgtgggatcttccgtgcccaaaattttatcccagaaagtgaaatatagtcatttcaattaacgatgagagaatttaatgtaaaattatggagtgtacaaaatgaacaggtttaaacaatggcttacagtttagatttaaggcaaagggtagtagcttatatagaagctggaggaaaaataactgaggcttccaagatatataaaataggaaaagcctcgatatacagatggttaaatagagtagatttaagcccaacaaaagtagagcgtcgccatagg";
            Sequence seq = doc.createSequence("sll00199_right_seq", version, seqStr, Sequence.IUPAC_DNA);
            rightFlank.addSequence(seq);
        } else {
            rightFlank = doc.getComponentDefinition("sll00199_right", version);
        }
        
        Component rC = plasmid.createComponent("right", AccessType.PUBLIC, rightFlank.getIdentity());
        Component gC = plasmid.createComponent("gap", AccessType.PUBLIC, endGap.getIdentity());

        plasmid.createSequenceConstraint("cs1", RestrictionType.PRECEDES, aC.getIdentity(), lC.getIdentity());
        plasmid.createSequenceConstraint("cs2", RestrictionType.PRECEDES, lC.getIdentity(), iC.getIdentity());
        plasmid.createSequenceConstraint("cs3", RestrictionType.PRECEDES, iC.getIdentity(), rC.getIdentity());
        plasmid.createSequenceConstraint("cs4", RestrictionType.PRECEDES, rC.getIdentity(), gC.getIdentity());

        String seqStr = "AAAGGGCCTCGTGATACGCCTATTTTTATAGGTTAATGTCATGATAATAATGGTTTCTTAGACGTCAGGTGGCACTTTTCGGGGAAATGTGCGCGGAACCCCTATTTGTTTATTTTTCTAAATACATTCAAATATGTATCCGCTCATGAGACAATAACCCTGATAAATGCTTCAATAATATTGAAAAAGGAAGAGTATGAGTATTCAACATTTCCGTGTCGCCCTTATTCCCTTTTTTGCGGCATTTTGCCTTCCTGTTTTTGCTCACCCAGAAACGCTGGTGAAAGTAAAAGATGCTGAAGATCAGTTGGGTGCACGAGTGGGTTACATCGAACTGGATCTCAACAGCGGTAAGATCCTTGAGAGTTTTCGCCCCGAAGAACGTTTTCCAATGATGAGCACTTTTAAAGTTCTGCTATGTGGCGCGGTATTATCCCGTATTGACGCCGGGCAAGAGCAACTCGGTCGCCGCATACACTATTCTCAGAATGACTTGGTTGAGTACTCACCAGTCACAGAAAAGCATCTTACGGATGGCATGACAGTAAGAGAATTATGCAGTGCTGCCATAACCATGAGTGATAACACTGCGGCCAACTTACTTCTGACAACGATCGGAGGACCGAAGGAGCTAACCGCTTTTTTGCACAACATGGGGGATCATGTAACTCGCCTTGATCGTTGGGAACCGGAGCTGAATGAAGCCATACCAAACGACGAGCGTGACACCACGATGCCTGTAGCAATGGCAACAACGTTGCGCAAACTATTAACTGGCGAACTACTTACTCTAGCTTCCCGGCAACAATTAATAGACTGGATGGAGGCGGATAAAGTTGCAGGACCACTTCTGCGCTCGGCCCTTCCGGCTGGCTGGTTTATTGCTGATAAATCTGGAGCCGGTGAGCGTGGTTCTCGCGGTATCATTGCAGCACTGGGGCCAGATGGTAAGCCCTCCCGTATCGTAGTTATCTACACGACGGGGAGTCAGGCAACTATGGATGAACGAAATAGACAGATCGCTGAGATAGGTGCCTCACTGATTAAGCATTGGTAACTGTCAGACCAAGTTTACTCATATATACTTTAGATTGATTTAAAACTTCATTTTTAATTTAAAAGGATCTAGGTGAAGATCCTTTTTGATAATCTCATGACCAAAATCCCTTAACGTGAGTTTTCGTTCCACTGAGCGTCAGACCCCGTAGAAAAGATCAAAGGATCTTCTTGAGATCCTTTTTTTCTGCGCGTAATCTGCTGCTTGCAAACAAAAAAACCACCGCTACCAGCGGTGGTTTGTTTGCCGGATCAAGAGCTACCAACTCTTTTTCCGAAGGTAACTGGCTTCAGCAGAGCGCAGATACCAAATACTGTTCTTCTAGTGTAGCCGTAGTTAGGCCACCACTTCAAGAACTCTGTAGCACCGCCTACATACCTCGCTCTGCTAATCCTGTTACCAGTGGCTGCTGCCAGTGGCGATAAGTCGTGTCTTACCGGGTTGGACTCAAGACGATAGTTACCGGATAAGGCGCAGCGGTCGGGCTGAACGGGGGGTTCGTGCACACAGCCCAGCTTGGAGCGAACGACCTACACCGAACTGAGATACCTACAGCGTGAGCTATGAGAAAGCGCCACGCTTCCCGAAGGGAGAAAGGCGGACAGGTATCCGGTAAGCGGCAGGGTCGGAACAGGAGAGCGCACGAGGGAGCTTCCAGGGGGAAACGCCTGGTATCTTTATAGTCCTGTCGGGTTTCGCCACCTCTGACTTGAGCGTCGATTTTTGTGATGCTCGTCAGGGGGGCGGAGCCTATGGAAAAACGCCAGCAACGCGGCCTTTTTACGGTTCCTGGCCTTTTGCTGGCCTTTTGCTCACATGTTCTTTCCTGCGTTATCCCCTGATTCTGTGGATAACCGTATTACCGCCTTTGAGTGAGCTGATACCGCTCGCCGCAGCCGAACGACCGAGCGCAGCGAGTCAGTGAGCGAGGAAGCGGATGAGCGCCCAATACGCAAACCGCCTCTCCCCGCGCGTTGGCCGATTCATTAATGCAGCTGGCACGACAGGTTTCggagcaaggcaaaaccaccgttatcagcagaacgacggcgggaaaaaatgattaaacgaaaaaatttgcaaggattcatagcggttgcccaatctaactcagggagcgacttcagcccacaaaaaacaccactgggcctactgggctattcccattatcatctacattgaagggatagcaagctaatttttatgacggcgatcgccaaaaacaaagaaaattcagcaattaccgtgggtagcaaaaaatccccatctaaagttcagtaaatatagctagaacaaccaagcattttcggcaaagtactattcagatagaacgagaaatgagcttgttctatccgcccggggctgaggctgtataatctacgacgggctgtcaaacattgtgataccatgggcagaagaaaggaaaaacgtccctgatcgcctttttgggcacggagtagggcgttaccccggcccgttcaaccacaagtccctatAGATACAATCGCCAAGAAGTATGaGAAGAGCACGGTAGCCTTNNNNNNNNNNNNNNNNNNTGCCCAGTCTTCTGCCTAAGGCAGGTGCCGCGGTGCGGGTGCCAGGGCGTGCCCCCGGGCTCCCCGGGCGCGTACTCCACtttacagctagctcagtcctaggtattatgctagctattgtgagcggataacaatttcacacatactagagaaagaggagaaatactaaATGTCTAACAACGCGCTGCAAACCATCATCAATGCACGCCTGCCTGGAGAGGAAGGGTTGTGGCAGATTCACTTACAGGACGGCAAAATCTCCGCGATCGACGCACAATCTGGGGTTATGCCGATCACCGAAAACTCTTTGGATGCCGAACAAGGGTTAGTCATTCCCCCATTCGTTGAACCACATATTCACCTGGATACTACTCAGACAGCCGGTCAGCCCAATTGGAACCAGTCCGGTACGCTGTTCGAAGGTATCGAACGATGGGCGGAGCGAAAAGCTCTACTCACGCATGACGATGTCAAGCAACGGGCCTGGCAGACCCTGAAGTGGCAGATCGCCAACGGAATACAGCACGTACGCACTCACGTGGATGTTTCCGATGCCACTTTGACGGCATTGAAGGCAATGCTCGAAGTTAAGCAGGAAGTAGCCCCGTGGATTGACTTGCAAATCGCTGCCTTCCCTCAGGAAGGCATCCTAAGTTATCCGAATGGAGAAGCGCTCCTGGAGGAGGCATTGCGGTTAGGAGCAGACGTGGTGGGAGCGATTCCCCATTTCGAGTTTACCCGCGAGTACGGTGTTGAATCTCTGCATAAAACATTTGCTTTAGCTCAGAAGTATGACCGTCTGATCGACGTACACTGCGACGAGATCGATGACGAACAGAGTCGCTTCGTGGAGACGGTGGCTGCGCTGGCGCATCACGAAGGCATGGGTGCACGTGTAACTGCAAGCCATACGACGGCTATGCACAGCTATAATGGGGCATATACATCTCGTTTGTTCCGATTACTAAAAATGAGCGGAATCAACTTTGTTGCCAATCCATTGGTCAACATTCATCTACAAGGACGCTTCGACACCTACCCGAAACGGCGAGGAATCACACGAGTTAAGGAAATGCTAGAGTCTGGTATCAATGTGTGTTTCGGGCATGATGACGTGTGTGGTCCCTGGTACCCTCTAGGAACAGCCAACATGCTGCAAGTTCTCCACATGGGTCTACACGTGTGTCAACTCATGGGGTATGGACAAATTAACGATGGACTCAATCTAATTACACACCATTCCGCCCGAACACTGAACCTCCAGGATTACGGGATCGCGGCGGGAAATTCTGCCAACCTCATCATTCTGCCCGCGGAAAACGGGTTCGACGCTCTACGCCGTCAAGTGCCAGTTCGGTATTCTGTTCGTGGGGGTAAGGTAATTGCAAGTACCCAACCGGCTCAGACCACGGTCTATTTAGAGCAACCGGAAGCTATCGACTACAAACGATGAgcttcaaataaaacgaaaggctcagtcgaaagactgggcctttcgttttatctgttgtttgtcggtgaacgctctctactagagtcacactggctcaccttcgggtgggcctttctgcgcgctCTGAGGTCTGCCTCGTGAAGAAGGTGTTGCTGACTCATACCAGGCCTGAATCGCCCCATCATCCAGCCAGAAAGTGAGGGAGCCACGGTTGATGAGAGCTTTGTTGTAGGTGGACCAGTTGGTGATTTTGAACTTTTGCTTTGCCACGGAACGGTCTGCGTTGTCGGGAAGATGCGTGATCTGATCCTTCAACTCAGCAAAAGTTCGATTTATTCAACAAAGCCGCCGTCCCGTCAAGTCAGCGTAATGCTCTGCCAGTGTTACAACCAATTAACCAATTCTGATTAGAAAAACTCATCGAGCATCAAATGAAACTGCAATTTATTCATATCAGGATTATCAATACCATATTTTTGAAAAAGCCGTTTCTGTAATGAAGGAGAAAACTCACCGAGGCAGTTCCATAGGATGGCAAGATCCTGGTATCGGTCTGCGATTCCGACTCGTCCAACATCAATACAACCTATTAATTTCCCCTCGTCAAAAATAAGGTTATCAAGTGAGAAATCACCATGAGTGACGACTGAATCCGGTGAGAATGGCAAAAGCTTATGCATTTCTTTCCAGACTTGTTCAACAGGCCAGCCATTACGCTCGTCATCAAAATCACTCGCATCAACCAAACCGTTATTCATTCGTGATTGCGCCTGAGCGAGACGAAATACGCGATCGCTGTTAAAAGGACAATTACAAACAGGAATCGAATGCAACCGGCGCAGGAACACTGCCAGCGCATCAACAATATTTTCACCTGAATCAGGATATTCTTCTAATACCTGGAATGCTGTTTTCCCGGGGATCGCAGTGGTGAGTAACCATGCATCATCAGGAGTACGGATAAAATGCTTGATGGTCGGAAGAGGCATAAATTCCGTCAGCCAGTTTAGTCTGACCATCTCATCTGTAACATCATTGGCAACGCTACCTTTGCCATGTTTCAGAAACAACTCTGGCGCATCGGGCTTCCCATACAATCGATAGATTGTCGCACCTGATTGCCCGACATTATCGCGAGCCCATTTATACCCATATAAATCAGCATCCATGTTGGAATTTAATCGCGGCCTCGAGCAAGACGTTTCCCGTTGAATATGGCTCATAACACCCCTTGTATTACTGTTTATGTAAGCAGACAGTTTTATTGTTCATGATGATATATTTTTATCTTGTGCAATGTAACATCAGAGATTTTGAGACACAACGTGGCTTTCCGCGGTGCGGGTGCCAGGGCGTGCCCTTGGGCTCCCCGGGCGCGTACTCCACCACCTGCCATTGGGAGAAGACTTGGGAGCTCTTCataatcagccagctcaatctgtgtgtcgttgatttaagcttaatgctacggggtctgtctccaactccctcagcttctcgcaatggcaaggcaaataatgtttctcttgctgagtagatgttcaggaggacggatcgaaagtctacaaaacagattcttgaccaagccatctacttagaaaaacttctgcgttttggcgatcgcatcttttaagcgagatgcgatttttttgtccattagtttgtattttaatactcttttgttgtttgatttcgtccaagcttttcttggtatgtgggatcttccgtgcccaaaattttatcccagaaagtgaaatatagtcatttcaattaacgatgagagaatttaatgtaaaattatggagtgtacaaaatgaacaggtttaaacaatggcttacagtttagatttaaggcaaagggtagtagcttatatagaagctggaggaaaaataactgaggcttccaagatatataaaataggaaaagcctcgatatacagatggttaaatagagtagatttaagcccaacaaaagtagagcgtcgccataggCGCTGCTTACAGACAAGCTGTGACCGTCTCCGGGAGCTGCATGTGTCAGAGGTTTTCACCGTCATCACCGAAACGCGCGAGACG";
        Sequence seq = doc.createSequence("sll00199_codA_Km_flat_seq", version, seqStr, Sequence.IUPAC_DNA);
        plasmid.addSequence(seq);
        
        SequenceAnnotation an = plasmid.createSequenceAnnotation("ann1", "ann1", 1, 2073);
        an.setComponent(aC.getIdentity());
        
        an = plasmid.createSequenceAnnotation("ann2", "ann2", 2074, 2573);
        an.setComponent(lC.getIdentity());
        
        an = plasmid.createSequenceAnnotation("AmpR_prom", "AmpR_prom", 92, 196);
        an.addRole(SequenceOntology.PROMOTER);
        
        an = plasmid.createSequenceAnnotation("AmpR", "AmpR", 197, 1057);
        an.addRole(SequenceOntology.CDS);    
        
        ComponentDefinition originD = doc.getComponentDefinition("ori", version);
        Component origin =  plasmid.createComponent("ori_instance", AccessType.PUBLIC, originD.getIdentity());
        an = plasmid.createSequenceAnnotation("ori", "ori", 1228, 1816);
        an.setComponent(origin.getIdentity());        
        
        /*ComponentDefinition originD = doc.createComponentDefinition("ori", version, ComponentDefinition.DNA_REGION);
        originD.addRole(SequenceOntology.ORIGIN_OF_REPLICATION);
        
        Component origin = region.createComponent("ori_instance", AccessType.PUBLIC, originD.getIdentity());
        
        an = region.createSequenceAnnotation("ori", "ori", 1228, 1816);
        an.setComponent(origin.getIdentity());*/        
        return plasmid;        
    }    

}