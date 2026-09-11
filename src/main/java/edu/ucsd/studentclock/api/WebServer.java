package edu.ucsd.studentclock.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import edu.ucsd.studentclock.model.Assignment;
import edu.ucsd.studentclock.model.Model;
import edu.ucsd.studentclock.model.Term;
import edu.ucsd.studentclock.model.WorkSession;
import edu.ucsd.studentclock.service.StudyHoursService;
import edu.ucsd.studentclock.util.SystemClock;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

/**
 * Lightweight HTTP server: REST API for assignments, work sessions, study hours;
 * serves static frontend from frontend/dist when present.
 * Updated to support new architecture: Term -> Course -> Series -> Assignment
 */
public final class WebServer {
    private static final int PORT = 8080;
    private static final Gson GSON = new GsonBuilder().create();

    private final Model model;
    private final SystemClock clock;
    private HttpServer server;

    public WebServer(Model model, SystemClock clock) {
        this.model = model;
        this.clock = clock;
    }

    public void start() throws IOException {
        server = HttpServer.create(new InetSocketAddress(PORT), 0);

        server.createContext("/api/assignments", this::handleAssignments);
        server.createContext("/api/work-sessions", this::handleWorkSessions);
        server.createContext("/api/study-hours", this::handleStudyHours);
        server.createContext("/api/progress", this::handleProgress);

        server.createContext("/", this::handleStatic);

        server.setExecutor(null);
        server.start();
        System.out.println("Server running at http://localhost:" + PORT);
    }

    public void stop() {
        if (server != null) {
            server.stop(0);
        }
    }

    private void handleAssignments(HttpExchange exchange) throws IOException {
        if (handleOptions(exchange)) return;

        String method = exchange.getRequestMethod();
        switch (method.toUpperCase()) {
            case "GET":
                List<AssignmentDto> list = model.getAllAssignments().stream()
                        .map(assignment -> new AssignmentDto(assignment.getId(), assignment.getName()))
                        .collect(Collectors.toList());
                sendJson(exchange, 200, list);
                break;
            case "POST":
                handlePostAssignment(exchange);
                break;
            default:
                sendError(exchange, 405, "method not allowed");
        }
    }

    private void handlePostAssignment(HttpExchange exchange) throws IOException {
        CreateAssignmentRequest request = parseBody(exchange, CreateAssignmentRequest.class);
        if (request == null || isBlank(request.name) || isBlank(request.courseName) || isBlank(request.seriesName)) {
            sendError(exchange, 400, "name, courseName, and seriesName required");
            return;
        }

        LocalDateTime dueDate = parseDueDate(request.dueDate);
        long estimatedMinutes = request.estimatedMinutes > 0 ? request.estimatedMinutes : 120L;
        
        Assignment assignment = new Assignment(
                UUID.randomUUID().toString(),
                request.name.trim(),
                dueDate,
                estimatedMinutes);
        
        if (model.addAssignment(request.courseName.trim(), request.seriesName.trim(), assignment)) {
            sendJson(exchange, 201, new AssignmentDto(assignment.getId(), assignment.getName()));
        } else {
            sendError(exchange, 500, "add failed");
        }
    }

    private void handleWorkSessions(HttpExchange exchange) throws IOException {
        if (handleOptions(exchange)) return;

        String method = exchange.getRequestMethod();
        if ("POST".equalsIgnoreCase(method)) {
            handlePostWorkSession(exchange);
        } else {
            sendError(exchange, 405, "method not allowed");
        }
    }

    private void handlePostWorkSession(HttpExchange exchange) throws IOException {
        CreateWorkSessionRequest request = parseBody(exchange, CreateWorkSessionRequest.class);
        if (request == null || isBlank(request.assignmentId) || request.durationMinutes <= 0) {
            sendError(exchange, 400, "assignmentId and durationMinutes > 0 required");
            return;
        }

        LocalDateTime end = clock.now();
        LocalDateTime start = end.minusMinutes(request.durationMinutes);
        String courseName = model.getCourseNameForAssignment(request.assignmentId.trim());
        WorkSession session = new WorkSession(
                start,
                end,
                request.assignmentId.trim(),
                courseName,
                request.durationMinutes);

        Assignment assignment = model.getAssignmentById(request.assignmentId.trim());
        if (assignment != null) {
            assignment.addWorkSession(session);
        }

        if (model.addWorkSession(session)) {
            model.saveTerm(); // persist assignment.minutes_worked to DB
            
            var series = model.getSeriesForAssignment(request.assignmentId.trim());
            if (series != null && assignment != null && 
                assignment.getMinutesWorked() >= assignment.getMinutesEstimated()) {
                series.advanceActiveAssignment();
                model.saveTerm();
            }
            sendJson(exchange, 201, Map.of("ok", true));
        } else {
            sendError(exchange, 500, "add failed");
        }
    }

