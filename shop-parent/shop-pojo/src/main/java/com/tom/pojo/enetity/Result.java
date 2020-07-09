package com.tom.pojo.enetity;

import lombok.Data;

import java.io.Serializable;

/**
 * 结果实体类
 */
@Data
public class Result implements Serializable {
    private Boolean success;
    private String message;

    public Result() {
    }

    public Result(Boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    @Override
    public String toString() {
        return "Result{" +
                "success=" + success +
                ", message='" + message + '\'' +
                '}';
    }
}
