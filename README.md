# IDM Association Utility

This utility is used to analyze matches between an Identity Vault (IDV) and a target system. It currently supports
eDirectory and Active Directory as target systems. The usility will create a pipe delimited
csv file with the following columns:
```
Taget DN | result | association value | IDV object DN 
```

Config files are used to define the IDV and target system configurations. The utility will read the config files and
analyze the matches between the two systems.

## Usage
java -jar IDMAssociationUtility.jar <config file> <'reportOnly'| 'createAssoc'>

The full class name is com.pointblue.idm.association.util.ProcessAssociation

Sample config files are provided