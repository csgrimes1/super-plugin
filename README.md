# Code Challenge Plugin

## About

The plugin in this code challenge supports configuration parameters for POST/PUT body,
content type, HTTP method, and webhook endpoint.

The plugin does not use annotation semantics to implement configuration properties. I could not
locate any documentation or sample code indicating whether the notification service changed
those annotated properties, passed them in the `config` parameter, or both. It was clear from samples
how this works with a `PropertyBuilder`, so I chose that route.
