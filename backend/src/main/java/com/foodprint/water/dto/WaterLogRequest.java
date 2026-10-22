package com.foodprint.water.dto;

/** Request body for logging water intake. Amount in millilitres (1 cup ≈ 237 ml). */
public class WaterLogRequest {

    /** Amount of water in millilitres. Use 237 per cup, 500 per standard bottle. */
    private double amountMl;

    private String note;

    public WaterLogRequest() {}

    public double getAmountMl() { return amountMl; }
    public void setAmountMl(double amountMl) { this.amountMl = amountMl; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
}
