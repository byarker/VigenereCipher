//Imports
import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Set;

public class Main {

    //Why type out a character array when you can write code to make one for you.
    public static final String LowercaseAlphabet = "abcdefghijklmnopqrstuvwxyz";
    public static final String UppercaseAlphabet = LowercaseAlphabet.toUpperCase();
    public static final char[] AlphabetArray = UppercaseAlphabet.toCharArray();

    //To store the entered key if the user wishes.
    private static String encryptionKey = "";

    private static HashMap<Character, HashMap<Character, Character>> cipherTable = buildCipherTable(AlphabetArray);

    public static void main(String[] args) {

        //Define and pre-initialize variables
        String[] dialogOptions = {
                "Encrypt Plaintext",
                "Decrypt Ciphertext",
                "Display Table",
                "Exit"
        };
        int dialogChoice;
        boolean running = true;

        while (running) {
            dialogChoice = JOptionPane.showOptionDialog(
                    null,                 //No parent component
                    "Please select an option.",  //Body message
                    "Vigenere Encrypter",           //Title
                    JOptionPane.YES_NO_CANCEL_OPTION,
                    JOptionPane.QUESTION_MESSAGE,
                    null,                          //Icon
                    dialogOptions,                      //Options
                    null);                     //Initial Value

            switch (dialogChoice) {
                case 0: //"Encrypt Plaintext"
                    showEncryptPhrase();
                    break;
                case 1: //"Decrypt Ciphertext"
                    showDecryptPhrase();
                    break;
                case 2: //"Display Table"
                    showCipherTable();
                    break;
                case 3: //"Exit"
                    running = false; //End condition for the loop.
                case JOptionPane.CLOSED_OPTION:
                    System.exit(0); //Re-implement cross to exit functionality.
            }
        }

    }



    public static HashMap<Character, HashMap<Character, Character>> buildCipherTable(char[] alphabet) {

        //Create two HashMaps, one to store the finished Vigenere Table, and one that will store the inner HashMaps to aid with readability.
        HashMap<Character, HashMap<Character, Character>> cipherTable = new HashMap<>();
        HashMap<Character, Character> innerMap;

        for (int i = 0; i < alphabet.length; i++) {
            innerMap = new HashMap<>(); //Clear the HashMap at the start of each loop.
            for (int j = 0; j < alphabet.length; j++) {
                innerMap.put(alphabet[j], alphabet[((i + j) % 26)]);
            }
            cipherTable.put(alphabet[i], innerMap);
        }

        return cipherTable;
    }


    public static void showCipherTable() {
        //Initialise variables used. Internal HTML will be used to alter the font later, so the HTML break tag is used instead of the java newline.
        String table = "";
        final String lineBreak = "<br />";
        final Set<Character> tableKeys = cipherTable.keySet();

        /*Add each letter to the heading, plus a space for ease of readability.

        *|This section is the heading
        -+---------------------------
         |
         |
         |
         */
        String heading = "*|";
        for (char letter: tableKeys) { heading += letter + " "; }
        table += heading + lineBreak;

        //Create a line of dashes to separate the heading from the table.
        table += "-+" + "-".repeat(tableKeys.size() * 2) + lineBreak;


        //Initialise variables used within the loops
        HashMap<Character, Character> tempMap;
        String tempString;
        /* Loop through each outer HashMap (Each row), followed by each inner HashMap (for the values in the tables).

                *|ABCDEFG.......
                -+---------------------------
    This section | This section is
    is the outer | the inner loop
            loop |
         */
        for (char outerLetter: cipherTable.keySet()) {
            tempMap = cipherTable.get(outerLetter);
            tempString = outerLetter + "|";

            for (char innerLetter: tempMap.keySet()) {
                tempString += tempMap.get(innerLetter) + " ";
            }
            table += tempString + lineBreak;
        }

        /*Set options for the dialog box
            HTML tags are added around the table created above to allow for line breaks, as JLabel does not support standard Java "\n" newlines.
            Font is set to Monospaced to ensure the columns line up neatly.
        */
        JLabel label = new JLabel("<html>" + table + "</html>");
        label.setFont(new Font("Monospaced", Font.PLAIN, 18));

        //Show the finished dialog box
        JOptionPane.showMessageDialog( null, label, "Vigenere Table", JOptionPane.PLAIN_MESSAGE );
    }



