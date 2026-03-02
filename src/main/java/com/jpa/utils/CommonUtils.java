package com.jpa.utils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.StringValue;
import com.google.protobuf.TypeRegistry;
import com.google.protobuf.util.JsonFormat;
import com.jpa.constant.ResponseStatus;
import com.jpa.dto.ResultDto;
import com.jpa.exceptions.CommonException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.lang.reflect.Method;
import java.security.SecureRandom;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class CommonUtils {
    final Environment env;

    public LocalDateTime milisecondToLocaleDate(long millis) {
        Instant instant = Instant.ofEpochMilli(millis);
        LocalDateTime date = instant.atZone(ZoneId.systemDefault()).toLocalDateTime();
        return date;
    }
    public String localDateToString(LocalDateTime date, String pattern) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
        return date.format(formatter);
    }
    public <T> Predicate<T> distinctByKey(Function<? super T, ?> keyExtractor) {
        Set<Object> seen = ConcurrentHashMap.newKeySet();
        return t -> seen.add(keyExtractor.apply(t));
    }
    public Date stringToDate(String date, String format) {
        try {
            return new SimpleDateFormat(format).parse(date);
        } catch (ParseException e) {
            return null;
        }
    }
    public String dateToString(Date date, String pattern) {
        if(date == null) {
            return null;
        }
        SimpleDateFormat format = new SimpleDateFormat(pattern);
        return format.format(date);
    }
    public String getPropertyValue(String key) throws Exception {
        String result = env.getProperty(key);
        if(result == null) {
            throw new Exception("Property " + key + " null");
        }
        return result;
    }
    public Map<String, String> getSppSignature(String params, String secretKey) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(secretKey.getBytes(), "HmacSHA256"));
        byte[] hash = mac.doFinal(params.getBytes());
        String signature = Base64.getEncoder().encodeToString(hash);
        Map<String, String> result = new HashMap<>();
        result.put("params", params);
        result.put("signature", signature);
        return result;
    }

//    public static void main(String[] args) throws Exception {
//        String params = "{\"amount\":37400000,\"payment_reference_id\":\"NEW202108250000\"}";
//        String secretKey = "1912010b01904df08e47dc6e2907df2f";
//        System.out.println(getSppSignature(params, secretKey));
//    }

//	public static <T> Map<String, String> getSppSignature(T t, String secretKey) throws Exception {
//		ObjectMapper mapper = new ObjectMapper();
//		mapper.setSerializationInclusion(Include.NON_NULL);
//		String paramsJson = mapper.writeValueAsString(t);
//		Mac mac = Mac.getInstance("HmacSHA256");
//		mac.init(new SecretKeySpec(secretKey.getBytes(), "HmacSHA256"));
//		byte[] hash = mac.doFinal(paramsJson.getBytes());
//		String signature = Base64.getEncoder().encodeToString(hash);
//		Map<String, String> result = new HashMap<>();
//		result.put("params", paramsJson);
//		result.put("signature", signature);
//		return result;
//	}

    public <T> ResultDto<T> handleApi(ExcuteApi<T> excuteApi) {
        try {
            var data = excuteApi.apply();
            return new ResultDto<T>()
                    .setStatus(ResponseStatus.SUCCESS.getCode())
                    .setData(data);
        }catch (CommonException e) {
            log.error("CommonExceptionHandle: " + e.getMessage(), e);
            return new ResultDto<T>()
                    .setStatus(ResponseStatus.ERROR.getCode())
                    .setMessage(e.getMessage());
        }catch (Exception e) {
            log.error("ExceptionHandle: " + e.getMessage(), e);
            return new ResultDto<T>()
                    .setStatus(ResponseStatus.ERROR.getCode())
                    .setMessage(e.getMessage());
        }
    }


