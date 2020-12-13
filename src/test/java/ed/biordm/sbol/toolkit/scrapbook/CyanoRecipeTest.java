/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ed.biordm.sbol.toolkit.scrapbook;

import static ed.biordm.sbol.toolkit.scrapbook.CyanoTemplate.createTemplatePlasmid;
import static ed.biordm.sbol.toolkit.transform.CommonAnnotations.*;
import ed.biordm.sbol.toolkit.transform.GenBankConverter;
import ed.biordm.sbol.toolkit.transform.TemplateTransformer;
import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import javax.xml.namespace.QName;
import static org.junit.Assert.*;
import org.junit.Ignore;
import org.junit.Test;
import org.sbolstandard.core2.AccessType;
import org.sbolstandard.core2.Component;
import org.sbolstandard.core2.ComponentDefinition;
import org.sbolstandard.core2.SBOLDocument;
import org.sbolstandard.core2.SBOLReader;
import org.sbolstandard.core2.SBOLValidate;
import org.sbolstandard.core2.SBOLValidationException;
import org.sbolstandard.core2.SBOLWriter;
import org.sbolstandard.core2.Sequence;
import org.sbolstandard.core2.SequenceAnnotation;
import org.sbolstandard.core2.SequenceOntology;

/**
 *
 * @author Tomasz Zielinski
 */
public class CyanoRecipeTest {

    Path templateFile = Paths.get("E:\\Temp\\cyano_full_template.xml");
    //Path templateFile = Paths.get("D:\\Temp\\sbol\\cyano_full_template.xml");

    private static final String TMP_PATH = "E:/Temp/";
    //private static final String TMP_PATH = "D:/Temp/sbol/";

    String version = "1.0.0";
    
