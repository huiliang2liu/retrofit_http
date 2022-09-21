### api说明
#### RetrofitOkhttpBuilder说明
| 方法名 | 方法说明      | 参数说明                               |
| --- |-----------|------------------------------------|
| closeLog | 关闭请求日志    |                                    |
| closeCrossDomainRedirect | 关闭跨域重定向   |
| addInterceptor | 添加拦截器     | interceptor需要添加的拦截器，不能为空           |
| addFilter | 添加日志过滤起   | filter需要过滤的接口，不过滤的话下载大文件会一次性返回     |
 | addNetworkInterceptor | 添加网络请求拦截器 | interceptor需要添加的拦截器,不能为空           |
 | setFactory | 设置序列化工厂   | factory序列化工厂                       |
 | setConnectTimeout | 设置链接超时时间  | connectTimeout超时时间，单位为秒，默认8秒，不能小于0 |
 | setReadTimeout | 设置读取超时时间  | readTimeout超时时间，单位为秒，默认8秒，不能小于0    |
 | setWriteTimeout | 设置写入超时时间  | writeTimeout超时时间，单位为秒，默认8秒，不能小于0   |
 | setDns | 设置dns解析起  | dns dns解析器，默认是系统的dns解析器            |