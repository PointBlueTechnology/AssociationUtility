package com.pointblue.idm.association.util;

import javax.naming.NamingException;
import javax.naming.ldap.InitialLdapContext;
import javax.naming.ldap.LdapContext;
import java.io.FileWriter;
import java.math.BigInteger;
import java.util.Date;
import java.util.Hashtable;

public class CommonImpl {

    static String idvHost;
    static String idvPort;
    static String idvDN;
    static String idvPwd;
    static String idvSearchBase;

    static String targetHost;
    static String targetPort;
    static String targetDN;
    static String targetPwd;

    static Integer logLevel = 0;
    static final Integer INFO = 1;
    static final Integer DEBUG = 2;
    static final Integer TRACE = 3;

    static  String driverDN;
    static String idvMatchAttr;
    static String targetMatchAttr;
    static String targetAssocValueAttr;
    //static boolean reportOnly = true;
    static Date now = new Date(0);
    static FileWriter csv;
    static {

        try {
            now = new Date();
            csv = new FileWriter("assocReport"+ now.getTime()+".csv");
        } catch (Exception e) {
            System.out.println("Error creating file: " + e.getMessage());
            System.exit(1);
        }

    }

    void log(Integer level, String message)
    {
        if (level <= logLevel)
        {
            System.out.println(this.getClass().getName() +": " + message);
        }
    }

    static void writeLine(String line) {
        try {
            csv.write(line);
            csv.write("\n");
            csv.flush();
        } catch (Exception e) {
            System.out.println("Error writing to file: " + e.getMessage());

        }

    }




    public static LdapContext getLdapCtx(String ldapHost, String loginDN, String pwd,
                                         boolean ssl, String ldapPort, boolean trustAllCerts)
    {
        LdapContext ldapCtx = null;

        try
        {
            // Create a Hashtable object.
            Hashtable env = new Hashtable(5, 0.75f);
            env.put("java.naming.ldap.attributes.binary", "GUID objectGUID");
            if (ssl)
            {
                // ldapPort     = LdapCtx.DEFAULT_SSL_PORT;
                env.put(javax.naming.Context.SECURITY_PROTOCOL, "ssl");

                if (trustAllCerts)
                {
                    env.put("java.naming.ldap.factory.socket",
                            "com.pointblue.idm.association.util.JndiSocketFactory");
                    System.out.println("trust all certs");
                }
            }
            else
            {
                //  ldapPort     = LdapCtx.DEFAULT_PORT;
            }

            env.put(javax.naming.Context.INITIAL_CONTEXT_FACTORY,
                    "com.sun.jndi.ldap.LdapCtxFactory");
            env.put(javax.naming.Context.PROVIDER_URL, "ldaps://" + ldapHost + ":"
                    + ldapPort);
            env.put(javax.naming.Context.SECURITY_AUTHENTICATION, "simple");
            env.put(javax.naming.Context.SECURITY_PRINCIPAL, loginDN);
            env.put(javax.naming.Context.SECURITY_CREDENTIALS, pwd);

//            System.out.println("ldapHost: " + ldapHost);
//            System.out.println("loginDN: " + loginDN);
//            System.out.println("pwd: " + pwd);



            // Construct an LdapContext object.
            ldapCtx = new InitialLdapContext(env, null);
        }
        catch (NamingException e)
        {
            System.out.println("REST: Error getting LdapCtx:  ");
            e.printStackTrace();
            //TODO: should let this flow through
        }

        return ldapCtx;
    }

    public static String guidToEdirString(byte[] decoded) {
//        if (guid ==null || guid.length() >0)
//            return "";
        //byte[] decoded = Base64.getDecoder().decode(guid);
        String hex = String.format("%x", new Object[] { new BigInteger(1, decoded) });
        String guidStr = String.format("%32s", new Object[] { hex }).replace(' ', '0');
        guidStr = guidStr.substring(6, 8) +guidStr.substring(4,6)+guidStr.substring(2,4)+guidStr.substring(0,2)+ "-" + guidStr.substring(10, 12)+guidStr.substring(8,10) + "-" + guidStr.substring(14, 16)+guidStr.substring(12,14) + "-" + guidStr.substring(16, 20) + "-" + guidStr.substring(20);
        return guidStr.toUpperCase();
    }


    public static String guidToADString(byte[] decoded) {
//        if (guid ==null || guid.length() >0)
//            return "";
        //byte[] decoded = Base64.getDecoder().decode(guid);
        String hex = String.format("%x", new Object[] { new BigInteger(1, decoded) });
        String guidStr = String.format("%32s", new Object[] { hex }).replace(' ', '0');
        //guidStr = guidStr.substring(6, 8) +guidStr.substring(4,6)+guidStr.substring(2,4)+guidStr.substring(0,2)+ "-" + guidStr.substring(10, 12)+guidStr.substring(8,10) + "-" + guidStr.substring(14, 16)+guidStr.substring(12,14) + "-" + guidStr.substring(16, 20) + "-" + guidStr.substring(20);
        return guidStr;
    }
}
