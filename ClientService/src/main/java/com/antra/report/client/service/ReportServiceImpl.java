package com.antra.report.client.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.antra.report.client.entity.ExcelReportEntity;
import com.antra.report.client.entity.PDFReportEntity;
import com.antra.report.client.entity.ReportRequestEntity;
import com.antra.report.client.entity.ReportStatus;
import com.antra.report.client.exception.ReportNotFoundException;
import com.antra.report.client.exception.RequestNotFoundException;
import com.antra.report.client.pojo.FileType;
import com.antra.report.client.pojo.reponse.ExcelResponse;
import com.antra.report.client.pojo.reponse.PDFResponse;
import com.antra.report.client.pojo.reponse.ReportVO;
import com.antra.report.client.pojo.reponse.SqsResponse;
import com.antra.report.client.pojo.request.ReportRequest;
import com.antra.report.client.repository.ReportRequestRepo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.io.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

@Service
public class ReportServiceImpl implements ReportService {
    private static final Logger log = LoggerFactory.getLogger(ReportServiceImpl.class);

    private final ReportRequestRepo reportRequestRepo;
    private final SNSService snsService;
    private final AmazonS3 s3Client;

    /*
    changed by Hengchao
     */
    public ReportServiceImpl(ReportRequestRepo reportRequestRepo, SNSService snsService, AmazonS3 s3Client) {
        this.reportRequestRepo = reportRequestRepo;
        this.snsService = snsService;
        this.s3Client = s3Client;
    }

    private ReportRequestEntity persistToLocal(ReportRequest request) {
        request.setReqId("Req-" + UUID.randomUUID().toString());

        ReportRequestEntity entity = new ReportRequestEntity();
        entity.setReqId(request.getReqId());
        entity.setSubmitter(request.getSubmitter());
        entity.setDescription(request.getDescription());
        entity.setCreatedTime(LocalDateTime.now());

        PDFReportEntity pdfReport = new PDFReportEntity();
        pdfReport.setRequest(entity);
        pdfReport.setStatus(ReportStatus.PENDING);
        pdfReport.setCreatedTime(LocalDateTime.now());
        entity.setPdfReport(pdfReport);

        ExcelReportEntity excelReport = new ExcelReportEntity();
        BeanUtils.copyProperties(pdfReport, excelReport);
        entity.setExcelReport(excelReport);

        return reportRequestRepo.save(entity);
    }

    @Override
    public ReportVO generateReportsSync(ReportRequest request) {
        persistToLocal(request);
        sendDirectRequests(request);
        return new ReportVO(reportRequestRepo.findById(request.getReqId()).orElseThrow());
    }

    /*
    changed by Hengchao
     */

