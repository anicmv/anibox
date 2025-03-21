# AniBox

一个Java语言编写的纯API无页面图床.

## 开始使用

### 图床注册

接口地址: `POST /token`   
接口描述: 用户注册接口 返回jwt密钥  
**请求头 (Headers)**

| 参数           | 必须 | 示例                  | 说明                 |
|--------------|----|---------------------|--------------------|
| Content-Type | 是  | multipart/form-data | 指定表单格式，包含文件在内的表单字段 |

**请求参数 (Body - form-data)**

| 参数       | 类型   | 必须 | 示例       | 说明                 |
|----------|------|----|----------|--------------------|
| username | Text | 是  | zhangsan | 用户名                |
| password | Text | 是  | 123456   | 用户密码               |
| rootPath | Text | 是  | /image   | 图床路径 与docker挂载路径一直 |

**示例请求**

```
POST /token
Content-Type: multipart/form-data

username: zhangsan
password: 123456
rootPath: /image
```

**成功响应**

```
{
    "token": "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJrZXkiOiI4cFl4YnA2cnkyU2dtV2xYIn0.1qRVKU0R-5Fr-NZAuKZCkdM7A9aqbBcQ1azQpmTRZg"
}
```

**响应字段说明**

| 字段    | 类型     | 说明         |
|-------|--------|------------|
| token | String | jwt密钥 用于鉴权 |

### 图片上传接口

接口地址: `POST /upload`   
接口描述: 用于接收并上传图片文件，返回已上传图片的相关信息。   
**请求头 (Headers)**

| 参数            | 必须 | 示例                  | 说明                 |
|---------------|----|---------------------|--------------------|
| Content-Type  | 是  | multipart/form-data | 指定表单格式，包含文件在内的表单字段 |
| Authorization | 是  | Bearer <Your_Token> | Token 授权           |

**请求参数 (Body - form-data)**

| 参数    | 类型   | 必须 | 示例                                          | 说明                                       |
|-------|------|----|---------------------------------------------|------------------------------------------|
| files | File | 可选 | Circle.png                                  | 需要上传的图片文件                                |
| urls  | Text | 可选 | https://img.com/1.png,https://img.com/2.png | 若需要同时上传网络图片，可在这里传入网络图片 URL（或多个 URL）用逗号隔开 |
| album | Text | 否  | blog                                        | 创建或指定图片要上传到的相册标识                         |
| name  | Text | 否  | test                                        | 为上传文件指定自定义的名称                            |

**示例请求**

```
POST /upload
Content-Type: multipart/form-data

urls: https://img1.doubanio.com/view/photo/l/public/p2915315229.webp,https://img3.doubanio.com/view/photo/l/public/p2919254593.webp
album: douban
```

**成功响应**

```
{
    "result": "success",
    "code": 200,
    "data": [
        {
            "id": 5,
            "shortUrl": "https://jpg.moe/i/4l56tjpa.webp",
            "url": "https://jpg.moe/i/1742480920301gv.webp",
            "originName": "p2915315229.webp",
            "album": "douban",
            "size": 0.00,
            "md5": "ec86aee5528ba9e44660bac0e1d9bb2e",
            "sha1": "da39a3ee5e6b4b0d3255bfef95601890afd80709",
            "width": 1080,
            "height": 1526,
            "markdown": "![p2915315229.webp](https://jpg.moe/i/1742480920301gv.webp)",
            "html": "<img src=\"https://jpg.moe/i/1742480920301gv.webp\" alt=\"p2915315229.webp\">",
            "bbcode": "[img]https://jpg.moe/i/1742480920301gv.webp[/img]"
        },
        {
            "id": 6,
            "shortUrl": "https://jpg.moe/i/az3xgu9k.webp",
            "url": "https://jpg.moe/i/1742480938250uh.webp",
            "originName": "p2919254593.webp",
            "album": "douban",
            "size": 15.00,
            "md5": "57580d1dd5e255c6488a1b4833de67b1",
            "sha1": "da39a3ee5e6b4b0d3255bfef95601890afd80709",
            "width": 1080,
            "height": 1620,
            "markdown": "![p2919254593.webp](https://jpg.moe/i/1742480938250uh.webp)",
            "html": "<img src=\"https://jpg.moe/i/1742480938250uh.webp\" alt=\"p2919254593.webp\">",
            "bbcode": "[img]https://jpg.moe/i/1742480938250uh.webp[/img]"
        }
    ]
}
```

**响应字段说明**

