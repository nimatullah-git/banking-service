package com.example.banking.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.time.LocalDate;

/**
 * @author nimatullah
 */

/**
 * Data Transfer Object (DTO) для представления данных клиента в ответе.
 * Игнорирует поля "username" и "password" при сериализации в JSON.
 */
@Data
@JsonIgnoreProperties({"username", "password"})
public class ClientResponseDTO {
    private Long id;
    private String username;
    private String password;
    private String fullName;
    private String phoneNumber;
    private String email;
    private LocalDate birthDate;

    /**
     * Устанавливает дату рождения клиента из строки в формате "yyyy-MM-dd".
     * Если этот сеттер не будет определен, Jackson будет ожидать строку в формате ISO
     * и не сможет автоматически преобразовать ее в LocalDate (устанавливается значение null).
     *
     * @param birthDate строка с датой рождения в формате "yyyy-MM-dd"
     */
    public void setBirthDate(String birthDate) {
        // Преобразование строки в объект LocalDate
        this.birthDate = LocalDate.parse(birthDate);
    }
}
