package auth;

import services.DbManager;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.Scanner;

public class Authentication {

    private DbManager dbManager;
    private String filePath;

    public Authentication() {
        this.dbManager = DbManager.getInstance();
        this.filePath = dbManager.configProperties.getProperty("authDir")
                + "/UserProfile.txt";
    }

    private String currentUID;

    public String getCurrentUID(){return currentUID;};

    public boolean signUp() {

        String uID,password,pswd,que,ans;
        que = "Which is your birth place city?";

        Scanner sc= new Scanner(System.in);
        System.out.println("Please enter your userID: ");
        uID=sc.nextLine();

        if(IsUserExist(getHashCode(uID)))
        {
            System.out.println("User already exist");
            return false;
        }

        System.out.println("Please Enter your password: ");
        if(System.console()!=null) {
            char[] passwordChars = System.console().readPassword();
            password = new String(passwordChars);
        }else{
            password=sc.nextLine();
        }

        System.out.println("Please Reenter your password: ");
        if(System.console()!=null) {
            char[] passwordChars = System.console().readPassword();
            pswd = new String(passwordChars);
        }else{
            pswd=sc.nextLine();
        }

        if(!password.equals(pswd))
        {
            System.out.println("Sorry, password doesn't match");
            return false;
        }

        System.out.println("Please provide the answer of following security question: ");
        System.out.println(que);
        ans=sc.nextLine();

        if(!IsUserExist(getHashCode(uID)))
        {
            if(register(getHashCode(uID),getHashCode(password),ans)) {
                return true;
            }
            else
            {
                System.out.println("Something went wrong, can not register");
                return false;
            }
        }
        else
        {
            System.out.println("User already exist");
            return false;
        }


    }

    public boolean logIn() {

        String uID,password,que,ans;
        que = "Which is your birth place city?";

        Scanner sc= new Scanner(System.in);
        System.out.println("Please enter your userID: ");
        uID=sc.nextLine();

        System.out.println("Please Enter your password: ");
        if(System.console()!=null) {
            char[] passwordChars = System.console().readPassword();
            password = new String(passwordChars);
        }else{
            password=sc.nextLine();
        }

        System.out.println(que);
        ans=sc.nextLine();

        if(IsUserExist(getHashCode(uID.trim())))
        {
            if(isLoginCredentialCorrect(getHashCode(uID.trim()),getHashCode(password.trim()),ans.trim()))
            {
                currentUID = uID;
                return true;
            }
            else
            {
                System.out.println("Incorrect Login Credentials");
                return false;
            }
        }
        else
        {
            System.out.println("User does not exist");
            return false;
        }



    }

    //check for correct credentials
    private boolean isLoginCredentialCorrect(String hashedUID, String hashedPSWD, String ans)
    {
        try {
            File myObj = new File(filePath);
            Scanner myReader = new Scanner(myObj);
            while (myReader.hasNextLine()) {
                String data = myReader.nextLine();
                String[] arr = data.split("³");
                if(arr[0].equals(hashedUID) && arr[1].equals(hashedPSWD) && arr[2].equals(ans))
                {
                    return true;
                }
            }
            myReader.close();
        } catch (FileNotFoundException e) {
            System.out.println("An error occurred while reading files.");
            e.printStackTrace();
        }
        return false;
    }

    //simple registration
    private boolean register(String hashedUID, String hashedPSWD, String ans)
    {
        String text = "\n"+hashedUID.trim()+"³"+hashedPSWD.trim()+"³"+ans.trim();
        try {
            FileWriter fw = new FileWriter(filePath, true);
            fw.write(text);
            fw.close();

            dbManager.pushAuthFile();
        }
        catch(IOException e) {
            return false;
        }
        return true;
    }

    //Check UserExistence
    private boolean IsUserExist(String hashedUID)
    {
        try {
            File myObj = new File(filePath);
            Scanner myReader = new Scanner(myObj);
            while (myReader.hasNextLine()) {
                String data = myReader.nextLine();
                String[] arr = data.split("³");
                if(arr[0].equals(hashedUID))
                {
                    return true;
                }
            }
            myReader.close();
        } catch (FileNotFoundException e) {
            System.out.println("An error occurred while reading files.");
            e.printStackTrace();
        }
        return false;
    }

    //Hashing using MD5
    private String getHashCode(String inputString) {
        String encryptedText = "";
        try {
            MessageDigest MD = MessageDigest.getInstance("MD5");
            MD.update(inputString.getBytes());
            byte[] digest = MD.digest();
            BigInteger num = new BigInteger(1, digest);
            encryptedText = num.toString(16);
            while (encryptedText.length() < 32) {
                encryptedText = "0" + encryptedText;
            }
        }catch (Exception e){
            System.out.println("Exception occurred: " + e.toString());
        }
        return encryptedText;
    }
}
