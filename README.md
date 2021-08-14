# sbol2easy

A library for operations on sbol designs.

The main operations are:
* flattening design: converting components tree into one annotated sequence
* annotating: adding descriptive metadata to component definitions using attributes from Excel table
see ed.biordm.sbol.sbol2easy.transform.ComponentAnnotator
* genbank conversion: converts to sbol to genbank, but supports more features types and handles multiline descrptions
* generation from table: using sbol template to generate a library of similar designs using details from Excel table
* taming: removing SynBioHub specific namespaces from a document so it can be re-uploaded to the server