| 字段     | 类型     | 说明                            |
|--------|--------|-------------------------------|
| result | String | 接口调用结果，一般为 "success" 或其他状态    |
| code   | Int    | 与 HTTP Status 类似的状态码，200 代表成功 |
| data   | Array  | 返回的Json数组，内部包含图片的各种信息         |

data字段说明:

| 字段         | 类型     | 说明                    |
|------------|--------|-----------------------|
| id         | Int    | image表中id字段值，图片唯一id   |
| shortUrl   | String | 短链接地址，用于快速访问已上传图片     |
| url        | String | 已上传图片的完整访问 URL        |
| originName | String | 上传时所使用的原始文件名称         |
| album      | String | 相册标识                  |
| size       | Float  | 文件大小 (单位为 KB、MB 等)    |
| md5        | Array  | 文件 MD5 值              |
| sha1       | String | 文件 SHA1 值             |
| width      | Int    | 已上传图片的宽度              |
| height     | Int    | 已上传图片的高度              |
| markdown   | String | 用于在 Markdown 中引用图片的文本 |
| html       | String | 用于在 HTML 中引用图片的文本     |
| bbcode     | String | 用于在论坛 BBcode 中引用图片的文本 |

### 图片删除接口

接口地址: `POST /delete`   
接口描述: 用于删除图片   
**请求头 (Headers)**

| 参数            | 必须 | 示例                  | 说明                 |
|---------------|----|---------------------|--------------------|
| Content-Type  | 是  | multipart/form-data | 指定表单格式，包含文件在内的表单字段 |
| Authorization | 是  | Bearer <Your_Token> | Token 授权           |

**请求参数 (Body - form-data)**

| 参数   | 类型   | 必须 | 示例                              | 说明             |
|------|------|----|---------------------------------|----------------|
| urls | Text | 可选 | https://jpg.moe/i/az3xgu9k.webp | 要删除的urls 用逗号隔开 |
| ids  | Text | 可选 | 7,8,9                           | 删除id为7,8,9的图片  |

**示例请求**

```
POST /delete
Content-Type: multipart/form-data

ids: 7
```

**成功响应**

```
{
    "result": "success",
    "code": 200,
    "data": "ok"
}
```

**响应字段说明**

| 字段     | 类型     | 说明                            |
|--------|--------|-------------------------------|
| result | String | 接口调用结果，一般为 "success" 或其他状态    |
| code   | Int    | 与 HTTP Status 类似的状态码，200 代表成功 |
| data   | String | 成功返回ok                        |

### 图床列表接口

接口地址: `POST /timeline`   
接口描述: 用于返回图片列表   
**请求头 (Headers)**

| 参数            | 必须 | 示例                  | 说明                 |
|---------------|----|---------------------|--------------------|
| Content-Type  | 是  | multipart/form-data | 指定表单格式，包含文件在内的表单字段 |
| Authorization | 是  | Bearer <Your_Token> | Token 授权           |

**请求参数 (Body - form-data)**

| 参数        | 类型   | 必须 | 示例     | 说明                           |
|-----------|------|----|--------|------------------------------|
| album     | Text | 否  | douban | 指定查询的相册                      |
| startTime | 否    | 可选 | 7,8,9  | 图片上传时间(开始时间 格式为: yyyy-MM-dd) |
| endTime   | 否    | 可选 | 7,8,9  | 图片上传时间(结束时间 格式为: yyyy-MM-dd) |
| page      | Text | 否  | 0      | 第几页 默认0                      |
| size      | Text | 否  | 50     | 每页多少个 默认50                   |

**示例请求**

```
POST /timeline
Content-Type: multipart/form-data

album: douban
startTime: 2025-03-01
endTime: 2025-03-20
page: 0
size: 50
```

**成功响应**

