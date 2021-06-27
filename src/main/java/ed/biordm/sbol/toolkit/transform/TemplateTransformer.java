/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ed.biordm.sbol.toolkit.transform;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.sbolstandard.core2.AccessType;
import org.sbolstandard.core2.Annotation;
import org.sbolstandard.core2.Component;
import org.sbolstandard.core2.ComponentDefinition;
import org.sbolstandard.core2.Identified;
import org.sbolstandard.core2.Location;
import org.sbolstandard.core2.Range;
import org.sbolstandard.core2.SBOLDocument;
import org.sbolstandard.core2.SBOLValidationException;
import org.sbolstandard.core2.Sequence;
import org.sbolstandard.core2.SequenceAnnotation;
import org.sbolstandard.core2.SequenceConstraint;

/**
 *
 * @author tzielins
 */
public class TemplateTransformer {
    
    final ComponentUtil util = new ComponentUtil();
    

    /**
     * Creates new instance of component definition using the provided template.
     *
     * @param template component to be copied
     * @param newName name of the component (will be transformed into displayId)
     * @param version
     * @param description description to be added to the definition
     * @param doc including sbol document
     * @return new component definition which is a deep copy of the template
     * with the given properties set
     * @throws SBOLValidationException
     * @throws URISyntaxException
     */
    public ComponentDefinition instantiateFromTemplate(ComponentDefinition template,
            String newName, String version, String description, SBOLDocument doc) throws SBOLValidationException {

        // name should be sanitized for conversion into display id as alphanumeric with _ (replace all non alphanumeric characters with _)
        // it should be deep copy, i.e. the owned object must be copied like component, sequenceanotations, sequenceConstraints
        // that should be already handled by doc.crateCopy method.
        String cleanName = util.sanitizeName(newName);

        ComponentDefinition copy = (ComponentDefinition) doc.createCopy(template, cleanName, version);
        copy.setName(newName);
        copy.setDescription(description);
        copy.addWasDerivedFrom(template.getIdentity());

        return copy;
    }

    /**
     * Replaces sub-component with a new one having similar properties but a
     * given sequence.
     *
     * @param parent component definition which is going to be altered to point
     * to new subcomponent
     * @param genericComponentId displayId of the component which is going to be
     * replaced by a new link
     * @param newName name of the new component instance / component definiton
     * @param newSequence DNA sequence to be added to the new component
     * definition if provided
     * @param doc including sbol document
     * @return defintion of the new sub component that has been linked to the
     * parent
     * @throws SBOLValidationException
     * @throws URISyntaxException
     */
    public ComponentDefinition concretizePart(ComponentDefinition parent, String genericComponentId,
            String newName, String newSequence, SBOLDocument doc) throws SBOLValidationException {

        // name shoudl be sanitize for conversion into display id as alphanumeric with _ (replace all not alphanumeri caracters with _)
        // parent has a sub component of the genericComponentId which has to be replaced by the new definiton
        // the CompomonentDefinition of the genericComponentId instance has to be found in the doc, 
        // a deep copy made with the newName (doc.createCopy should handle it)
        // a sequence has to be added if present
        // derived from should be set to the generic-definition
        // the genericComponentId has to be removed and a new component instance pointing the created new component defintion created
        // in the parent component defition the sequenceAnotations and sequeceConstraints have to be updated to point
        // to new component instead of genericComponentId
        // it returns the new sub component definion not the parent so it can be further customized if needed
        String cleanName = util.sanitizeName(newName);

        Component cmp = parent.getComponent(genericComponentId);
        ComponentDefinition prevCmpDef = cmp.getDefinition();

        // make copy of existing component definition - does version have to be supplied?
        // should use instantiateFromTemplate method here
        ComponentDefinition newCmpDef = (ComponentDefinition) doc.createCopy(prevCmpDef, cleanName, parent.getVersion());
        newCmpDef.setName(cleanName);
        newCmpDef.addWasDerivedFrom(prevCmpDef.getIdentity());

        // Find the sequence annotation for the previous component definition
        for (SequenceAnnotation seqAnn : parent.getSequenceAnnotations()) {
            if (seqAnn.getComponent() == cmp) {

                for (Location loc : seqAnn.getLocations()) {
                    Range range = (Range) loc;
                    int seqStart = range.getStart();
                    int seqEnd = range.getEnd();

                    // Update the barcode sequence in the parent sequence
                    for (Sequence seq : parent.getSequences()) {
                        String newParentSeq = this.updateBarcodeSequence(seq.getElements(), newSequence, seqStart, seqEnd);
                        seq.setElements(newParentSeq);
                    }
                }
            }
        }
        // Create a new sequence for the component
        String version = "1.0.0"; // should this be the version of the component definition?
        Sequence seq = doc.createSequence(cleanName + "_seq", version,
                newSequence, Sequence.IUPAC_DNA);
        newCmpDef.addSequence(seq);

        // Create instance of new component definition
        Component link = parent.createComponent(cleanName, AccessType.PUBLIC, newCmpDef.getIdentity());
        link.addWasDerivedFrom(cmp.getIdentity());
        link.setName(cleanName);

        // Replace component and update sequence constraints
        replaceComponent(parent, cmp, link);

        return newCmpDef;
    }