    // TODO: Change to parallel process using Threadpool? CompletableFuture? finished
    private void sendDirectRequests(ReportRequest request) {
        RestTemplate rs = new RestTemplate();
        ExecutorService newMultipleThreadExecutor = Executors.newScheduledThreadPool(2);
        List<Callable<Void>> al = new ArrayList();
        al.add(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                ExcelResponse excelResponse = new ExcelResponse();
                try {
                    excelResponse = rs.postForEntity("http://localhost:8888/excel", request, ExcelResponse.class).getBody();
                } catch (Exception e) {
                    log.error("Excel Generation Error (Sync) : e", e);
                    excelResponse.setReqId(request.getReqId());
                    excelResponse.setFailed(true);
                } finally {
                    updateLocal(excelResponse);
                }
                return null;
            }
        });
        al.add(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                PDFResponse pdfResponse = new PDFResponse();
                try {
                    pdfResponse = rs.postForEntity("http://localhost:9999/pdf", request, PDFResponse.class).getBody();
                } catch (Exception e) {
                    log.error("PDF Generation Error (Sync) : e", e);
                    pdfResponse.setReqId(request.getReqId());
                    pdfResponse.setFailed(true);
                } finally {
                    updateLocal(pdfResponse);
                }
                return null;
            }
        });
        try {
            newMultipleThreadExecutor.invokeAll(al); // List<Future<Void>>
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void updateLocal(ExcelResponse excelResponse) {
        SqsResponse response = new SqsResponse();
        BeanUtils.copyProperties(excelResponse, response);
        updateAsyncExcelReport(response);
    }

    private void updateLocal(PDFResponse pdfResponse) {
        SqsResponse response = new SqsResponse();
        BeanUtils.copyProperties(pdfResponse, response);
        updateAsyncPDFReport(response);
    }

    @Override
    @Transactional
    public ReportVO generateReportsAsync(ReportRequest request) {
        ReportRequestEntity entity = persistToLocal(request);
        snsService.sendReportNotification(request);
        log.info("Send SNS the message: {}", request);
        return new ReportVO(entity);
    }

    @Override
    @Transactional
    public void updateAsyncPDFReport(SqsResponse response) {
        ReportRequestEntity entity = reportRequestRepo.findById(response.getReqId()).orElseThrow(RequestNotFoundException::new);
        var pdfReport = entity.getPdfReport();
        pdfReport.setUpdatedTime(LocalDateTime.now());
        if (response.isFailed()) {
            pdfReport.setStatus(ReportStatus.FAILED);
        } else {
            pdfReport.setStatus(ReportStatus.COMPLETED);
            pdfReport.setFileId(response.getFileId());
            pdfReport.setFileLocation(response.getFileLocation());
            pdfReport.setFileSize(response.getFileSize());
        }
        entity.setUpdatedTime(LocalDateTime.now());
        reportRequestRepo.save(entity);
    }

    @Override
    @Transactional
    public void updateAsyncExcelReport(SqsResponse response) {
        ReportRequestEntity entity = reportRequestRepo.findById(response.getReqId()).orElseThrow(RequestNotFoundException::new);
        var excelReport = entity.getExcelReport();
        excelReport.setUpdatedTime(LocalDateTime.now());
        if (response.isFailed()) {
            excelReport.setStatus(ReportStatus.FAILED);
        } else {
            excelReport.setStatus(ReportStatus.COMPLETED);
            excelReport.setFileId(response.getFileId());
            excelReport.setFileLocation(response.getFileLocation());
            excelReport.setFileSize(response.getFileSize());
        }
        entity.setUpdatedTime(LocalDateTime.now());
        reportRequestRepo.save(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReportVO> getReportList() {
        return reportRequestRepo.findAll().stream().map(ReportVO::new).collect(Collectors.toList());
    }

    /*
         change by Hengchao
     */

    @Override
    public InputStream getFileBodyByReqId(String reqId, FileType type) {
        ReportRequestEntity entity = reportRequestRepo.findById(reqId).orElseThrow(RequestNotFoundException::new);

        if (type == FileType.PDF) {
            String fileLocation = entity.getPdfReport().getFileLocation();
            String bucket = fileLocation.split("/")[0];
            String key = fileLocation.split("/")[1];
            return s3Client.getObject(bucket, key).getObjectContent();
        } else if (type == FileType.EXCEL) {
            String fileLocation = entity.getExcelReport().getFileLocation();
            String bucket = fileLocation.split("/")[0];
            String key = fileLocation.split("/")[1];
            return s3Client.getObject(bucket, key).getObjectContent();
        }
        return null;
    }

    /*
        changed by Hengchao
     */
    @Override
    @Transactional
    public void deleteReport(String reqId) {
        ReportRequestEntity entity = reportRequestRepo.findById(reqId).orElseThrow(ReportNotFoundException::new);

        String ExcelFileLocation = entity.getExcelReport().getFileLocation();
        String ExcelBucket = ExcelFileLocation.split("/")[0];
        String ExcelKey = ExcelFileLocation.split("/")[1];
        log.info(ExcelKey);
        if (!s3Client.doesBucketExist(ExcelBucket)) {
            throw new ReportNotFoundException();
        }
        String PDFFileLocation = entity.getPdfReport().getFileLocation();
        String PDFBucket = PDFFileLocation.split("/")[0];
        String PDFKey = PDFFileLocation.split("/")[1];
        log.info(PDFKey);
        if (!s3Client.doesBucketExist(PDFBucket)) {
            throw new ReportNotFoundException();
        }
        ObjectListing ExcelObjectListing = s3Client.listObjects(ExcelBucket);
//        int countEXCEL = 0;
        for (S3ObjectSummary objectSummary : ExcelObjectListing.getObjectSummaries()) {  // traverse
            String S3key = objectSummary.getKey();
            if (S3key.equals(ExcelKey))
                s3Client.deleteObject(ExcelBucket, S3key);
            break;
//            countEXCEL++;
        }
//        int countPDF = 0;
        ObjectListing PDFObjectListing = s3Client.listObjects(PDFBucket);
        for (S3ObjectSummary objectSummary : PDFObjectListing.getObjectSummaries()) {
            String S3key = objectSummary.getKey();
            if (S3key.equals(PDFKey))
                s3Client.deleteObject(PDFBucket, S3key);
            break;
//            countPDF++;
        }
        reportRequestRepo.delete(entity);
    }
}
