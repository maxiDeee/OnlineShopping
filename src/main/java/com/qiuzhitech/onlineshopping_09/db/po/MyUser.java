package com.qiuzhitech.onlineshopping_09.db.po;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MyUser {
    private Integer id;
    private String name;
    private Integer age;
    private String email;
    }