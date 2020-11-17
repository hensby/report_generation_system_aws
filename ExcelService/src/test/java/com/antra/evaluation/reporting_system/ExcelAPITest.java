package com.antra.evaluation.reporting_system;

import com.antra.evaluation.reporting_system.endpoint.ExcelGenerationController;
import com.antra.evaluation.reporting_system.service.ExcelService;
import io.restassured.http.ContentType;
import io.restassured.module.mockmvc.RestAssuredMockMvc;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.mockito.ArgumentMatchers.anyString;

public class ExcelAPITest {
    @Mock
    ExcelService excelService;

    @BeforeEach
    public void configMock() {
        MockitoAnnotations.initMocks(this);
        RestAssuredMockMvc.standaloneSetup(new ExcelGenerationController(excelService));
    }

    @Test
    @Disabled
    public void testFileDownload() throws FileNotFoundException {
        Mockito.when(excelService.getExcelBodyById(anyString())).thenReturn(new FileInputStream("temp.xlsx"));
        String reqId = "Req-3a33e0b0-839b-48e5-a34e-a73826c15c25";
        String type = "Excel";
        given().accept("application/json").get("/excel/content/"+reqId+type).peek().
                then().assertThat()
                .statusCode(200);
    }

    @Test
    @Disabled
    public void testListFiles() throws FileNotFoundException {
       // Mockito.when(excelService.getExcelBodyById(anyString())).thenReturn(new FileInputStream("temp.xlsx"));
        given().accept("application/json").get("/excel").peek().
                then().assertThat()
                .statusCode(200);
    }

    @Test
    @Disabled
    public void testExcelGeneration() throws FileNotFoundException {
        Mockito.when(excelService.getExcelBodyById(anyString())).thenReturn(new FileInputStream("File-3bdb7010-f10d-4236-852e-6475f945754f.xlsx"));
        given().accept("application/json").contentType(ContentType.JSON)
                .body("{\"description\":\"Student Math Course Report\",\"headers\":[\"Student #\",\"Name\",\"Class\",\"Score\"],\"data\":[[\"s-008\",\"Sarah\",\"Class-A\",\"B\"]],\"submitter\":\"Mrs. York\"}")
                .post("/excel").peek().
                then().assertThat()
                .statusCode(200);
//                .body("fileId", Matchers.notNullValue());
    }

    /*
        changed by Hengchao
     */

    @Test
    public void excel() {
        given().accept("application/json").contentType(ContentType.JSON)
                .body("{\"description\":\"Student Math Course Report\",\"headers\":[\"Student #\",\"Name\",\"Class\",\"Score\"],\"data\":[[\"s-008\",\"Sarah\",\"Class-A\",\"B\"]],\"submitter\":\"Mrs. York\"}")
                .post("/excel").peek().
                then().assertThat()
                .statusCode(200);
    }
}