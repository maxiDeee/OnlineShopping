package com.qiuzhitech.onlineshopping_09.db.po;

import lombok.Data;
import lombok.AllArgsConstructor;

@Data
@AllArgsConstructor
public class MyUser {
    int id;
    String name;
    int age;
    String email;
}
