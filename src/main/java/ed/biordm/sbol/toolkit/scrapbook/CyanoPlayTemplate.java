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

public class CyanoPlayTemplate {

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
                
                String fName = "cyano_template";
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
        //region.addRole(SequenceOntology.ENGINEERED_REGION);
        // engineered insert
        region.addRole(new URI(SeqenceOntoPref+"SO:0000915"));
        
        String seqStr = "ATGaGAAGAGCACGGTAGCCTTNNNNNNNNNNNNNNNNNNTGCCCAGTCTTCTGCCTAAGGCAGGTGCCGCGGTGCGGGTGCCAGGGCGTGCCCCCGGGCTCCCCGGGCGCGTACTCCACtttacagctagctcagtcctaggtattatgctagctattgtgagcggataacaatttcacacatactagagaaagaggagaaatactaaATGTCTAACAACGCGCTGCAAACCATCATCAATGCACGCCTGCCTGGAGAGGAAGGGTTGTGGCAGATTCACTTACAGGACGGCAAAATCTCCGCGATCGACGCACAATCTGGGGTTATGCCGATCACCGAAAACTCTTTGGATGCCGAACAAGGGTTAGTCATTCCCCCATTCGTTGAACCACATATTCACCTGGATACTACTCAGACAGCCGGTCAGCCCAATTGGAACCAGTCCGGTACGCTGTTCGAAGGTATCGAACGATGGGCGGAGCGAAAAGCTCTACTCACGCATGACGATGTCAAGCAACGGGCCTGGCAGACCCTGAAGTGGCAGATCGCCAACGGAATACAGCACGTACGCACTCACGTGGATGTTTCCGATGCCACTTTGACGGCATTGAAGGCAATGCTCGAAGTTAAGCAGGAAGTAGCCCCGTGGATTGACTTGCAAATCGCTGCCTTCCCTCAGGAAGGCATCCTAAGTTATCCGAATGGAGAAGCGCTCCTGGAGGAGGCATTGCGGTTAGGAGCAGACGTGGTGGGAGCGATTCCCCATTTCGAGTTTACCCGCGAGTACGGTGTTGAATCTCTGCATAAAACATTTGCTTTAGCTCAGAAGTATGACCGTCTGATCGACGTACACTGCGACGAGATCGATGACGAACAGAGTCGCTTCGTGGAGACGGTGGCTGCGCTGGCGCATCACGAAGGCATGGGTGCACGTGTAACTGCAAGCCATACGACGGCTATGCACAGCTATAATGGGGCATATACATCTCGTTTGTTCCGATTACTAAAAATGAGCGGAATCAACTTTGTTGCCAATCCATTGGTCAACATTCATCTACAAGGACGCTTCGACACCTACCCGAAACGGCGAGGAATCACACGAGTTAAGGAAATGCTAGAGTCTGGTATCAATGTGTGTTTCGGGCATGATGACGTGTGTGGTCCCTGGTACCCTCTAGGAACAGCCAACATGCTGCAAGTTCTCCACATGGGTCTACACGTGTGTCAACTCATGGGGTATGGACAAATTAACGATGGACTCAATCTAATTACACACCATTCCGCCCGAACACTGAACCTCCAGGATTACGGGATCGCGGCGGGAAATTCTGCCAACCTCATCATTCTGCCCGCGGAAAACGGGTTCGACGCTCTACGCCGTCAAGTGCCAGTTCGGTATTCTGTTCGTGGGGGTAAGGTAATTGCAAGTACCCAACCGGCTCAGACCACGGTCTATTTAGAGCAACCGGAAGCTATCGACTACAAACGATGAgcttcaaataaaacgaaaggctcagtcgaaagactgggcctttcgttttatctgttgtttgtcggtgaacgctctctactagagtcacactggctcaccttcgggtgggcctttctgcgcgctCTGAGGTCTGCCTCGTGAAGAAGGTGTTGCTGACTCATACCAGGCCTGAATCGCCCCATCATCCAGCCAGAAAGTGAGGGAGCCACGGTTGATGAGAGCTTTGTTGTAGGTGGACCAGTTGGTGATTTTGAACTTTTGCTTTGCCACGGAACGGTCTGCGTTGTCGGGAAGATGCGTGATCTGATCCTTCAACTCAGCAAAAGTTCGATTTATTCAACAAAGCCGCCGTCCCGTCAAGTCAGCGTAATGCTCTGCCAGTGTTACAACCAATTAACCAATTCTGATTAGAAAAACTCATCGAGCATCAAATGAAACTGCAATTTATTCATATCAGGATTATCAATACCATATTTTTGAAAAAGCCGTTTCTGTAATGAAGGAGAAAACTCACCGAGGCAGTTCCATAGGATGGCAAGATCCTGGTATCGGTCTGCGATTCCGACTCGTCCAACATCAATACAACCTATTAATTTCCCCTCGTCAAAAATAAGGTTATCAAGTGAGAAATCACCATGAGTGACGACTGAATCCGGTGAGAATGGCAAAAGCTTATGCATTTCTTTCCAGACTTGTTCAACAGGCCAGCCATTACGCTCGTCATCAAAATCACTCGCATCAACCAAACCGTTATTCATTCGTGATTGCGCCTGAGCGAGACGAAATACGCGATCGCTGTTAAAAGGACAATTACAAACAGGAATCGAATGCAACCGGCGCAGGAACACTGCCAGCGCATCAACAATATTTTCACCTGAATCAGGATATTCTTCTAATACCTGGAATGCTGTTTTCCCGGGGATCGCAGTGGTGAGTAACCATGCATCATCAGGAGTACGGATAAAATGCTTGATGGTCGGAAGAGGCATAAATTCCGTCAGCCAGTTTAGTCTGACCATCTCATCTGTAACATCATTGGCAACGCTACCTTTGCCATGTTTCAGAAACAACTCTGGCGCATCGGGCTTCCCATACAATCGATAGATTGTCGCACCTGATTGCCCGACATTATCGCGAGCCCATTTATACCCATATAAATCAGCATCCATGTTGGAATTTAATCGCGGCCTCGAGCAAGACGTTTCCCGTTGAATATGGCTCATAACACCCCTTGTATTACTGTTTATGTAAGCAGACAGTTTTATTGTTCATGATGATATATTTTTATCTTGTGCAATGTAACATCAGAGATTTTGAGACACAACGTGGCTTTCCGCGGTGCGGGTGCCAGGGCGTGCCCTTGGGCTCCCCGGGCGCGTACTCCACCACCTGCCATTGGGAGAAGACTTGGGAGCTCTTCataa";
        Sequence seq = doc.createSequence(name+"_seq", version, seqStr, Sequence.IUPAC_DNA);
        region.addSequence(seq);

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
        
        an = region.createSequenceAnnotation("AmpR", "AmpR", 197, 1057);
        an.addRole(SequenceOntology.CDS);    
        
        ComponentDefinition originD = doc.createComponentDefinition("ori", version, ComponentDefinition.DNA_REGION);
        originD.addRole(SequenceOntology.ORIGIN_OF_REPLICATION);
        
        Component origin = region.createComponent("ori_instance", AccessType.PUBLIC, originD.getIdentity());
        
        an = region.createSequenceAnnotation("ori", "ori", 1228, 1816);
        an.setComponent(origin.getIdentity());
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