package com.ufs.gestaofazenda.DTO;

import jakarta.validation.constraints.*;

import lombok.Data;

import java.time.*;
import java.util.*;

public class VersaoLimitesDTO {
    @Data
    public static class VersaoLimitesRequest {
        @NotNull public Long codTalhao;

        public String descricaoLimites;
    }

    @Data
    public static class VersaoLimitesResponse {
        public Long versaoLimitesId;
        public Long codFazenda;
        public Long codTalhao;
        public String descricaoLimites;
        public Instant dtaRegistro;
    }
}
