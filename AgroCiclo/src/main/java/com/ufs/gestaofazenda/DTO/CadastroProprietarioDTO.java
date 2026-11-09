package com.ufs.gestaofazenda.DTO;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CadastroProprietarioDTO(
        @NotBlank @Size(max = 140) String nomeCompleto,
        @NotBlank @Email @Size(max = 254) String email,
        @NotBlank @Size(min = 10, max = 72) String senha,
        @AssertTrue(message = "É necessário aceitar os termos de uso.") boolean aceitaTermos) {}
