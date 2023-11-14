package com.nex.search.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;
import java.util.Objects;

public interface NewKeywordResultDto {
    String getNewKeyword();
}
