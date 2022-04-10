import analysis.Analytics;
import auth.Authentication;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpException;
import parser.Parser;
import transactions.Transaction;

import java.io.IOException;
import java.util.Scanner;

public class D2_DB {

    public static void main(String[] arg) throws JSchException, SftpException, IOException {

        //Auth strts here
        boolean IsLoggedIn = false;
        Scanner sc = new Scanner(System.in);
        Authentication auth = new Authentication();

        while (!IsLoggedIn) {
            System.out.println("Please enter the numbers given below to perform the following action:\n1: SignUp\n2: Login");
            System.out.println("Enter your command: ");
            int inputt = Integer.parseInt(sc.nextLine());
            switch (inputt) {
                case 1:
                    try {
                        auth.signUp();
                    } catch (Exception e) {
                        System.out.println("Something went wrong, can not sign you up");
                        e.printStackTrace();
                    }
                    break;
                case 2:
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

        abc:
        while (true) {

            System.out.println("Which operation you wanna perform?\nPlease select provide number between 1 to 4 inclusive");
            System.out.println();
            System.out.println("1. Write Queries");
            System.out.println("2. Export");
            System.out.println("3. Data Models");
            System.out.println("4. Analytics");
            System.out.println("5. Exit");
            System.out.print("Enter your command: ");
            String input = sc.nextLine();
            Integer op = Integer.parseInt(input);
            //System.out.println("\n");
            switch (op) {
                case 1:
                    Parser parser = new Parser(currentUID);
                    parser.parseQuery();
                    break;
                case 2:
                    // code block to export
                    break;
                case 3:
                    // code block for Data Models
                    break;
                case 4:
                    Analytics a = new Analytics();
                    a.generateAnalytics();
                    break;
                case 5:
                    break abc;
                default:
                    System.out.println("Please select correct input");
                    break;
            }
        }

        Transaction t = new Transaction();
        t.copyWholeDirectory("C:\\G drive\\DW\\dummy", "C:\\G drive\\DW\\dummyc");
        t.deleteWholeDirectory("C:\\G drive\\DW\\dummyc");

        System.out.print("Enter your query here: ");
        String query = sc.nextLine();
        Parser parser = new Parser(currentUID);
        parser.getColumnValues(query.trim());
    }
}
