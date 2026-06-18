package com.example.ems.controller;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    // Saare live active tabs/browsers ke connections store karne ke liye registry
    private static final Map<String, SseEmitter> emitRegistry = new ConcurrentHashMap<>();

    // 1. Frontend is endpoint par connect hokar permanent free tunnel kholega
    @GetMapping(value = "/subscribe/{email}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribe(@PathVariable String email) {
        // 30 Minutes timeout standard initialization
        SseEmitter emitter = new SseEmitter(30 * 60 * 1000L);

        emitRegistry.put(email, emitter);

        emitter.onCompletion(() -> emitRegistry.remove(email));
        emitter.onTimeout(() -> emitRegistry.remove(email));
        emitter.onError((e) -> emitRegistry.remove(email));

        // Connection open hote hi chota sa instant success message check
        try {
            emitter.send(SseEmitter.event().name("INIT").data("Connected Successfully!"));
        } catch (IOException e) {
            emitRegistry.remove(email);
        }

        return emitter;
    }

    // 2. Helper Method: Isko call karke hum kisi bhi user ya pure system ko refresh event push kar sakte hain
    public static void sendRefreshSignalToAll() {
        emitRegistry.forEach((email, emitter) -> {
            try {
                emitter.send(SseEmitter.event().name("REFRESH").data("DATA_CHANGED"));
            } catch (IOException e) {
                emitRegistry.remove(email);
            }
        });
    }
}