    @Test
    public void makeSll0199() throws Exception {

        //SBOLDocument templateDoc = SBOLReader.read(templateFile.toFile());
        SBOLDocument templateDoc = new SBOLDocument();
        templateDoc.setDefaultURIprefix("http://bio.ed.ac.uk/a_mccormick/cyano_source/");
        templateDoc.setComplete(true);
        templateDoc.setCreateDefaults(true);
        
        

        TemplateTransformer transformer = new TemplateTransformer();

        //ComponentDefinition template = templateDoc.getComponentDefinition("cyano_codA_Km", version);
        ComponentDefinition template = createTemplatePlasmid(templateDoc, version);
        assertNotNull(template);

        String description = template.getDescription();
        ComponentDefinition sll0199 = transformer.instantiateFromTemplate(template, "sll0199", version,
                description, templateDoc);
        
        //sll0199.createAnnotation(SBH_DESCRIPTION, "Generate a description for each plasmid, for example\n"
        //        + "Recombinant plasmid targetting sll0199");

        //String lFlankSeq = "caaggcaaaaccaccgttatcagcagaacgacggcgggaaaaaatgattaaacgaaaaaatttgcaaggattcatagcggttgcccaatctaactcagggagcgacttcagcccacaaaaaacaccactgggcctactgggctattcccattatcatctacattgaagggatagcaagctaatttttatgacggcgatcgccaaaaacaaagaaaattcagcaattaccgtgggtagcaaaaaatccccatctaaagttcagtaaatatagctagaacaaccaagcattttcggcaaagtactattcagatagaacgagaaatgagcttgttctatccgcccggggctgaggctgtataatctacgacgggctgtcaaacattgtgataccatgggcagaagaaaggaaaaacgtccctgatcgcctttttgggcacggagtagggcgttaccccggcccgttcaaccacaagtccctatAGATACAATCGCCAAGAAGT";
        String lFlankSeq = "CACTAGGCCAACCATAATGGCCATCGGCAAGGCAAAACCACCGTTATCAGCAGAACGACGGCGGGAAAAAATGATTAAACGAAAAAATTTGCAAGGATTCATAGCGGTTGCCCAATCTAACTCAGGGAGCGACTTCAGCCCACAAAAAACACCACTGGGCCTACTGGGCTATTCCCATTATCATCTACATTGAAGGGATAGCAAGCTAATTTTTATGACGGCGATCGCCAAAAACAAAGAAAATTCAGCAATTACCGTGGGTAGCAAAAAATCCCCATCTAAAGTTCAGTAAATATAGCTAGAACAACCAAGCATTTTCGGCAAAGTACTATTCAGATAGAACGAGAAATGAGCTTGTTCTATCCGCCCGGGGCTGAGGCTGTATAATCTACGACGGGCTGTCAAACATTGTGATACCATGGGCAGAAGAAAGGAAAAACGTCCCTGATCGCCTTTTTGGGCACGGAGTAGGGCGTTACCCCGGCCCGTTCAACCACAAGTCCCTATAGATACAATCGCCAAGAAGT";
        transformer.concretizePart(sll0199, "left", "sll0199_left", lFlankSeq, templateDoc);

        //String rFlankSeq = "tcagccagctcaatctgtgtgtcgttgatttaagcttaatgctacggggtctgtctccaactccctcagcttctcgcaatggcaaggcaaataatgtttctcttgctgagtagatgttcaggaggacggatcgaaagtctacaaaacagattcttgaccaagccatctacttagaaaaacttctgcgttttggcgatcgcatcttttaagcgagatgcgatttttttgtccattagtttgtattttaatactcttttgttgtttgatttcgtccaagcttttcttggtatgtgggatcttccgtgcccaaaattttatcccagaaagtgaaatatagtcatttcaattaacgatgagagaatttaatgtaaaattatggagtgtacaaaatgaacaggtttaaacaatggcttacagtttagatttaaggcaaagggtagtagcttatatagaagctggaggaaaaataactgaggcttccaagatatataaaataggaaaagcctcgatatacagatggttaaatagagtagatttaagcccaacaaaagtagagcgtcgccatagg";
        String rFlankSeq = "TCAGCCAGCTCAATCTGTGTGTCGTTGATTTAAGCTTAATGCTACGGGGTCTGTCTCCAACTCCCTCAGCTTCTCGCAATGGCAAGGCAAATAATGTTTCTCTTGCTGAGTAGATGTTCAGGAGGACGGATCGAAAGTCTACAAAACAGATTCTTGACCAAGCCATCTACTTAGAAAAACTTCTGCGTTTTGGCGATCGCATCTTTTAAGCGAGATGCGATTTTTTTGTCCATTAGTTTGTATTTTAATACTCTTTTGTTGTTTGATTTCGTCCAAGCTTTTCTTGGTATGTGGGATCTTCCGTGCCCAAAATTTTATCCCAGAAAGTGAAATATAGTCATTTCAATTAACGATGAGAGAATTTAATGTAAAATTATGGAGTGTACAAAATGAACAGGTTTAAACAATGGCTTACAGTTTAGATTTAAGGCAAAGGGTAGTAGCTTATATAGAAGCTGGAGGAAAAATAACTGAGGCTTCCAAGATATATAAAATAGGAAAAGCCTCGATATACAGATGGT";
        transformer.concretizePart(sll0199, "right", "sll0199_right", rFlankSeq, templateDoc);

        //to make it top level
        sll0199.clearWasDerivedFroms();
        
        ComponentDefinition sll0199Flat = transformer.flattenSequences2(sll0199, "sl0199_flatten", templateDoc);
        
        //to make it top level
        sll0199Flat.clearWasDerivedFroms();

        SBOLValidate.validateSBOL(templateDoc, true, true, true);
        if (SBOLValidate.getNumErrors() > 0) {
            for (String error : SBOLValidate.getErrors()) {
                System.out.println(error);
            }
            throw new IllegalStateException("Stoping cause of validation errors");
        }

        try {
            SBOLWriter.write(templateDoc, TMP_PATH + "cyano_sl1099.xml");
        } catch (IOException e) {
            e.printStackTrace();
            throw e;
        }
    }
    
    @Test
    @Ignore
    public void testGBConversion() throws Exception {
        SBOLDocument doc = SBOLReader.read(TMP_PATH + "cyano_sl1099.xml");
        doc.setDefaultURIprefix("http://bio.ed.ac.uk/a_mccormick/cyano_source/");
        
        ComponentDefinition sll0199Flat = doc.getComponentDefinition("sl0199_flatten", version);
        assertNotNull(sll0199Flat);
        
        try (Writer w =Files.newBufferedWriter(Paths.get(TMP_PATH).resolve("sl0199_flatten.gb"))) {
            GenBankConverter.write(sll0199Flat, w);
        };
        
        
    }