    /**
     * Creates new component definiton which contains a flattened sequence from
     * its subcomponents. The new component has its sequence annotated using its
     * subocmponents annotations
     *
     * @param template component for which a sequence should be generated
     * @param newName name (converted to diplayid) for the new component
     * defintion
     * @param doc including sbol document
     * @return new component definition with explicit sequence
     * @throws SBOLValidationException
     * @throws URISyntaxException
     */
    public ComponentDefinition flattenSequences(ComponentDefinition template, String newName, SBOLDocument doc) throws SBOLValidationException, URISyntaxException {

        // template has parts which all have concrete sequences (we can assume it, throw exceptino if not??)
        // the sequences from the parts have to be joined into one, following the correct order 
        // order may come from sequence annotations that map to components or from the sequenceconstrains that define 
        // respective locations. (ther may be API methods in the lib to have those parts ordered correctly, as the synbio hub renders the
        // parts on the graphs in the correct order
        // to be usable for the user (see the features), the new sequence has to be annotated with featues from the subcomponents
        // lets assume one level down as it is our case (if not it can be always called recursively)
        // the tricky part will be that subcomponents sequences are annotted from 1 for each
        // in the joined sequence the annotations has to be shifted depending where the componante instance has been located (how long
        // was the sequence that came from other parts).
        // check the code for template.getImpliedNucleicAcidSequence()
        // it may already generate the correct sequence
        // then only creating new sequence annotations in the new locations needs implementation.
        // for subcomponents whose annotations were pointing to components not having own role
        // a new component instance has to be created in the top component using the same component definition
        // and then it has to be set in the sequence annotations (grandfather cannot use its granchilids components directly
        // in the sequence annotations.
        String cleanName = util.sanitizeName(newName);

        ComponentDefinition newCmpDef = (ComponentDefinition) doc.createCopy(template, cleanName, template.getVersion());
        newCmpDef.setName(cleanName);
        newCmpDef.addWasDerivedFrom(template.getIdentity());

        Map<Component, List<Sequence>> cmpSeqMap = new HashMap<>();
        cmpSeqMap = rebuildSequences(newCmpDef, newCmpDef, doc, cmpSeqMap);
        //does not position child components correctly and raises validation errors
        //addCustomSequenceAnnotations(newCmpDef, cmpSeqMap);

        return newCmpDef;
    }


    


    /**
     * recursively adds SequenceAnnotation objects from child components into
     * Set parameter
     *
     * @param comp The parent component definition to descend through children
     * from
     * @param doc The top level SBOL document
     * @param childSequenceAnns The set to populate with all child sequence
     * annotations
     */
    protected void addChildSequenceAnnotations(ComponentDefinition comp, SBOLDocument doc, Set<SequenceAnnotation> childSequenceAnns) throws SBOLValidationException {
        Set<SequenceAnnotation> oldSequenceAnn = comp.getSequenceAnnotations();
        int saCount = 1;

        for (Component child : comp.getComponents()) {
            ComponentDefinition cmpDef = child.getDefinition();

            if (cmpDef.getSequenceAnnotations().size() > 0) {
                for (SequenceAnnotation seqAn : cmpDef.getSequenceAnnotations()) {
                    childSequenceAnns.add(seqAn);
                }
            } else {
                for (Sequence seq : cmpDef.getSequences()) {
                    String newSAName = "ann".concat(String.valueOf(saCount));
                    SequenceAnnotation newSA = comp.createSequenceAnnotation(newSAName, newSAName, 1, seq.getElements().length());
                    newSA.setComponent(child.getIdentity());

                    saCount += 1;

                    childSequenceAnns.add(newSA);
                }

            }

            addChildSequenceAnnotations(cmpDef, doc, childSequenceAnns);
        }
    }

    /**
     * remove old component and replace with new component in parent component
     * definition
     *
     * @param parent The parent component definition to replace component in
     * @param oldComponent The child component to be replaced
     * @param newComponent The new child component that will replace the
     * existing
     */
    protected void replaceComponent(ComponentDefinition parent, Component oldComponent, Component newComponent) throws SBOLValidationException {
        for (SequenceConstraint sc : parent.getSequenceConstraints()) {
            if (sc.getSubject().equals(oldComponent)) {
                // Replace the subject in the constraint with the new component
                sc.setSubject(newComponent.getIdentity());
            }
            if (sc.getObject().equals(oldComponent)) {
                // Replace the object in the constraint with the new component
                sc.setObject(newComponent.getIdentity());
            }
        }

        parent.removeComponent(oldComponent);
    }

