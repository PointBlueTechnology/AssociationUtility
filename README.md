# IDM Association Utility

This utility is used to analyze matches between an NetIQ (Opentext) identity Manager Identity Vault (IDV) and a target system. It currently supports
eDirectory and Active Directory as target systems. The current code only supports single attribute mapping but can 
be easily extended. The output is useful for data analysis and cleansing. Enabling the createAssoc option can be used to pre-load the associations in the IDV.


The utility will create a pipe delimited
csv file with the following columns:
```
Target DN | result | association value | IDV object DN 
```
The possible results are:

    Duplicate match value - More than one object in the target system has the same match value

    Multiple Matches found - More than one object in the IDV has the match value

    Association already exists - Match found and the correct association already exists

    Conflicting Association - Match found but the existing association is incorrect

    No Match found - No match found in the IDV

    No match attribute - The object in the target system does not have the match attribute

```
Config files are used to define the IDV and target system configurations. The utility will read the config files and
analyze the matches between the two systems.

## Usage
java -jar IDMAssociationUtility.jar <config file> <'reportOnly'| 'createAssoc'>

The full class name is com.pointblue.idm.association.util.ProcessAssociation

Sample config files are provided