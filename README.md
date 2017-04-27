# hattery

Hattery (mad, of course) is a Java library for making HTTP requests. It provides a simple fluent interface based around immutable objects.
 
Hattery includes two transports. `DefaultTransport` uses `HttpURLConnection`; `AppEngineTransport` uses the asynchronous urlfetch service and allows multiple requests to operate in parallel.
 
```java
// Typically start with an empty request, no need to hold on to the transport.
// In fact, since requests are immutable, feel free to make them final static.
HttpRequest request = new DefaultTransport().request();

// A GET request
Thing thing1 = request
	.url("http://example.com/1")
	.param("foo", "bar")
	.fetch().as(Thing.class);

// A POST request as application/x-www-form-urlencoded 
Thing thing2 = request
	.url("http://example.com/2")
	.POST()
	.param("foo", "bar")
	.fetch().as(Thing.class);

// A POST request with a JSON body
Thing thing3 = request
	.url("http://example.com/3")
	.POST()
	.body(objectThatWillBeSerializedWithJackson)
	.fetch().as(Thing.class);

// Some extra stuff you can set
List<Thing> things4 = request
	.url("http://example.com")
	.path("/4")
	.path("andMore")	// adds '/' between path elements automatically
	.header("X-Whatever", "WHATEVER")
	.basicAuth("myname", "mypassword")
	.param("foo", "bar")
	.timeout(1000)
	.retries(3)
	.mapper(new MySpecialObjectMapper())
	.preflightAndThen(req -> req.header("X-Auth-Signature", sign(req)))
	.fetch().as(new TypeReference<List<Thing>>(){});
```

Install with maven:

```xml
	<dependency>
		<groupId>com.voodoodyne.hattery</groupId>
		<artifactId>hattery</artifactId>
		<version>look up the latest version number</version>
	</dependency>
```

Some philosphy:

 * Checked exceptions are a horrible misfeature of Java. Only runtime exceptions are thrown; all `IOException`s become `IORException`s
 * `HttpRequest`s are immutable and thread-safe. You can pass them around anywhere. 
 * `Transport`s, while immutable and thread-safe, exist only to bootstrap `HttpRequest`s. You probably don't want to pass them around in your code; instead pass around an empty `HttpRequest`.

A common pattern is to build a partial request and extend it when you need it; don't rebuild all the state every time. A contrived, self-contained example:

```java
public class FooBarService {
	private static final HttpRequest HTTP = new DefaultTransport().request();
	
	private final HttpRequest base;
	
	public Service(final String authorization) {
		this.base = HTTP
			.url("http://example.com/api")
			.header("Authorization", authorization);
	}
	
	public Foo foo() {
		return base.path("/foo").fetch().as(Foo.class);
	}

	public Bar bar(final String color) {
		return base.path("/bar").param("color", color).fetch().as(Bar.class);
	}
} 
```
 
Some extra features:

 * `path()` calls append to the url; `url()` calls replace the whole url.
 * `Content-Type` determines what is to be done with the `body()` and `param()`s (if either are present).
 * Unspecified `Content-Type` is inferred:
   * If there is a `body()`, `application/json` is assumed. Any `param()`s will become query parameters.
   * If `POST()` and no `body()`, `application/x-www-form-urlencoded` will be submitted
     * ...unless a `BinaryAttachment` parameter is included, in which case the content becomes `multipart/form-data`.
 * To run multiple async fetches concurrently with Google App Engine, use the `AppEngineTransport` and `fetch()` multiple `HttpResponse` objects. Getting the content of the response (say, via `as()`) completes the underlying asynchronous `Future`.
