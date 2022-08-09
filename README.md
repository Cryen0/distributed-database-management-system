# Distributed Database Management System
D2_DB is a distributed database management system that manages the data in its rawest form. It does not use any JSON or CSV to store data. Instead it stores data in a simple file where the first line is considered as the column names and all the other lines are considered as an individual entry. Each cell is split by a superscript (²) value as this is the most uncommon character used in storing data.<br>

This CLI based application will parse the queries just as SQL database would with a very similar syntax, manage multiple user authentication, handle transactions, log fired queries, generate an ERD. Apart from these, it can also export an entire database into a dump which can be imported into another SQL service and provide analytics of the same.<br>

The feature that makes this DBMS different is the distributed behavior. The system splits the data horizontally across two different servers hosted on Google Compute Engine VM using SSH and SCP protocols. <br> 
## Core Team
 1. Kavya Kasaraneni
 2. Naveed Hussain Khowaja
 3. Ridham Kathiriya
 4. Siddharth Kharwar
 5. Tasnim Khan

## Contributing
Pull requests are welcome. For major changes, please open an issue first to discuss what you would like to change. Please make sure to update tests as appropriate.
