/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ed.biordm.sbol.sbol2easy.scrapbook;

import static ed.biordm.sbol.sbol2easy.transform.CommonAnnotations.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import javax.xml.namespace.QName;

import org.sbolstandard.core2.AccessType;
import org.sbolstandard.core2.Component;
import org.sbolstandard.core2.ComponentDefinition;
import org.sbolstandard.core2.OrientationType;
import org.sbolstandard.core2.RestrictionType;
import org.sbolstandard.core2.SBOLConversionException;
import org.sbolstandard.core2.SBOLDocument;
import org.sbolstandard.core2.SBOLValidate;
import org.sbolstandard.core2.SBOLValidationException;
import org.sbolstandard.core2.SBOLWriter;
import org.sbolstandard.core2.Sequence;
import org.sbolstandard.core2.SequenceAnnotation;
import org.sbolstandard.core2.SequenceOntology;

/*
 * Plasmid template
 * 
 * @author tzielins
 * 
 */

public class CyanoTemplate {

    
    
	public static void main(String[] args) throws SBOLValidationException, SBOLConversionException, IOException, URISyntaxException {
		
		SBOLDocument doc = new SBOLDocument();

		doc.setDefaultURIprefix("http://bio.ed.ac.uk/a_mccormick/cyano_source/");
		doc.setComplete(true);
		doc.setCreateDefaults(true);
                
		
		String version = "1.0.0";
                
                
                ComponentDefinition plasmid = createTemplatePlasmid(doc, version);
                
                
                String fName = "cyano_gen_template";
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
        
    public static ComponentDefinition createTemplatePlasmid(SBOLDocument doc, String version) throws SBOLValidationException {
        
                ComponentDefinition insert = createCassete(doc, version);
		
                ComponentDefinition ampROrg = createBackbone(doc, version);

                //ComponentDefinition endGap = createEndGap(doc, version);
                
                ComponentDefinition leftFlank = createLeftFlank(doc, version);

                ComponentDefinition rightFlank = createRightFlank(doc, version);
                
                ComponentDefinition plasmid = assembleTemplatePlasmid(doc, version, ampROrg, leftFlank, insert, rightFlank);  
                return plasmid;
    }    

    static ComponentDefinition createCassete(SBOLDocument doc, String version) throws SBOLValidationException {
        
        String name = "insert";
        ComponentDefinition region = doc.createComponentDefinition(name, version, ComponentDefinition.DNA_REGION);
        region.setName("insert");
        //region.setDescription("transgene insert with codA and Km");
        region.createAnnotation(SBH_DESCRIPTION,"transgene insert with codA and Km");
        
        //region.addRole(SequenceOntology.ENGINEERED_REGION);
        // engineered insert
        region.addRole(SO("SO:0000915"));
        
        String seqStr = "ATGaGAAGAGCACGGTAGCCTTNNNNNNNNNNNNNNNNNNTGCCCAGTCTTCTGCCTAAGGCAGGTGCCGCGGTGCGGGTGCCAGGGCGTGCCCCCGGGCTCCCCGGGCGCGTACTCCACtttacagctagctcagtcctaggtattatgctagctattgtgagcggataacaatttcacacatactagagaaagaggagaaatactaaATGTCTAACAACGCGCTGCAAACCATCATCAATGCACGCCTGCCTGGAGAGGAAGGGTTGTGGCAGATTCACTTACAGGACGGCAAAATCTCCGCGATCGACGCACAATCTGGGGTTATGCCGATCACCGAAAACTCTTTGGATGCCGAACAAGGGTTAGTCATTCCCCCATTCGTTGAACCACATATTCACCTGGATACTACTCAGACAGCCGGTCAGCCCAATTGGAACCAGTCCGGTACGCTGTTCGAAGGTATCGAACGATGGGCGGAGCGAAAAGCTCTACTCACGCATGACGATGTCAAGCAACGGGCCTGGCAGACCCTGAAGTGGCAGATCGCCAACGGAATACAGCACGTACGCACTCACGTGGATGTTTCCGATGCCACTTTGACGGCATTGAAGGCAATGCTCGAAGTTAAGCAGGAAGTAGCCCCGTGGATTGACTTGCAAATCGCTGCCTTCCCTCAGGAAGGCATCCTAAGTTATCCGAATGGAGAAGCGCTCCTGGAGGAGGCATTGCGGTTAGGAGCAGACGTGGTGGGAGCGATTCCCCATTTCGAGTTTACCCGCGAGTACGGTGTTGAATCTCTGCATAAAACATTTGCTTTAGCTCAGAAGTATGACCGTCTGATCGACGTACACTGCGACGAGATCGATGACGAACAGAGTCGCTTCGTGGAGACGGTGGCTGCGCTGGCGCATCACGAAGGCATGGGTGCACGTGTAACTGCAAGCCATACGACGGCTATGCACAGCTATAATGGGGCATATACATCTCGTTTGTTCCGATTACTAAAAATGAGCGGAATCAACTTTGTTGCCAATCCATTGGTCAACATTCATCTACAAGGACGCTTCGACACCTACCCGAAACGGCGAGGAATCACACGAGTTAAGGAAATGCTAGAGTCTGGTATCAATGTGTGTTTCGGGCATGATGACGTGTGTGGTCCCTGGTACCCTCTAGGAACAGCCAACATGCTGCAAGTTCTCCACATGGGTCTACACGTGTGTCAACTCATGGGGTATGGACAAATTAACGATGGACTCAATCTAATTACACACCATTCCGCCCGAACACTGAACCTCCAGGATTACGGGATCGCGGCGGGAAATTCTGCCAACCTCATCATTCTGCCCGCGGAAAACGGGTTCGACGCTCTACGCCGTCAAGTGCCAGTTCGGTATTCTGTTCGTGGGGGTAAGGTAATTGCAAGTACCCAACCGGCTCAGACCACGGTCTATTTAGAGCAACCGGAAGCTATCGACTACAAACGATGAgcttcaaataaaacgaaaggctcagtcgaaagactgggcctttcgttttatctgttgtttgtcggtgaacgctctctactagagtcacactggctcaccttcgggtgggcctttctgcgcgctCTGAGGTCTGCCTCGTGAAGAAGGTGTTGCTGACTCATACCAGGCCTGAATCGCCCCATCATCCAGCCAGAAAGTGAGGGAGCCACGGTTGATGAGAGCTTTGTTGTAGGTGGACCAGTTGGTGATTTTGAACTTTTGCTTTGCCACGGAACGGTCTGCGTTGTCGGGAAGATGCGTGATCTGATCCTTCAACTCAGCAAAAGTTCGATTTATTCAACAAAGCCGCCGTCCCGTCAAGTCAGCGTAATGCTCTGCCAGTGTTACAACCAATTAACCAATTCTGATTAGAAAAACTCATCGAGCATCAAATGAAACTGCAATTTATTCATATCAGGATTATCAATACCATATTTTTGAAAAAGCCGTTTCTGTAATGAAGGAGAAAACTCACCGAGGCAGTTCCATAGGATGGCAAGATCCTGGTATCGGTCTGCGATTCCGACTCGTCCAACATCAATACAACCTATTAATTTCCCCTCGTCAAAAATAAGGTTATCAAGTGAGAAATCACCATGAGTGACGACTGAATCCGGTGAGAATGGCAAAAGCTTATGCATTTCTTTCCAGACTTGTTCAACAGGCCAGCCATTACGCTCGTCATCAAAATCACTCGCATCAACCAAACCGTTATTCATTCGTGATTGCGCCTGAGCGAGACGAAATACGCGATCGCTGTTAAAAGGACAATTACAAACAGGAATCGAATGCAACCGGCGCAGGAACACTGCCAGCGCATCAACAATATTTTCACCTGAATCAGGATATTCTTCTAATACCTGGAATGCTGTTTTCCCGGGGATCGCAGTGGTGAGTAACCATGCATCATCAGGAGTACGGATAAAATGCTTGATGGTCGGAAGAGGCATAAATTCCGTCAGCCAGTTTAGTCTGACCATCTCATCTGTAACATCATTGGCAACGCTACCTTTGCCATGTTTCAGAAACAACTCTGGCGCATCGGGCTTCCCATACAATCGATAGATTGTCGCACCTGATTGCCCGACATTATCGCGAGCCCATTTATACCCATATAAATCAGCATCCATGTTGGAATTTAATCGCGGCCTCGAGCAAGACGTTTCCCGTTGAATATGGCTCATAACACCCCTTGTATTACTGTTTATGTAAGCAGACAGTTTTATTGTTCATGATGATATATTTTTATCTTGTGCAATGTAACATCAGAGATTTTGAGACACAACGTGGCTTTCCGCGGTGCGGGTGCCAGGGCGTGCCCTTGGGCTCCCCGGGCGCGTACTCCACCACCTGCCATTGGGAGAAGACTTGGGAGCTCTTCataa";
        Sequence seq = doc.createSequence(name+"_seq", version, seqStr, Sequence.IUPAC_DNA);
        region.addSequence(seq);

        final int SS = 2657;
        SequenceAnnotation an = region.createSequenceAnnotation("SapI_ATG_over", "SapI_ATG_over", 2658-SS, 2660-SS);
        //an.addRole(SO("SO:0001695"));
        an.addRole(SO("SO:0001933"));
        an.setName("SapI-ATG overhang");
    
        an = region.createSequenceAnnotation("SapI_BspQI", "SapI_BspQI", 2662-SS, 2668-SS);
        an.addRole(SO("SO:0001687"));
        an.setName("SapI/BspQI");
        
        ComponentDefinition barcode = doc.createComponentDefinition("barcode", version, ComponentDefinition.DNA_REGION);
        barcode.addRole(SO("SO:0000730"));

        Component barcodeI = region.createComponent("barcode_inst", AccessType.PUBLIC, barcode.getIdentity());
        
        an = region.createSequenceAnnotation("barcode", "barcode", 2680-SS, 2697-SS);
        an.setComponent(barcodeI.getIdentity());
        //an = region.createSequenceAnnotation("barcode", "barcode", 2680-SS, 2697-SS);
        //an.addRole(SO("SO:0000730"));

        an = region.createSequenceAnnotation("BpiI_TGCC_over", "BpiI_TGCC_over", 2698-SS, 2701-SS);
        //an.addRole(SO("SO:0001695"));
        an.addRole(SO("SO:0001933"));
        an.setName("BpiI-TGCC overhang");

        an = region.createSequenceAnnotation("BpiI", "BpiI", 2704-SS, 2709-SS);
        an.addRole(SO("SO:0001687"));
        

        an = region.createSequenceAnnotation("AarI_TGCC_over", "AarI_TGCC_over", 2710-SS, 2713-SS);
        //an.addRole(SO("SO:0001695"));
        an.addRole(SO("SO:0001933"));
        an.setName("AarI-TGCC overhang");

        an = region.createSequenceAnnotation("AarI", "AarI", 2718-SS, 2724-SS);
        an.addRole(SO("SO:0001687"));
        
        
        an = region.createSequenceAnnotation("attB_CC", "attB_CC", 2725-SS, 2777-SS);
        //recombination_signal_sequence
        an.addRole(SO("SO:0001532"));
        an.setName("attB CC");
        
        an = region.createSequenceAnnotation("J23101MH_prom", "J23101MH_prom", 2778-SS, 2866-SS);
        an.addRole(SequenceOntology.PROMOTER); 
        an.setName("J23101MH prom");
        an.createAnnotation(SBH_DESCRIPTION,"pC0.031 https://doi.org/10.1104/pp.18.01401");
        //an.createAnnotation(new QName("http://sbols.org/genBankConversion#", "note","gbconv"), 
        //        "pC0.031  https://doi.org/10.1104/pp.18.01401");
        
        
        ComponentDefinition codA = doc.createComponentDefinition("codA", version, ComponentDefinition.DNA_REGION);
        codA.addRole(SequenceOntology.CDS);
        codA.createAnnotation(SBH_DESCRIPTION, "Codon optimised (V153A, F317C)  ndoi:10.1111/tpj.12675");
        //codA.createAnnotation(new QName("http://sbols.org/genBankConversion#", "note","gbconv"), 
        //        "Codon optimised (V153A, F317C)  doi:10.1111/tpj.12675");
        
        seqStr = "ATGTCTAACAACGCGCTGCAAACCATCATCAATGCACGCCTGCCTGGAGAGGAAGGGTTGTGGCAGATTCACTTACAGGACGGCAAAATCTCCGCGATCGACGCACAATCTGGGGTTATGCCGATCACCGAAAACTCTTTGGATGCCGAACAAGGGTTAGTCATTCCCCCATTCGTTGAACCACATATTCACCTGGATACTACTCAGACAGCCGGTCAGCCCAATTGGAACCAGTCCGGTACGCTGTTCGAAGGTATCGAACGATGGGCGGAGCGAAAAGCTCTACTCACGCATGACGATGTCAAGCAACGGGCCTGGCAGACCCTGAAGTGGCAGATCGCCAACGGAATACAGCACGTACGCACTCACGTGGATGTTTCCGATGCCACTTTGACGGCATTGAAGGCAATGCTCGAAGTTAAGCAGGAAGTAGCCCCGTGGATTGACTTGCAAATCGCTGCCTTCCCTCAGGAAGGCATCCTAAGTTATCCGAATGGAGAAGCGCTCCTGGAGGAGGCATTGCGGTTAGGAGCAGACGTGGTGGGAGCGATTCCCCATTTCGAGTTTACCCGCGAGTACGGTGTTGAATCTCTGCATAAAACATTTGCTTTAGCTCAGAAGTATGACCGTCTGATCGACGTACACTGCGACGAGATCGATGACGAACAGAGTCGCTTCGTGGAGACGGTGGCTGCGCTGGCGCATCACGAAGGCATGGGTGCACGTGTAACTGCAAGCCATACGACGGCTATGCACAGCTATAATGGGGCATATACATCTCGTTTGTTCCGATTACTAAAAATGAGCGGAATCAACTTTGTTGCCAATCCATTGGTCAACATTCATCTACAAGGACGCTTCGACACCTACCCGAAACGGCGAGGAATCACACGAGTTAAGGAAATGCTAGAGTCTGGTATCAATGTGTGTTTCGGGCATGATGACGTGTGTGGTCCCTGGTACCCTCTAGGAACAGCCAACATGCTGCAAGTTCTCCACATGGGTCTACACGTGTGTCAACTCATGGGGTATGGACAAATTAACGATGGACTCAATCTAATTACACACCATTCCGCCCGAACACTGAACCTCCAGGATTACGGGATCGCGGCGGGAAATTCTGCCAACCTCATCATTCTGCCCGCGGAAAACGGGTTCGACGCTCTACGCCGTCAAGTGCCAGTTCGGTATTCTGTTCGTGGGGGTAAGGTAATTGCAAGTACCCAACCGGCTCAGACCACGGTCTATTTAGAGCAACCGGAAGCTATCGACTACAAACGATGA";
        seq = doc.createSequence("codA_seq", version, seqStr, Sequence.IUPAC_DNA);
        codA.addSequence(seq);


        Component codAI = region.createComponent("codA_inst", AccessType.PUBLIC, codA.getIdentity());
        
        an = region.createSequenceAnnotation("codA", "codA", 2867-SS, 4150-SS);
        an.setComponent(codAI.getIdentity());
        //an.addRole(SequenceOntology.CDS);
        //an.createAnnotation(new QName("http://sbols.org/genBankConversion#", "product","gbconv"), 
        //        "Codon optimised (V153A, F317C)  \ndoi:10.1111/tpj.12675");
        
        an = region.createSequenceAnnotation("rrnBT1_T7_term", "rrnBT1_T7_term", 4155-SS, 4269-SS);
        an.addRole(SequenceOntology.TERMINATOR);
        an.setName("rrnBT1/T7 term");
        an.createAnnotation(SBH_DESCRIPTION, 
                "BBa_B0015 double terminator (B0010-B0012) pC0.082 https://doi.org/10.1104/pp.18.01401");
        //an.createAnnotation(new QName("http://sbols.org/genBankConversion#", "note","gbconv"), 
        //        "BBa_B0015 double terminator (B0010-B0012)  pC0.082  https://doi.org/10.1104/pp.18.01401");
        
        an = region.createSequenceAnnotation("KanR_term", "KanR_term", 4274-SS, 4557-SS, OrientationType.REVERSECOMPLEMENT);
        an.addRole(SequenceOntology.TERMINATOR);
        an.setName("KanR term");
        
        an = region.createSequenceAnnotation("KanR", "KanR", 4558-SS, 5373-SS, OrientationType.REVERSECOMPLEMENT);
        an.addRole(SequenceOntology.CDS); 
        an.createAnnotation(SBH_DESCRIPTION,
                "Gene: aph(3')-Ia aminoglycoside phosphotransferase confers resistance to kanamycin "
                        + "in bacteria or G418 (Geneticin(R)) in eukaryotes");
        
        //an.createAnnotation(new QName("http://sbols.org/genBankConversion#", "note","gbconv"), 
        //        "confers resistance to kanamycin in bacteria or G418 (Geneticin(R)) in eukaryotes");
        an.createAnnotation(GB_GENE, 
                "aph(3')-Ia");
        an.createAnnotation(GB_PRODUCT, 
                "aminoglycoside phosphotransferase");

        an = region.createSequenceAnnotation("KanR_prom", "KanR_prom", 5374-SS, 5483-SS, OrientationType.REVERSECOMPLEMENT);
        an.addRole(SequenceOntology.PROMOTER);
        an.setName("KanR prom");
        
        an = region.createSequenceAnnotation("attB_TT", "attB_TT", 5484-SS, 5536-SS);
        //recombination_signal_sequence
        an.addRole(SO("SO:0001532"));
        an.setName("attB TT");
        an.createAnnotation(SBH_DESCRIPTION, "https://doi.org/10.1002/bit.26854");
        //an.createAnnotation(new QName("http://sbols.org/genBankConversion#", "note","gbconv"), 
        //        "https://doi.org/10.1002/bit.26854");

        
        an = region.createSequenceAnnotation("AarI_2", "AarI_2", 5537-SS, 5543-SS);
        an.setName("AarI");
        an.addRole(SO("SO:0001687"));
        
        an = region.createSequenceAnnotation("AarI_GGGA_over", "AarI_GGGA_over", 5548-SS, 5551-SS);
        //an.addRole(SO("SO:0001695"));
        an.addRole(SO("SO:0001933"));
        an.setName("AarI-GGGA overhang");

        an = region.createSequenceAnnotation("BpiI_2", "BpiI_2", 5552-SS, 5557-SS);
        an.setName("BpiI");
        an.addRole(SO("SO:0001687"));
        
        an = region.createSequenceAnnotation("BpiI_GGGA_over", "BpiI_GGGA_over", 5560-SS, 5563-SS);
        //an.addRole(SO("SO:0001695"));
        an.addRole(SO("SO:0001933"));
        an.setName("BpiI-GGGA overhang");

        an = region.createSequenceAnnotation("SapI_2", "SapI_2", 5564-SS, 5570-SS);
        an.setName("SapI");
        an.addRole(SO("SO:0001687"));
        
        an = region.createSequenceAnnotation("SapI_TAA_over", "SapI_TAA_over", 5572-SS, 5574-SS);
        //an.addRole(SO("SO:0001695"));
        an.addRole(SO("SO:0001933"));
        an.setName("SapI-TAA overhang");
        
        
        return region;
    }

    static ComponentDefinition createBackbone(SBOLDocument doc, String version) throws SBOLValidationException {

        String name = "backbone";
        ComponentDefinition region = doc.createComponentDefinition(name, version, ComponentDefinition.DNA_REGION);
        region.addRole(SequenceOntology.ENGINEERED_REGION);
        
        String seqStr = "CGCTGCTTACAGACAAGCTGTGACCGTCTCCGGGAGCTGCATGTGTCAGAGGTTTTCACCGTCATCACCGAAACGCGCGAGACGAAAGGGCCTCGTGATACGCCTATTTTTATAGGTTAATGTCATGATAATAATGGTTTCTTAGACGTCAGGTGGCACTTTTCGGGGAAATGTGCGCGGAACCCCTATTTGTTTATTTTTCTAAATACATTCAAATATGTATCCGCTCATGAGACAATAACCCTGATAAATGCTTCAATAATATTGAAAAAGGAAGAGTATGAGTATTCAACATTTCCGTGTCGCCCTTATTCCCTTTTTTGCGGCATTTTGCCTTCCTGTTTTTGCTCACCCAGAAACGCTGGTGAAAGTAAAAGATGCTGAAGATCAGTTGGGTGCACGAGTGGGTTACATCGAACTGGATCTCAACAGCGGTAAGATCCTTGAGAGTTTTCGCCCCGAAGAACGTTTTCCAATGATGAGCACTTTTAAAGTTCTGCTATGTGGCGCGGTATTATCCCGTATTGACGCCGGGCAAGAGCAACTCGGTCGCCGCATACACTATTCTCAGAATGACTTGGTTGAGTACTCACCAGTCACAGAAAAGCATCTTACGGATGGCATGACAGTAAGAGAATTATGCAGTGCTGCCATAACCATGAGTGATAACACTGCGGCCAACTTACTTCTGACAACGATCGGAGGACCGAAGGAGCTAACCGCTTTTTTGCACAACATGGGGGATCATGTAACTCGCCTTGATCGTTGGGAACCGGAGCTGAATGAAGCCATACCAAACGACGAGCGTGACACCACGATGCCTGTAGCAATGGCAACAACGTTGCGCAAACTATTAACTGGCGAACTACTTACTCTAGCTTCCCGGCAACAATTAATAGACTGGATGGAGGCGGATAAAGTTGCAGGACCACTTCTGCGCTCGGCCCTTCCGGCTGGCTGGTTTATTGCTGATAAATCTGGAGCCGGTGAGCGTGGTTCTCGCGGTATCATTGCAGCACTGGGGCCAGATGGTAAGCCCTCCCGTATCGTAGTTATCTACACGACGGGGAGTCAGGCAACTATGGATGAACGAAATAGACAGATCGCTGAGATAGGTGCCTCACTGATTAAGCATTGGTAACTGTCAGACCAAGTTTACTCATATATACTTTAGATTGATTTAAAACTTCATTTTTAATTTAAAAGGATCTAGGTGAAGATCCTTTTTGATAATCTCATGACCAAAATCCCTTAACGTGAGTTTTCGTTCCACTGAGCGTCAGACCCCGTAGAAAAGATCAAAGGATCTTCTTGAGATCCTTTTTTTCTGCGCGTAATCTGCTGCTTGCAAACAAAAAAACCACCGCTACCAGCGGTGGTTTGTTTGCCGGATCAAGAGCTACCAACTCTTTTTCCGAAGGTAACTGGCTTCAGCAGAGCGCAGATACCAAATACTGTTCTTCTAGTGTAGCCGTAGTTAGGCCACCACTTCAAGAACTCTGTAGCACCGCCTACATACCTCGCTCTGCTAATCCTGTTACCAGTGGCTGCTGCCAGTGGCGATAAGTCGTGTCTTACCGGGTTGGACTCAAGACGATAGTTACCGGATAAGGCGCAGCGGTCGGGCTGAACGGGGGGTTCGTGCACACAGCCCAGCTTGGAGCGAACGACCTACACCGAACTGAGATACCTACAGCGTGAGCTATGAGAAAGCGCCACGCTTCCCGAAGGGAGAAAGGCGGACAGGTATCCGGTAAGCGGCAGGGTCGGAACAGGAGAGCGCACGAGGGAGCTTCCAGGGGGAAACGCCTGGTATCTTTATAGTCCTGTCGGGTTTCGCCACCTCTGACTTGAGCGTCGATTTTTGTGATGCTCGTCAGGGGGGCGGAGCCTATGGAAAAACGCCAGCAACGCGGCCTTTTTACGGTTCCTGGCCTTTTGCTGGCCTTTTGCTCACATGTTCTTTCCTGCGTTATCCCCTGATTCTGTGGATAACCGTATTACCGCCTTTGAGTGAGCTGATACCGCTCGCCGCAGCCGAACGACCGAGCGCAGCGAGTCAGTGAGCGAGGAAGCGGATGAGCGCCCAATACGCAAACCGCCTCTCCCCGCGCGTTGGCCGATTCATTAATGCAGCTGGCACGACAGGTTTCggag";
        Sequence seq = doc.createSequence(name+"_seq", version, seqStr, Sequence.IUPAC_DNA);
        region.addSequence(seq);
        
        
        SequenceAnnotation an = region.createSequenceAnnotation("BsmBI", "BsmBI", 31, 31);
        an.addRole(SO("SO:0001687"));
        
        an = region.createSequenceAnnotation("BsmBI_2", "BsmBI_2", 73, 73);
        an.setName("BsmBI");
        an.addRole(SO("SO:0001687"));
        
        an = region.createSequenceAnnotation("AmpR_prom", "AmpR_prom", 176, 280);
        an.addRole(SequenceOntology.PROMOTER);
        an.createAnnotation(GB_GENE, "bla");
        
        an = region.createSequenceAnnotation("AmpR", "AmpR", 281, 1141);
        an.addRole(SequenceOntology.CDS);    
        an.createAnnotation(SBH_DESCRIPTION, 
                "confers resistance to ampicillin, carbenicillin, and related antibiotics");
        //an.createAnnotation(new QName("http://sbols.org/genBankConversion#", "note","gbconv"), 
        //        "confers resistance to ampicillin, carbenicillin, and related antibiotics");
        an.createAnnotation(GB_GENE,"bla");
        an.createAnnotation(GB_PRODUCT,"beta-lactamase");        
        
        ComponentDefinition originD = doc.createComponentDefinition("ori", version, ComponentDefinition.DNA_REGION);
        originD.addRole(SequenceOntology.ORIGIN_OF_REPLICATION);
        //originD.createAnnotation(new QName("http://sbols.org/genBankConversion#", "note","gbconv"), 
        //        "high-copy-number ColE1/pMB1/pBR322/pUC origin of replication");   
        originD.createAnnotation(SBH_DESCRIPTION, "high-copy-number ColE1/pMB1/pBR322/pUC origin of replication");
        
        seqStr = "TTGAGATCCTTTTTTTCTGCGCGTAATCTGCTGCTTGCAAACAAAAAAACCACCGCTACCAGCGGTGGTTTGTTTGCCGGATCAAGAGCTACCAACTCTTTTTCCGAAGGTAACTGGCTTCAGCAGAGCGCAGATACCAAATACTGTTCTTCTAGTGTAGCCGTAGTTAGGCCACCACTTCAAGAACTCTGTAGCACCGCCTACATACCTCGCTCTGCTAATCCTGTTACCAGTGGCTGCTGCCAGTGGCGATAAGTCGTGTCTTACCGGGTTGGACTCAAGACGATAGTTACCGGATAAGGCGCAGCGGTCGGGCTGAACGGGGGGTTCGTGCACACAGCCCAGCTTGGAGCGAACGACCTACACCGAACTGAGATACCTACAGCGTGAGCTATGAGAAAGCGCCACGCTTCCCGAAGGGAGAAAGGCGGACAGGTATCCGGTAAGCGGCAGGGTCGGAACAGGAGAGCGCACGAGGGAGCTTCCAGGGGGAAACGCCTGGTATCTTTATAGTCCTGTCGGGTTTCGCCACCTCTGACTTGAGCGTCGATTTTTGTGATGCTCGTCAGGGGGGCGGAGCCTATGGAAA";
        seq = doc.createSequence("ori_seq", version, seqStr, Sequence.IUPAC_DNA);
        originD.addSequence(seq);
        
        Component origin = region.createComponent("ori_instance", AccessType.PUBLIC, originD.getIdentity());
        
        an = region.createSequenceAnnotation("ori", "ori", 1312, 1900);
        an.setComponent(origin.getIdentity());
        return region;
    }

    /*
    static ComponentDefinition createEndGap(SBOLDocument doc, String version) throws SBOLValidationException, URISyntaxException {

        String name = "end_of_backbone";
        ComponentDefinition region = doc.createComponentDefinition(name, version, ComponentDefinition.DNA_REGION);
        region.setName("backbone end");
        region.addRole(SequenceOntology.ENGINEERED_REGION);
        
        String seqStr = "CGCTGCTTACAGACAAGCTGTGACCGTCTCCGGGAGCTGCATGTGTCAGAGGTTTTCACCGTCATCACCGAAACGCGCGAGACG";
        Sequence seq = doc.createSequence(name+"_seq", version, seqStr, Sequence.IUPAC_DNA);
        region.addSequence(seq);

        int SS = 6058;
        SequenceAnnotation an = region.createSequenceAnnotation("BsmBI", "BsmBI", 6089-SS, 6089-SS);
        an.addRole(SO("SO:0001687"));
        
        an = region.createSequenceAnnotation("BsmBI_2", "BsmBI_2", 6131-SS, 6131-SS);
        an.setName("BsmBI");
        an.addRole(SO("SO:0001687"));        
        
        return region;
    }*/

    static ComponentDefinition createLeftFlank(SBOLDocument doc, String version) throws SBOLValidationException {

        String name = "left_flank";
        ComponentDefinition region = doc.createComponentDefinition(name, version, ComponentDefinition.DNA_REGION);
        //region.addRole(SequenceOntology.ENGINEERED_REGION);
        // recombination feature
        //region.addRole(SO("SO:0000298"));
        //site_specific_recombination_target_region‘
        region.addRole(SO("SO:0000342"));
        return region;
    }

    static ComponentDefinition createRightFlank(SBOLDocument doc, String version) throws SBOLValidationException {

        String name = "right_flank";
        ComponentDefinition region = doc.createComponentDefinition(name, version, ComponentDefinition.DNA_REGION);
        //region.addRole(SequenceOntology.ENGINEERED_REGION);
        // recombination feature
        //region.addRole(SO("SO:0000298"));
        //region.addRole(OrientationType.REVERSECOMPLEMENT);
        //site_specific_recombination_target_region‘
        region.addRole(SO("SO:0000342"));
        return region;
    }

    static ComponentDefinition assembleTemplatePlasmid(SBOLDocument doc, String version, 
            ComponentDefinition backbone, 
            ComponentDefinition leftFlank, 
            ComponentDefinition insert, 
            ComponentDefinition rightFlank) throws SBOLValidationException {

        return assebmleTemplatePlasmid(doc, version, backbone.getIdentity(), leftFlank.getIdentity(), insert.getIdentity(), 
                rightFlank.getIdentity());
        
    }

    static ComponentDefinition assebmleTemplatePlasmid(SBOLDocument doc, String version, 
            URI backbone, 
            URI leftFlank, 
            URI insert, 
            URI rightFlank) throws SBOLValidationException {

        String name = "cyano_codA_Km";
        ComponentDefinition plasmid = doc.createComponentDefinition(name, version, ComponentDefinition.DNA_REGION);
        plasmid.addType(SequenceOntology.CIRCULAR);
        //engineered plasmid
        plasmid.addRole(SO("SO:0000637"));
        
        plasmid.createAnnotation(CREATOR,"Alistair McCormick");
        plasmid.createAnnotation(CREATOR,"Anja Nenninger");
        //plasmid.createAnnotation(SBH_DESCRIPTION,"Do we want some general template description?");
        
        
        Component aC = plasmid.createComponent("backbone", AccessType.PUBLIC, backbone);
        Component lC = plasmid.createComponent("left", AccessType.PUBLIC, leftFlank);
        Component iC = plasmid.createComponent("insert", AccessType.PUBLIC, insert);
        Component rC = plasmid.createComponent("right", AccessType.PUBLIC, rightFlank);
        //Component gC = plasmid.createComponent("gap", AccessType.PUBLIC, endGap);

        plasmid.createSequenceConstraint("cs1", RestrictionType.PRECEDES, aC.getIdentity(), lC.getIdentity());
        plasmid.createSequenceConstraint("cs2", RestrictionType.PRECEDES, lC.getIdentity(), iC.getIdentity());
        plasmid.createSequenceConstraint("cs3", RestrictionType.PRECEDES, iC.getIdentity(), rC.getIdentity());
        //plasmid.createSequenceConstraint("cs4", RestrictionType.PRECEDES, rC.getIdentity(), gC.getIdentity());
                
        return plasmid;
    }
    

    
  

}
