/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ed.biordm.sbol.toolkit.scrapbook;

import static ed.biordm.sbol.toolkit.scrapbook.CyanoTemplates.SeqenceOntoPref;
import ed.biordm.sbol.toolkit.transform.SynBioTamer;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.Set;
import javax.xml.namespace.QName;
import org.sbolstandard.core2.Component;
import org.sbolstandard.core2.ComponentDefinition;
import org.sbolstandard.core2.SBOLConversionException;
import org.sbolstandard.core2.SBOLDocument;
import org.sbolstandard.core2.SBOLReader;
import org.sbolstandard.core2.SBOLValidationException;
import org.sbolstandard.core2.SequenceOntology;

/**
 *
 * @author Tomasz Zielinski
 */
public class TrevorFixer {
    
    static    QName description = new QName("http://wiki.synbiohub.org/wiki/Terms/synbiohub#", 
            "mutableDescription"); 
    
    static     QName notes = new QName("http://wiki.synbiohub.org/wiki/Terms/synbiohub#", 
            "mutableNotes");    

    
    // does not work as contains :
    static    QName selection = new QName(SequenceOntology.NAMESPACE.toString(), 
            "SO%3A0002232","so");
        
    static     QName origin = new QName(SequenceOntology.NAMESPACE.toString(), 
            "SO%3A0000296","so");     

    static QName creator = new QName("http://purl.org/dc/elements/1.1/","creator");    

    public SBOLDocument fix(SBOLDocument doc, TrevorMetaReader meta, String version) throws SBOLValidationException {
        return fix(doc, meta, version, true);
    }
    
    public SBOLDocument fix(SBOLDocument doc, TrevorMetaReader meta, String version, boolean stopOnMissing) throws SBOLValidationException {
        
        URI plasmid = null;
        
        try {
            plasmid = new URI(SequenceOntology.NAMESPACE.toString() + "SO:0000637");
        } catch (URISyntaxException e) {
            throw new RuntimeException();
        }
    
        
        SynBioTamer tamer = new SynBioTamer();
        doc = tamer.tameForSynBio(doc);
        
        for (String id: meta.ids()) {
            
            ComponentDefinition comp = doc.getComponentDefinition(id, version);
            if (comp == null) {
                if (stopOnMissing)
                    throw new IllegalArgumentException("Missing component id: "+id);
                else continue;
            }
            comp.removeRole(SequenceOntology.ENGINEERED_REGION);
            comp.addRole(plasmid);    
            
            comp.setDescription(meta.summary(id));
            
            comp.createAnnotation(description, meta.description(id));
            comp.createAnnotation(notes, meta.notes(id));
            comp.createAnnotation(creator, meta.creator(id));
            comp.createAnnotation(creator, meta.pi(id));
            
            // sequence ontology terms do not pass
            //comp.createAnnotation(selection, meta.selection(id));
            //comp.createAnnotation(origin, meta.originOfRep(id));
        }
        
        return doc;
    }
    

}
