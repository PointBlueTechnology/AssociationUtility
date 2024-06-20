package com.pointblue.idm.association.util;

import javax.naming.NamingEnumeration;
import javax.naming.directory.*;
import javax.naming.ldap.LdapContext;
import java.util.HashSet;
import java.util.Properties;

public class ProcessAD extends CommonImpl {
    /*
    1. query for users from target
    2. query for users from idv
    3. process for matching users
    4. Report matching users
    5. Create association if not in report only mode
     */

    static String targetSearchBase;
    static String targetSearchFilter;


    public void run(Properties props, boolean reportOnly) {
        log(INFO, "Starting ProcessEdir");

        // Set the parameters
        idvHost = props.getProperty("idvHost");
        idvPort = props.getProperty("idvPort");
        idvDN = props.getProperty("idvDN");
        idvPwd = props.getProperty("idvPwd");
        idvSearchBase = props.getProperty("idvSearchBase");
        driverDN = props.getProperty("driverDN");
        idvMatchAttr = props.getProperty("idvMatchAttr");

        targetHost = props.getProperty("targetHost");
        targetPort = props.getProperty("targetPort");
        targetDN = props.getProperty("targetDN");
        targetPwd = props.getProperty("targetPwd");

        targetAssocValueAttr = props.getProperty("targetAssocValueAttr");
        targetMatchAttr = props.getProperty("targetMatchAttr");
        targetSearchBase = props.getProperty("targetSearchBase");
        targetSearchFilter = props.getProperty("targetSearchFilter");

        HashSet<String> matchValues = new HashSet<String>();

        // Get the LdapContext for the IDV
        LdapContext idvCtx = getLdapCtx(idvHost, idvDN, idvPwd, true, idvPort, true);
        if (idvCtx == null)
        {
            this.log(INFO, "Could not get LdapContext for IDV");
            return;
        }

        // Get the LdapContext for the target
        LdapContext targetCtx = getLdapCtx(targetHost, targetDN, targetPwd, true, targetPort, true);

        // Get the users from the target
        try{
            SearchControls sControls = new SearchControls();
            sControls.setSearchScope(SearchControls.SUBTREE_SCOPE);
            String[] returningAttributes = {targetMatchAttr, targetAssocValueAttr, "DirXML-Associations"};
            sControls.setReturningAttributes(returningAttributes);

            NamingEnumeration<SearchResult> targetUsers = targetCtx.search(targetSearchBase, targetSearchFilter, sControls);
            while(targetUsers.hasMore()){
                SearchResult result = targetUsers.next();
                System.out.println(result.getName());
                byte[] guid = (byte[]) (result.getAttributes().get(targetAssocValueAttr)).get();
                System.out.println("   GUID: "+ guidToADString(guid));

                Attribute matchAttribute = result.getAttributes().get(targetMatchAttr);
                if(matchAttribute != null)
                {
                    String matchValue = (String) matchAttribute.get();
                    if(matchValues.contains(matchValue))
                    {
                        System.out.println("   Duplicate match value: "+ matchValue + " : "+ result.getName());
                        continue; //move on to next user
                    }
                    matchValues.add(matchValue);
                    // System.out.println("   matchValue: " + matchValue);

                    NamingEnumeration matchedIdvUsers = idvCtx.search(idvSearchBase, "("+idvMatchAttr+"="+ matchValue +")", sControls);

                    //System.out.println("   Found Matching IDV Users: "+ matchedIdvUsers.hasMore());
                    if(!matchedIdvUsers.hasMoreElements())
                    {
                        System.out.println("   No Matching IDV Users: "+ matchValue);
                        writeLine(result.getName()+"|No Match found "+ matchValue);
                    }

                    while(matchedIdvUsers.hasMore()){
                        SearchResult idvUser = (SearchResult) matchedIdvUsers.next();
                        //System.out.println("checking for multiple matches");
                        if(matchedIdvUsers.hasMoreElements())
                        {
                            System.out.println("   Multiple Matching IDV Users: "+ matchValue);
                            break;
                        }
                        System.out.println("Found Matching IDV User: "+ idvUser.getName());
                        String associationValue = driverDN+"#1#"+guidToADString(guid);
                        Attribute currentAssociations = idvUser.getAttributes().get("DirXML-Associations");
                        //This check depends on the case of the DN being correct!
                        if(currentAssociations != null && currentAssociations.contains(associationValue))
                        {
                            System.out.println("   Association already exists: "+ associationValue);
                            writeLine(idvUser.getName() +"|"+"Association already exists|"+ associationValue);
                            continue;
                        }
                        if(currentAssociations != null)
                        {
                            NamingEnumeration currentAssociationsEnum = currentAssociations.getAll();
                            boolean hasConflictingAssociation = false;
                            while(currentAssociationsEnum.hasMoreElements())
                            {
                                String value = (String) currentAssociationsEnum.next();
                                if(value.toUpperCase().startsWith(driverDN.toUpperCase()))
                                {
                                    System.out.println("   Conflicting Association: "+ value);
                                    writeLine(idvUser.getName() +"|"+"Conflicting Association|"+ value);
                                    hasConflictingAssociation = true;
                                    continue;
                                }
                            }
                            if(hasConflictingAssociation)
                            {
                                continue;
                            }
                        }

                        if(reportOnly)
                        {
                            System.out.println("   Would Add Association: "+ associationValue);
                            writeLine(idvUser.getName() +"|"+"Would Add Association|"+ associationValue);
                        }
                        else
                        {
                            System.out.println("   Adding Association: "+ associationValue);
                            writeLine(idvUser.getName() +"|"+"Added Association|"+ associationValue);
                            BasicAttribute associationAttr = new BasicAttribute("DirXML-Associations", associationValue);
                            ModificationItem[] mods = new ModificationItem[]{new ModificationItem(DirContext.ADD_ATTRIBUTE, associationAttr)};
                            idvCtx.modifyAttributes(idvUser.getName(), mods);
                        }

                    }
                }
            }

        } catch (Exception e) {
            this.log(INFO, "Could not get users from target: "+e.getMessage());
            e.printStackTrace();
            return;
        }


        this.log(INFO, "Ending ProcessEdir");
    }

 HashSet<String> matchValues = new HashSet<String>();


}