    /**
     * Iterate through child components of parent component definition and add
     * all sub-components' sequence annotations to the parent
     *
     * @param parent The parent component definition to iterate over
     * @param child
     * @param startIdx
     */
    protected void addSequenceAnnotationsToParent(ComponentDefinition parent,
            ComponentDefinition child, int startIdx) throws SBOLValidationException {
        List<SequenceAnnotation> seqAnns = child.getSortedSequenceAnnotations();
        for (SequenceAnnotation seqAnn : seqAnns) {

            Set<Location> seqAnnLocs = seqAnn.getLocations();

            for (Location loc : seqAnnLocs) {
                Range locRange = (Range) loc;

                int seqAnnStart = locRange.getStart();
                int seqAnnEnd = locRange.getEnd();

                SequenceAnnotation newSA = parent
                        .createSequenceAnnotation(seqAnn.getDisplayId(),
                                seqAnn.getDisplayId(),
                                startIdx + seqAnnStart - 1,
                                startIdx + +seqAnnEnd - 1);

                // Copy attributes from old SA to new SA
                copySequenceAnnotationAttributes(parent, seqAnn, newSA);

                if (newSA.getComponent() == null) {
                    // Throws org.sbolstandard.core2.SBOLValidationException: sbol-10522:  Strong Validation Error: 
                    // The sequenceAnnotations property of a ComponentDefinition MUST NOT contain two or more SequenceAnnotation objects that refer to the same Component.
                    // A SequenceAnnotation MUST NOT include both a component property and a roles property. 
                    //newSA.setComponent(c.getIdentity());
                }
            }
        }
    }

    /**
     * Does not appear to be a 'copy' or 'deepCopy' method in SequenceAnnotation
     * class, so doing this manually.
     *
     * See https://www.javadoc.io/static/org.sbolstandard/libSBOLj/2.4.0/org/sbolstandard/core2/SequenceAnnotation.html
     *
     * @param parent
     * @param origSeqAnn
     * @param newSeqAnn
     * @return
     * @throws SBOLValidationException
     */
    protected SequenceAnnotation copySequenceAnnotationAttributes(ComponentDefinition parent,
            SequenceAnnotation origSeqAnn, SequenceAnnotation newSeqAnn) throws SBOLValidationException {
        newSeqAnn.setRoles(origSeqAnn.getRoles());

        newSeqAnn.setDescription(origSeqAnn.getDescription());

        Component origCmp = origSeqAnn.getComponent();

        if (origCmp != null) {
            if (origCmp.getIdentity() == null) {
                if (origCmp.getDisplayId() == null) {
                    //System.out.println("Original Component Display ID is null!");
                } else {
                    newSeqAnn.setComponent(origCmp.getDisplayId());
                }
            } else {
                /*
                "The Component referenced by the component property of a
                SequenceAnnotation MUST be contained by the ComponentDefinition that contains the SequenceAnnotation."
                Therefore, need to add this component to the parent component definition.
                */
                Component parentChild = parent.getComponent(origCmp.getDisplayId());

                if (parentChild == null) {
                    parentChild = parent.createComponent(origCmp.getDisplayId(), AccessType.PUBLIC, origCmp.getDefinitionURI());
                }
                // newSeqAnn.setComponent(origSeqAnn.getComponent().getIdentity());
                newSeqAnn.setComponent(parentChild.getIdentity());
            }
        }

        newSeqAnn.setName(origSeqAnn.getName());

        newSeqAnn.setWasDerivedFroms(origSeqAnn.getWasDerivedFroms());

        // TODO: should do this recursively for nested annotations?
        for (Annotation origAnno : origSeqAnn.getAnnotations()) {
            // TODO: should retrieve type of literal value, not assume it's string?
            newSeqAnn.createAnnotation(origAnno.getQName(), origAnno.getStringValue());
        }

        for (Location origLoc : origSeqAnn.getSortedLocations()) {
            //newSeqAnn.addGenericLocation(origLoc.getDisplayId());
        }

        return newSeqAnn;
    }


    protected boolean isComponentAnnotated(Set<SequenceAnnotation> seqAnns, URI componentIdentity) {
        boolean seqAnnCmpExists = false;

        // Cycle through sequence annotations to check there isn't one already for this component
        for (SequenceAnnotation seqAnn : seqAnns) {
            Component existingCmp = seqAnn.getComponent();

            if (existingCmp != null) {
                if (existingCmp.getIdentity().equals(componentIdentity)) {
                    seqAnnCmpExists = true;
                    break;
                }
            }
        }

        return seqAnnCmpExists;
    }

