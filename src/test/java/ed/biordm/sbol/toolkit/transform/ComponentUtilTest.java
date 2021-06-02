/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ed.biordm.sbol.toolkit.transform;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import org.sbolstandard.core2.AccessType;
import org.sbolstandard.core2.ComponentDefinition;
import org.sbolstandard.core2.SBOLDocument;

/**
 *
 * @author tzielins
 */
public class ComponentUtilTest {
    
    public ComponentUtilTest() {
    }
    
    ComponentUtil instance;
    SBOLDocument doc;
    
    @Before
    public void setUp() {
        instance = new ComponentUtil();
        
        doc = new SBOLDocument();
        doc.setDefaultURIprefix("http://bio.ed.ac.uk/a_mccormick/cyano_source/");
        doc.setComplete(true);
        doc.setCreateDefaults(true);        
    }

    @Test
    public void getsOneRootComponent() throws Exception {
        
        
        try {
            instance.extractRootComponent(doc);
            fail("Exception expected");
        } catch (IllegalArgumentException e){}
        
        ComponentDefinition c1 = doc.createComponentDefinition("uno", "1.0",ComponentDefinition.DNA_REGION);
        
        assertSame(c1, instance.extractRootComponent(doc));
        ComponentDefinition c2 = doc.createComponentDefinition("dos", "1.0",ComponentDefinition.DNA_REGION);
        
        try {
            instance.extractRootComponent(doc);
            fail("Exception expected");
        } catch (IllegalArgumentException e){}
        
        c1.createComponent("c2_insta", AccessType.PUBLIC, c2.getPersistentIdentity());
        
        assertSame(c1, instance.extractRootComponent(doc));
        
    }
    
    @Test
    public void getsLastVersionedComponentByName() throws Exception {
        
        
        String name = "dos";
        
        try {
            instance.extractComponent(name, doc);
            fail("Exception expected");
        } catch (IllegalArgumentException e){}
        
        ComponentDefinition c1 = doc.createComponentDefinition("uno", "1.0",ComponentDefinition.DNA_REGION);

        try {
            instance.extractComponent(name, doc);
            fail("Exception expected");
        } catch (IllegalArgumentException e){}

        
        ComponentDefinition c2 = doc.createComponentDefinition(name, "1.0",ComponentDefinition.DNA_REGION);
        
        assertSame(c2, instance.extractComponent(name, doc));
        
        c1.createComponent("c2_insta", AccessType.PUBLIC, c2.getPersistentIdentity());        
        assertSame(c2, instance.extractComponent(name, doc));
        
        ComponentDefinition c3 = doc.createComponentDefinition(name, "1.2",ComponentDefinition.DNA_REGION);
        assertSame(c3, instance.extractComponent(name, doc));
        
        c1.createComponent("c3_insta", AccessType.PUBLIC, c3.getPersistentIdentity());        
        assertSame(c3, instance.extractComponent(name, doc));
        
    }
    
    
}