    @Test
    @Ignore("Used to generate sbol with different id/names/labels combination to check rendering")
    public void testNaming() throws Exception {

        String version = "1.0.0";
        SBOLDocument doc = new SBOLDocument();
        doc.setDefaultURIprefix("http://bio.ed.ac.uk/a_mccormick/cyano_source/");
        doc.setComplete(true);
        doc.setCreateDefaults(true);

        String name = "backbone";
        ComponentDefinition region = doc.createComponentDefinition(name, version, ComponentDefinition.DNA_REGION);
        region.addRole(SequenceOntology.ENGINEERED_REGION);
        region.setDescription("backbonde sbol description");
        region.createAnnotation(new QName("http://wiki.synbiohub.org/wiki/Terms/synbiohub#", "mutableDescription"), "SynBio descriptio description");

        String seqStr = "CGCTGCTTACAGACAAGCTGTGACCGTCTCCGGGAGCTGCATGTGTCAGAGGTTTTCACCGTCATCACCGAAACGCGCGAGACGAAAGGGCCTCGTGATACGCCTATTTTTATAGGTTAATGTCATGATAATAATGGTTTCTTAGACGTCAGGTGGCACTTTTCGGGGAAATGTGCGCGGAACCCCTATTTGTTTATTTTTCTAAATACATTCAAATATGTATCCGCTCATGAGACAATAACCCTGATAAATGCTTCAATAATATTGAAAAAGGAAGAGTATGAGTATTCAACATTTCCGTGTCGCCCTTATTCCCTTTTTTGCGGCATTTTGCCTTCCTGTTTTTGCTCACCCAGAAACGCTGGTGAAAGTAAAAGATGCTGAAGATCAGTTGGGTGCACGAGTGGGTTACATCGAACTGGATCTCAACAGCGGTAAGATCCTTGAGAGTTTTCGCCCCGAAGAACGTTTTCCAATGATGAGCACTTTTAAAGTTCTGCTATGTGGCGCGGTATTATCCCGTATTGACGCCGGGCAAGAGCAACTCGGTCGCCGCATACACTATTCTCAGAATGACTTGGTTGAGTACTCACCAGTCACAGAAAAGCATCTTACGGATGGCATGACAGTAAGAGAATTATGCAGTGCTGCCATAACCATGAGTGATAACACTGCGGCCAACTTACTTCTGACAACGATCGGAGGACCGAAGGAGCTAACCGCTTTTTTGCACAACATGGGGGATCATGTAACTCGCCTTGATCGTTGGGAACCGGAGCTGAATGAAGCCATACCAAACGACGAGCGTGACACCACGATGCCTGTAGCAATGGCAACAACGTTGCGCAAACTATTAACTGGCGAACTACTTACTCTAGCTTCCCGGCAACAATTAATAGACTGGATGGAGGCGGATAAAGTTGCAGGACCACTTCTGCGCTCGGCCCTTCCGGCTGGCTGGTTTATTGCTGATAAATCTGGAGCCGGTGAGCGTGGTTCTCGCGGTATCATTGCAGCACTGGGGCCAGATGGTAAGCCCTCCCGTATCGTAGTTATCTACACGACGGGGAGTCAGGCAACTATGGATGAACGAAATAGACAGATCGCTGAGATAGGTGCCTCACTGATTAAGCATTGGTAACTGTCAGACCAAGTTTACTCATATATACTTTAGATTGATTTAAAACTTCATTTTTAATTTAAAAGGATCTAGGTGAAGATCCTTTTTGATAATCTCATGACCAAAATCCCTTAACGTGAGTTTTCGTTCCACTGAGCGTCAGACCCCGTAGAAAAGATCAAAGGATCTTCTTGAGATCCTTTTTTTCTGCGCGTAATCTGCTGCTTGCAAACAAAAAAACCACCGCTACCAGCGGTGGTTTGTTTGCCGGATCAAGAGCTACCAACTCTTTTTCCGAAGGTAACTGGCTTCAGCAGAGCGCAGATACCAAATACTGTTCTTCTAGTGTAGCCGTAGTTAGGCCACCACTTCAAGAACTCTGTAGCACCGCCTACATACCTCGCTCTGCTAATCCTGTTACCAGTGGCTGCTGCCAGTGGCGATAAGTCGTGTCTTACCGGGTTGGACTCAAGACGATAGTTACCGGATAAGGCGCAGCGGTCGGGCTGAACGGGGGGTTCGTGCACACAGCCCAGCTTGGAGCGAACGACCTACACCGAACTGAGATACCTACAGCGTGAGCTATGAGAAAGCGCCACGCTTCCCGAAGGGAGAAAGGCGGACAGGTATCCGGTAAGCGGCAGGGTCGGAACAGGAGAGCGCACGAGGGAGCTTCCAGGGGGAAACGCCTGGTATCTTTATAGTCCTGTCGGGTTTCGCCACCTCTGACTTGAGCGTCGATTTTTGTGATGCTCGTCAGGGGGGCGGAGCCTATGGAAAAACGCCAGCAACGCGGCCTTTTTACGGTTCCTGGCCTTTTGCTGGCCTTTTGCTCACATGTTCTTTCCTGCGTTATCCCCTGATTCTGTGGATAACCGTATTACCGCCTTTGAGTGAGCTGATACCGCTCGCCGCAGCCGAACGACCGAGCGCAGCGAGTCAGTGAGCGAGGAAGCGGATGAGCGCCCAATACGCAAACCGCCTCTCCCCGCGCGTTGGCCGATTCATTAATGCAGCTGGCACGACAGGTTTCggag";
        Sequence seq = doc.createSequence(name + "_seq", version, seqStr, Sequence.IUPAC_DNA);
        region.addSequence(seq);

        SequenceAnnotation an;

        an = region.createSequenceAnnotation("AmpR_prom", "AmpR_prom", 176, 280);
        an.addRole(SequenceOntology.PROMOTER);
        an.createAnnotation(new QName("http://sbols.org/genBankConversion#", "gene", "gbconv"),
                "bla");

        an = region.createSequenceAnnotation("AmpR_Ann", "AmpR_Ann_Loc", 281, 1141);
        an.addRole(SequenceOntology.CDS);
        an.createAnnotation(new QName("http://sbols.org/genBankConversion#", "note", "gbconv"),
                "confers resistance to ampicillin, carbenicillin, and related antibiotics");
        an.createAnnotation(new QName("http://sbols.org/genBankConversion#", "gene", "gbconv"),
                "bla");
        an.createAnnotation(new QName("http://sbols.org/genBankConversion#", "product", "gbconv"),
                "beta-lactamase");
        an.setDescription("AmpR annot desc");

        ComponentDefinition originD = doc.createComponentDefinition("ori", version, ComponentDefinition.DNA_REGION);
        originD.addRole(SequenceOntology.ORIGIN_OF_REPLICATION);
        originD.createAnnotation(new QName("http://sbols.org/genBankConversion#", "note", "gbconv"),
                "high-copy-number ColE1/pMB1/pBR322/pUC origin of replication");
        originD.setDescription("high-copy-number ColE1/pMB1/pBR322/pUC origin of replication");

        seqStr = "TTGAGATCCTTTTTTTCTGCGCGTAATCTGCTGCTTGCAAACAAAAAAACCACCGCTACCAGCGGTGGTTTGTTTGCCGGATCAAGAGCTACCAACTCTTTTTCCGAAGGTAACTGGCTTCAGCAGAGCGCAGATACCAAATACTGTTCTTCTAGTGTAGCCGTAGTTAGGCCACCACTTCAAGAACTCTGTAGCACCGCCTACATACCTCGCTCTGCTAATCCTGTTACCAGTGGCTGCTGCCAGTGGCGATAAGTCGTGTCTTACCGGGTTGGACTCAAGACGATAGTTACCGGATAAGGCGCAGCGGTCGGGCTGAACGGGGGGTTCGTGCACACAGCCCAGCTTGGAGCGAACGACCTACACCGAACTGAGATACCTACAGCGTGAGCTATGAGAAAGCGCCACGCTTCCCGAAGGGAGAAAGGCGGACAGGTATCCGGTAAGCGGCAGGGTCGGAACAGGAGAGCGCACGAGGGAGCTTCCAGGGGGAAACGCCTGGTATCTTTATAGTCCTGTCGGGTTTCGCCACCTCTGACTTGAGCGTCGATTTTTGTGATGCTCGTCAGGGGGGCGGAGCCTATGGAAA";
        seq = doc.createSequence("ori_seq", version, seqStr, Sequence.IUPAC_DNA);
        originD.addSequence(seq);

        Component origin = region.createComponent("ori_instance", AccessType.PUBLIC, originD.getIdentity());

        an = region.createSequenceAnnotation("ori_ann", "ori_loc", 1312, 1900);
        an.setComponent(origin.getIdentity());

        try {
            SBOLWriter.write(doc, TMP_PATH + "naming.xml");
        } catch (IOException e) {
            e.printStackTrace();
            throw e;
        }
    }

}
