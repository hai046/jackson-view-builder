# jackjson-view-builder

通过jackjson注解来
- 树状数据渲染
- 对明感id等进行加密，避免穷举破解
- 减少业务处理逻辑


## 解决问题

### 1、复杂的树状渲染
```java
@Data
public class UserVO {

    private Long userId;

    private String name;

    private Long image;

    private Integer school;
}

```

例如`UserVO`里面有`school`,`image`

这里2个对象是需要通过id换取对应实体，按照传统的方法一般有下面两种方案

- 先获取UserVO，然后逐一通过id获取实体，逐一查询数据
- 通过ORM等定义一个和业务强耦合的view



上面方案需要定义多种数据结构，或者需要连表查询等


## 最终使用

```java

@Data
public class UserVO {

    /**
     * 把数字id加密转成对称的string Id，避免客户端传递穷举
     * <p>
     * 返回给客户端的是string类型，
     * 客户端传递string类型给服务端会反解成数字类型，对客户端和服务端都是透明
     */
    @JsonUUID
    private Long userId;

    private String name;

    /**
     * 通过image来换取实体
     */
    @ViewField(ImageVO.class)
    private Long image;

    @JsonProperty("school")
    @ViewField(SchoolVO.class)
    private Integer schoolId;
}

```

如果返回给客户端的话

json对象
```
{
  "userId": "ZgJnGNOVZNkDF9jWvgPuwg",
  "name": "name0",
  "image": {
    "id": "ZgJnGNOVZNkDF9jWvgPuwg",
    "url": "http://image.com/0",
    "width": null,
    "height": null
  },
  "school": {
    "id": "_lbLHlsMBFWswl7xW-_liw",
    "name": "清华大学-152800936"
  }
}
```


注意：如果要接受客户端传递回来的对象，需要重新定义一个`DTO`,字段要对应上，如果需要把加密的id转化成真实id加上`@JsonUUID`即可