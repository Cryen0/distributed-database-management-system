import model.Column;
import model.Table;
import services.DbManager;

public class TempSimulator {
    public static void main(String[] args) {
        DbManager dbManager = DbManager.getInstance();

        // Create DB
        String dbName = "School";
        if (!dbManager.dbExists(dbName)) {
            System.out.println("Db Created - " + dbName + ": " + dbManager.createDb(dbName));
        } else {
            System.out.println("Db - " + dbName + ": Exists!");
        }

        // Set Current DB
        dbManager.setCurrentDb(dbName);

        // Create Table - Course
        String tableCourse = "Course";
        if (!dbManager.tableExists(tableCourse)) {
            Table table = new Table();
            table.setName(tableCourse);

            Column courseID = new Column("CourseID", "int", true);
            Column name = new Column("Name", "varchar", false);
            Column semester = new Column("Semester", "varchar", false);

            table.getColumnList().add(courseID);
            table.getColumnList().add(name);
            table.getColumnList().add(semester);

            System.out.println("Table Created - " + table.getName() + ": " + dbManager.createTable(table));
        } else {
            System.out.println("Table - " + tableCourse + ": Exists!");
        }

        // Create Table - Books
        String tableBooks= "Books";
        if (!dbManager.tableExists(tableBooks)) {
            Table table = new Table();
            table.setName(tableBooks);

            Column bookId = new Column("BookId", "int", true);
            Column name = new Column("Name", "text", false);
            Column author = new Column("Author", "varchar", false);
            Column price = new Column("Price", "double", false);

            table.getColumnList().add(bookId);
            table.getColumnList().add(name);
            table.getColumnList().add(author);
            table.getColumnList().add(price);

            System.out.println("Table Created - " + table.getName() + ": " + dbManager.createTable(table));
        } else {
            System.out.println("Table - " + tableBooks + ": Exists!");
        }


        // Delete DB
//        if (dbManager.dbExists("School")) {
//            System.out.println("Db Delete - School: " + dbManager.deleteDb("School"));
//        } else {
//            System.out.println("Db - School: does not Exists!");
//        }

        dbManager.disconnectSession();
    }
}
