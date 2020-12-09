/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ed.biordm.sbol.toolkit.scrapbook;

import static ed.biordm.sbol.toolkit.transform.CommonAnnotations.*;
import ed.biordm.sbol.toolkit.transform.SynBioTamer;
import java.net.URI;
import org.sbolstandard.core2.ComponentDefinition;
import org.sbolstandard.core2.SBOLDocument;
import org.sbolstandard.core2.SBOLValidationException;
import org.sbolstandard.core2.SequenceOntology;

/**
 *
 * @author Tomasz Zielinski
 */
public class TrevorFixer {
    

    public SBOLDocument fix(SBOLDocument doc, TrevorMetaReader meta, String version) throws SBOLValidationException {
        return fix(doc, meta, version, true);
    }
    
    public SBOLDocument fix(SBOLDocument doc, TrevorMetaReader meta, String version, boolean stopOnMissing) throws SBOLValidationException {
        
        URI plasmid = SO("SO:0000637");
    
        
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
            
            comp.createAnnotation(SBH_DESCRIPTION, meta.description(id));
            comp.createAnnotation(SBH_NOTES, meta.notes(id));
            comp.createAnnotation(CREATOR, meta.creator(id));
            comp.createAnnotation(CREATOR, meta.pi(id));
            
            // sequence ontology terms do not pass
            //comp.createAnnotation(selection, meta.selection(id));
            //comp.createAnnotation(origin, meta.originOfRep(id));
        }
        
        return doc;
    }
    

}
