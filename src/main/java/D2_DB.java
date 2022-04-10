import analysis.Analytics;
import auth.Authentication;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpException;
import erd.Erd;
import export.Export;
import parser.Parser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Scanner;

public class D2_DB {

    public static void main(String[] arg) throws JSchException, SftpException, IOException {

        //Auth strts here
        boolean IsLoggedIn = false;
        InputStreamReader ir = new InputStreamReader(System.in);
        BufferedReader bufferedReader = new BufferedReader(ir);
        Scanner sc = new Scanner(System.in);
        Authentication auth = new Authentication();

        while (!IsLoggedIn) {
            System.out.println("Please enter the numbers given below to perform the following action:\n1: SignUp\n2: Login");
            System.out.print("Enter your command:\t");
//            String inputt = Integer.parseInt(sc.nextLine());
            String input = bufferedReader.readLine();
            switch (input) {
                case "1":
                    try {
                        auth.signUp();
                    } catch (Exception e) {
                        System.out.println("Something went wrong, can not sign you up");
                        e.printStackTrace();
                    }
                    break;
                case "2":
                    try {
                        if (auth.logIn()) {
                            IsLoggedIn = true;
                            System.out.println("You are now logged in");
                        }
                    } catch (Exception e) {
                        System.out.println("Something went wrong, can not log you in");
                        e.printStackTrace();
                    }
                    break;
                default:
                    System.out.println("Incorrect input");
                    break;
            }

        }
        //auth ends
        String currentUID = auth.getCurrentUID();
        System.out.println("Welcome " + currentUID);

        operationLoop:
        while (true) {

            System.out.println("Which operation you wanna perform?\nPlease select provide number between 1 to 4 inclusive");
            System.out.println();
            System.out.println("1. Write Queries");
            System.out.println("2. Export");
            System.out.println("3. Data Models");
            System.out.println("4. Analytics");
            System.out.println("5. Exit");
            System.out.print("Enter your command:\t");
//            String input = sc.nextLine();
//            Integer op = Integer.parseInt(input);
            String op = bufferedReader.readLine();
            //System.out.println("\n");
            switch (op) {
                case "1":
                    Parser parser = new Parser(currentUID);
                    parser.parseQuery();
                    break;
                case "2":
                    // code block to export
                    Export dump = new Export();
                    System.out.print("Enter Database Name: ");
                    String db = sc.nextLine();
                    dump.export(db);
                    break;
                case "3":
                    // code block for Data Models
                    System.out.println("Enter the valid database name: ");
                    String db1 = sc.nextLine();
                    Erd erd = new Erd();
                    erd.generateErd(db1);
                    break;
                case "4":
                    Analytics a = new Analytics();
                    a.generateAnalytics();
                    break;
                case "5":
                    break operationLoop;
                default:
                    System.out.println("Please select correct input");
                    break;
            }
        }
    }
}
