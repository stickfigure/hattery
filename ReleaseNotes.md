# 1.1.1
2023-12-12
  * Add a `Java11Transport` constructor that takes a `HttpClient.Builder`

# 1.1.0
2023-12-12
  * Java11 is now the minimum required Java version
  * The default transport is the Java11 HttpClient
  * `DefaultTransport` has been renamed `URLConnectionTransport`
  * `application/xml` error fragments are included in error messages, just like `application/json`

# 1.0.4
2023-03-10
  * Added `getContentString()` methods to `HttpException` (see javadocs)

# 1.0.3
2023-03-10
  * Added `errorTranslator()` mechanism to `HttpRequest` (see javadocs)

# 1.0.2
2021-03-03
  * Specifying an XML content type now runs the body through the mapper. You still need to specify the XmlMapper explicitly.

# 1.0.1
2021-02-26
  * Added `HttpRequest.PATCH()` method
  * Added `HttpRequest.paramJson()` and related methods

# 1.0
2020-08-28
  * Baseline 1.0 release, API is now officially stable
