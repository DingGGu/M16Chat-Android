package bnetp.util;

import android.util.Log;

import java.io.EOFException;
import java.io.IOException;

import bnetp.BNetInputStream;

public class StatString {
    private boolean parsed = false;
    private BNetInputStream is = null;

    private String[] statString;
    private String[] statString2;
    private int product;
    private int level;
    private int icon;
    private String clan = "";

    public StatString(BNetInputStream is) {
        this.is = is;
        try {
            parse();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public int getIcon() {
        return icon;
    }

    public String getClan() {
        return clan;
    }
    public boolean checkClan() {
        return clan.equals("");
    }

    private void parse() throws IOException {
        if (parsed)
            return;
        parsed = true;

        if(is == null) {
            try {
                product = HexDump.StringToDWord(statString[0]);
            } catch(Exception e) {
                return;
            }
        } else {
            try {
                product = is.readDWord();
            } catch(EOFException e) {
                return;
            }
        }

        if (is != null) {
            if (is.readByte() == 0) // discard the space, or terminate the string
                return;
            statString2 = is.readNTString().split(" ");
        }

        if ((is != null) || (statString.length > 1) || (statString[0].length() > 4)) {
            //3RAW 1R3W 1 UNB
            icon = HexDump.StringToDWord(statString2[0]);
            level = Integer.parseInt(statString2[1]);


            if (statString2.length >= 3) {
                byte[] bytes = statString2[2].getBytes();
                for (int j = bytes.length - 1; j >= 0; j--)
                    clan += (char) bytes[j];
            }
        }
    }
}
