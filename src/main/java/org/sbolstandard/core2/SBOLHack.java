/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.sbolstandard.core2;

/**
 *
 * @author tzielins
 */
public class SBOLHack {
    
    
    public static SBOLDocument getSBOLDocument(Identified elm) {
        return elm.getSBOLDocument();
    }
    

    public static SBOLConversionException conversionException(String msg) {
        return new SBOLConversionException(msg);
    }
    
    public static SBOLConversionException conversionException(Throwable cause) {
        return new SBOLConversionException(cause);
    }
    
    public static SBOLConversionException conversionException(String message, Throwable cause) {
        RuntimeException inter = new RuntimeException(message, cause);
        return new SBOLConversionException(inter);
    }
    
}
