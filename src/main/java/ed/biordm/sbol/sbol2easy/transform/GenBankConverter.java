/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ed.biordm.sbol.sbol2easy.transform;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import javax.xml.namespace.QName;
import org.sbolstandard.core2.Annotation;
import org.sbolstandard.core2.Component;
import org.sbolstandard.core2.ComponentDefinition;
import org.sbolstandard.core2.Cut;
import org.sbolstandard.core2.Identified;
import org.sbolstandard.core2.Location;
import org.sbolstandard.core2.OrientationType;
import org.sbolstandard.core2.Range;
import org.sbolstandard.core2.SBOLConversionException;
import org.sbolstandard.core2.SBOLDocument;
import static org.sbolstandard.core2.SBOLHack.conversionException;
import org.sbolstandard.core2.SBOLValidationException;
import org.sbolstandard.core2.Sequence;
import org.sbolstandard.core2.SequenceAnnotation;
import org.sbolstandard.core2.SequenceOntology;
//import org.sbolstandard.core2.URIcompliance;

    /**
     * This class provides methods for converting GenBank files to and from SBOL
     * 2.0 files.
     * It is based on sbol oficial library but read the mapping between features and SO from a file
     * which makes it easier to maintain.
     *
     * @author Chris Myers
     * @author Ernst Oberortner
     * @version 2.1
     */public class GenBankConverter {


    private static SequenceOntology so = null;

    public static final String GBPREFIX = "genbank";
    public static final String GBNAMESPACE = "http://www.ncbi.nlm.nih.gov/genbank#";
    public static final String LOCUS = "locus";
    public static final String REGION = "region";
    public static final String MOLECULE = "molecule";
    public static final String TOPOLOGY = "topology"; // Only used for backward compatiblity with 2.1.0
    public static final String DIVISION = "division";
    public static final String DATE = "date";
    public static final String GINUMBER = "GInumber";
    public static final String KEYWORDS = "keywords";
    public static final String SOURCE = "source";
    public static final String ORGANISM = "organism";
    public static final String REFERENCE = "reference";
    public static final String NESTEDREFERENCE = "Reference";
    public static final String LABEL = "label";
    public static final String AUTHORS = "authors";
    public static final String TITLE = "title";
    public static final String JOURNAL = "journal";
    public static final String MEDLINE = "medline";
    public static final String PUBMED = "pubmed";
    public static final String COMMENT = "comment";
    public static final String BASECOUNT = "baseCount";
    public static final String FEATURETYPE = "featureType";

    public static final String GBCONVPREFIX = "gbConv";
    public static final String GBCONVNAMESPACE = "http://sbols.org/genBankConversion#";
    public static final String POSITION = "position";
    public static final String STRADLESORIGIN = "stradlesOrigin";
    public static final String STARTLESSTHAN = "startLessThan";
    public static final String ENDGREATERTHAN = "endGreaterThan";
    public static final String SINGLEBASERANGE = "singleBaseRange";
    public static final String MULTIRANGETYPE = "multiRangeType";

    // locus line
    protected static final Pattern lp = Pattern.compile("LOCUS\\s+([\\S+\\s]*)\\s+(\\d+)\\s+(bp|BP|aa|AA)\\s{0,4}(([dmsDMS][sS]-)?(\\S+))?\\s*(circular|CIRCULAR|linear|LINEAR)?\\s*(\\S+)?\\s*(\\S+)?$");

    protected static final GenBank2SO genBank2SO = new GenBank2SO();

    // This array holds the QName local part Strings that should be written as "/note" elements in the GenBank output file
    protected static final String[] noteQNames = new String[]{"description", "mutableDescription"};

    private static void writeGenBankLine(Writer w, String line, int margin, int indent) throws IOException {
        if (line.length() < margin) {
            w.write(line + "\n");
        } else {
            String spaces = "";
            for (int i = 0; i < indent; i++) {
                spaces += " ";
            }
            int breakPos = line.substring(0, margin - 1).lastIndexOf(" ") + 1;
            if (breakPos == 0 || breakPos < 0.75 * margin) {
                breakPos = margin - 1;
            }
            w.write(line.substring(0, breakPos) + "\n");
            int i = breakPos;
            while (i < line.length()) {
                if ((i + (margin - indent)) < line.length()) {
                    breakPos = line.substring(i, i + (margin - indent) - 1).lastIndexOf(" ") + 1;
                    if (breakPos == 0 || breakPos < 0.65 * margin) {
                        breakPos = (margin - indent) - 1;
                    }
                    w.write(spaces + line.substring(i, i + breakPos) + "\n");
                } else {
                    w.write(spaces + line.substring(i) + "\n");
                    breakPos = (margin - indent) - 1;
                }
                i += breakPos;
            }
        }
    }

    protected static void writeComponentDefinition(ComponentDefinition componentDefinition, Writer w) throws IOException, SBOLConversionException {
        so = new SequenceOntology();
        Sequence seq = null;
        for (Sequence sequence : componentDefinition.getSequences()) {
            if (sequence.getEncoding().equals(Sequence.IUPAC_DNA)
                    || sequence.getEncoding().equals(Sequence.IUPAC_RNA)) {
                seq = sequence;
                break;
            }
        }
        if (seq == null) {
            throw conversionException("ComponentDefinition " + componentDefinition.getIdentity()
                    + " does not have an IUPAC sequence.");
        }
        int size = seq.getElements().length();
        writeHeader(w, componentDefinition, size);
        writeReferences(w, componentDefinition);
        writeComment(w, componentDefinition);
        w.write("FEATURES             Location/Qualifiers\n");
        recurseComponentDefinition(componentDefinition, w, 0, true, 0);
        w.write("ORIGIN\n");
        writeSequence(w, seq, size);
        w.write("//\n");
    }

    /**
     * Serializes a given ComponentDefinition and outputs the data from the
     * serialization to the given output stream in GenBank format.
     *
     * @param componentDefinition a given ComponentDefinition
     * @param out the given output file name in GenBank format
     * @throws IOException input/output operation failed
     * @throws SBOLConversionException violates conversion limitations
     */
    public static void write(ComponentDefinition componentDefinition, Writer w) throws IOException, SBOLConversionException {
        writeComponentDefinition(componentDefinition, w);
    }

    /**
     * Serializes a given SBOLDocument and outputs the data from the
     * serialization to the given output stream in GenBank format.
     *
     * @param sbolDocument a given SBOLDocument
     * @param out the given output file name in GenBank format
     * @throws IOException input/output operation failed
     * @throws SBOLConversionException violates conversion limitations
     */
    public static void write(SBOLDocument sbolDocument, OutputStream out) throws IOException, SBOLConversionException {
        try (Writer w = new OutputStreamWriter(out, "UTF-8")) {
            for (ComponentDefinition componentDefinition : sbolDocument.getRootComponentDefinitions()) {
                write(componentDefinition, w);
            }
        } 
    }

    private static String convertSOtoGenBank(String soTerm) {
        
        // Use the custom term converter to derive the feature type if it's misc_feature
        String featureType = String.format("%-15s", genBank2SO.termToFeature(soTerm));
        return featureType;
    }

    private static URI convertGenBanktoSO(String genBankTerm) {
        if (genBankTerm.equals("allele")) {
            return so.getURIbyId("SO:0001023");
        }
        if (genBankTerm.equals("assembly_gap")) {
            return so.getURIbyId("SO:0000730");
        }
        if (genBankTerm.equals("attenuator")) {
            return so.getURIbyId("SO:0000140");
        }
        if (genBankTerm.equals("C_region")) {
            return so.getURIbyId("SO:0001834");
        }
        if (genBankTerm.equals("CAAT_signal")) {
            return so.getURIbyId("SO:0000172");
        }
        if (genBankTerm.equals("CDS")) {
            return so.getURIbyId("SO:0000316");
        }
        if (genBankTerm.equals("centromere")) {
            return so.getURIbyId("SO:0000577");
        }
        /* if (genBankTerm.equals("conflict")) {
		return so.getURIbyId("SO_");} */
        if (genBankTerm.equals("D-loop")) {
            return so.getURIbyId("SO:0000297");
        }
        if (genBankTerm.equals("D_segment")) {
            return so.getURIbyId("SO:0000458");
        }
        if (genBankTerm.equals("enhancer")) {
            return so.getURIbyId("SO:0000165");
        }
        if (genBankTerm.equals("exon")) {
            return so.getURIbyId("SO:0000147");
        }
        if (genBankTerm.equals("gap")) {
            return so.getURIbyId("SO:0000730");
        }
        if (genBankTerm.equals("gene")) {
            return so.getURIbyId("SO:0000704");
        }
        if (genBankTerm.equals("GC_signal")) {
            return so.getURIbyId("SO:0000173");
        }
        if (genBankTerm.equals("iDNA")) {
            return so.getURIbyId("SO:0000723");
        }
        if (genBankTerm.equals("intron")) {
            return so.getURIbyId("SO:0000188");
        }
        if (genBankTerm.equals("J_region")) {
            return so.getURIbyId("SO:0000470");
        }
        if (genBankTerm.equals("J_gene_segment")) {
            return so.getURIbyId("SO:0000470");
        }
        if (genBankTerm.equals("J_segment")) {
            return so.getURIbyId("SO:0000470");
        }
        if (genBankTerm.equals("LTR")) {
            return so.getURIbyId("SO:0000286");
        }
        if (genBankTerm.equals("mat_peptide")) {
            return so.getURIbyId("SO:0000419");
        }
        if (genBankTerm.equals("misc_binding")) {
            return so.getURIbyId("SO:0000409");
        }
        if (genBankTerm.equals("misc_difference")) {
            return so.getURIbyId("SO:0000413");
        }
        if (genBankTerm.equals("misc_feature")) {
            return so.getURIbyId("SO:0001411");
        }
//		return so.getURIbyId("SO:0000001");}
        if (genBankTerm.equals("misc_marker")) {
            return so.getURIbyId("SO:0001645");
        }
        if (genBankTerm.equals("misc_recomb")) {
            return so.getURIbyId("SO:0000298");
        }
        if (genBankTerm.equals("misc_RNA")) {
            return so.getURIbyId("SO:0000673");
        }
//		return so.getURIbyId("SO:0000233");}
        if (genBankTerm.equals("misc_signal")) {
            return so.getURIbyId("SO:0001411");
        }
        if (genBankTerm.equals("misc_structure")) {
            return so.getURIbyId("SO:0000002");
        }
        if (genBankTerm.equals("mobile_element")) {
            return so.getURIbyId("SO:0001037");
        }
        if (genBankTerm.equals("mobile_genetic_element")) {
            return so.getURIbyId("SO:0001037");
        }
        if (genBankTerm.equals("modified_base")) {
            return so.getURIbyId("SO:0000305");
        }
        if (genBankTerm.equals("mRNA")) {
            return so.getURIbyId("SO:0000234");
        }
        /* if (genBankTerm.equals("mutation")) {
		return so.getURIbyId("SO_");} */
        if (genBankTerm.equals("N_region")) {
            return so.getURIbyId("SO:0001835");
        }
        if (genBankTerm.equals("old_sequence")) {
            return so.getURIbyId("SO:0000413");
        } // TODO: alias with misc_difference
        if (genBankTerm.equals("ncRNA")) {
            return so.getURIbyId("SO:0000655");
        }
        if (genBankTerm.equals("operon")) {
            return so.getURIbyId("SO:0000178");
        }
        if (genBankTerm.equals("oriT")) {
            return so.getURIbyId("SO:0000724");
        }
        if (genBankTerm.equals("polyA_signal")) {
            return so.getURIbyId("SO:0000551");
        }
        if (genBankTerm.equals("polyA_site")) {
            return so.getURIbyId("SO:0000553");
        }
        if (genBankTerm.equals("precursor_RNA")) {
            return so.getURIbyId("SO:0000185");
        }
        if (genBankTerm.equals("prim_transcript")) {
            return so.getURIbyId("SO:0000185");
        }
        if (genBankTerm.equals("primer")) {
            return so.getURIbyId("SO:0000112");
        }
        if (genBankTerm.equals("primer_bind")) {
            return so.getURIbyId("SO:0005850");
        }
        if (genBankTerm.equals("promoter")) {
            return so.getURIbyId("SO:0000167");
        }
        if (genBankTerm.equals("promoter")) {
            return so.getURIbyId("SO:0000167");
        }
        if (genBankTerm.equals("propeptide")) {
            return so.getURIbyId("SO:0001062");
        }
        if (genBankTerm.equals("RBS")) {
            return so.getURIbyId("SO:0000139");
        }
        if (genBankTerm.equals("rep_origin")) {
            return so.getURIbyId("SO:0000296");
        }
        if (genBankTerm.equals("repeat_region")) {
            return so.getURIbyId("SO:0000657");
        }
        if (genBankTerm.equals("repeat_unit")) {
            return so.getURIbyId("SO:0000726");
        }
        if (genBankTerm.equals("rRNA")) {
            return so.getURIbyId("SO:0000252");
        }
        if (genBankTerm.equals("S_region")) {
            return so.getURIbyId("SO:0001836");
        }
        if (genBankTerm.equals("satellite")) {
            return so.getURIbyId("SO:0000005");
        }
        if (genBankTerm.equals("scRNA")) {
            return so.getURIbyId("SO:0000013");
        }
        if (genBankTerm.equals("sig_peptide")) {
            return so.getURIbyId("SO:0000418");
        }
        if (genBankTerm.equals("snRNA")) {
            return so.getURIbyId("SO:0000274");
        }
        if (genBankTerm.equals("source")) {
            return so.getURIbyId("SO:0002206");
        }
//		return so.getURIbyId("SO:0000149");}
        if (genBankTerm.equals("stem_loop")) {
            return so.getURIbyId("SO:0000313");
        }
        if (genBankTerm.equals("STS")) {
            return so.getURIbyId("SO:0000331");
        }
        if (genBankTerm.equals("TATA_signal")) {
            return so.getURIbyId("SO:0000174");
        }
        if (genBankTerm.equals("telomere")) {
            return so.getURIbyId("SO:0000624");
        }
        if (genBankTerm.equals("terminator")) {
            return so.getURIbyId("SO:0000141");
        }
        if (genBankTerm.equals("tmRNA")) {
            return so.getURIbyId("SO:0000584");
        }
        if (genBankTerm.equals("transit_peptide")) {
            return so.getURIbyId("SO:0000725");
        }
        if (genBankTerm.equals("transposon")) {
            return so.getURIbyId("SO:0001054");
        }
        if (genBankTerm.equals("tRNA")) {
            return so.getURIbyId("SO:0000253");
        }
        if (genBankTerm.equals("sequence_uncertainty")) {
            return so.getURIbyId("SO:0001086");
        }
        if (genBankTerm.equals("unsure")) {
            return so.getURIbyId("SO:0001086");
        }
        if (genBankTerm.equals("V_region")) {
            return so.getURIbyId("SO:0001833");
        }
        if (genBankTerm.equals("variation")) {
            return so.getURIbyId("SO:0001060");
        }
        if (genBankTerm.equals("-10_signal")) {
            return so.getURIbyId("SO:0000175");
        }
        if (genBankTerm.equals("-35_signal")) {
            return so.getURIbyId("SO:0000176");
        }
        if (genBankTerm.equals("3'clip")) {
            return so.getURIbyId("SO:0000557");
        }
        if (genBankTerm.equals("3'UTR")) {
            return so.getURIbyId("SO:0000205");
        }
        if (genBankTerm.equals("5'clip")) {
            return so.getURIbyId("SO:0000555");
        }
        if (genBankTerm.equals("5'UTR")) {
            return so.getURIbyId("SO:0000204");
        }
        if (genBankTerm.equals("regulatory")) {
            return so.getURIbyId("SO:0005836");
        }
        if (genBankTerm.equals("snoRNA")) {
            return so.getURIbyId("SO:0000275");
        }
        if (genBankTerm.equals("V_gene_segment")) {
            return so.getURIbyId("SO:0000466");
        }
        if (genBankTerm.equals("V_segment")) {
            return so.getURIbyId("SO:0000466");
        }
        return so.getURIbyId("SO:0000110");
        //return null;
        /*
		URI soTerm = so.getURIbyName(genBankTerm);
		if (soTerm==null && genBankTerm.equals("misc_feature")) {
			soTerm = SequenceOntology.ENGINEERED_REGION;
		}
		return soTerm;
         */
    }

    private static void writeHeader(Writer w, ComponentDefinition componentDefinition, int size) throws SBOLConversionException, IOException {
        String locus = componentDefinition.getDisplayId().substring(0,
                componentDefinition.getDisplayId().length() > 15 ? 15 : componentDefinition.getDisplayId().length());
        Annotation annotation = componentDefinition.getAnnotation(new QName(GBNAMESPACE, LOCUS, GBPREFIX));
        if (annotation != null) {
            locus = annotation.getStringValue();
        }
        String type = null;
        for (URI typeURI : componentDefinition.getTypes()) {
            if (typeURI.equals(ComponentDefinition.RNA_REGION)) {
                type = "RNA";
                break;
            } else if (typeURI.equals(ComponentDefinition.DNA_REGION)) {
                type = "DNA";
            }
        }
        if (type == null) {
            throw conversionException("ComponentDefinition " + componentDefinition.getIdentity()
                    + " is not DNA or RNA type.");
        }
        annotation = componentDefinition.getAnnotation(new QName(GBNAMESPACE, MOLECULE, GBPREFIX));
        if (annotation != null) {
            type = annotation.getStringValue();
        }
        String linCirc = "linear";
        // Below only needed for backwards compatibility with 2.1.0 converter.
        annotation = componentDefinition.getAnnotation(new QName(GBNAMESPACE, TOPOLOGY, GBPREFIX));
        if (annotation != null) {
            linCirc = annotation.getStringValue();
        }
        if (componentDefinition.containsType(SequenceOntology.CIRCULAR)) {
            linCirc = "circular";
        }
        if (componentDefinition.containsType(SequenceOntology.LINEAR)) {
            linCirc = "linear";
        }
        String division = "   "; //UNK";
        annotation = componentDefinition.getAnnotation(new QName(GBNAMESPACE, DIVISION, GBPREFIX));
        if (annotation != null) {
            division = annotation.getStringValue();
        }
        DateFormat df = new SimpleDateFormat("dd-MMM-yyyy");
        Date dateobj = new Date();
        String date = df.format(dateobj);
        annotation = componentDefinition.getAnnotation(new QName(GBNAMESPACE, DATE, GBPREFIX));
        if (annotation != null) {
            date = annotation.getStringValue();
        }
        String locusLine = "LOCUS       " + String.format("%-16s", locus) + " "
                + String.format("%11s", "" + size) + " bp " + "   " + String.format("%-6s", type) + "  "
                + String.format("%-8s", linCirc) + " " + division + " " + date + "\n";
        w.write(locusLine);
        if (componentDefinition.isSetDescription()) {
            writeGenBankLine(w, "DEFINITION  " + componentDefinition.getDescription(), 80, 12);
        }
        String region = "";
        annotation = componentDefinition.getAnnotation(new QName(GBNAMESPACE, REGION, GBPREFIX));
        if (annotation != null) {
            region = annotation.getStringValue();
            w.write("ACCESSION   " + componentDefinition.getDisplayId() + " REGION: " + region + "\n");
        } else {
            w.write("ACCESSION   " + componentDefinition.getDisplayId() + "\n");
        }
        if (componentDefinition.isSetVersion()) {
            String giNumber = "";
            annotation = componentDefinition.getAnnotation(new QName(GBNAMESPACE, GINUMBER, GBPREFIX));
            if (annotation != null) {
                giNumber = annotation.getStringValue();
            }
            w.write("VERSION     " + componentDefinition.getDisplayId() + "."
                    + componentDefinition.getVersion() + "  " + giNumber + "\n");
        }
        annotation = componentDefinition.getAnnotation(new QName(GBNAMESPACE, KEYWORDS, GBPREFIX));
        if (annotation != null) {
            w.write("KEYWORDS    " + annotation.getStringValue() + "\n");
        }
        annotation = componentDefinition.getAnnotation(new QName(GBNAMESPACE, SOURCE, GBPREFIX));
        if (annotation != null) {
            w.write("SOURCE      " + annotation.getStringValue() + "\n");
        }
        annotation = componentDefinition.getAnnotation(new QName(GBNAMESPACE, ORGANISM, GBPREFIX));
        if (annotation != null) {
            writeGenBankLine(w, "  ORGANISM  " + annotation.getStringValue(), 80, 12);
        }
    }

    private static void writeReferences(Writer w, ComponentDefinition componentDefinition) throws IOException {
        for (Annotation a : componentDefinition.getAnnotations()) {
            if (a.getQName().equals(new QName(GBNAMESPACE, REFERENCE, GBPREFIX))) {
                String label = null;
                String authors = null;
                String title = null;
                String journal = null;
                String medline = null;
                String pubmed = null;
                for (Annotation ref : a.getAnnotations()) {
                    if (ref.getQName().equals(new QName(GBNAMESPACE, LABEL, GBPREFIX))) {
                        label = ref.getStringValue();
                    } else if (ref.getQName().equals(new QName(GBNAMESPACE, AUTHORS, GBPREFIX))) {
                        authors = ref.getStringValue();
                    } else if (ref.getQName().equals(new QName(GBNAMESPACE, TITLE, GBPREFIX))) {
                        title = ref.getStringValue();
                    } else if (ref.getQName().equals(new QName(GBNAMESPACE, JOURNAL, GBPREFIX))) {
                        journal = ref.getStringValue();
                    } else if (ref.getQName().equals(new QName(GBNAMESPACE, MEDLINE, GBPREFIX))) {
                        medline = ref.getStringValue();
                    } else if (ref.getQName().equals(new QName(GBNAMESPACE, PUBMED, GBPREFIX))) {
                        pubmed = ref.getStringValue();
                    }
                }
                if (label != null) {
                    writeGenBankLine(w, "REFERENCE   " + label, 80, 12);
                    if (authors != null) {
                        writeGenBankLine(w, "  AUTHORS   " + authors, 80, 12);
                    }
                    if (title != null) {
                        writeGenBankLine(w, "  TITLE     " + title, 80, 12);
                    }
                    if (journal != null) {
                        writeGenBankLine(w, "  JOURNAL   " + journal, 80, 12);
                    }
                    if (medline != null) {
                        writeGenBankLine(w, "   MEDLINE  " + medline, 80, 12);
                    }
                    if (pubmed != null) {
                        writeGenBankLine(w, "   PUBMED   " + pubmed, 80, 12);
                    }
                }
            }
        }
    }

    private static void writeComment(Writer w, ComponentDefinition componentDefinition) throws IOException {
        Annotation annotation = componentDefinition.getAnnotation(new QName(GBNAMESPACE, COMMENT, GBPREFIX));
        if (annotation != null) {
            String[] comments = annotation.getStringValue().split("\n ");
            for (String comment : comments) {
                w.write("COMMENT     " + comment + "\n");
            }
        }
    }
//	
//	private static String startStr(Range range,int offset) {
//		if (range.getAnnotation(new QName(GBNAMESPACE,STARTLESSTHAN,GBPREFIX))!=null) {
//			return "<"+(offset+range.getStart());
//		}
//		return ""+(offset+range.getStart());
//	}
//	
//	private static String rangeType(Range range) {
//		if (range.getAnnotation(new QName(GBNAMESPACE,SINGLEBASERANGE,GBPREFIX))!=null) {
//			return ".";
//		}
//		return "..";
//	}
//	
//	private static String endStr(Range range,int offset) {
//		if (range.getAnnotation(new QName(GBNAMESPACE,ENDGREATERTHAN,GBPREFIX))!=null) {
//			return ">"+(offset+range.getEnd());
//		}
//		return ""+(offset+range.getEnd());
//	}
//	

    private static String locationStr(Location location, int offset, boolean complement, Location location2) throws SBOLConversionException {
        int start;
        int end;
        String locationStr = "";
        boolean isCut = false;
        if (location instanceof Range) {
            Range range = (Range) location;
            start = offset + range.getStart();
            end = offset + range.getEnd();
        } else if (location instanceof Cut) {
            Cut cut = (Cut) location;
            start = offset + cut.getAt();
            end = offset + cut.getAt() + 1;
            isCut = true;
        } else {
            throw conversionException("Location " + location.getIdentity() + " is not range or cut.");
        }
        if (location2 != null) {
            if (location2 instanceof Range) {
                Range range = (Range) location2;
                end = offset + range.getEnd();
            } else if (location2 instanceof Cut) {
                Cut cut = (Cut) location2;
                end = offset + cut.getAt() + 1;
            }
        }
        if (complement) {
            locationStr += "complement(";
        }
        if (location.getAnnotation(new QName(GBCONVNAMESPACE, STARTLESSTHAN, GBCONVPREFIX)) != null) {
            locationStr += "<";
        }
        locationStr += start;
        if (isCut) {
            locationStr += "^";
        } else if (location.getAnnotation(new QName(GBCONVNAMESPACE, SINGLEBASERANGE, GBCONVPREFIX)) != null) {
            locationStr += ".";
        } else {
            locationStr += "..";
        }
        if (location.getAnnotation(new QName(GBCONVNAMESPACE, ENDGREATERTHAN, GBCONVPREFIX)) != null) {
            locationStr += ">";
        }
        locationStr += end;
        if (complement) {
            locationStr += ")";
        }
        return locationStr;
    }

    private static boolean stradlesOrigin(SequenceAnnotation sa) {
        Annotation annotation = sa.getAnnotation(new QName(GBCONVNAMESPACE, STRADLESORIGIN, GBCONVPREFIX));
        if (annotation != null) {
            return true;
        }
        return false;
    }

    private static void writeFeature(Writer w, SequenceAnnotation sa, String role, int offset, boolean inline)
            throws IOException, SBOLConversionException {
        if (sa.getPreciseLocations().size() == 0) {
            throw conversionException("SequenceAnnotation " + sa.getIdentity() + " has no range/cut locations.");
        } else if (sa.getPreciseLocations().size() == 1) {
            Location loc = sa.getPreciseLocations().iterator().next();
            boolean locReverse = false;
            if (loc.isSetOrientation()) {
                locReverse = loc.getOrientation().equals(OrientationType.REVERSECOMPLEMENT);
            }
            w.write("     " + role + " " + locationStr(loc, offset,
                    ((inline && locReverse)
                    || (!inline && !locReverse)), null) + "\n");
        } else if (stradlesOrigin(sa)) {
            Location loc = sa.getLocation("range0");
            Location loc2 = sa.getLocation("range1");
            boolean locReverse = false;
            if (loc.isSetOrientation()) {
                locReverse = loc.getOrientation().equals(OrientationType.REVERSECOMPLEMENT);
            }
            w.write("     " + role + " " + locationStr(loc, offset,
                    ((inline && locReverse)
                    || (!inline && !locReverse)), loc2) + "\n");
        } else {
            String multiType = "join";
            Annotation annotation = sa.getAnnotation(new QName(GBNAMESPACE, MULTIRANGETYPE, GBCONVPREFIX));
            if (annotation != null) {
                multiType = annotation.getStringValue();
            }
            String rangeStr = "     " + role + " " + multiType + "(";
            boolean first = true;
            for (Location loc : sa.getSortedLocations()) {
                if (!first) {
                    rangeStr += ",";
                } else {
                    first = false;
                }
                boolean locReverse = false;
                if (loc.isSetOrientation()) {
                    locReverse = loc.getOrientation().equals(OrientationType.REVERSECOMPLEMENT);
                }
                rangeStr += locationStr(loc, offset,
                        ((inline && locReverse) || (!inline && !locReverse)), null);
            }
            rangeStr += ")";
            writeGenBankLine(w, rangeStr, 80, 21);
        }
        boolean foundLabel = false;
        for (Annotation a : sa.getAnnotations()) {
            if (a.getQName().getLocalPart().equals("multiRangeType")) {
                continue;
            }
            if (a.getQName().getLocalPart().equals("label")) {
                foundLabel = true;
            }
            if (a.getQName().getLocalPart().equals("organism")) {
                foundLabel = true;
            }
            if (a.getQName().getLocalPart().equals("Apeinfo_label")) {
                foundLabel = true;
            }
            if (a.getQName().getLocalPart().equals("product")) {
                foundLabel = true;
            }
            if (a.getQName().getLocalPart().equals("gene")) {
                foundLabel = true;
            }
            if (a.getQName().getLocalPart().equals("note")) {
                foundLabel = true;
            }

            if (a.isStringValue()) {
                String qNameLocal = a.getQName().getLocalPart();
                boolean isNote = false;

                for (String noteQName : noteQNames) {
                    if (qNameLocal.equals(noteQName)) {
                        isNote = true;
                    }
                }

                try {
                    int aInt = Integer.parseInt(a.getStringValue());
                    writeGenBankLine(w, "                     /"
                            + qNameLocal + "="
                            + aInt, 80, 21);
                } catch (NumberFormatException e) {
                    // If it's an unrecognised QName, check if it's one of our
                    // description types that wwill be converted to "/note" later
                    if (!isNote) {
                        writeGenBankLine(w, "                     /"
                                + qNameLocal + "="
                                + "\"" + sanitizeStringValue(a.getStringValue()) + "\"", 80, 21);
                    }
                }
            } else if (a.isIntegerValue()) {
                writeGenBankLine(w, "                     /"
                        + a.getQName().getLocalPart() + "="
                        + a.getIntegerValue(), 80, 21);
            }
        }

        String label = null;
        if (!foundLabel) {
            if (sa.isSetName()) {
                label = sa.getName();
            } else if (sa.isSetComponent() && sa.getComponent() != null
                    && sa.getComponent().isSetName()) {
                label = sa.getComponent().getName();
            } else if (sa.isSetComponent() && sa.getComponent().getDefinition() != null
                    && sa.getComponent().getDefinition().isSetName()) {
                label = sa.getComponent().getDefinition().getName();
            }
            if (label != null) {
                writeGenBankLine(w, "                     /label=" + label, 80, 21);
            }
        }

        // Add this in so that a label is generated from the display ID
        if (label == null) {
            writeNameLabel(w, sa);
        }

        // Add multiple notes for sequence annotations and linked components/definitions
        addNotes(w, sa);
    }

    protected static void writeNameLabel(Writer w, SequenceAnnotation sa) throws IOException {
        String label = null;
        // In order of precedence: if Sequence Annotation's linked component
        // has a component definition that has a Name, use that. If the component
        // Definition doesn't have a Name, check if the component does and use
        // that. If no name is present in either, check the definition for 
        // display ID and use that. If there is no component definition, use 
        // the component's display ID, and finally if there is no component, use
        // the Sequence Annotation's display ID.
        if (sa.isSetName()) {
            label = sa.getName();
        } else if (sa.isSetComponent() && sa.getComponent().getDefinition() != null
                && sa.getComponent().getDefinition().isSetName()) {
            label = sa.getComponent().getDefinition().getName();
        } else if (sa.isSetComponent() && sa.getComponent().getDefinition() != null
                && sa.getComponent().getDefinition().isSetDisplayId()) {
            label = sa.getComponent().getDefinition().getDisplayId();
        } else if (sa.isSetComponent() && sa.getComponent() != null
                && sa.getComponent().isSetName()) {
            label = sa.getComponent().getName();
        } else if (sa.isSetComponent() && sa.getComponent() != null
                && sa.getComponent().isSetDisplayId()) {
            label = sa.getComponent().getDisplayId();
        } else if (sa.isSetDisplayId()) {
            label = sa.getDisplayId();
        }

        if (label != null) {
            writeGenBankLine(w, "                     /label=" + label, 80, 21);
        }
    }

    /**
     * Add multiple notes for each description/note attached to the
     * sequence annotation along with any linked component definitions and
     * components
     *
     * @param w
     * @param sa
     * @throws IOException
     */
    protected static void addNotes(Writer w, SequenceAnnotation sa) throws IOException {
        String note = null;

        if (sa.isSetComponent() && sa.getComponent().getDefinition() != null) {
            writeNotes(w, sa.getComponent().getDefinition());
        }

        if (sa.isSetComponent() && sa.getComponent() != null) {
            writeNotes(w, sa.getComponent());
        }

        writeNotes(w, sa);
    }

    /**
     * Iterate through the description annotations attached to the SBOL entity
     * and write out a new '/note' element in the GenBank output for each one
     *
     * @param w
     * @param i
     * @throws IOException
     */
    protected static void writeNotes(Writer w, Identified i) throws IOException {
        String note = null;
        boolean hasDescription = false;

        if (i.isSetDescription() && i.getDescription() != null
                && !i.getDescription().isBlank()) {
            // Captures the <dcterms:description> tag
            String cleanDesc = sanitizeStringValue(i.getDescription());
            writeGenBankLine(w, "                     /note=" + "\"" + cleanDesc + "\"", 80, 21);
            hasDescription = true;
        }

        for (Annotation a : i.getAnnotations()) {
            if (a.isStringValue()) {
                for (String noteQName : noteQNames) {
                    if (a.getQName().getLocalPart().equals(noteQName)) {
                        note = a.getStringValue();
                    }
                }
            }

            if (note != null && !note.isBlank()) {
                String cleanNote = sanitizeStringValue(note);
                writeGenBankLine(w, "                     /note=" + "\"" + cleanNote + "\"", 80, 21);
                note = null;
            }
        }
    }

    private static void writeSequence(Writer w, Sequence sequence, int size) throws IOException {
        for (int i = 0; i < size; i += 60) {
            String padded = String.format("%9s", "" + (i + 1));
            w.write(padded);
            for (int j = i; j < size && j < i + 60; j += 10) {
                if (j + 10 < size) {
                    w.write(" " + sequence.getElements().substring(j, j + 10));
                } else {
                    w.write(" " + sequence.getElements().substring(j));
                }
            }
            w.write("\n");
        }
    }

    static int getFeatureStart(SequenceAnnotation sa) {
        int featureStart = Integer.MAX_VALUE;
        for (Location location : sa.getPreciseLocations()) {
            if (location instanceof Range) {
                Range range = (Range) location;
                if (range.getStart() < featureStart) {
                    featureStart = range.getStart();
                }
            } else if (location instanceof Cut) {
                Cut cut = (Cut) location;
                if (cut.getAt() < featureStart) {
                    featureStart = cut.getAt();
                }
            }
        }
        if (featureStart == Integer.MAX_VALUE) {
            return 1;
        }
        return featureStart;
    }

    static int getFeatureEnd(SequenceAnnotation sa) {
        int featureEnd = 0;
        for (Location location : sa.getPreciseLocations()) {
            if (location instanceof Range) {
                Range range = (Range) location;
                if (range.getEnd() > featureEnd) {
                    featureEnd = range.getEnd();
                }
            } else if (location instanceof Cut) {
                Cut cut = (Cut) location;
                if (cut.getAt() < featureEnd) {
                    featureEnd = cut.getAt();
                }
            }
        }
        //if (featureEnd==Integer.MAX_VALUE) return 1;
        return featureEnd;
    }

    // TODO: assumes any complement then entirely complemented, need to fix
    static boolean isInlineFeature(SequenceAnnotation sa) {
        boolean inlineFeature = true;
        for (Location location : sa.getPreciseLocations()) {
            if (location.isSetOrientation() && location.getOrientation().equals(OrientationType.REVERSECOMPLEMENT)) {
                inlineFeature = false;
            }
        }
        return inlineFeature;
    }

    private static void recurseComponentDefinition(ComponentDefinition componentDefinition, Writer w, int offset,
            boolean inline, int featureEnd) throws IOException, SBOLConversionException {
        Method getSortedSequenceAnnotationsByDisplayId = null;
        List<SequenceAnnotation> cmpDefSeqAnns = new ArrayList<>();

        String definition = null;

        try {
            getSortedSequenceAnnotationsByDisplayId = ComponentDefinition.class.getDeclaredMethod("getSortedSequenceAnnotationsByDisplayId");

            getSortedSequenceAnnotationsByDisplayId.setAccessible(true);

            cmpDefSeqAnns = (List<SequenceAnnotation>)getSortedSequenceAnnotationsByDisplayId.invoke(componentDefinition);
        } catch (NoSuchMethodException ex) {
            Logger.getLogger(GenBankConverter.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SecurityException ex) {
            Logger.getLogger(GenBankConverter.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            Logger.getLogger(GenBankConverter.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalArgumentException ex) {
            Logger.getLogger(GenBankConverter.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InvocationTargetException ex) {
            Logger.getLogger(GenBankConverter.class.getName()).log(Level.SEVERE, null, ex);
        }

        //definition = concatDescription(definition, componentDefinition);

        for (SequenceAnnotation sa : cmpDefSeqAnns) {
            String role = "misc_feature   ";
            Component comp = sa.getComponent();
            if (comp != null) {
                ComponentDefinition compDef = comp.getDefinition();
                if (compDef != null) {
                    for (URI roleURI : compDef.getRoles()) {
                        String soRole = so.getId(roleURI);
                        if (soRole != null) {
                            if (soRole == "SO:0000110" && sa.isSetName()) {
                                Annotation annotation = sa.getAnnotation(new QName(GBCONVNAMESPACE, FEATURETYPE, GBCONVPREFIX));
                                if (annotation != null) {
                                    role = annotation.getStringValue();
                                    for (int i = role.length(); i < 15; i++) {
                                        role += " ";
                                    }
                                }
                            } else {
                                role = convertSOtoGenBank(soRole);
                            }
                            break;
                        }
                    }
                    int newFeatureEnd = featureEnd;
                    if (!isInlineFeature(sa)) {
                        newFeatureEnd = getFeatureEnd(sa);
                    }
                    recurseComponentDefinition(compDef, w, offset + getFeatureStart(sa) - 1,
                            !(inline ^ isInlineFeature(sa)), newFeatureEnd);
                }
            } else {
                for (URI roleURI : sa.getRoles()) {
                    String soRole = so.getId(roleURI);
                    if (soRole != null) {
                        if (soRole.equals("SO:0000110") && sa.isSetName()) {
                            Annotation annotation = sa.getAnnotation(new QName(GBCONVNAMESPACE, FEATURETYPE, GBCONVPREFIX));
                            if (annotation != null) {
                                role = annotation.getStringValue();
                                for (int i = role.length(); i < 15; i++) {
                                    role += " ";
                                }
                            }
                        } else {
                            role = convertSOtoGenBank(soRole);
                        }
                        break;
                    }
                }
            }
            if (!inline) {
                writeFeature(w, sa, role, (featureEnd - (getFeatureEnd(sa) + getFeatureStart(sa) - 1) - offset), inline);

            } else {
                writeFeature(w, sa, role, offset, inline);
            }
        }
    }

    // "look-ahead" line
    private static String nextLine = null;

    private static boolean featureMode = false;
    private static boolean originMode = false;

    //private static int lineCounter = 0;
    private static String readGenBankLine(BufferedReader br) throws IOException {
        String newLine = "";

        if (nextLine == null) {
            newLine = br.readLine();
            //lineCounter ++;

            if (newLine == null) {
                return null;
            }
            newLine = newLine.trim();
        } else {
            newLine = nextLine;
        }

        while (true) {
            nextLine = br.readLine();

            if (nextLine == null) {
                return newLine;
            }
            nextLine = nextLine.trim();

            if (featureMode) {
                if (nextLine.startsWith("/")) {
                    return newLine;
                }

                String[] strSplit = nextLine.split("\\s+");
                URI role = convertGenBanktoSO(strSplit[0]);

                if (role != null) {
                    return newLine;
                }
            }

            if (originMode) {
                return newLine;
            }
            if (nextLine.startsWith("DEFINITION")) {
                return newLine;
            }
            if (nextLine.startsWith("ACCESSION")) {
                return newLine;
            }
            if (nextLine.startsWith("VERSION")) {
                return newLine;
            }
            if (nextLine.startsWith("KEYWORDS")) {
                return newLine;
            }
            if (nextLine.startsWith("SOURCE")) {
                return newLine;
            }
            if (nextLine.startsWith("ORGANISM")) {
                return newLine;
            }
            if (nextLine.startsWith("REFERENCE")) {
                return newLine;
            }
            if (nextLine.startsWith("COMMENT")) {
                return newLine;
            }
            if (nextLine.startsWith("AUTHORS")) {
                return newLine;
            }
            if (nextLine.startsWith("TITLE")) {
                return newLine;
            }
            if (nextLine.startsWith("JOURNAL")) {
                return newLine;
            }
            if (nextLine.startsWith("MEDLINE")) {
                return newLine;
            }
            if (nextLine.startsWith("PUBMED")) {
                return newLine;
            }
            if (nextLine.startsWith("BASE COUNT")) {
                return newLine;
            }

            if (nextLine.startsWith("FEATURES")) {
                featureMode = true;
                return newLine;
            }
            if (nextLine.startsWith("ORIGIN")) {
                originMode = true;
                return newLine;
            }
            if (featureMode) {
                if (newLine.contains(" ") || nextLine.contains(" ")) {
                    newLine += " " + nextLine;
                } else {
                    newLine += nextLine;
                }
            } else {
                newLine += " " + nextLine;
            }
            //lineCounter++;
        }
    }

    /**
     * @param doc
     * @param topCD
     * @param type
     * @param elements
     * @param version
     * @throws SBOLValidationException if an SBOL validation rule violation
     * occurred in any of the following methods:
     * <ul>
     * <li>{@link SBOLDocument#createSequence(String, String, String, URI)},
     * or</li>
     * <li>{@link ComponentDefinition#addSequence(Sequence)}.</li>
     * </ul>
     */
    private static void createSubComponentDefinitions(SBOLDocument doc, ComponentDefinition topCD, URI type, String elements, String version) throws SBOLValidationException {
        for (SequenceAnnotation sa : topCD.getSequenceAnnotations()) {
            if (!sa.isSetComponent()) {
                continue;
            }
            Range range = (Range) sa.getLocation("range");
            if (range != null) {
                String subElements = elements.substring(range.getStart() - 1, range.getEnd()).toLowerCase();
                if (range.getOrientation().equals(OrientationType.REVERSECOMPLEMENT)) {
                    subElements = Sequence.reverseComplement(subElements, type);
                }
                ComponentDefinition subCompDef = sa.getComponent().getDefinition();
                String compDefId = subCompDef.getDisplayId();
                Sequence subSequence = doc.createSequence(compDefId + "_seq", version, subElements, Sequence.IUPAC_DNA);
                subCompDef.addSequence(subSequence);
            }
        }
    }

    private static String fixTag(String tag) {
        tag = tag.replaceAll("[^a-zA-Z0-9_\\-]", "_");
        tag = tag.replace(" ", "_");
        if (Character.isDigit(tag.charAt(0)) || tag.charAt(0) == '-') {
            tag = "_" + tag;
        }
        return tag;
    }

    protected static String sanitizeStringValue(String value) {
        value = value.replaceAll("&#x2028;", "   ");
        value = value.replaceAll("\n", "   ");
        value = value.replaceAll(System.lineSeparator(), "   ");
        value = value.replaceAll("\u2028", "   ");
        return value;
    }


}
