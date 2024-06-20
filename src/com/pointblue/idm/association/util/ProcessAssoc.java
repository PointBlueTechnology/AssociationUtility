package com.pointblue.idm.association.util;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class ProcessAssoc {

    public static void main(String[] args) {
        System.out.println("Starting ProcessAssoc");
        if (args.length < 2)
        {
            System.out.println("Usage: ProcessAssoc <config file>, <reportOnly|createAssoc>");
            System.exit(1);
        }
        Properties props = new Properties();
        try
        {
            props.load(new FileInputStream(args[0]));


        } catch (IOException e)
        {
            System.out.println("Error reading config file: " + e.getMessage());
            System.exit(1);
        }
        boolean createAssoc = args[1].equals("createAssoc");
        if (createAssoc)
        {
            System.out.println("Running in createAssoc mode");
        } else
        {
            System.out.println("Running in reportOnly mode");
        }

        String targetType = props.getProperty("targetType");
        if (targetType == null)
        {
            System.out.println("targetType not specified in config file");
            System.exit(1);
        }
        switch (targetType)
        {
            case "edir":
                System.out.println("Processing EDIR");
                ProcessEdir edir = new ProcessEdir();
                edir.run(props, !createAssoc);
                break;
            case "AD":
                System.out.println("Processing AD");
                ProcessAD ad = new ProcessAD();
                ad.run(props, !createAssoc);
                break;
            default:
                System.out.println("Unknown target type: " + targetType);
                System.exit(1);
        }

    }

}
