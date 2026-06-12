package com.patelhardware.model;

/**
 * Deliverable 3 – 2a
 * Response object returned after a registration attempt.
 * Serialised to JSON and sent back to the client.
 */
public class RegistrationResponse {

    private boolean success;
    private String  message;

    // ── Constructors ──────────────────────────────────────────────────────────

    public RegistrationResponse() {}

    public RegistrationResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    // ── Getters & Setters ─────────────────────────────────────────────────────

    public boolean isSuccess()              { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public String getMessage()              { return message; }
    public void setMessage(String message)  { this.message = message; }
}
