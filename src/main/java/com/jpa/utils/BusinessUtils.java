//package com.jpa.utils;
//
//import com.google.gson.Gson;
//import com.google.protobuf.InvalidProtocolBufferException;
//import com.google.protobuf.StringValue;
//import com.google.protobuf.util.JsonFormat;
//import com.jpa.dto.ResultDto;
//import com.jpa.exceptions.CommonException;
//import com.jpa.grpc.GrpcRequest;
//import lombok.AllArgsConstructor;
//import lombok.Getter;
//import lombok.RequiredArgsConstructor;
//import lombok.Setter;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Qualifier;
//import org.springframework.stereotype.Component;
//import org.springframework.util.CollectionUtils;
//import org.springframework.util.function.ThrowingFunction;
//
//import java.lang.reflect.Method;
//import java.util.stream.Collectors;
//
//@Slf4j
//@Component
//@RequiredArgsConstructor
//public class BusinessUtils {
//    @Autowired
//    @Qualifier("customGson")
//    private Gson customGson;
//
//    final Validator validator;
//    final CoreUtils coreUtils;
//    private static final JsonFormat.Printer printer;
//    private static final JsonFormat.Parser parser;
//
//    static {
//        var registry = JsonFormat.TypeRegistry.newBuilder()
//                .add(StringValue.getDescriptor())
//                .build();
//
//        printer = JsonFormat
//                .printer()
//                .usingTypeRegistry(registry)
//                .includingDefaultValueFields()
//                .omittingInsignificantWhitespace();
//
//        parser = JsonFormat
//                .parser()
//                .ignoringUnknownFields()
//                .usingTypeRegistry(registry);
//    }
//
//    public <T> void validateDto(RequestDto<T> dto) {
//        log.info("{}: validateRequest", dto.getLmid());
//        var violations = validator.validate(dto);
//        if(!CollectionUtils.isEmpty(violations)) {
//            var errorMsg = violations.stream()
//                    .peek(s -> {
//                        if("requestId".equals(s.getPropertyPath().toString())) {
//                            dto.setRequestId(null);
//                        }
//                        if("requestDateTime".equals(s.getPropertyPath().toString())) {
//                            dto.setRequestDateTime(null);
//                        }
//                        if("channel".equals(s.getPropertyPath().toString())) {
//                            dto.setChannel(null);
//                        }
//                    })
//                    .map(s -> {
//                        var fieldName = s.getPropertyPath()
//                                .toString()
//                                .replace("data.", "");
//                        return new ValidateError(fieldName, s.getMessage());
//                    })
//                    .sorted(Comparator.comparing(ValidateError::getFieldName))
//                    .map(ValidateError::getMessage)
//                    .collect(Collectors.joining("; "));
//            throw new CommonException(BaseStatus.INVALID_INPUT.getCode(), errorMsg);
//        }
//    }
//    @Getter
//    @Setter
//    @AllArgsConstructor
//    private static class ValidateError {
//        private String fieldName;
//        private String message;
//    }
//
//    public <T, R> ResponseDto<R> handleApi(ThrowingFunction<RequestDto<T>, R> excuteApi, RequestDto<T> dto, String functionName) {
//        if(StringUtils.isEmpty(dto.getLmid())) {
//            dto.setLmid(CoreUtils.randomAlphabet(6));
//        }
//        String lmid = dto.getLmid();
//        long start = System.currentTimeMillis();
//        log.info("{}: start {} request={}", lmid, functionName, customGson.toJson(dto));
//        try {
//            var data = excuteApi.apply(dto);
//            log.info("{}: end {} executeTime={}ms response={}", lmid, functionName, System.currentTimeMillis() - start, customGson.toJson(data));
//            return ResponseDto.buildResponse(dto, data, BaseStatus.SUCCESS.getCode(), BaseStatus.SUCCESS.getDetail());
//        } catch (CommonException e) {
//            log.error("{}: CommonExceptionHandle executeTime={}ms {}", lmid, System.currentTimeMillis() - start, CoreUtils.getErrorTraceMessage(e));
//            return ResponseDto.buildResponse(dto, null, e.getResponseCode(), e.getMessage());
//        } catch (Exception e) {
//            log.error("{}: InternalExceptionHandle executeTime={}ms {}", lmid, System.currentTimeMillis() - start, CoreUtils.getErrorTraceMessage(e));
//            return ResponseDto.buildResponse(dto, null, BaseStatus.ERROR.getCode(), BaseStatus.ERROR.getDetail());
//        }
//    }
//
//    public <T extends Message, R extends Message> R handleGrpcApi(T request, Class<R> responseType, ThrowingFunction<T, R> excuteApi) {
//        long start = System.currentTimeMillis();
//        String lmid = generateGrpcLmid(request);
//        try {
//            var builder = request.toBuilder();
//            builder.setField(builder.getDescriptorForType().findFieldByName("lmid"), lmid);
//            request = (T) builder.build();
//        }catch (Exception e) {
//            log.warn("setLmidToGrpcRequest: {}", CoreUtils.getErrorTraceMessage(e));
//        }
//        log.info("{}: handleGrpcApi request={}", lmid, getGrpcString(request));
//        try {
//            var data = excuteApi.apply(request);
//            log.info("{}: executeTime={}ms response={}", lmid, System.currentTimeMillis() - start, getGrpcString(data));
//            return data;
//        }catch (CommonException e) {
//            log.error("{}: CommonExceptionHandle executeTime={}ms {}", lmid, System.currentTimeMillis() - start, CoreUtils.getErrorTraceMessage(e));
//            return buildError(request, responseType, e.getResponseCode(), e.getMessage());
//        }catch (Exception e) {
//            log.error("{}: executeTime={}ms {}", lmid, System.currentTimeMillis() - start, CoreUtils.getErrorTraceMessage(e));
//            return buildError(request, responseType, BaseStatus.ERROR.getCode(), BaseStatus.ERROR.getDetail());
//        }
//    }
//
//    private <T extends Message> String generateGrpcLmid(T request) {
//        try {
//            var requestBuilder = request.toBuilder();
//            String lmid = (String) requestBuilder.getField(requestBuilder.getDescriptorForType().findFieldByName("lmid"));
//            return StringUtils.isEmpty(lmid) ? CoreUtils.randomAlphabet(6) : lmid;
//        }catch (Exception e) {
//            log.warn("generateGrpcLmid: {}", CoreUtils.getErrorTraceMessage(e));
//            return CoreUtils.randomAlphabet(6);
//        }
//    }
//    private String getGrpcString(Message message) {
//        try {
//            return printer.print(message);
//        } catch (InvalidProtocolBufferException e) {
//            log.warn("getGrpcString: {}", CoreUtils.getErrorTraceMessage(e));
//            return e.getMessage();
//        }
//    }
//    private <R extends Message, T extends Message> R buildError(T request, Class<R> responseType, String responseCode, String message) {
//        try {
//            var responseDto = new ResponseDto<String>()
//                    .setData(null)
//                    .setResult(new ResultDto()
//                            .setResponseCode(responseCode)
//                            .setDescription(message));
//
//            var json = customGson.toJson(responseDto);
//            final Method method = responseType.getMethod("newBuilder");
//            var builder = (Message.Builder) method.invoke(null);
//            parser.merge(json, builder);
//            mergeGrpcFieldFromReqToRes(request, builder, "lmid");
//            mergeGrpcFieldFromReqToRes(request, builder, "requestId");
//            mergeGrpcFieldFromReqToRes(request, builder, "requestDateTime");
//            mergeGrpcFieldFromReqToRes(request, builder, "channel");
//            return (R)builder.build();
//        } catch (Exception e) {
//            throw new CommonException(BaseStatus.ERROR.getCode(), e.getMessage());
//        }
//    }
//    private <T extends Message> void mergeGrpcFieldFromReqToRes(T request, Message.Builder builder, String fieldName) {
//        try {
//            String value = (String) request.getField(request.toBuilder().getDescriptorForType().findFieldByName(fieldName));
//            builder.setField(builder.getDescriptorForType().findFieldByName(fieldName), value);
//        }catch (Exception e) {
//            log.warn("mergeGrpcFieldFromReqToRes: fieldName={} {}", fieldName, CoreUtils.getErrorTraceMessage(e));
//        }
//    }
//
//    public static final String SPLIT_CHAR_CORRELATION_ID = "__";
//    public String generateJMSCorrelationID(GrpcRequest request, String adapterName) {
//        return adapterName
//                + SPLIT_CHAR_CORRELATION_ID + request.getLmid()
//                + SPLIT_CHAR_CORRELATION_ID + request.getFunctionName()
//                + SPLIT_CHAR_CORRELATION_ID + request.getTaskId();
//    }
//    public AdapterResponse jMSCorrelationIDToAdapterRequest(String jMSCorrelationID) {
//        var data = jMSCorrelationID.split(SPLIT_CHAR_CORRELATION_ID);
//        return new AdapterResponse()
//                .setLmid(data[1])
//                .setFunctionName(data[2])
//                .setTaskId(data[3]);
//    }
//
//}
