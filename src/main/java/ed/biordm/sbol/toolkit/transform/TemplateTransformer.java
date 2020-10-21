/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ed.biordm.sbol.toolkit.transform;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.sbolstandard.core2.AccessType;
import org.sbolstandard.core2.Component;
import org.sbolstandard.core2.ComponentDefinition;
import org.sbolstandard.core2.OperatorType;
import org.sbolstandard.core2.OrientationType;
import org.sbolstandard.core2.RestrictionType;
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

        ComponentDefinition prevCmpDef = null;
        ComponentDefinition newCmpDef = null;

        List<Component> cmpsToRemove = new ArrayList<>();

        for (Component c : parent.getSortedComponents()) {
            // If this component identity matches the generic component ID, replace it
            // if (c.getIdentity().equals(URI.create(genericComponentId))) {
            if (c.getDisplayId().equals(genericComponentId)) {
                prevCmpDef = c.getDefinition();

                // make copy of existing component definition - does version have to be supplied?
                // should use instantiateFromTemplate method here
                newCmpDef = (ComponentDefinition) doc.createCopy(prevCmpDef, cleanName, prevCmpDef.getVersion());
                newCmpDef.setName(cleanName);
                newCmpDef.addWasDerivedFrom(prevCmpDef.getIdentity());

                // Assume we are adding a new sequence to the component
                String version = "1.0.0"; // should this be the version of the component definition?
                Sequence seq = doc.createSequence(cleanName + "_seq", version,
                        newSequence, Sequence.IUPAC_DNA);
                newCmpDef.addSequence(seq);

                Component link = parent.createComponent(cleanName, AccessType.PUBLIC, newCmpDef.getIdentity());
                link.addWasDerivedFrom(c.getIdentity());
                link.setName(cleanName);

                cmpsToRemove.add(c);

                for (SequenceConstraint sc : parent.getSequenceConstraints()) {
                    Component object = sc.getObject();
                    Component subject = sc.getSubject();

                    if (subject.getIdentity().equals(c.getIdentity())) {
                        parent.removeSequenceConstraint(sc);
                        parent.createSequenceConstraint(sc.getDisplayId(), RestrictionType.PRECEDES, link.getIdentity(), object.getIdentity());
                    } else if (object.getIdentity().equals(c.getIdentity())) {
                        parent.removeSequenceConstraint(sc);
                        parent.createSequenceConstraint(sc.getDisplayId(), RestrictionType.PRECEDES, object.getIdentity(), link.getIdentity());
                    }
                }
            }
        }

        for (Component cmp : cmpsToRemove) {
            removeConstraintReferences(parent, cmp);
            parent.removeComponent(cmp);
        }

        // Add the flattened sequences to the parent component's SequenceAnnotation component
        //parent = flattenSequences(parent, newName, doc);
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

        rebuildSequences(newCmpDef, doc);

        return newCmpDef;
    }

    /**
     * name should be sanitized for conversion into display id as alphanumeric
     * with _ (replace all non alphanumeric characters with _)
     *
     * @param name
     * @return
     */
    protected String sanitizeName(String name) {
        String cleanName = name.replaceAll("[^\\p{IsAlphabetic}\\p{IsDigit}]", "_");
        return cleanName;
    }

    /**
     * Copied from
     * edu.utah.ece.async.sboldesigner.sbol.editor.SBOLDesign.rebuildSequences
     *
     * @param comp
     * @param doc
     * @throws SBOLValidationException
     */
    private void rebuildSequences(ComponentDefinition comp, SBOLDocument doc) throws SBOLValidationException {
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
                rebuildSequences(curr, doc);
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

    /**
     * following method is copied from
     * edu.utah.ece.async.sboldesigner.sbol.CombinatorialExpansionUtil
     *
     * @param originalTemplate
     * @param originalComponent
     * @param newParent
     * @param children
     * @throws SBOLValidationException
     */
    private static void addChildren(ComponentDefinition originalTemplate, Component originalComponent,
            ComponentDefinition newParent, HashSet<ComponentDefinition> children) throws SBOLValidationException {
        /* private static void addChildren(ComponentDefinition originalTemplate, Component originalComponent,
                    ComponentDefinition newParent, List<ComponentDefinition> children) throws SBOLValidationException {*/
        Component newComponent = newParent.getComponent(originalComponent.getDisplayId());
        newComponent.addWasDerivedFrom(originalComponent.getIdentity());

        if (children.isEmpty()) {
            removeConstraintReferences(newParent, newComponent);
            for (SequenceAnnotation sa : newParent.getSequenceAnnotations()) {
                if (sa.isSetComponent() && sa.getComponentURI().equals(newComponent.getIdentity())) {
                    newParent.removeSequenceAnnotation(sa);
                }
            }
            newParent.removeComponent(newComponent);
            return;
        }

        boolean first = true;
        for (ComponentDefinition child : children) {
            if (first) {
                // take over the definition of newParent's version of the
                // original component
                newComponent.setDefinition(child.getIdentity());
                first = false;
            } else {
                // create a new component
                /*String uniqueId = SBOLUtils.getUniqueDisplayId(newParent, null, child.getDisplayId() + "_Component",
                                            "1", "Component", null);*/
                String childId = child.getDisplayId().concat("_Component");
                Component link = newParent.createComponent(childId, AccessType.PUBLIC, child.getIdentity());
                link.addWasDerivedFrom(originalComponent.getIdentity());

                // create a new 'prev precedes link' constraint
                Component oldPrev = getBeforeComponent(originalTemplate, originalComponent);
                if (oldPrev != null) {
                    Component newPrev = newParent.getComponent(oldPrev.getDisplayId());
                    if (newPrev != null) {
                        String seqId = newParent.getDisplayId().concat("_SequenceConstraint");
                        /*uniqueId = SBOLUtils.getUniqueDisplayId(newParent, null,
                                                            newParent.getDisplayId() + "_SequenceConstraint", null, "SequenceConstraint", null);*/
                        newParent.createSequenceConstraint(seqId, RestrictionType.PRECEDES, newPrev.getIdentity(),
                                link.getIdentity());
                    }
                }

                // create a new 'link precedes next' constraint
                Component oldNext = getAfterComponent(originalTemplate, originalComponent);
                if (oldNext != null) {
                    Component newNext = newParent.getComponent(oldNext.getDisplayId());
                    if (newNext != null) {
                        String seqId = newParent.getDisplayId().concat("_SequenceConstraint");
                        /*uniqueId = SBOLUtils.getUniqueDisplayId(newParent, null,
                                                            newParent.getDisplayId() + "_SequenceConstraint", null, "SequenceConstraint", null);*/
                        newParent.createSequenceConstraint(seqId, RestrictionType.PRECEDES, link.getIdentity(),
                                newNext.getIdentity());
                    }
                }
            }
        }
    }

    /**
     * following method is copied from
     * edu.utah.ece.async.sboldesigner.sbol.CombinatorialExpansionUtil
     *
     * @param newParent
     * @param newComponent
     * @throws SBOLValidationException
     */
    private static void removeConstraintReferences(ComponentDefinition newParent, Component newComponent) throws SBOLValidationException {
        Component subject = null;
        Component object = null;
        for (SequenceConstraint sc : newParent.getSequenceConstraints()) {
            if (sc.getSubject().equals(newComponent)) {
                object = sc.getObject();
                //If we know what the new subject of this sequence constraint should be, modify it
                if (subject != null) {
                    sc.setSubject(subject.getIdentity());
                    object = null;
                    subject = null;
                } else {//else remove it
                    newParent.removeSequenceConstraint(sc);
                }
            }
            if (sc.getObject().equals(newComponent)) {
                subject = sc.getSubject();
                //If we know what the new object of this sequence constraint should be, modify it
                if (object != null) {
                    sc.setObject(object.getIdentity());
                    object = null;
                    subject = null;
                } else {//else remove it
                    newParent.removeSequenceConstraint(sc);
                }
            }
        }
    }

    /**
     * following method is copied from
     * edu.utah.ece.async.sboldesigner.sbol.CombinatorialExpansionUtil
     *
     * @param template
     * @param component
     * @return
     */
    private static Component getBeforeComponent(ComponentDefinition template, Component component) {
        for (SequenceConstraint sc : template.getSequenceConstraints()) {
            if (sc.getRestriction().equals(RestrictionType.PRECEDES) && sc.getObject().equals(component)) {
                return sc.getSubject();
            }
        }
        return null;
    }

    /**
     * following method is copied from
     * edu.utah.ece.async.sboldesigner.sbol.CombinatorialExpansionUtil
     *
     * @param template
     * @param component
     * @return
     */
    private static Component getAfterComponent(ComponentDefinition template, Component component) {
        for (SequenceConstraint sc : template.getSequenceConstraints()) {
            if (sc.getRestriction().equals(RestrictionType.PRECEDES) && sc.getSubject().equals(component)) {
                return sc.getObject();
            }
        }
        return null;
    }

    private static HashSet<HashSet<ComponentDefinition>> group(HashSet<ComponentDefinition> variants,
            OperatorType operator) {
        HashSet<HashSet<ComponentDefinition>> groups = new HashSet<>();

        for (ComponentDefinition CD : variants) {
            HashSet<ComponentDefinition> group = new HashSet<>();
            group.add(CD);
            groups.add(group);
        }

        if (operator == OperatorType.ONE) {
            return groups;
        }

        if (operator == OperatorType.ZEROORONE) {
            groups.add(new HashSet<>());
            return groups;
        }

        groups.clear();
        generateCombinations(groups, variants.toArray(new ComponentDefinition[0]), 0, new HashSet<>());
        if (operator == OperatorType.ONEORMORE) {
            return groups;
        }

        if (operator == OperatorType.ZEROORMORE) {
            groups.add(new HashSet<>());
            return groups;
        }

        throw new IllegalArgumentException(operator.toString() + " operator not supported");
    }

    /**
     * Generates all combinations except the empty set.
     */
    private static void generateCombinations(HashSet<HashSet<ComponentDefinition>> groups,
            ComponentDefinition[] variants, int i, HashSet<ComponentDefinition> set) {
        if (i == variants.length) {
            if (!set.isEmpty()) {
                groups.add(set);
            }
            return;
        }

        HashSet<ComponentDefinition> no = new HashSet<>(set);
        generateCombinations(groups, variants, i + 1, no);

        HashSet<ComponentDefinition> yes = new HashSet<>(set);
        yes.add(variants[i]);
        generateCombinations(groups, variants, i + 1, yes);
    }

    /*private static HashSet<ComponentDefinition> collectVariants(SBOLDocument doc, VariableComponent vc)
                    throws SBOLValidationException {
            HashSet<ComponentDefinition> variants = new HashSet<>();

            //Recursively collect variants from possible nested VariantDerivations 
//		for(CombinatorialDerivation cd : vc.getVariantDerivations())
//		{
//			for (VariableComponent v : cd.getVariableComponents()) {
//				variants.addAll(collectVariants(doc, v));
//				
//			}
//		}
            // add all variants
            variants.addAll(vc.getVariants());

            // add all variants from variantCollections
            for (Collection c : vc.getVariantCollections()) {
                    for (TopLevel tl : c.getMembers()) {
                            if (tl instanceof ComponentDefinition) {
                                    variants.add((ComponentDefinition) tl);
                            }
                    }
            }

            // add all variants from variantDerivations
            for (CombinatorialDerivation derivation : vc.getVariantDerivations()) {
                    variants.addAll(enumerate(doc, derivation));
            }

            return variants;
    }*/
}