```
{
    "result": "success",
    "code": 200,
    "data": [
        {
            "id": 5,
            "shortUrl": "https://jpg.moe/i/4l56tjpa.webp",
            "url": "https://jpg.moe/i/1742480920301gv.webp",
            "originName": "p2915315229.webp",
            "album": "douban",
            "size": 0.00,
            "md5": "ec86aee5528ba9e44660bac0e1d9bb2e",
            "sha1": "da39a3ee5e6b4b0d3255bfef95601890afd80709",
            "width": 1080,
            "height": 1526,
            "markdown": "![p2915315229.webp](https://jpg.moe/i/1742480920301gv.webp)",
            "html": "<img src=\"https://jpg.moe/i/1742480920301gv.webp\" alt=\"p2915315229.webp\">",
            "bbcode": "[img]https://jpg.moe/i/1742480920301gv.webp[/img]"
        },
        {
            "id": 6,
            "shortUrl": "https://jpg.moe/i/az3xgu9k.webp",
            "url": "https://jpg.moe/i/1742480938250uh.webp",
            "originName": "p2919254593.webp",
            "album": "douban",
            "size": 15.00,
            "md5": "57580d1dd5e255c6488a1b4833de67b1",
            "sha1": "da39a3ee5e6b4b0d3255bfef95601890afd80709",
            "width": 1080,
            "height": 1620,
            "markdown": "![p2919254593.webp](https://jpg.moe/i/1742480938250uh.webp)",
            "html": "<img src=\"https://jpg.moe/i/1742480938250uh.webp\" alt=\"p2919254593.webp\">",
            "bbcode": "[img]https://jpg.moe/i/1742480938250uh.webp[/img]"
        }
    ]
}
```

**响应字段说明**

与上传接口一致.

### 相册管理接口

接口地址: `POST /album`   
接口描述: 用于修改图片相册   
**请求头 (Headers)**

| 参数            | 必须 | 示例                  | 说明                 |
|---------------|----|---------------------|--------------------|
| Content-Type  | 是  | multipart/form-data | 指定表单格式，包含文件在内的表单字段 |
| Authorization | 是  | Bearer <Your_Token> | Token 授权           |

**请求参数 (Body - form-data)**

| 参数    | 类型   | 必须 | 示例                              | 说明           |
|-------|------|----|---------------------------------|--------------|
| urls  | Text | 可选 | https://jpg.moe/i/az3xgu9k.webp | 更新该链接图片对应的相册 |
| ids   | Text | 可选 | 7,8,9                           | 更新该id对应的相册   |
| album | Text | 是  | douban                          | 相册名称         |

**示例请求**

```
POST /album
Content-Type: multipart/form-data

ids: 5,6
```

**成功响应**

```
{
    "result": "success",
    "code": 200,
    "data": "ok"
}
```

**响应字段说明**

| 字段     | 类型     | 说明                            |
|--------|--------|-------------------------------|
| result | String | 接口调用结果，一般为 "success" 或其他状态    |
| code   | Int    | 与 HTTP Status 类似的状态码，200 代表成功 |
| data   | String | 成功返回ok                        |

### 相册 修改/访问 接口

接口地址: `GET /i/{name}?w=500&h=500&x=100&y=100`   
接口描述: 用于 修改/访问 图片

**请求参数**

| 参数   | 类型   | 必须 | 示例                              | 说明           |
|------|------|----|---------------------------------|--------------|
| name | Text | 是  | https://jpg.moe/i/az3xgu9k.webp | 更新该链接图片对应的相册 |
| w    | Int  | 否  | 500                             | 图片宽度         |
| h    | Int  | 否  | 500                             | 图片高度         |
| x    | Int  | 否  | 100                             | 图片x轴偏移量      |
| y    | Int  | 否  | 100                             | 图片y轴偏移量      |

**示例请求**

```
GET /i/1742480695662wn.jpg?w=500&h=500&x=100&y=100
```

**成功响应**   
返回处理后的图片

**说明**   
当w,h,x,y不传值时,返回原始图片.   
当w或h有值 图片按照长宽居中切分.   
当x或y有值 图片按照x,y二维坐标切分(起点在左上角),如果是正值,则右下角为空白.

### 图片圆形头像接口

接口地址: `GET /a/{name}`   
接口描述: 返回圆形图片

**示例请求**

```
GET /a/1742480695662wn.jpg
```

**成功响应**   
返回圆形图片

**说明**   
裁剪最大圆型图片

### 随机图片接口

接口地址: `POST /random`   
接口描述: 随机图片api

**示例请求**

```
GET /random
```

**成功响应**   
返回图片

**说明**   
返回图片相册名random的图片

## 功能

- [x] api接口上传
- [x] url上传
- [x] 批量文件上传
- [x] 批量删除
- [x] 指定上传的文件名
- [x] markdown格式
- [x] bbcode格式
- [x] html格式
- [x] 图片短链
- [x] 相册管理
- [x] 热门图片redis防攻击 (一定程度)

## TODO

- [ ] base64上传
- [ ] 防止批量注册
- [ ] 图床路径自定义
- [ ] 配置化注册接口
- [ ] 支持文件与链接一起上传
- [ ] 随机图片加用户限制






