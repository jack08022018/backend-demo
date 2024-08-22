package com.jpa.grpc_service;

import com.jpa.utils.CommonUtils;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.jpa.grpc.EncryptorServiceGrpc;

@Slf4j
@GrpcService
@RequiredArgsConstructor
public class EncryptorServiceImpl extends EncryptorServiceGrpc.EncryptorServiceImplBase {
    final CommonUtils commonUtils;

    @Override
    public void decrypt(com.jpa.grpc.GrpcRequest request, StreamObserver<com.jpa.grpc.GrpcResponse> responseObserver) {
        responseObserver.onNext(com.jpa.grpc.GrpcResponse.newBuilder()
                        .setResponseCode("0000")
                .build());
        responseObserver.onCompleted();
    }

}
