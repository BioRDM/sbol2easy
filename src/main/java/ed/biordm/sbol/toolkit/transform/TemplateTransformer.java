/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ed.biordm.sbol.toolkit.transform;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.sbolstandard.core2.AccessType;
import org.sbolstandard.core2.Component;
import org.sbolstandard.core2.ComponentDefinition;
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
     * @param doc including sbol document
     * @return new component definition which is a deep copy of the template
     * with the given properties set
     * @throws SBOLValidationException
     * @throws URISyntaxException
     */
    public ComponentDefinition instantiateFromTemplate(ComponentDefinition template,
            String newName, String version, SBOLDocument doc) throws SBOLValidationException {

        String cleanName = util.sanitizeName(newName);

        ComponentDefinition copy = (ComponentDefinition) doc.createCopy(template, cleanName, version);
        copy.setName(newName);
        copy.addWasDerivedFrom(template.getIdentity());

        return copy;
    }    

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

        ComponentDefinition copy = instantiateFromTemplate(template, newName, version, doc);
        copy.setDescription(description);

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
