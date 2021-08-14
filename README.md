# sbol2easy

A library for operations on sbol designs.

The main operations are:
* annotating: adding descriptive metadata to component definitions using attributes from Excel table  
  see `ed.biordm.sbol.sbol2easy.transform.ComponentAnnotator`
* flattening design: converting components tree into one annotated sequence  
  see `ed.biordm.sbol.sbol2easy.transform.ComponentFlattener`
* genbank conversion: converts to sbol to genbank, but supports more features types and handles multiline descrptions  
  see `ed.biordm.sbol.sbol2easy.transform.GenBankConverter`
* generation from table: using sbol template to generate a library of similar designs using details from Excel table  
  see `ed.biordm.sbol.sbol2easy.transform.LibraryGenerator`
* taming: removing SynBioHub specific namespaces from a document so it can be re-uploaded to the server  
  see `ed.biordm.sbol.sbol2easy.transform.SynBioTamer`

## Usage

For example how to use check:  
`ed.biordm.sbol.sbol2easy.scrapbook.PaperRecipes`   
which in its main method simulates a workflows scenario that uses the inputs files from [examples](examples) 

### Metadata input file

Metadata that describe the biological designs to be generated or annotated in SBOL documents  
are specified in a metadata input file. 
These data are expected to be in tabular format in an MS Excel spreadsheet file, 
including any of the column names as described in the list below.

* display_id: This is the only mandatory column in the spreadsheet. It is used as the primary key to match with component definition display IDs in SynBioHub and in SBOL documents.
* name: The optional name of the design, which will be displayed as free text in the record in SynBioHub
* version: The optional version of the component definition, which can be either numeric (e.g. 1.0) or free text (e.g. 1.0-alpha)
* attachment_filename: The absolute or relative (from the current working directory) path to a file to be attached to the design in SynBioHub
* summary: The short description that will be written in the design’s description property of the SBOL component definition.
* description: The text that will appear as the record description in SynBioHub (mutable description)
* notes: The notes that will appear on the record in SynBioHub
* author: The authors that will be listed in the design in the SBOL document
* key: An entirely optional column that can be used to store a unique identifier string for a design, 
which can then be interpolated by the library when it is referenced in other columns, such as in the ‘display_id’ and ‘name’ cells’ values

The cells in the spreadsheet columns support simple templating using keyword strings such as “{key}”, “{display_id}” and “{name}”: 
the interpolation engine can then construct the target string value with the relevant values 
from those cells in the same row. 
In addition, standard Excel formulas are supported, for example ‘concatenate’ which provides powerful 
ways to combine values from other cells.

## Building

It is a standard maven project  
`maven clean install`






