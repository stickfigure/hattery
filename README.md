# hattery

Hattery (mad, of course) is a Java library for making HTTP requests. It provides a simple fluent interface based around immutable objects.
 
```java
// Requests are immutable, start with the base that uses Java11Transport
import static com.voodoodyne.hattery.HttpRequest.HTTP;

// A GET request
Thing thing1 = HTTP
    .url("http://example.com/1")
    .param("foo", "bar")
    .fetch().as(Thing.class);

// A POST request, application/x-www-form-urlencoded is inferred
Thing thing2 = HTTP
    .url("http://example.com/2")
    .POST()
    .param("foo", "bar")
    .fetch().as(Thing.class);

// A POST request with a JSON body, application/json is inferred
Thing thing3 = HTTP
    .url("http://example.com/3")
    .POST()
    .body(objectThatWillBeSerializedWithJackson)
    .fetch().as(Thing.class);

// Some extra stuff you can configure
List<Thing> things4 = HTTP
    .transport(new MyCustomTransport())
    .url("http://example.com")
    .path("/4")
    .path("andMore")	// adds '/' between path elements automatically
    .header("X-Whatever", "WHATEVER")
    .basicAuth("myname", "mypassword")
    .param("foo", "bar")
    .timeout(1000)
    .retries(3)
    .mapper(new MySpecialObjectMapper())
    .preflight(req -> req.header("X-Auth-Signature", sign(req)))
    .fetch().as(new TypeReference<List<Thing>>(){});

// Request objects are immutable and can be reused
HttpRequest base = HTTP
    .url("http://example.com/base/endpoint")
    .timeout(1000);

Cow cow = base.path("/cows/123").fetch().as(Cow.class);
Goat goat = base.path("/goats/456").fetch().as(Goat.class);
```

Install with maven:

```xml
    <dependency>
        <groupId>com.voodoodyne.hattery</groupId>
        <artifactId>hattery</artifactId>
        <version>look up the latest version number</version>
    </dependency>
```

Hattery v1.1+ requires Java 11+. Previous versions require Java 8+.

Some philosphy:

 * `HttpRequest`s are immutable and thread-safe. You can pass them around anywhere. 
 * Checked exceptions are a misfeature of Java. Only runtime exceptions are thrown; all `IOException`s become `IORuntimeException`s
 * Transports are distinct from the request API
   * The default transport is the Java11 HttpClient
   * There's a URLConnectionTransport if you want to use the legacy URL.openConnection()
   * You can write your own custom transports (eg, ApacheHttpClientTransport).
 
A common pattern is to build a partial request and extend it when you need it; don't rebuild all the state every time. A contrived, self-contained example:

```java
public class FooBarService {
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

 * `path()` appends to the url; `url()` replaces the whole url.
 * `Content-Type` determines what is to be done with the `body()` and `param()`s (if either are present).
 * Unspecified `Content-Type` is inferred:
   * If there is a `body()`, `application/json` is assumed. Any `param()`s will become query parameters.
   * If `POST()` and no `body()`, parameters will be submitted as `application/x-www-form-urlencoded`
     * ...unless a `BinaryAttachment` parameter is included, in which case the content becomes `multipart/form-data`.
     * ...or unless params are submitted as `queryParam()`, which forces them onto the query string.
 
