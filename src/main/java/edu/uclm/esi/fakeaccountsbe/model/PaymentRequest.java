package edu.uclm.esi.fakeaccountsbe.model;


public class PaymentRequest {
    private int amount; // Monto en centavos
    private String paymentMethodId; // ID del método de pago
    private String email; // Correo del usuario

    // Getters y setters
    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public String getPaymentMethodId() {
        return paymentMethodId;
    }

    public void setPaymentMethodId(String paymentMethodId) {
        this.paymentMethodId = paymentMethodId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}

