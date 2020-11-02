/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ed.biordm.sbol.toolkit.transform;

import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.sbolstandard.core2.AccessType;
import org.sbolstandard.core2.Component;
import org.sbolstandard.core2.ComponentDefinition;
import org.sbolstandard.core2.Location;
import org.sbolstandard.core2.OrientationType;
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
            String newName, String version, String description, SBOLDocument doc) throws SBOLValidationException, URISyntaxException {

        // name should be sanitized for conversion into display id as alphanumeric with _ (replace all non alphanumeric characters with _)
        // it should be deep copy, i.e. the owned object must be copied like component, sequenceanotations, sequenceConstraints
        // that should be already handled by doc.crateCopy method.
        String cleanName = sanitizeName(newName);

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
            String newName, String newSequence, SBOLDocument doc) throws SBOLValidationException, URISyntaxException {

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
        String cleanName = sanitizeName(newName);

        Component cmp = parent.getComponent(genericComponentId);
        ComponentDefinition prevCmpDef = cmp.getDefinition();

        // make copy of existing component definition - does version have to be supplied?
        // should use instantiateFromTemplate method here
        ComponentDefinition newCmpDef = (ComponentDefinition) doc.createCopy(prevCmpDef, cleanName, parent.getVersion());
        newCmpDef.setName(cleanName);
        newCmpDef.addWasDerivedFrom(prevCmpDef.getIdentity());

        // Assume we are adding a new sequence to the component
        String version = "1.0.0"; // should this be the version of the component definition?
        Sequence seq = doc.createSequence(cleanName + "_seq", version,
                newSequence, Sequence.IUPAC_DNA);
        newCmpDef.addSequence(seq);

        // Create instance of new component definition
        Component link = parent.createComponent(cleanName, AccessType.PUBLIC, newCmpDef.getIdentity());
        link.addWasDerivedFrom(cmp.getIdentity());
        link.setName(cleanName);

        // Create SequenceAnnotation for new component definition and link to instance
        // SequenceAnnotation seqAnn = newCmpDef.createSequenceAnnotation(cleanName + "_sa", cleanName + "_sa", 1, newSequence.length());
        //seqAnn.setComponent(link.getDisplayId());

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
        String cleanName = sanitizeName(newName);

        ComponentDefinition newCmpDef = (ComponentDefinition) doc.createCopy(template, cleanName, template.getVersion());
        newCmpDef.setName(cleanName);
        newCmpDef.addWasDerivedFrom(template.getIdentity());

        Set<SequenceAnnotation> flatSAs = new HashSet<>();
        //addChildSequenceAnnotations(newCmpDef, doc, flatSAs);

        for (SequenceAnnotation sa : flatSAs) {
            if(!newCmpDef.getSequenceAnnotations().contains(sa)) {
                if (sa.getComponent() == null) {
                    System.out.println("here1");

                    if (sa.getComponentDefinition() == null) {
                        System.out.println("No component or component definition?!");
                    }
                   //System.out.println(sa.getComponentDefinition().getIdentity());
                } else {
                    System.out.println(sa.getComponent().getDisplayId());
                }
                //System.out.println(sa.getDisplayId());

                Set<Location> saLocs = sa.getLocations();

                for (Location saLoc : saLocs) {
                    System.out.println(saLoc.getSequence());
                }
                //newCmpDef.createSequenceAnnotation(sa.getDisplayId(), cleanName);
            }
            //System.out.println(((Location)sa.getLocations().toArray()[0]).getSequence().getElements());
        }

        rebuildSequences(newCmpDef, doc, flatSAs);
        //rebuildSequencesOrig(newCmpDef, doc);

        // this sort of works
        addSequenceAnnotationsToParent(newCmpDef);

        //addChildSequenceAnnotations(newCmpDef, doc, flatSAs);

        return newCmpDef;
    }

    /**
     * name should be sanitized for conversion into display id as alphanumeric
     * with _ (replace all non alphanumeric characters with _)
     *
     * @param name
     * @return The sanitized string
     */
    protected String sanitizeName(String name) {
        String cleanName = name.replaceAll("[^\\p{IsAlphabetic}\\p{IsDigit}]", "_");
        return cleanName;
    }

    /**
     * recursively adds SequenceAnnotation objects from child components into
     * Set parameter
     *
     * @param comp The parent component definition to descend through children from
     * @param doc The top level SBOL document
     * @param childSequenceAnns The set to populate with all child sequence annotations
     */
    protected void addChildSequenceAnnotations(ComponentDefinition comp, SBOLDocument doc, Set<SequenceAnnotation> childSequenceAnns) throws SBOLValidationException {
        Set<SequenceAnnotation> oldSequenceAnn = comp.getSequenceAnnotations();
        int saCount = 1;

        for(Component child : comp.getComponents()) {
            ComponentDefinition cmpDef = child.getDefinition();

            if (cmpDef.getSequenceAnnotations().size() > 0) {
            for(SequenceAnnotation seqAn : cmpDef.getSequenceAnnotations()) {
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
     * remove old component and replace with new component in parent component definition
     *
     * @param parent The parent component definition to replace component in
     * @param oldComponent The child component to be replaced
     * @param newComponent The new child component that will replace the existing
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
     */
    protected void addSequenceAnnotationsToParent(ComponentDefinition parent) throws SBOLValidationException {
        Set<SequenceConstraint> flatSCs = parent.getSequenceConstraints();
        int length = 0;
        int start = 1;

        List<Component> flatCmps = parent.getSortedComponents();

        for (Component cmp : flatCmps) {
            length = 0;

            ComponentDefinition cmpDef = cmp.getDefinition();
            Set<Sequence> cmpDefSeqs = cmpDef.getSequences();

            for (Sequence seq : cmpDefSeqs) {
                length += seq.getElements().length();
            }

            Set<SequenceAnnotation> seqAnns = cmpDef.getSequenceAnnotations();
            for (SequenceAnnotation seqAnn : seqAnns) {

                Set<Location> seqAnnLocs = seqAnn.getLocations();

                SequenceAnnotation newSA = parent.createSequenceAnnotation(seqAnn.getDisplayId(), seqAnn.getDisplayId(), start, start+length);

                if (newSA.getComponent() == null) {
                    // Throws org.sbolstandard.core2.SBOLValidationException: sbol-10522:  Strong Validation Error: 
                    // The sequenceAnnotations property of a ComponentDefinition MUST NOT contain two or more SequenceAnnotation objects that refer to the same Component.
                    //newSA.setComponent(cmp.getIdentity());
                }
            }
            start += length;
        }
    }

    /**
     * Copied from
     * edu.utah.ece.async.sboldesigner.sbol.editor.SBOLDesign.rebuildSequences.
     * Modified to create new Sequence Annotations for each sequence in all 
     * sub-components.
     *
     * @param comp
     * @param doc
     * @param newSequenceAnns The set of SequenceAnnotation objects to recursively collect from sub-components
     * @throws SBOLValidationException
     */
    protected void rebuildSequences(ComponentDefinition comp, SBOLDocument doc, Set<SequenceAnnotation> newSequenceAnns) throws SBOLValidationException {
        Set<SequenceAnnotation> oldSequenceAnns = comp.getSequenceAnnotations();

        //comp.clearSequenceAnnotations();
        Set<Sequence> currSequences = new HashSet<Sequence>();

        int start = 1;
        int length;
        int count = 0;
        String newSeq = "";
        ComponentDefinition curr;
        for (org.sbolstandard.core2.Component c : comp.getSortedComponents()) {
            curr = c.getDefinition();
            if (!curr.getComponents().isEmpty()) {
                rebuildSequences(curr, doc, newSequenceAnns);
            }
            length = 0;
            //Append sequences to build newly constructed sequence
            for (Sequence s : curr.getSequences()) {
                currSequences.add(s);
                newSeq = newSeq.concat(s.getElements());
                length += s.getElements().length();
            }
            /*String currSeq = curr.getImpliedNucleicAcidSequence();
            newSeq = newSeq.concat(currSeq);
            length += currSeq.length();*/

            OrientationType o = OrientationType.INLINE;
            for(SequenceAnnotation seqAnn : curr.getSequenceAnnotations()) {
                if(seqAnn == null) {
                    if (length == 0) {
                        seqAnn = comp.createSequenceAnnotation(seqAnn.getDisplayId(), "GenericLocation", o);
                    } else {
                        seqAnn = comp.createSequenceAnnotation(seqAnn.getDisplayId(), "Range", start, start + length - 1, o);
                        start += length;
                    }
                    seqAnn.setComponent(c.getIdentity());
                }

                newSequenceAnns.add(seqAnn);
            }

            for (SequenceAnnotation sa : oldSequenceAnns) {
                ComponentDefinition saCmpDef = sa.getComponentDefinition();
                if (saCmpDef != null) {
                    if (saCmpDef.getDisplayId().equals(c.getDisplayId())) {
                        o = sa.getLocations().iterator().next().getOrientation();
                        SequenceAnnotation seqAnn = comp.getSequenceAnnotation(sa.getDisplayId());

                        if(seqAnn == null) {
                            if (length == 0) {
                                seqAnn = comp.createSequenceAnnotation(sa.getDisplayId(), "GenericLocation", o);
                            } else {
                                seqAnn = comp.createSequenceAnnotation(sa.getDisplayId(), "Range", start, start + length - 1, o);
                                start += length;
                            }
                            seqAnn.setComponent(c.getIdentity());
                        }

                        newSequenceAnns.add(seqAnn);
                    }
                }
            }

            count++;
        }
        if (!newSeq.isBlank()) {
            if (comp.getSequences().isEmpty()) {
                String uniqueId = comp.getDisplayId().concat("_seq");
                comp.addSequence(doc.createSequence(uniqueId, comp.getVersion(), newSeq, Sequence.IUPAC_DNA));
            } else {
                comp.getSequences().iterator().next().setElements(newSeq);
            }
        }
    }

    /**
     * Copied from
     * edu.utah.ece.async.sboldesigner.sbol.editor.SBOLDesign.rebuildSequences.
     * Unused at present (23/10/20).
     *
     * @param comp
     * @param doc
     * @throws SBOLValidationException
     */
    private void rebuildSequencesOrig(ComponentDefinition comp, SBOLDocument doc) throws SBOLValidationException {
        Set<SequenceAnnotation> oldSequenceAnn = comp.getSequenceAnnotations();
        comp.clearSequenceAnnotations();
        Set<Sequence> currSequences = new HashSet<Sequence>();

        int start = 1;
        int length;
        int count = 0;
        String newSeq = "";
        ComponentDefinition curr;
        for (org.sbolstandard.core2.Component c : comp.getSortedComponents()) {
            curr = c.getDefinition();
            if (!curr.getComponents().isEmpty()) {
                rebuildSequencesOrig(curr, doc);
            }
            length = 0;
            //Append sequences to build newly constructed sequence
            for (Sequence s : curr.getSequences()) {
                currSequences.add(s);
                newSeq = newSeq.concat(s.getElements());
                length += s.getElements().length();
            }

            OrientationType o = OrientationType.INLINE;
            for (SequenceAnnotation sa : oldSequenceAnn) {
                Component saCmp = sa.getComponent();
                if (saCmp != null) {
                    if (saCmp.getIdentity() == c.getIdentity()) {
                        o = sa.getLocations().iterator().next().getOrientation();
                    }
                }
            }

            SequenceAnnotation seqAnn;
            if (length == 0) {
                seqAnn = comp.createSequenceAnnotation("SequenceAnnotation_" + count, "GenericLocation", o);
            } else {
                seqAnn = comp.createSequenceAnnotation("SequenceAnnotation_" + count, "Range", start, start + length - 1, o);
                start += length;
            }
            seqAnn.setComponent(c.getIdentity());

            count++;
        }
        if (newSeq != "") {
            if (comp.getSequences().isEmpty()) {
                /*String uniqueId = SBOLUtils.getUniqueDisplayId(null, null,
                                comp.getDisplayId() + "Sequence", comp.getVersion(), "Sequence", doc);*/
                String uniqueId = comp.getDisplayId().concat("_seq");
                comp.addSequence(doc.createSequence(uniqueId, comp.getVersion(), newSeq, Sequence.IUPAC_DNA));
            } else {
                comp.getSequences().iterator().next().setElements(newSeq);
            }
        }
    }
}
