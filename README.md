# yatspec-lsd-interceptors [![Download](https://api.bintray.com/packages/nickmcdowall/nkm/yatspec-lsd-interceptors/images/download.svg) ](https://bintray.com/nickmcdowall/nkm/yatspec-lsd-interceptors/_latestVersion)

A central library for interceptors that can be used with [yatspec-lsd ](https://github.com/nickmcdowall/yatspec) (aka
living sequence diagrams).

The interceptors capture interactions that flow in and out of the App during tests so that they can be added to
the `TestState` bean used by the [yatspec-lsd ](https://github.com/nickmcdowall/yatspec) framework to generate sequence
diagrams.

## Autoconfig

This library is designed with `@SpringBootTest` in mind and attempts to minimise boilerplate code by wiring up default
bean configurations based on the beans and classes available in the project.

The interceptors can be used outside of a spring project but will require some manual setup. The classes in the
`com/nickmcdowall/lsd/interceptor/autoconfigure` package would be a good starting point for examples on how to configure
the interceptors when autowiring is not an option.

To disable autoconfig so that the beans can be used in another library add the following property:

```properties
yatspec.lsd.interceptors.autoconfig.enabled=false
```

### Available Interceptors

#### LsdRestTemplateInterceptor

If a `TestState` bean exists and a `RestTemplate` class is on the classpath then a `RestTemplateCustomizer` bean will be
loaded into the default `RestTemplateBuilder` bean.

This causes an interceptor to be injected along with a `BufferingClientHttpRequestFactory` to allow for multiple reads
of the response stream (to avoid breaking the chain on additional reads).

- Don't instantiate a `RestTemplate` bean using the default constructor (or else you won't get the interceptor and
  factory out the box), avoid:

```java
    // Wrong
@Bean
public RestTemplate restTemplate(){
        return new RestTemplate();
        }
```

instead use a `RestTemplateBuilder` bean which will provide a correctly configured bean:

```java
    // Correct
@Bean
public RestTemplate restTemplate(RestTemplateBuilder builder){
        return builder.build();
        }
```

- `TestRestTemplate` beans just need to be `@Autowired` into your tests and will be instantiated and configured for you.

#### LsdFeignLoggerInterceptor

- For `Feign` clients
- Auto configured if a `TestState` bean exists and both `FeignClientBuilder` and `Logger.Level` classes are on the
  classpath. Note that if no feign `Logger.Level` bean exists one will be created (`Logger.Level.BASIC`) to enable the
  interceptor to work. If one exists it will not be replaced.

#### LsdOkHttpInterceptor

- For `OkHttpClient` clients.
- Auto configured if `TestState` and `OkHttpClient.Builder` beans exists *and* has spring
  property `yatspec.lsd.interceptors.autoconfig.okhttp.enabled=true`
  (requires explicit property to prevent clashing with `LsdFeignLoggerInterceptor` - as it is a popular client
  implementation for `Feign` and the former interceptor should work across all Feign client implementations).

(Additional interceptors and auto configuration will be added over time).

### Naming


### Source Name

If you set the property `info.app.name` then this will be used as the default source name for interactions captured on 
the sequence diagrams. (i.e. your app is calling downstream services)

This can be overridden by setting the `Source-Name` http header on a request. For example if you want to create a client 
that represents a user calling into your app say from within a test then you can set the `Source-Name` header value to 
the name of the user.

If neither are set then the library will default to the value `App`.

### Destination Name

Set the `Target-Name` header value to control the name of the destination service of an interaction on the sequence diagrams.

If this header is not set the library will attempt to derive a destination name based on the path of the http request.

## Build/Release

### Requirements

JDK

* Java 11

IDE

* Lombok plugin and enable annotation processing

```
./gradlew clean build
```

### Release

Run script

```
./release.sh
```