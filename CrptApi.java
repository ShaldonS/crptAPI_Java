package com.example;

import java.net.http.*;
import java.net.URI;
import java.time.Duration;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import com.fasterxml.jackson.databind.ObjectMapper;

public class CrptApi {
    private final Semaphore semaphore;
    private final TimeUnit timeUnit;

    public CrptApi(TimeUnit timeUnit, int requestLimit) {
        this.timeUnit = timeUnit;
        this.semaphore = new Semaphore(requestLimit);
    }

    public static void main(String[] args) {
        prepareAndCreateDocument();
    }

    public static void prepareAndCreateDocument() {
        CrptApi api = new CrptApi(TimeUnit.SECONDS, 10);

        Document document = new Document();
        document.description = "Описание товара";
        document.doc_id = "123456789";
        document.doc_status = "NEW";
        document.doc_type = "LP_INTRODUCE_GOODS";
        document.importRequest = true;
        document.owner_inn = "1234567890";
        document.participant_inn = "123456789";
        document.producer_inn = "123456789";
        document.production_date = "2020-01-23";
        document.production_type = "Тип производства";
        document.products = new Document.Product[1];
        document.products[0] = new Document.Product();
        document.products[0].certificate_document = "Сертификат";
        document.products[0].certificate_document_date = "2020-01-23";
        document.products[0].certificate_document_number = "№123456";
        document.products[0].owner_inn = "1234567890";
        document.products[0].producer_inn = "1231231234";
        document.products[0].production_date = "2020-01-23";
        document.products[0].tnved_code = "Код ТН ВЭД";
        document.products[0].uit_code = "Код uit";
        document.products[0].uitu_code = "Код uitu";
        document.reg_date = "2020-01-23";
        document.reg_number = "Регистрационный номер";

        String signature = "sign";

        try {
            api.createDocument(document, signature);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void createDocument(Document document, String signature) throws InterruptedException {
        if (!semaphore.tryAcquire(1, timeUnit)) {
            semaphore.acquire();
        }
        try {
            String url = "https://ismp.crpt.ru/api/v3/lk/documents/create";
            sendPostRequest(document, signature, url);
        } catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            semaphore.release();
        }
    }

    private void sendPostRequest(Document document, String signature, String url) throws Exception {
        HttpClient client = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_2)
            .connectTimeout(Duration.ofSeconds(10))
            .build();

        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .timeout(Duration.ofMinutes(2))
            .header("Content-Type", "application/json")
            .header("Signature", signature)
            //.header("Authorization", "Bearer " + token)
            .POST(HttpRequest.BodyPublishers.ofString(new ObjectMapper().writeValueAsString(document)))
            .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        System.out.println("Response status: " + response.statusCode());
        //System.out.println(response.body());
    }

    public static class Document {
        public String description;
        public String doc_id;
        public String doc_status;
        public String doc_type;
        public boolean importRequest;
        public String owner_inn;
        public String participant_inn;
        public String producer_inn;
        public String production_date;
        public String production_type;
        public Product[] products;
        public String reg_date;
        public String reg_number;

        public static class Product {
            public String certificate_document;
            public String certificate_document_date;
            public String certificate_document_number;
            public String owner_inn;
            public String producer_inn;
            public String production_date;
            public String tnved_code;
            public String uit_code;
            public String uitu_code;
        }
    }
}