//    private static final String ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
//    private static final String NUMERIC = "0123456789";
//    private static final String ALPHANUMERIC = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
//    private static final SecureRandom RANDOM = new SecureRandom();
//    private static final JsonFormat.Printer printer;
//    private static final JsonFormat.Parser parser;
//
//    public static String randomAlphabet(int length) {
//        StringBuilder sb = new StringBuilder(length);
//
//        for(int i = 0; i < length; ++i) {
//            sb.append("ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz".charAt(RANDOM.nextInt("ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz".length())));
//        }
//
//        return sb.toString();
//    }
//
//    public static String randomNumeric(int length) {
//        StringBuilder sb = new StringBuilder(length);
//
//        for(int i = 0; i < length; ++i) {
//            sb.append("0123456789".charAt(RANDOM.nextInt("0123456789".length())));
//        }
//
//        return sb.toString();
//    }
//
//    public static String randomAlphaNumeric(int length) {
//        StringBuilder sb = new StringBuilder(length);
//
//        for(int i = 0; i < length; ++i) {
//            sb.append("ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789".charAt(RANDOM.nextInt("ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789".length())));
//        }
//
//        return sb.toString();
//    }

    public static String getErrorTraceMessage(Throwable e) {
        try {
            String errorMsg = (String)Arrays.stream(e.getStackTrace()).filter((s) -> {
                return s.getClassName().contains("com.sacombank");
            }).map((s) -> {
                String var10000 = s.getClassName();
                return var10000 + "." + s.getMethodName() + "(" + s.getFileName() + ":" + s.getLineNumber() + ")";
            }).collect(Collectors.joining("; "));
            String message;
            if (e instanceof CommonException ex) {
                message = String.format("%s-%s", ex.getStatus(), ex.getMessage());
            } else {
                message = e.getMessage();
            }

            return String.format("message=%s cause=%s trace=%s", message, e.getCause(), errorMsg);
        } catch (Exception var4) {
            Exception ex = var4;
            return "getErrorTraceMessage: " + ex.getMessage();
        }
    }

//    public String grpcToString(Message message) {
//        try {
//            return printer.print(message);
//        } catch (InvalidProtocolBufferException var3) {
//            InvalidProtocolBufferException e = var3;
//            throw new CommonException(BaseStatus.ERROR.getCode(), e.getMessage());
//        }
//    }
//
//    public <T extends Message, V> T javaToGrpc(V javaObject, Class<T> grpcType) {
//        try {
//            String json = this.customGson.toJson(javaObject);
//            Method method = grpcType.getMethod("newBuilder");
//            Message.Builder builder = (Message.Builder)method.invoke((Object)null);
//            parser.merge(json, builder);
//            return builder.build();
//        } catch (Exception var6) {
//            Exception e = var6;
//            log.error("javaToGrpc: {}", getErrorTraceMessage(e));
//            throw new CommonException(BaseStatus.ERROR.getCode(), BaseStatus.ERROR.getDetail());
//        }
//    }
//
//    public <T> RequestDto<T> grpcToRequestDto(Message message, Class<T> innerType) {
//        TypeToken<?> typeToken = TypeToken.getParameterized(RequestDto.class, new Type[]{innerType});
//        return (RequestDto)this.customGson.fromJson(this.grpcToString(message), typeToken.getType());
//    }
//
//    public <T> RequestDto<T> stringToRequestDto(String message, Class<T> innerType) {
//        TypeToken<?> typeToken = TypeToken.getParameterized(RequestDto.class, new Type[]{innerType});
//        return (RequestDto)this.customGson.fromJson(message, typeToken.getType());
//    }
//
//    public <T> T grpcToJava(Message message, Class<T> innerType) {
//        return this.customGson.fromJson(this.grpcToString(message), innerType);
//    }
//
//    public static Object transformNullToEmpty(Object objectClass) {
//        try {
//            Field[] var9 = objectClass.getClass().getDeclaredFields();
//            int var2 = var9.length;
//
//            for(int var3 = 0; var3 < var2; ++var3) {
//                Field field = var9[var3];
//                field.setAccessible(true);
//                Object value = field.get(objectClass);
//                if (value == null && field.getType().equals(List.class)) {
//                    field.set(objectClass, new ArrayList());
//                } else if (value == null && field.getType().equals(String.class)) {
//                    field.set(objectClass, "");
//                }
//            }
//        } catch (RuntimeException var6) {
//            log.error("Convert null to empty has error {}", getErrorTraceMessage(var6));
//        } catch (Exception var7) {
//            Exception e = var7;
//            log.error("Convert null to empty has error {}", getErrorTraceMessage(e));
//        }
//
//        return objectClass;
//    }
//
//    static {
//        JsonFormat.TypeRegistry registry = TypeRegistry.newBuilder().add(StringValue.getDescriptor()).build();
//        printer = JsonFormat.printer().usingTypeRegistry(registry).includingDefaultValueFields().omittingInsignificantWhitespace();
//        parser = JsonFormat.parser().ignoringUnknownFields().usingTypeRegistry(registry);
//    }
}
