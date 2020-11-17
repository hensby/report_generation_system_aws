# Evaluation project
## Hengchao Wang
### What I did in Configure AWS and DB:
* create my own AMAZON SQS.

	***Excel_Request_Queue, Excel_Responce_Queue, PDF_Request_Queue, PDF_Responce_Queue***
	
* create my own AMAZON SNS.

	***reporting_topic***
	
* create my own AMAZON S3(Simple Storage Service, Buckets).

	***evaluation-bucket-file-excel, evaluation-bucket-file-pdf***

* generate AWS secret key access key.

* edit the AWS and MYSQL DB configuration in all three [application.properties](ClientService/src/main/resources/application.properties) files. 

### what I did in functional part:
* Edit ExcelServiceImpl.java in ExcelService to implement S3 storage 
**([ExcelServiceImpl](ExcelService/src/main/java/com/antra/evaluation/reporting_system/service/ExcelServiceImpl.java) line 50)**
	
	***Add S3client, save file in S3. sent the S3 location back to client server.***
	
* Edit the download function in clientService to implement download Excal file from S3 
**([ReportServiceImpl](ClientService/src/main/java/com/antra/report/client/service/ReportServiceImpl.java) line45, line194)**

    ***Modified download Excel function.***

* Change the sendDirectRequests method to parallel process using Threadpool, ExecutorService.
**([ReportServiceImpl](ClientService/src/main/java/com/antra/report/client/service/ReportServiceImpl.java) line 83)**

    ***create newScheduledThreadPool(2) and add two Callable<Void> object into thread pool. Implemented the multitasking of this method***    

* Implemented Delete report function 
**([ReportServiceImpl](ClientService/src/main/java/com/antra/report/client/service/ReportServiceImpl.java) line214, 
[reportController](ClientService/src/main/java/com/antra/report/client/controller/ReportController.java) line77, 
[app.js](ClientService/src/main/resources/static/app.js) line76, line86)**

    ***When client click delete button,frontend will send a DELETE request to clientService 
    "/report/content/{reqId}" (using AJAX). Design Restful API in reportController to handle DELETE requests. 
    Design the deleteReport method in ReportServiceImpl to do the logic part. Delete the report from S3 and local DB***

* Design GlobalExceptionHandler for three services to handle exceptions. 
**([GlobalExceptionHandler1](ClientService/src/main/java/com/antra/report/client/exceptionHandler/GlobalExceptionHandler.java),
[GlobalExceptionHandler2](ExcelService/src/main/java/com/antra/evaluation/reporting_system/exceptionHandler/GlobalExceptionHandler.java),
[GlobalExceptionHandler3](PDFService/src/main/java/com/antra/evaluation/reporting_system/ExcaptionHandler/GlobalExceptionHandler.java))**
    
    ***Design GlobalExceptionHandler for three services to handle different exceptions type of Exception***

* Edit ExcelGenerationServiceImpl in ExcelService to create temporary files 
**([ExcelGenerationServiceImpl](ExcelService/src/main/java/com/antra/evaluation/reporting_system/service/ExcelGenerationServiceImpl.java) line 103)**
	
	***create a temporary file in "_temp" folder instead of create a new file in root location. Easy to upload and delete.***

* Edit frontend JS file in ClientService to order result set 
**([app.js](ClientService/src/main/resources/static/app.js) line 7)**

	***keep the displayed results are ordered by CreateTime. User - friendly*** 

### Test

* Design API unit test by using Junit5 
**([APITest](ClientService/src/test/java/com/antra/report/client/APITest.java)
[PDFAPITest](PDFService/src/test/java/com/antra/evaluation/reporting_system/PDFAPITest.java)
[ExcelAPITest](ExcelService/src/test/java/com/antra/evaluation/reporting_system/ExcelAPITest.java))**

    ***Design API test to test all RestFul API in client services, to make sure all request is correct.***    

* Add AWS test in Excel and PDF services
**([AwsTest](ClientService/src/test/java/com/antra/evaluation/reporting_system/AwsTest.java))**

    ***Add three AWS test in Excel and PDF services to make sure S3 services are available in all services.***
    
## Contributor
[Hengchao Wang](https://github.com/hensby)