    private void handleStudyHours(HttpExchange exchange) throws IOException {
        if (handleOptions(exchange)) return;

        if ("GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            Term term = model.getTerm();
            List<WorkSession> sessions = model.getWorkSessions();
            LocalDateTime now = clock.now();
            
            String hhmm = StudyHoursService.getRemainingHHMM(term, sessions, clock);
            long remaining = StudyHoursService.getRemainingMinutes(
                    sessions, term.getWeeklyTargetMinutes(), now);
            sendJson(exchange, 200, new StudyHoursDto(hhmm, remaining));
        } else {
            sendError(exchange, 405, "method not allowed");
        }
    }

    private void handleProgress(HttpExchange exchange) throws IOException {
        if (handleOptions(exchange)) return;

        if ("GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            Map<String, Long> byAssignment = StudyHoursService.getLoggedMinutesByAssignment(model.getWorkSessions());
            List<ProgressItemDto> list = model.getAllAssignments().stream()
                    .map(assignment -> new ProgressItemDto(
                            assignment.getId(), 
                            assignment.getName(),
                            byAssignment.getOrDefault(assignment.getId(), 0L)))
                    .collect(Collectors.toList());
            sendJson(exchange, 200, list);
        } else {
            sendError(exchange, 405, "method not allowed");
        }
    }

    private void handleStatic(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        if (path.equals("/")) path = "/index.html";
        if (path.startsWith("/api/")) {
            exchange.close();
            return;
        }

        Path base = Path.of("frontend/dist").toAbsolutePath();
        Path file = base.resolve(path.replaceFirst("^/", "")).normalize();
        if (!file.startsWith(base) || !Files.isRegularFile(file)) {
            file = base.resolve("index.html");
        }
        if (!Files.isRegularFile(file)) {
            sendResponse(exchange, 404, "Not found. Run 'npm run build' in frontend/ first.");
            return;
        }

        addCorsHeaders(exchange);
        String contentType = getContentType(path);
        exchange.getResponseHeaders().set("Content-Type", contentType);
        exchange.sendResponseHeaders(200, Files.size(file));
        
        try (OutputStream out = exchange.getResponseBody()) {
            Files.copy(file, out);
        }
    }

    private static String getContentType(String path) {
        if (path.endsWith(".html")) return "text/html; charset=utf-8";
        if (path.endsWith(".js")) return "application/javascript; charset=utf-8";
        if (path.endsWith(".css")) return "text/css; charset=utf-8";
        if (path.endsWith(".ico")) return "image/x-icon";
        return "application/octet-stream";
    }

    private void addCorsHeaders(HttpExchange exchange) {
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");
    }

    private void sendJson(HttpExchange exchange, int code, Object body) throws IOException {
        String json = GSON.toJson(body);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        sendResponse(exchange, code, json);
    }

    private void sendResponse(HttpExchange exchange, int code, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(code, bytes.length);
        try (OutputStream out = exchange.getResponseBody()) {
            out.write(bytes);
        }
    }

    private boolean handleOptions(HttpExchange exchange) throws IOException {
        if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
            addCorsHeaders(exchange);
            sendResponse(exchange, 204, "");
            return true;
        }
        addCorsHeaders(exchange);
        return false;
    }

    private <T> T parseBody(HttpExchange exchange, Class<T> clazz) throws IOException {
        String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        return GSON.fromJson(body, clazz);
    }

    private void sendError(HttpExchange exchange, int code, String message) throws IOException {
        sendJson(exchange, code, Map.of("error", message));
    }

    private static boolean isBlank(String s) {
        return s == null || s.isBlank();
    }

    private LocalDateTime parseDueDate(String dueDateStr) {
        if (dueDateStr == null || dueDateStr.isBlank()) {
            return clock.now().plusDays(7);
        }
        try {
            return LocalDateTime.parse(dueDateStr);
        } catch (Exception e) {
            return clock.now().plusDays(7);
        }
    }

    private static class CreateAssignmentRequest {
        String name;
        String courseName;
        String seriesName;
        String dueDate;
        long estimatedMinutes;
    }

    private static class CreateWorkSessionRequest {
        String assignmentId;
        int durationMinutes;
    }
}