package bnetp.util;

import android.util.Log;

import java.io.EOFException;
import java.io.IOException;

import bnetp.BNetInputStream;

public class StatString {
    private boolean parsed = false;
    private BNetInputStream is = null;


    private String[] statString2;
    private int product;
    private int level;
    private int icon;
    private String clan = "";

    public StatString(BNetInputStream is) {
        this.is = is;
//        try {
//            parse();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        Log.i("Stat", clan);
    }

    private void parse() throws IOException {
        if (parsed)
            return;
        parsed = true;

        if(is == null) {
            try {
//                product = HexDump.StringToDWord(statString[0]);
//                icon = product.getDword();
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

        if (statString2.length >= 2) {
            //3RAW 1R3W 1 UNB
//            Log.i("dd", statString2[0]);
//            icon = HexDump.StringToDWord(statString2[0]);
            level = Integer.parseInt(statString2[1]);


            if (statString2.length >= 3) {
                byte[] bytes = statString2[2].getBytes();
                for (int j = bytes.length - 1; j >= 0; j--)
                    clan += (char) bytes[j];
            }
        }
    }
}
