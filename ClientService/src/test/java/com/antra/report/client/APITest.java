package com.antra.report.client;

import com.antra.report.client.controller.ReportController;
import com.antra.report.client.entity.ReportRequestEntity;
import com.antra.report.client.pojo.FileType;
import com.antra.report.client.service.ReportService;
import io.restassured.http.ContentType;
import io.restassured.module.mockmvc.RestAssuredMockMvc;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;

import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;

public class APITest {

    @Mock
    ReportService reportService;

    @BeforeEach
    public void configMock() {
        MockitoAnnotations.initMocks(this);
        RestAssuredMockMvc.standaloneSetup(new ReportController(reportService));
    }

    @Test
    public void testFileDownloadExcel() throws FileNotFoundException {
        String reqId = "Req-4cd17fa3-aa0a-4d9a-ad87-43fba16e6f22";
        Mockito.when(reportService.getFileBodyByReqId(anyString(), eq(FileType.EXCEL))).thenReturn(new FileInputStream("File-3bdb7010-f10d-4236-852e-6475f945754f.xlsx"));
        given().accept("application/json").get("/report/content/" + reqId + "/EXCEL").peek().
                then().assertThat()
                .contentType("application/vnd.ms-excel");
    }

    @Test
    public void testFileDownloadPDF() throws FileNotFoundException {
        String reqId = "Req-4cd17fa3-aa0a-4d9a-ad87-43fba16e6f22";
        Mockito.when(reportService.getFileBodyByReqId(anyString(), eq(FileType.PDF))).thenReturn(new FileInputStream("test.pdf"));
        given().accept("application/json").get("/report/content/" + reqId + "/PDF").peek().
                then().assertThat()
                .contentType("application/pdf");
    }

    @Test
    public void testListFiles() {
        Mockito.when(reportService.getReportList()).thenReturn(null);
        given().accept("application/json").get("/report").peek().
                then().assertThat()
                .statusCode(200);
    }

    @Test
    public void testDelete() {
        String reqId = "Req-2597d427-56a3-4ef6-8f18-bd383a05e099";
        given().accept("application/json").delete("/report/content/" + reqId + "/").peek().
                then().assertThat()
                .statusCode(200)
                .body("statusCode", Matchers.equalTo("OK"))
                .body("data", Matchers.equalTo("delete success"));
    }

    @Test
    public void testDelete1() {
        String reqId = "Req";
        given().accept("application/json").delete("/report/content/" + reqId + "/").peek().
                then().assertThat()
                .statusCode(200)
                .body("statusCode", Matchers.equalTo("OK"))
                .body("data", Matchers.equalTo("delete success"));
    }

    @Test
    public void testSync() {
        given().accept("application/json").contentType(ContentType.JSON)
                .body("{\"description\":\"Student Math Course Report\",\"headers\":[\"Student #\",\"Name\",\"Class\",\"Score\"],\"data\":[[\"s-008\",\"Sarah\",\"Class-A\",\"B\"]],\"submitter\":\"Mrs. York\"}")
                .post("/report/sync").peek().then().assertThat().statusCode(200);
    }

    @Test
    public void testAsync() {
        given().accept("application/json").contentType(ContentType.JSON)
                .body("{\"description\":\"Student Math Course Report\",\"headers\":[\"Student #\",\"Name\",\"Class\",\"Score\"],\"data\":[[\"s-008\",\"Sarah\",\"Class-A\",\"B\"]],\"submitter\":\"Mrs. York\"}")
                .post("/report/async").peek().then().assertThat().statusCode(200);
    }
}