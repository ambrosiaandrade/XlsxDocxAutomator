# XlsxDocxAutomator

**XlsxDocxAutomator** is a Java application built with **Spring Boot** to automate document generation.  
It programmatically populates **Word templates (.docx)** with data extracted from **Excel spreadsheets (.xlsx)**, organizing generated documents by session and user.

---

## About This Project

Thrilled to share my journey transforming education at the **University of Pernambuco (UPE)**.  
I streamlined document creation with Google Forms and Excel, and now I'm modernizing processes with Spring Boot and Google Sheets.  

This isn't just a project; it's a commitment to **revolutionizing the educational system**, cutting bureaucracy and empowering students and faculty.  
Excited for the positive impact ahead!  
**#EducationInnovation #UPE**

---

## Features

- Read Excel files (.xlsx) and extract row data  
- Populate Word templates (.docx) automatically  
- Organize generated documents per session and per user  
- Create temporary session-based folders to isolate files for each user  
- Optionally zip multiple documents for easy download  
- Automatic cleanup of temporary folders when the session expires  

---

## Technologies

- Java 17+  
- Spring Boot  
- Apache POI (Excel & Word handling)  
- SLF4J / Logback (logging)  
- Maven  

---

## Usage

### Running the application

1. Clone the repository:

```bash
git clone https://github.com/your-username/XlsxDocxAutomator.git
cd XlsxDocxAutomator
````

2. Build the project:

```bash
mvn clean install
```

3. Run the Spring Boot application:

```bash
mvn spring-boot:run
```

4. Access endpoints to upload Excel files and generate documents.

---

### Generating Documents

1. Upload an Excel file with user data.
2. Provide a Word template with placeholders matching Excel columns.
3. Each row generates a Word document inside a **user-specific folder**.
4. Documents are organized inside a **session folder**.
5. Optionally download all documents as a ZIP file.

---

## Process Flow

```plantuml
@startuml
!define RECTANGLE class

title XlsxDocxAutomator Process Flow

start
:User uploads Excel & Word Template;
:Read Excel rows;
repeat
  :For each row / user;
  :Create folder: /tmp/upe/<SESSION_ID>/<USER_NAME>;
  :Populate Word template with row data;
  :Save Word document in user folder;
repeat while (More rows?)

:Optionally zip documents for download;
:Return generated documents / ZIP to user;
stop

' -----------------------------------
' Folder structure visualization
' -----------------------------------
package "Temporary Folder Structure" {
    folder "/tmp/upe/" {
        folder "<SESSION_ID>/" {
            folder "<USER_1>/" {
                :Document1.docx;
                :Document2.pdf;
            }
            folder "<USER_2>/" {
                :Document1.docx;
            }
        }
    }
}

@enduml
```

---

## Contributing

Contributions are welcome! Open issues or submit pull requests for improvements.

---

## License

This project is licensed under the MIT License.