    public static String encryptPhrase(String message, String key) {

        String mEncrypt = "";

        if (key == null || key.isBlank()) {return message;} //Skip the following process if no valid key is provided

        //Creative discretion, all output is upper case for simplicity.
        message = message.toUpperCase();
        key = key.toUpperCase();

        for (int i = 0,  j = 0; i < message.length(); i++) {
            if (Character.isWhitespace(message.charAt(i))) {continue;} //Skip whitespaces
            while (Character.isWhitespace(key.charAt(j))) {j = ++j % key.length();} //Cycle through the key if whitespace is found

            //The build cipher table simplifies this process significantly, and avoids unintuitive formulae
            mEncrypt += cipherTable.get(message.charAt(i)).get(key.charAt(j));

            //Cycle through the key at the end of each loop
            j = ++j % key.length();
        }

        return mEncrypt;
    }


    public static void showEncryptPhrase() {
        //Define and pre-initialize variables
        String[] options = {
                "Ok",
                "Ok And Save",
                "Cancel"
        };
        String message = "";
        String key = "";

        //Get the key from a regular input field
        String messageChoice = JOptionPane.showInputDialog(null, null, "Enter Plaintext Message", JOptionPane.QUESTION_MESSAGE);
        if (messageChoice == null) {return;} //Quit if cancel is selected

        //Get the key from a password field.
        JPasswordField passwordField = new JPasswordField(encryptionKey);
        int keyChoice = JOptionPane.showOptionDialog(
                null,                 //No parent component
                passwordField,                      //Input field in body
                "Enter Key",                   //Title
                JOptionPane.YES_NO_CANCEL_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,                          //Icon
                options,                            //Options
                "Ok");                     //Initial Value

        if (keyChoice == 2) { //"Cancel"
            return;
        } else { //"Ok" or "Ok And Save"
            key = new String(passwordField.getPassword());
            if (keyChoice == 1) {  //"Ok And Save"
                encryptionKey = key;
            }
        }

        String encryptedMessage = encryptPhrase(messageChoice, key);

        JTextArea outputField = new JTextArea(encryptedMessage);
        outputField.setEditable(false);
        JOptionPane.showMessageDialog(null, outputField, "Encrypted Message", JOptionPane.INFORMATION_MESSAGE);
    }



    public static String decryptPhrase(String message, String decryptionKey) {

        //Pre-initialization
        String decryptedMessage = "";
        HashMap<Character, Character> innerMap;

        if (decryptionKey == null || decryptionKey.isBlank()) {return message;} //Skip the following process if no valid key is provided

        //Creative discretion, all output is upper case for simplicity.
        message = message.toUpperCase();
        decryptionKey = decryptionKey.toUpperCase();

        for (int i = 0,  j = 0; i < message.length(); i++) {
            if (Character.isWhitespace(message.charAt(i))) {continue;} //Skip whitespaces
            while (Character.isWhitespace(decryptionKey.charAt(j))) {j = ++j % decryptionKey.length();} //Cycle through the key if whitespace is found

            //Decrypting through the table is more difficult than encrypting, requiring looping through the map until the value is found
            innerMap = cipherTable.get(decryptionKey.charAt(j));
            for (char key: innerMap.keySet()) {
                //Check each value in the inner HashMap to see if the value matches the encrypted message
                if (innerMap.get(key) == message.charAt(i)) {decryptedMessage += key; break;}
            }

            //Cycle through the key at the end of each loop
            j = ++j % decryptionKey.length();
        }

        return decryptedMessage;
    }


    public static void showDecryptPhrase() {
        //Define and pre-initialize variables
        String[] options = {
                "Ok",
                "Ok And Save",
                "Cancel"
        };
        String message = "";
        String key = "";

        //Get the key from a regular input field
        String messageChoice = JOptionPane.showInputDialog(null, null, "Enter Ciphertext Message", JOptionPane.QUESTION_MESSAGE);
        if (messageChoice == null) {return;} //Quit if cancel is selected

        //Get the key from a password field.
        JPasswordField passwordField = new JPasswordField(encryptionKey);
        int keyChoice = JOptionPane.showOptionDialog(
                null,                 //No parent component
                passwordField,                      //Input field in body
                "Enter Key",                   //Title
                JOptionPane.YES_NO_CANCEL_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,                          //Icon
                options,                            //Options
                "Ok");                     //Initial Value

        if (keyChoice == 2) { //"Cancel"
            return;
        } else { //"Ok" or "Ok And Save"
            key = new String(passwordField.getPassword());
            if (keyChoice == 1) {  //"Ok And Save"
                encryptionKey = key;
            }
        }

        //Run decryption method
        String decryptedMessage = decryptPhrase(messageChoice, key);

        //Output decrypted message in copyable field
        JTextArea outputField = new JTextArea(decryptedMessage);
        outputField.setEditable(false);
        JOptionPane.showMessageDialog(null, outputField, "Decrypted Message", JOptionPane.INFORMATION_MESSAGE);
    }
}
