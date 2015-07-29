# hattery

Hattery (mad, of course) is a Java library for making HTTP requests. It provides a simple fluent interface based around immutable objects.
 
Hattery includes two transports. `DefaultTransport` uses `HttpURLConnection`; `AppEngineTransport` uses the asynchronous urlfetch service and allows multiple requests to operate in parallel.
 
```java
Transport transport = new DefaultTransport();

// A GET request
Thing thing1 = transport.request("http://example.com")
	.param("foo", "bar")
	.fetch().as(Thing.class);

// A POST request
Thing thing2 = transport.request("http://example.com")
	.POST()
	.param("foo", "bar")
	.fetch().as(Thing.class);

// Some extra stuff you can set
Thing thing1 = transport.request()
	.url("http://example.com")
	.path("/morethings")
	.header("X-Authorization", "SOMETHING")
	.param("foo", "bar")
	.timeout(1000)
	.retries(3)
	.mapper(new MySpecialObjectMapper())
	.fetch().as(Thing.class);
```

Install with maven:

```xml
	<dependency>
		<groupId>com.voodoodyne.hattery</groupId>
		<artifactId>hattery</artifactId>
		<version>look up the latest version number</version>
	</dependency>
```
