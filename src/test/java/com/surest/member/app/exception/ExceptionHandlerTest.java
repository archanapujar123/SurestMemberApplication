package com.surest.member.app.exception;

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.WriteListener;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ExceptionHandlerTest {

    private GlobalExceptionHandler exceptionHandler;


     @Mock
     private HttpServletRequest request;

     @Mock
     private HttpServletResponse response;

     private CustomAccessDeniedHandler handler;

    private AutoCloseable closeable;


    @BeforeEach
     void setUp()  {
        closeable = MockitoAnnotations.openMocks(this);
        exceptionHandler = new GlobalExceptionHandler();
         handler = new CustomAccessDeniedHandler();

     }

    @AfterEach
    void tearDown() throws Exception {
        closeable.close();
    }

    @Test
    void testHandleResourceNotFoundException() {
        ResourceNotFoundException ex = new ResourceNotFoundException("Member not found");

        ResponseEntity<Map<String, String>> responseEntity = exceptionHandler.handleResourceNotFound(ex);

        assertNotNull(responseEntity.getBody());
        assertEquals("Member not found", responseEntity.getBody().get("error"));    }


    @Test
    void testValidationExceptions() {
        // Mock BindingResult with one field error
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError fieldError = new FieldError("memberDTO", "username", "must not be blank");
        when(bindingResult.getFieldErrors()).thenReturn(Collections.singletonList(fieldError));

        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        when(ex.getBindingResult()).thenReturn(bindingResult);

        ResponseEntity<Map<String, String>> responseEntity = exceptionHandler.handleValidationExceptions(ex);

        assertNotNull(responseEntity.getBody());
        assertEquals("must not be blank", responseEntity.getBody().get("username"));    }

    @Test
    void testHandleOtherExceptions() {
        Exception ex = new Exception("Something went wrong");

        ResponseEntity<Map<String, String>> responseEntity = exceptionHandler.handleOtherExceptions(ex);

        assertNotNull(responseEntity.getBody());
        assertEquals("An unexpected error occurred: Something went wrong",
                responseEntity.getBody().get("error"));    }

    @Test
    void testHandleShouldReturn403AndJsonResponse() throws IOException {
        // Create mock output stream
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ServletOutputStream servletOutputStream = new ServletOutputStream() {
            @Override
            public void write(int b)  {
                outputStream.write(b);
            }

            @Override
            public boolean isReady() {
                return true;
            }

            @Override
            public void setWriteListener(WriteListener listener) {
                // not needed for unit test
            }
        };

        when(response.getOutputStream()).thenReturn(servletOutputStream);
        when(request.getRequestURI()).thenReturn("/api/members/123");

        // Act
        handler.handle(request, response, new AccessDeniedException("Forbidden"));

        // Assert
        verify(response).setStatus(HttpServletResponse.SC_FORBIDDEN);
        verify(response).setContentType("application/json");

        String jsonOutput = outputStream.toString();
        assertTrue(jsonOutput.contains("\"status\":403"));
        assertTrue(jsonOutput.contains("\"error\":\"Forbidden\""));
        assertTrue(jsonOutput.contains("Access Denied"));
        assertTrue(jsonOutput.contains("\"path\":\"/api/members/123\""));
    }


 }
