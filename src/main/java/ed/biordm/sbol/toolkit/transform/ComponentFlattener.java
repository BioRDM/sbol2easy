/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ed.biordm.sbol.toolkit.transform;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.sbolstandard.core2.Component;
import org.sbolstandard.core2.ComponentDefinition;
import org.sbolstandard.core2.SBOLDocument;
import org.sbolstandard.core2.SBOLValidationException;
import org.sbolstandard.core2.Sequence;
import org.sbolstandard.core2.SequenceAnnotation;

/**
 *
 * @author tzielins
 */
public class ComponentFlattener {
    
    final ComponentUtil util = new ComponentUtil();
    
    /**
     * Creates a new component definition which represents the flattened version of the template.
     * Flattened version has a sequence constructed by joining children sequences, and which is annotated
     * using roles and annotations of descending components. 
     * 
     * Subcomponents are ordered by sequence constraints, or explicitly located using sequence annotations.
     * 
     *
     * @param template component for which a sequence should be generated
     * @param newName name (converted to diplayid) for the new component
     * defintion
     * @param destDoc sbol document in which to create the new definition
     * @return new component definition with the full joined sequence
     * @throws SBOLValidationException from sbol library
     * @throws IllegalArgumentException if definition is abstract i.e. does not have sequences in the comp tree
     */
    public ComponentDefinition flattenDesign(ComponentDefinition template, String newName, SBOLDocument destDoc) throws SBOLValidationException {

        String cleanName = util.sanitizeName(newName);
        ComponentDefinition newCmpDef = destDoc.createComponentDefinition(cleanName, template.getVersion(), template.getTypes());
        newCmpDef.setName(newName);
        newCmpDef.addWasDerivedFrom(template.getIdentity());
        util.copyMeta(template, newCmpDef);

        List<Component> children = template.getSortedComponents();
        
        Sequence joinedSequence = joinDNASequences(template, cleanName+"_seq", template.getVersion(),destDoc);
        newCmpDef.addSequence(joinedSequence);
        
        convertComponentsToFeatures(children, newCmpDef);
        
        copySequenceFeatures(children, newCmpDef);
        

        return newCmpDef;
    }
    
    /**
     * Flattens the top level root designs and stores them in the destDoc. 
     * For each of the root component definition creates a flattenedDesgin (see flattenDesign)
     * the new design has a suffix appended to its name.
     * @param source
     * @param nameSuffix
     * @param destDoc
     * @return list of the created, new, flat designs
     * @throws SBOLValidationException 
     */
    public List<ComponentDefinition> flattenDesigns(SBOLDocument source, String nameSuffix, SBOLDocument destDoc) throws SBOLValidationException {
        
        List<ComponentDefinition> flattened = new ArrayList<>();
        
        for (ComponentDefinition comp : source.getRootComponentDefinitions()) {
            ComponentDefinition flat = flattenDesign(comp, util.nameOrId(comp)+nameSuffix, destDoc);
            flattened.add(flat);
        }
        
        return flattened;
    }
    
    
    void convertComponentsToFeatures(List<Component> children, ComponentDefinition dest) throws SBOLValidationException {
        convertComponentsToFeatures(children, dest, 0);
    }    
    
    void convertComponentsToFeatures(List<Component> children, ComponentDefinition dest, int shift) throws SBOLValidationException {
        for (Component comp: children) {
            convertComponentToFeature(comp, dest, shift);
            if (abstractComp(comp)) {
                convertComponentsToFeatures(comp.getDefinition().getSortedComponents(), dest, shift);
            }
            shift+=getJoinedDNASequenceLength(comp);
        }
    }
    
    boolean abstractComp(Component comp) {
        return getSequence(comp, Sequence.IUPAC_DNA).isEmpty();
    }

    SequenceAnnotation convertComponentToFeature(Component comp, ComponentDefinition dest, int shift) throws SBOLValidationException {
        
        int length = getJoinedDNASequenceLength(comp);
        ComponentDefinition compD = comp.getDefinition();
        
        SequenceAnnotation ann = dest.createSequenceAnnotation(comp.getDisplayId(), comp.getDisplayId(), shift+1, shift+length);
        
        Set<URI> roles = new HashSet<>(comp.getRoles());
        roles.addAll(compD.getRoles());
        
        ann.setRoles(roles);
        ann.setName( compD.getName() != null ? compD.getName() : compD.getDisplayId());
        ann.setDescription(compD.getDescription());
        util.copyAnnotations(compD, ann);
        
        return ann;
    }
    
