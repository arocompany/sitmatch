package com.nex.user.entity;

import com.nex.search.entity.SearchInfoEntity;
import jakarta.persistence.*;
import lombok.*;

import java.sql.Timestamp;
import java.util.Objects;

@Getter
@Setter
public class AutoKeywordDto {


    private int AUTO_UNO;
    private String AUTO_USER_ID;
    private String AUTO_KEYWORD;

}
