package com.edson.gae_final.model;

import java.io.Serializable;

public class ProductInterest implements Serializable {

    private String cpf;
    private String productSalesId;
    private String crmId;
    private Double price;

    public String getCpf() {
        return cpf;
    }

    public void setCpf(String cpf) {
        this.cpf = cpf;
    }

    public String getProductSalesId() {
        return productSalesId;
    }

    public void setProductSalesId(String productSalesId) {
        this.productSalesId = productSalesId;
    }

    public String getCrmId() {
        return crmId;
    }

    public void setCrmId(String crmId) {
        this.crmId = crmId;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }
}