    void copySequenceFeatures(List<Component> children, ComponentDefinition dest) throws SBOLValidationException {
        copySequenceFeatures(children, dest, 0);
    }    
    
    void copySequenceFeatures(List<Component> children, ComponentDefinition dest, int shift) throws SBOLValidationException {
        
        for (Component comp: children) {
            ComponentDefinition def = comp.getDefinition();
            copySequenceFeatures(def, dest, shift);
            if (abstractComp(comp)) {
                copySequenceFeatures(def.getSortedComponents(), dest, shift);
            }
            shift+=getJoinedDNASequenceLength(comp);
        }
    }

    void copySequenceFeatures(ComponentDefinition src, ComponentDefinition dest, int shift) throws SBOLValidationException {
        
        for (SequenceAnnotation ann: src.getSequenceAnnotations()) {
            //ann.get
            
            SequenceAnnotation feature = util.createAnnCopy(ann, dest, shift);
            if (ann.isSetComponent()) {
                Component comp = util.createCmpCopy(ann.getComponent(), dest);
                feature.setComponent(comp.getPersistentIdentity());
            }
        }
    }
    
    
    
    Sequence joinDNASequences(ComponentDefinition def, String displayId, String version, SBOLDocument doc) throws SBOLValidationException {
        
        String seq = getJoinedSequenceElements(def, Sequence.IUPAC_DNA)
                .orElseThrow( () -> new IllegalArgumentException("Cannot join component without sequence "+def.getDisplayId()));
        
        return doc.createSequence(displayId, version, seq, Sequence.IUPAC_DNA);
    }    
    
    
    Optional<String> getSequenceElements(ComponentDefinition def, URI seqType) {
        for (Sequence seq : def.getSequences()) {
            if (seq.getEncoding().equals(seqType)) return Optional.of(seq.getElements());
        }
        return Optional.empty();        
    }
    
    Optional<String> getJoinedSequenceElements(ComponentDefinition def, URI seqType) throws SBOLValidationException {
        
        Optional<String> concrete = getSequenceElements(def, seqType);
        if (concrete.isPresent()) return concrete;
        if (def.getComponents().isEmpty()) return Optional.empty();
        
        StringBuilder sb = new StringBuilder();
        for (Component comp : def.getSortedComponents()) {
            
            String part = getJoinedSequenceElements(comp.getDefinition(), seqType)
                    .orElseThrow( () -> new IllegalArgumentException("Cannot join elements from component without sequence "+
                             comp.getDisplayId()+" in "+def.getDisplayId()));
            sb.append(part);            
        }
        
        return Optional.of(sb.toString());        
    }
    
    int getJoinedSequenceLength(ComponentDefinition def, URI seqType) {
        
        Optional<String> concrete = getSequenceElements(def, seqType);
        if (concrete.isPresent()) return concrete.get().length();
        if (def.getComponents().isEmpty()) 
            throw new IllegalArgumentException("Cannot calculate length from missing sequence in comp "+def.getDisplayId());
        
        int length = 0;
        for (Component comp : def.getComponents()) {
            
            length += getJoinedSequenceLength(comp.getDefinition(), seqType);
        }
        
        return length;        
    }    

    int getJoinedDNASequenceLength(Component comp) throws SBOLValidationException {
        return getJoinedSequenceLength(comp.getDefinition(), Sequence.IUPAC_DNA);
    } 
    
    Optional<Sequence> getSequence(Component comp, URI seqType) {
        return getSequence(comp.getDefinition(), seqType);
    }
    
    Optional<Sequence> getSequence(ComponentDefinition def, URI seqType) {
        for (Sequence seq : def.getSequences()) {
            if (seq.getEncoding().equals(seqType)) return Optional.of(seq);
        }
        return Optional.empty();
    }    
}