    protected void createNewSequenceAnnotation(ComponentDefinition parent, String seqAnnId,
            URI componentIdentity, int start, int end) throws SBOLValidationException {
        SequenceAnnotation newSA = parent.createSequenceAnnotation(seqAnnId, seqAnnId, start, end);

        newSA.setComponent(componentIdentity);
    }

    /**
     * For the flattened plasmid, this method creates the sequence annotations
     * that reflect the sequences attached to the original child sub-components
     * 
     * @param cmpSeqMap
     * @throws SBOLValidationException 
     */
    protected void addCustomSequenceAnnotations(ComponentDefinition parent,
            Map<Component, List<Sequence>> cmpSeqMap) throws SBOLValidationException {
        /*
        // Finally the original child components (backbone, left, insert) after
        // flattening should get sequence annotations which locates them in
        // correct regions (1-length_bacbone) (lenght-backbone+1, lengh_left)
        // as after flattening we assume they follow each other and we know
        // their exact locations
        */

        int saCount = 1;
        int start = 1;

        Set<SequenceAnnotation> parentSeqAnns = parent.getSequenceAnnotations();

        for (Component cmp : cmpSeqMap.keySet()) {
            List<Sequence> seqs = cmpSeqMap.get(cmp);
            URI cmpId = cmp.getIdentity();

            for (Sequence seq : seqs) {
                parentSeqAnns = parent.getSequenceAnnotations();

                if (! isComponentAnnotated(parentSeqAnns, cmpId)) {
                    String newSADispId = "ann".concat(String.valueOf(saCount));
                    int end = start + seq.getElements().length()-1;
                    createNewSequenceAnnotation(parent, newSADispId, cmpId,
                            start, end);
                    start += seq.getElements().length();
                    saCount += 1;
                }
            }
        }
    }

    /**
     * Copied from
     * edu.utah.ece.async.sboldesigner.sbol.editor.SBOLDesign.rebuildSequences.
     * Modified to create new Sequence Annotations for each sequence in all
     * sub-components.
     *
     * @param parent
     * @param subCmp
     * @param doc
     * @param newSequenceAnns The set of SequenceAnnotation objects to
     * recursively collect from sub-components
     * @throws SBOLValidationException
     */
    protected Map<Component, List<Sequence>> rebuildSequences(ComponentDefinition parent,
            ComponentDefinition subCmp, SBOLDocument doc,
            Map<Component, List<Sequence>> cmpSeqMap) throws SBOLValidationException {
        int start = 1;
        int length;
        int count = 0;
        String newSeq = "";
        ComponentDefinition curr;

        List<String> customSAIds = Arrays.asList("left", "backbone", "insert", "right");

        for (org.sbolstandard.core2.Component c : subCmp.getSortedComponents()) {
            curr = c.getDefinition();
            if (!curr.getComponents().isEmpty()) {
                cmpSeqMap = rebuildSequences(parent, curr, doc, cmpSeqMap);
            }
            length = 0;

            addSequenceAnnotationsToParent(parent, curr, start);

            //Append sequences to build newly constructed sequence
            for (Sequence s : curr.getSequences()) {
                newSeq = newSeq.concat(s.getElements());
                length += s.getElements().length();

                for(String customSAId : customSAIds) {
                    if(curr.getDisplayId().contains(customSAId)) {
                        cmpSeqMap.computeIfAbsent(c, k -> new LinkedList<>()).add(s);
                    }
                }
            }

            start += length;

            count++;
        }
        if (!newSeq.isBlank()) {
            if (parent.getSequences().isEmpty()) {
                String uniqueId = parent.getDisplayId().concat("_seq");
                parent.addSequence(doc.createSequence(uniqueId, parent.getVersion(), newSeq, Sequence.IUPAC_DNA));
            } else {
                parent.getSequences().iterator().next().setElements(newSeq);
            }
        }

        return cmpSeqMap;
    }

    /**
     * Replace the sub-string between the startIdx and endIdx positions in the
     * provided oldSeq String parameter, with the content of the barcodeSeq
     * String value. If the length of the barcodeSeq String parameter is shorter
     * or longer than the section of the oldSeq defined by the index values, the
     * resulting sub-string is truncated or padded accordingly.
     *
     * @param oldSeq
     * @param barcodeSeq
     * @param startIdx
     * @param endIdx
     * @return
     */
    public String updateBarcodeSequence(String oldSeq, String barcodeSeq, int startIdx, int endIdx) {

        String startSubStr = oldSeq.substring(0, startIdx);
        String endSubStr = oldSeq.substring(endIdx, oldSeq.length());

        String newSeq = startSubStr.concat(barcodeSeq).concat(endSubStr);

        return newSeq;
    }


}
