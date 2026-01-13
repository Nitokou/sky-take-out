package com.sky.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class StatusDTO implements Serializable {
    private Integer status;
    private Integer cnt;
}
