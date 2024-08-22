package com.jpa.controller;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import com.jpa.dto.LoginRequest;
import com.jpa.entity.ProductEntity;
import com.jpa.service.ApiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/api")
public class ApiController {
    final ApiService apiService;
    final ObjectMapper customObjectMapper;

    private static final Marker IMPORTANT = MarkerFactory.getMarker("IMPORTANT");

    @PostMapping("/login")
    public String login(@RequestBody LoginRequest dto) {
        System.out.println(Thread.currentThread());
        if(!dto.getUsername().equals("0767786939")) {
            return """
                    {
                        "status": "07",
                        "message": "Wrong username or password!"
                    }""";
        }
        return """
                {
                     "status": "00",
                     "data": {
                         "accessToken": "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiIwNzY3Nzg2OTM5IiwiaWF0IjoxNjk4MjkxNzM4LCJleHAiOjE2OTgzNzgxMzh9.xDdijJwoHDkA_zrM9r81B_KmNIfva1vNBM0Ioj8mUpHtp25-3xEwN3LCsH5TvvzTqNSqUhTC4gEbeSzsIWc7Pg",
                         "issuedAt": "2023-10-26 03:42:18",
                         "expiration": "2023-10-27 03:42:18",
                         "tokenType": "Bearer",
                         "username": "0767786939",
                         "roles": [
                             "USER"
                         ]
                     }
                 }""";
    }

    @PostMapping("/checkAuthority")
    public ResponseEntity checkAuthority(@RequestHeader(name = "Authorization") String token) {
        System.out.printf("Token: " + token);
        String userToken = "Bearer eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiIwNzY3Nzg2OTM5IiwiaWF0IjoxNjk4MjkxNzM4LCJleHAiOjE2OTgzNzgxMzh9.xDdijJwoHDkA_zrM9r81B_KmNIfva1vNBM0Ioj8mUpHtp25-3xEwN3LCsH5TvvzTqNSqUhTC4gEbeSzsIWc7Pg";
        if(userToken.equals(token)) {
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    @PostMapping("/getProductData")
    public JsonNode getProductData() throws JsonProcessingException {
        String jsonStr = "{\"ownerArray\":[{\"id\":1,\"value\":\"John Nash\"},{\"id\":2,\"value\":\"Leonhard Euler\"},{\"id\":3,\"value\":\"Alan Turing\"}],\"categoryArray\":[{\"id\":1,\"value\":\"Clothing\"},{\"id\":2,\"value\":\"Jewelery\"},{\"id\":3,\"value\":\"Accessory\"}]}";
        JsonNode jsonNode = customObjectMapper.readTree(jsonStr);
//        String json = "{\"id\": 49, \"name\": \"小林　利奈\", \"type\": 1}";
//        return mapper.readTree(json);
        log.info("This is a log message that is not important!");
        log.info(IMPORTANT, "This is a very important log message!");
//        System.out.println(customObjectMapper.writeValueAsString(params));
        return jsonNode;
    }

    @PostMapping(value = "/products")
    public Page<ProductEntity> products(@RequestBody ProductEntity dto) throws InterruptedException {
//        TimeUnit.SECONDS.sleep(4);
        return apiService.getProductList(dto);
    }

    @CrossOrigin
    @PostMapping("/getStaffList")
    public JsonNode getStaffList(@RequestBody LoginRequest dto) throws Exception {
        System.out.println("username: " + dto.getUsername());
        String jsonStr = """
                [
                    {"name": "Sarah", "age": "19", "role": "Student"},
                    {"name": "Janine", "age": "43", "role": "Professor"},
                    {"name": "William", "age": "27", "role": "Associate Professor"}
                  ]""";
        JsonNode jsonNode = customObjectMapper.readTree(jsonStr);
//        TimeUnit.SECONDS.sleep(3);
        return jsonNode;
    }

    @GetMapping(value = "/threadName")
    public String threadName() {
        return Thread.currentThread().toString();
    }

    @PostMapping("/upload")
    public void upload(@RequestParam("file") MultipartFile file) {
        ChannelSftp channelSftp = null;
        Session session = null;
        try(InputStream inputStream = file.getInputStream()) {
            var jsch = new JSch();
//            System.setProperty("jsch.debug", "true");
            session = jsch.getSession("centos", "51.79.145.101", 22);
            session.setPassword("VQYKprnEJB3D");
            session.setConfig("StrictHostKeyChecking", "no");
            session.setTimeout(10000);
            session.connect();
            channelSftp = (ChannelSftp) session.openChannel("sftp");
            channelSftp.connect();
            channelSftp.cd("/home/centos/");
            channelSftp.put(inputStream, "report.xlsx");
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }finally {
            if(channelSftp != null && channelSftp.isConnected()) channelSftp.disconnect();
            if(session != null && session.isConnected()) session.disconnect();
        }
    }

}
