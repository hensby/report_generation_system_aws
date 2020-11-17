package com.antra.evaluation.reporting_system;

import com.antra.evaluation.reporting_system.endpoint.PDFGenerationController;
import com.antra.evaluation.reporting_system.service.PDFService;
import io.restassured.http.ContentType;
import io.restassured.module.mockmvc.RestAssuredMockMvc;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;

public class PDFAPITest {

    @Mock
    PDFService pdfService;

    @BeforeEach
    public void configMock() {
        MockitoAnnotations.initMocks(this);
        RestAssuredMockMvc.standaloneSetup(new PDFGenerationController(pdfService));
    }

    @Test
    public void findFile() throws FileNotFoundException {
        File f = new File("Coffee_Landscape.jasper");
        System.out.println(f.exists());

        File file = ResourceUtils.getFile("classpath:Coffee_Landscape.jasper");
        System.out.println(file.exists());
    }

    /*
    changed by Hengchao
     */
    @Test
    public void pdf() {
        given().accept("application/json").contentType(ContentType.JSON)
                .body("{\"description\":\"Student Math Course Report\",\"headers\":[\"Student #\",\"Name\",\"Class\",\"Score\"],\"data\":[[\"s-008\",\"Sarah\",\"Class-A\",\"B\"]],\"submitter\":\"Mrs. York\"}")
                .post("/pdf").peek().
                then().assertThat()
                .statusCode(200);
    }
}
