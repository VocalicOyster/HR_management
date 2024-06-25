package it.portfolio.hr.humanResource.models.DTOs.request;

import it.portfolio.hr.humanResource.entities.Hiring;
import jakarta.persistence.Column;
import jakarta.persistence.OneToOne;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EmployeesRequestDTO {

    private Long id;

    private String name;
    private String address;
    private String fiscalCode;
    private Long hiring_id;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getFiscalCode() {
        return fiscalCode;
    }

    public void setFiscalCode(String fiscalCode) {
        this.fiscalCode = fiscalCode;
    }

    public Long getHiring_id() {
        return hiring_id;
    }

    public void setHiring_id(Long hiring_id) {
        this.hiring_id = hiring_id;
    }
}
