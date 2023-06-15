package com.search.docsearch.entity.vo;

import java.io.Serial;
import java.io.Serializable;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class SysResult implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;
    private Integer status;
    private String msg;
    private Object obj;

    public SysResult(Integer status, String msg, Object obj) {
        this.status = status;
        this.msg = msg;
        this.obj = obj;
    }

    public SysResult() {
    }

    public SysResult(Throwable e) {
        this.status = 201;
        this.msg = e.getMessage();
    }

    //定义成功的静态方法
    public static SysResult ok(String msg, Object obj) {
        return new SysResult(200, msg, obj);
    }

    //表示定义成功的静态方法
    public static SysResult ok() {
        return new SysResult(200, null, null);
    }

    public static SysResult ok(Object data) {
        return new SysResult(200, null, data);
    }

    //定义一个失败的静态方法
    public static SysResult fail(String msg, Object data) {
        return new SysResult(201, msg, data);
    }

    public static SysResult fail() {
        return new SysResult(201, null, null);
    }
}
